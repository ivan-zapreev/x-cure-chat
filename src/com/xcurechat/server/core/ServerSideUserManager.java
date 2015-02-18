/**
 * X-Cure-Chat
 * Copyright (C) 2013  Dr. Ivan S. Zapreev
 * www: https://nl.linkedin.com/in/zapreevis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.#
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The server-side RPC package.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.server.core;

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.UserData;

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.UserLoginException;

import com.xcurechat.server.ForumManagerImpl;
import com.xcurechat.server.cache.Top10UserDataCache;
import com.xcurechat.server.jdbc.ConnectionWrapper;
import com.xcurechat.server.jdbc.forum.files.DeleteOwnerForumFilesExecutor;
import com.xcurechat.server.jdbc.images.SelectProfileFileDescriptorsExecutor;
import com.xcurechat.server.jdbc.messages.RerouteMessagesToDoletedUserExecutor;
import com.xcurechat.server.jdbc.profile.DeleteUserProfileExecutor;
import com.xcurechat.server.jdbc.profile.GetUserIDByLoginExecutor;
import com.xcurechat.server.jdbc.profile.IncrementUserGoldExecutor;
import com.xcurechat.server.jdbc.profile.InsertNewUserExecutor;
import com.xcurechat.server.jdbc.profile.LoginUserExecutor;
import com.xcurechat.server.jdbc.profile.SetUserOnlineStatusExecutor;
import com.xcurechat.server.jdbc.profile.MarkAllUserOfflineExecutor;
import com.xcurechat.server.jdbc.profile.UpdateUserAttributesExecutor;
import com.xcurechat.server.jdbc.statistics.InsertUserStatisticsExecutor;

import com.xcurechat.server.security.statistics.MessageSendAbuseFilter;
import com.xcurechat.server.security.statistics.StatisticsSecurityManager;

import com.xcurechat.server.utils.AddUnifiedLoginThread;
import com.xcurechat.server.utils.IPtoLocationLocator;

/**
 * @author zapreevis
 * This class manager user sessions and user objects.
 */
public class ServerSideUserManager {

	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( ServerSideUserManager.class );
	
	//Get the synchronization factory for users
	private static final SynchFactory userSynchFactory = SynchFactory.getSynchFactory( SynchFactory.USER_FACTORY_NAME );
	
	static {
		//This section does some important web-site initialization
		//Such as it marks all the users as offline, in case there
		//was a crash and some of the user in the DB have an online status
		ConnectionWrapper<Void> markOfflineConnWrap = ConnectionWrapper.createConnectionWrapper( new MarkAllUserOfflineExecutor( ) );
		try {
			markOfflineConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
		}catch(SiteException e){
			logger.error( "An unexpected exception when marking all users offline", e);
		}
		
		//Run the class that adds the unified login names to the user profiles
		//This class is needed due to a one-time DB change that happened during
		//the site development and in principle it has to be invoked only one time
		//after this the site can work without it, and this class is not needed
		//if the site DB is created from scratch using the create_user_and_database.sql
		//after the revision 1451
		AddUnifiedLoginThread loginUnifier = new AddUnifiedLoginThread();
		loginUnifier.start();
	}

	/**
	 * Manager the user registration, it fills in the MainUserData object with
	 * a sessionId and a hash code of the new ServerUserObject
	 * @param httpSession the HttpSession from which the call is made, MUST NOT BE NULL!
	 * @param remoteAddr the IP address of the user making the request
	 * @param userData the user data object for the user that is registering.
	 * @param userPassword the user password
	 * @return the user user data object with session and etc.
	 */
	public static MainUserData register( HttpSession httpSession, final String remoteAddr,
										final MainUserData userData, final String userPassword )
										throws SiteException {
		logger.info("Registering a new user: " + userData.getUserLoginName());
		//Here we only check that the user is not blocked due too frequent server requests
		StatisticsSecurityManager.validateAccess( httpSession, remoteAddr, UserData.DEFAULT_UID, true, true, false );
		
		//Set the user registration date and the last online value now.
		final Date currentDate = new Date();
		userData.setUserRegistrationDate( currentDate );
		userData.setUserLastOnlineDate( currentDate );
		
		//Add a new user to the database, if the same login already exists
		//then an exception is thrown and we do not go further.
		ConnectionWrapper<Void> registerUserConnWrap = ConnectionWrapper.createConnectionWrapper( new InsertNewUserExecutor( userData, userPassword ) );
		registerUserConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
		
		final int userID = userData.getUID();
		
		try {
			//Do actions requiring synchronization of the user
			synchronized( userSynchFactory.getSynchObject( userID ) ) {
				//Register user session, if the user was successfully added to the database,
				//this means that it is a new user and since all is fine we register the session.
				UserSessionManager.registerUserSession( httpSession, userData, remoteAddr, false );
				
				//NOTE: Here we do not need to retrieve the user profile image descriptors, because the profile was just created
				
				//Update the last logged on time and other statistics
				InsertUserStatisticsExecutor statsExecutor = new InsertUserStatisticsExecutor( userID, true, false, remoteAddr, IPtoLocationLocator.getLocationbyIP(remoteAddr) );
				ConnectionWrapper<Void> userStatsConnWrap = ConnectionWrapper.createConnectionWrapper( statsExecutor );
				userStatsConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
				
				//Set the user online status to online
				ConnectionWrapper<Void> onlieStatsConnWrap = ConnectionWrapper.createConnectionWrapper( new SetUserOnlineStatusExecutor( userID, true ) );
				onlieStatsConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
				
				//Flush the top10 cache for user registrations to make the new registration appear
				Top10UserDataCache.getInstance().flushUserRegistrationsCachedData();
				//Flush the user visits cache to mark the user as online
				Top10UserDataCache.getInstance().flushUserVisitsCachedData();
				
				return userData;
			}
		}finally{
			userSynchFactory.releaseSynchObject( userID );
		}
	}
	
	/**
	 * Allows to increment the gold count in the user's wallet by the given number of gold pieces.
	 * If the user is online, then we only increment it in the current user data, if he is offline
	 * then only in the database. This method synchronizes on the user Id. Note that this 
	 * does not do anything if the userID equals to ShortUserData.UNKNOWN_UID or ShortUserData.DEFAULT_UID. 
	 * @param userID the id of the user for which we want to increment the gold count
	 * @param goldPiecesIncrement the number of gold pieces by which the gold count will be incremented
	 * @throws SiteException in case of some database error
	 */
	public static void incrementUserGoldCount( final int userID, final int goldPiecesIncrement ) throws SiteException {
		if( ( userID != ShortUserData.UNKNOWN_UID ) && ( userID != ShortUserData.DEFAULT_UID ) ) {
			try {
				//Do actions requiring synchronization of the user
				synchronized( userSynchFactory.getSynchObject( userID ) ) {
					logger.info("Incrementing the gold count for user " + userID + " by " + goldPiecesIncrement + " gold pieces");
					//Get the user data 
					UserData userData = UserSessionManager.getUserDataObject( userID );
					
					if( userData != null ) {
						//The user is online, just increment the user's gold in his online data
						logger.info("The user " + userID + " is online, just increment the user's gold in his online data");
						userData.incrementGoldenPiecesCount( goldPiecesIncrement );
					} else {
						//The user is offline, just increment the user's gold in the database
						logger.info("The user " + userID + " is offline, just increment the user's gold in the database");
						ConnectionWrapper<Void> goldIncrementConnWrap = ConnectionWrapper.createConnectionWrapper( new IncrementUserGoldExecutor( userID, goldPiecesIncrement ) );
						goldIncrementConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
					}
				}
			}finally{
				userSynchFactory.releaseSynchObject( userID );
			}
		}
	}
	
	/**
	 * Manages the user login, it fills in the MainUserData object and
	 * allocates user session objects 
	 * @param httpSession the HttpSession from which the call is made, MUST NOT BE NULL!
	 * @param remoteAddr the IP address of the user making the request
	 * @param userLoginName the user login name
	 * @param userPassword the user's password
	 * @return the user-data object for the logged in user
	 */
	public static MainUserData login( HttpSession httpSession, final String remoteAddr,
								final String userLoginName, final String userPassword ) throws SiteException {
		logger.info("Loggin in user: " + userLoginName);
		
		//Get user data from the database and check, if the login/password are incorrect an exception is thrown
		MainUserData userData = new MainUserData();
		userData.setUserLoginName( userLoginName );
		//Here we skip the password vs login validation and only retrieve the user data, including its login name
		//The exception is thrown if the user does not exist, but we do not report any failed logins for it here
		ConnectionWrapper<MainUserData> userExistsCheckConnWrap = ConnectionWrapper.createConnectionWrapper( new GetUserIDByLoginExecutor( userLoginName ) );
		userExistsCheckConnWrap.executeQuery( userData, ConnectionWrapper.XCURE_CHAT_DB );
		
		//We have obtained the id of the user we are looking for
		final int userID = userData.getUID();
		
		//Now when we know the user ID we actually check if the login for this user from the IP is blocked or not 
		StatisticsSecurityManager.validateAccess( httpSession, remoteAddr, userID, true );
		
		try {
			//Do actions requiring synchronization of the user
			synchronized( userSynchFactory.getSynchObject( userID ) ) {
				
				//Now when we know the user ID we re-check the database because the user could
				//have been deleted mean while and we still need to check if the password is valid
				try {
					ConnectionWrapper<Void> loginUserConnWrap = ConnectionWrapper.createConnectionWrapper( new LoginUserExecutor( userData, userPassword ) );
					loginUserConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
				} catch ( UserLoginException exception ){
					//The exception is thrown if the user does not exist, so some one is guessing the login and password...
					StatisticsSecurityManager.reportFailedLogin( remoteAddr, userID );
					throw exception;
				}
				
				//Get the user profile image descriptors, and add them into the user data object
				ConnectionWrapper<Void> getUserImgDescrConnWrap = ConnectionWrapper.createConnectionWrapper( new SelectProfileFileDescriptorsExecutor( userData ) );
				getUserImgDescrConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				
				//Register user session
				UserSessionManager.registerUserSession( httpSession, userData, remoteAddr, true );
				
				//Update the last logged on time and other statistics
				InsertUserStatisticsExecutor statsExecutor = new InsertUserStatisticsExecutor( userID, true, false, remoteAddr, IPtoLocationLocator.getLocationbyIP(remoteAddr) );
				ConnectionWrapper<Void> userStatsConnWrap = ConnectionWrapper.createConnectionWrapper( statsExecutor );
				userStatsConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
				
				//Set the user online status to true
				if( UserSessionManager.getUserLoginCounter( userID ) == 1 ) {
					ConnectionWrapper<Void> onlineStatsConnWrap = ConnectionWrapper.createConnectionWrapper( new SetUserOnlineStatusExecutor( userID, true ) );
					onlineStatsConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
					
					//Flush the user visits cache to set the user as online
					Top10UserDataCache.getInstance().flushUserVisitsCachedData();
				}
				
				//Update the list of active chat rooms, to bring the user's room's online
				ChatRoomsManager.getInstance().updateActiveRooms( true );
				
				//Return the user data
				return userData;
			}
		}finally{
			userSynchFactory.releaseSynchObject( userID );
		}
	}

	/**
	 * Manages the user logout, it removes user session objects and etc.
	 * Invalidates the HTTP session only in case it is available, the same
	 * goes for removing the Message filter from the session. Also, here
	 * there is no validation for the user session and servlet session access.
	 * Also there is no synchronization of the user access by its user ID.
	 * @param sessionListener the session listener object, MUST NOT BE NULL!
	 * @param userID the unique user ID
	 * @param sessionID the user's password
	 * @param remoteAddr the user's IP address
	 * @param httpSession the http session or null if it is not available
	 * @param isAutoLogout true if the user gets logged out automatically
	 */
	public static void logout( HttpUserSessionListener sessionListener, final int userID,
							   final String sessionId, final String remoteAddr,
							   HttpSession httpSession, boolean isAutoLogout ) throws SiteException {
		//The user leaves all rooms where he is present
		ChatRoomsManager.getInstance().leaveAllRooms(userID);
		
		//Get the user data object, we might need it further to update the user's attributes in DB
		final MainUserData userData = UserSessionManager.getUserDataObject( userID );
		
		if( userData != null ) {
			//Unregister user session
			UserSessionManager.unregisterUserSession( sessionListener, userID, sessionId );
			
			//Update the last online on time and other statistics
			try {
				InsertUserStatisticsExecutor executor = new InsertUserStatisticsExecutor( userID, false, true, remoteAddr, IPtoLocationLocator.getLocationbyIP(remoteAddr) );
				ConnectionWrapper<Void> userStatsConnWrap = ConnectionWrapper.createConnectionWrapper( executor );
				userStatsConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
			} catch ( Exception e ) {
				logger.error("An exception while user log-out", e);
			}
			
			//If the user is completely logged out
			if( UserSessionManager.isUserOffline(userID) ) {
				try {
					//Set the user online status to false
					ConnectionWrapper<Void> onlieStatsConnWrap = ConnectionWrapper.createConnectionWrapper( new SetUserOnlineStatusExecutor( userID, false ) );
					onlieStatsConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
				} catch ( Exception e ) {
					logger.error("An exception while user log-out", e);
				}
				
				try {
					//Update the user's gold and other attributes in the DB
					ConnectionWrapper<Void>  attributesUpdater =  ConnectionWrapper.createConnectionWrapper( new UpdateUserAttributesExecutor( userData, true ) );
					attributesUpdater.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
				} catch ( Exception e ) {
					logger.error("An exception while user log-out", e);
				}
				
				//Remove the message filter, note that if the auto_logout is called, then 
				//it means that the session gets invalidated, thus there is no need to un-
				//bind the message filter from the session, it will happen by itself
				MessageSendAbuseFilter.removeMessageFilter( httpSession, userID );
				
				//Flush the top10 cache in order to update the user data
				Top10UserDataCache.getInstance().flushCachedData( true );
			}
		}
		
		//Invalidate the server session if it is present 
		if( httpSession != null ) {
			httpSession.invalidate();
		}
	}
	
	/**
	 * This method allows to delete the user profile from the database.
	 * This method is synchronized on the user ID
	 * @param userID the unique user ID
	 * @throws SiteException 
	 */
	public static void deleteUserProfile( final int userID ) throws SiteException {
		//Synchronize on the user ID
		Object synchObj = userSynchFactory.getSynchObject( userID );
		try {
			synchronized( synchObj ) {
				//Reconnect all the private (personal) messages to the 'deleted' user profile
				RerouteMessagesToDoletedUserExecutor rerouterExec = new RerouteMessagesToDoletedUserExecutor(userID, true);
				//As for a sender
				ConnectionWrapper<Void> rerouterConnWrap = ConnectionWrapper.createConnectionWrapper( rerouterExec ); 
				rerouterConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
				//As for a receiver
				rerouterExec = new RerouteMessagesToDoletedUserExecutor(userID, false);
				rerouterConnWrap = ConnectionWrapper.createConnectionWrapper( rerouterExec ); 
				rerouterConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
				
				//Detach all forum messages from this user
				ForumManagerImpl.deleteMessageSender( userID );
				
				//Detach all forum files from this user
				DeleteOwnerForumFilesExecutor deleteForumFilesOwnerExec = new DeleteOwnerForumFilesExecutor( userID );
				ConnectionWrapper<Void> deleteForumFilesOwnerConnWrap = ConnectionWrapper.createConnectionWrapper( deleteForumFilesOwnerExec ); 
				deleteForumFilesOwnerConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
				
				//Delete user profile in the database. Here the cascade delete is done to
				//remove all related user data from other tables.
				ConnectionWrapper<Void> deleteUserConnWrap = ConnectionWrapper.createConnectionWrapper( new DeleteUserProfileExecutor( userID ) );
				deleteUserConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
				
				//Flush the top10 cache in order to update the user data
				Top10UserDataCache.getInstance().flushCachedData( true );
			}	
		} finally {
			//Release the synchronization object for the user
			userSynchFactory.releaseSynchObject( userID );
		}
	}
}
