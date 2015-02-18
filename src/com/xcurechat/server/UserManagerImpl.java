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
package com.xcurechat.server;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.apache.log4j.Logger;

import com.xcurechat.client.userstatus.UserStatusType;
import com.xcurechat.client.utils.AvatarSpoilersHelper;
import com.xcurechat.client.utils.PresetAvatarImages;

import com.xcurechat.client.rpc.UserManager;
import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.CaptchaTestFailedException;
import com.xcurechat.client.rpc.exceptions.UserLoginException;
import com.xcurechat.client.rpc.exceptions.UserStateException;

import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.ShortUserFileDescriptor;
import com.xcurechat.client.data.SiteInfoData;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.data.UserFileData;
import com.xcurechat.client.data.search.OnePageViewData;
import com.xcurechat.client.data.search.Top10SearchData;
import com.xcurechat.client.data.search.UserSearchData;

import com.xcurechat.server.cache.ShortUserDataCache;
import com.xcurechat.server.cache.Top10UserDataCache;
import com.xcurechat.server.core.ChatBotManager;
import com.xcurechat.server.core.ChatRoomsManager;
import com.xcurechat.server.core.HttpUserSessionListener;
import com.xcurechat.server.core.SecureServerAccess;
import com.xcurechat.server.core.ServerSideUserManager;
import com.xcurechat.server.core.SynchFactory;
import com.xcurechat.server.core.UserSessionManager;
import com.xcurechat.server.core.SiteUsersStatisticsManager;

import com.xcurechat.server.files.FileServletHelper;

import com.xcurechat.server.jdbc.ConnectionWrapper;
import com.xcurechat.server.jdbc.QueryExecutor;

import com.xcurechat.server.jdbc.images.DeleteProfileFilesExecutor;
import com.xcurechat.server.jdbc.images.SelectProfileFileDescriptorsExecutor;
import com.xcurechat.server.jdbc.images.SelectTop10FileDescriptorsExecutor;
import com.xcurechat.server.jdbc.images.avatars.DeleteProfileAvatarExecutor;

import com.xcurechat.server.jdbc.profile.GetUserProfileExecutor;
import com.xcurechat.server.jdbc.profile.MarkAsBotOrHumanProfileExecutor;
import com.xcurechat.server.jdbc.profile.UpdateUserAttributesExecutor;
import com.xcurechat.server.jdbc.profile.UpdateUserProfileExecutor;
import com.xcurechat.server.jdbc.profile.VerifyPasswordExecutor;

import com.xcurechat.server.jdbc.profile.friends.SelectFriendsExecutor;
import com.xcurechat.server.jdbc.profile.friends.IsFriendCheckExecutor;
import com.xcurechat.server.jdbc.profile.friends.AddUserFriendExecutor;
import com.xcurechat.server.jdbc.profile.friends.RemoveUserFriendExecutor;

import com.xcurechat.server.jdbc.profile.search.CountUserSearchResultsExecutor;
import com.xcurechat.server.jdbc.profile.search.BrowseUserSearchResultsExecutor;
import com.xcurechat.server.jdbc.profile.search.SelectTop10UserProfilesExecutor;

import com.xcurechat.server.security.captcha.CaptchaServiceManager;

import com.xcurechat.server.security.statistics.StatisticsSecurityManager;

import com.xcurechat.server.utils.HTTPUtils;

/**
 * @author zapreevis
 * The RPC interface that allows for users to login, logout,
 * register, delete profile, change personal information
 * (the password etc).
 */
public class UserManagerImpl extends RemoteServiceServlet implements UserManager {
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;

	private static final SynchFactory userPrankSynchFactory = SynchFactory.getSynchFactory( SynchFactory.AVATAR_PRANK_FACTORY_NAME );

	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( UserManagerImpl.class );

	/**
	 * Retrieves the HttpSession object if any.
	 * @return the HttpSession object, either an old or a newly created one
	 */
	private HttpSession getLocalHttpSession() {
		return this.getThreadLocalRequest().getSession( true );
	}
	
	/**
	 * @param userID the unique user ID
	 * @return returns the remote address as specified in the http request 
	 */
	private String getTrueRemoteAddress(final int userID) {
		return HTTPUtils.getTrueRemoteAddr( userID, this.getThreadLocalRequest() );
	}
	
	/**
	 * This method should be user for registering a new user
	 * @param userData the user data object with no password hash and user object hash 
	 * @param userPassword the user password
	 * @param captchaResponse the CAPTCHA verification string
	 * @return the user user data object with session and etc.
	 */
	public MainUserData register(final MainUserData userData, final String userPassword,
								final String captchaResponse) throws SiteException{
		Log4jInit.pushDNC( getThreadLocalRequest(), null, UserData.DEFAULT_UID );
		logger.info( "Initiating registration for user " + userData.getUserLoginName() );

		try{
			//Validate the correct answer for CAPTCHA 
			if( CaptchaServiceManager.validateCaptchaProblem( getLocalHttpSession().getId(), captchaResponse)){
				//Register the user and login him/her
				return ServerSideUserManager.register( getLocalHttpSession(),
														getTrueRemoteAddress( UserData.DEFAULT_UID ),
														userData, userPassword );
			} else {
				throw new CaptchaTestFailedException(CaptchaTestFailedException.CAPTCHA_TEST_FAILED_ERR);
			}
		} finally{
			Log4jInit.cleanDNC();
		}
	}
	
	/**
	 * This method processes user login
	 * @param userLoginName the user name
	 * @param userPassword the user password
	 * @return the user-data object for the logged in user
	 */
	public MainUserData login( final String userLoginName, final String userPassword ) throws SiteException {
		Log4jInit.pushDNC( getThreadLocalRequest(), null, UserData.DEFAULT_UID );
		logger.info( "Initiating login of user " + userLoginName );
		
		MainUserData mainUserData = null;
		try{
			//Login the user
			mainUserData = ServerSideUserManager.login( getLocalHttpSession(),
													 	getTrueRemoteAddress( UserData.DEFAULT_UID ),
													 	userLoginName, userPassword);
		} finally{
			Log4jInit.cleanDNC();
		}
		return mainUserData;
	}
	
	/**
	 * Validates if the user is logged in by checking the login name and the session
	 * @param userID the unique user ID
	 * @param userSessionId the user's server side session id
	 * @throws UserStateException if the user is not logged in!
	 */
	public void validate( final int userID, final String userSessionId ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {		
				logger.info( "Validating session for user " + userID );
				
				//Do nothing, we simply use this call to validate the user session
				
				return null;
			}
		}).execute();
	}

	/**
	 * This method should be user to perform user logout
	 * @param userID the unique user ID
	 * @param sessionId the id of the user session
	 */
	public void logout( final int userID, final String sessionId) throws SiteException {
		//Do not invalidate the session, it is not needed, we prefer to keep 
		//the session statistics intact until the session dies out by itself
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, sessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Initiating logout for user " + userID );
				
				//Get the user's session listener
				HttpUserSessionListener sessionListener = (HttpUserSessionListener) httpSession.getAttribute( HttpUserSessionListener.USER_SERVER_SESSION_LISTENER );
				
				//Log the user out, invalidate the server session
				ServerSideUserManager.logout(sessionListener, userID, sessionId, remoteAddr, httpSession, false);
				
				return null;
			}
		}).execute();
	}
	
	/**
	 * This method allows the user to delete his avatar
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 */
	public void deleteAvatar( final int userID, final String userSessionId ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Deleting an avatar for user " + userID );
				
				ConnectionWrapper<Void> deleteAvatarConnWrap = ConnectionWrapper.createConnectionWrapper( new DeleteProfileAvatarExecutor( userID ) );
				deleteAvatarConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
				
				return null;
			}
		}).execute();
	}
	
	/**
	 * This method allws user to choose an avatar from by index
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @param index the avatar's index
	 */
	public void chooseAvatar( final int userID, final String userSessionId, final int index ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Choosing the avatar " + index + " for user " + userID );
						
				final String avatarPathPrefix =  getServletContext().getRealPath("/") + getInitParameter("relative-avatar-images-path");
				PresetAvatarImages.AvatarDescriptor descriptor = PresetAvatarImages.getAvatarDescriptor( index );
				if( descriptor != null ) {
					
					//Make the user pay if the avatar costs something
					final UserData userData = UserSessionManager.getUserDataObject(userID);
					userData.decrementGoldPiecesCount( descriptor.price );
					
					UserFileData fileData = new UserFileData();
					fileData.mimeType = descriptor.mimeType.getMainMTSuffix();
					fileData.fileData = FileServletHelper.getReadFileDataInBytes( logger, avatarPathPrefix + descriptor.relativeURL );
					fileData.ownerID = userID;
					
					ProfileAvatarManager.insertUpdateAvatar( fileData );
				} else {
					logger.error("Unable to find avatar with index " + index + " the avatar is not set");
				}
				
				return null;
			}
		}).execute();		
	}
	
	/**
	 * This method allows to update user-profile 
	 * @param userData the user profile, with the new data
	 * @param oldUserPassword the old user password
	 * @param newUserPassword the new user password
	 * @param userSessionId the user session identifier
	 */
	public void update(final MainUserData userData, final String oldUserPassword,
						final String newUserPassword, final String userSessionId ) throws SiteException {
		final int userID = userData.getUID();
		
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {		
				logger.info( "Initiating profile update for user " + userID );
				
				try {
					//Check if we need to update the password hash
					if( ( newUserPassword != null ) && !newUserPassword.isEmpty() ){
						//Check that the old password is correct!
						ConnectionWrapper<Void> verifyPasswordConnWrap = ConnectionWrapper.createConnectionWrapper( new VerifyPasswordExecutor( userID, oldUserPassword ) );
						//Verify the old login-password pare
						verifyPasswordConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
					}
				} catch ( UserLoginException exception ){
					StatisticsSecurityManager.reportFailedLogin( remoteAddr, userID );
					throw exception;
				}
				
				//If nothing went wrong so far, then update the user profile data in the database
				ConnectionWrapper<Void> updateUserConnWrap = ConnectionWrapper.createConnectionWrapper( new UpdateUserProfileExecutor( userData, newUserPassword ) );
				//Execute the update
				updateUserConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
				
				//Update the data object in the user session
				UserSessionManager.setUserDataObject( userSessionId, userData, true );
				
				return null;
			}
		}).execute();
	}
	
	/**
	 * This method allows to get user profile data based on session Id
	 * @param userID the unique user ID
	 * @param userSessionId the id of the user session
	 * @return the user data 
	 */
	public MainUserData profile( final int userID, final String userSessionId ) throws SiteException {
		return (new SecureServerAccess<MainUserData>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected MainUserData action() throws SiteException {
				logger.info( "Initiating profile retrieval for user " + userID );
				
				//Get the user data 
				return UserSessionManager.getUserDataObject( userID );
			}
		}).execute();
	}
	
	/**
	 * Allows to get a profile for the given user. Either from the session manager, if the user is online, or otherwise from the db.
	 * @param profileForUserID the id of the user we want to get profile for
	 * @param isGetImages if true then we also get the user image descriptors for the profile
	 * @return the user profile
	 * @throws SiteException 
	 */
	private UserData getUserProfile( final int profileForUserID, final boolean isGetImages ) throws SiteException {
		//Get the user data 
		UserData userData = UserSessionManager.getUserDataObject( profileForUserID );
		
		if( userData == null ) {
			logger.debug( "The user " + profileForUserID + " is not online, retrieving profile from DB" );
			//Prepare the user data object for retrieving the profile
			userData = new UserData();
			userData.setUID( profileForUserID );
			
			//If the user is not online then we get the profile data from the database
			ConnectionWrapper<Void> getUserProfileConnWrap = ConnectionWrapper.createConnectionWrapper( new GetUserProfileExecutor( userData ) );
			getUserProfileConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
			
			if( isGetImages ) {
				//Get the user profile image descriptors, and add them into the user data object
				ConnectionWrapper<Void> getUserImgDescrConnWrap = ConnectionWrapper.createConnectionWrapper( new SelectProfileFileDescriptorsExecutor( userData ) );
				getUserImgDescrConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
			}
		}
		
		return userData;
	}
	
	/**
	 * This method allows to get user profile data based on user Id
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @param profileForUserID the unique ID user of the user we are getting profile for
	 * @return the user data 
	 */
	public UserData profile( final int userID, final String userSessionId, final int profileForUserID ) throws SiteException {
		return (new SecureServerAccess<UserData>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected UserData action() throws SiteException {
				logger.info( "Initiating profile retrieval by user " + userID + " for user " + profileForUserID );
				return getUserProfile( profileForUserID, true );
			}
		}).execute();
	}

	/**
	 * This method allows to check if user with ID: friendUserID is a friend of the user with ID: userID   
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @param friendUserID the unique ID user of the user we what to check that is a friend
	 * @return true if the user is a friend
	 */
	public Boolean isFriend( final int userID, final String userSessionId, final int friendUserID ) throws SiteException {
		return (new SecureServerAccess<Boolean>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Boolean action() throws SiteException {
				logger.info( "Checking that user " + friendUserID + " is a friend of user " + userID );
				
				IsFriendCheckExecutor isFriendExecutor = new IsFriendCheckExecutor( userID, friendUserID );
				ConnectionWrapper<IsFriendCheckExecutor.ResultHolder> isFriendConnWrap = ConnectionWrapper.createConnectionWrapper( isFriendExecutor );
				IsFriendCheckExecutor.ResultHolder result = isFriendExecutor.new ResultHolder(); 
				isFriendConnWrap.executeQuery( result, ConnectionWrapper.XCURE_CHAT_DB );

				return result.isFriend;
			}
		}).execute();
	}
	
	
	/**
	 * This method allows to retrieve the set of user friend IDs
	 * @param userID the user unique ID
	 * @param userSessionId the user's server side session id
	 * @return the set of friend of the user userID 
	 */
	public Set<Integer> getAllFriends( final int userID, final String userSessionId ) throws SiteException {
		return (new SecureServerAccess<Set<Integer>>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Set<Integer> action() throws SiteException {
				logger.info( "Retrieving the set of friends for user " + userID );
				
				Set<Integer> friendIds = new HashSet<Integer>();
				ConnectionWrapper<Set<Integer>> getFriendConnWrap = ConnectionWrapper.createConnectionWrapper( new SelectFriendsExecutor( userID ) );
				getFriendConnWrap.executeQuery( friendIds, ConnectionWrapper.XCURE_CHAT_DB );

				return friendIds;
			}
		}).execute();
	}
	
	/**
	 * This method allows to add/remove friend of the user with ID: userID   
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @param friendUserID the unique ID user of the user we what add/remove as a friend
	 * @param doRemove true if we want to remove a friend, false if we want to add one
	 * @return the true if the user was added as a friend, otherwise false
	 */
	public Boolean manageFriend( final int userID, final String userSessionId,
								final int friendUserID, final boolean doRemove ) throws SiteException {
		return (new SecureServerAccess<Boolean>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Boolean action() throws SiteException {
				logger.info( (doRemove? "Removing ": "Adding ") + friendUserID + " as a friend of user " + userID );
				
				QueryExecutor<Void> friendManagerExecutor;
				if( doRemove ) {
					friendManagerExecutor = new RemoveUserFriendExecutor( userID, friendUserID );
				} else {
					friendManagerExecutor = new AddUserFriendExecutor( userID, friendUserID );
				} 
				ConnectionWrapper<Void> friendManagerConnWrap = ConnectionWrapper.createConnectionWrapper( friendManagerExecutor );
				friendManagerConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );

				return new Boolean( !doRemove);
			}
		}).execute();
		
	}

	
	/**
	 * This method allows to delete user profile.
	 * @param userID the unique user ID
	 * @param userPassword the user password
	 * @param userSessionId the id of the user session
	 */
	public void delete( final int userID, final String userPassword,
						final String userSessionId ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Initiating profile deletion for user " + userID );
				
				try {
					//Check that the password is correct!
					ConnectionWrapper<Void> verifyPasswordConnWrap = ConnectionWrapper.createConnectionWrapper( new VerifyPasswordExecutor( userID, userPassword ) );
					//Verify the old login-password pare
					verifyPasswordConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
				} catch ( UserLoginException exception ){
					StatisticsSecurityManager.reportFailedLogin( remoteAddr, userID );
					throw exception;
				}
				
				//The user leaves all rooms where he is present
				ChatRoomsManager.getInstance().leaveAllRooms(userID);
				
				//Unregister user session, after the password is verified
				HttpUserSessionListener sessionListener = (HttpUserSessionListener) httpSession.getAttribute( HttpUserSessionListener.USER_SERVER_SESSION_LISTENER );
				UserSessionManager.unregisterAllUserSessions( sessionListener, userID, userSessionId );
				
				//Close all rooms created by the user!
				ChatRoomsManager.getInstance().removeOnlineAndCloseUserRooms( userID );
				
				//Delete the user from the database
				ServerSideUserManager.deleteUserProfile( userID );
				
				//Nothing to be returned here
				return null;
			}
		}).execute();
	}
	
	/**
	 * This method allows to delete user profile files.
	 * @param userID the unique user ID
	 * @param userSessionId the id of the user session
	 * @param fileIDs the list of ids of the files to delete
	 */
	public void deleteProfileFiles( final int userID, final String userSessionId, final List<Integer> fileIDs ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Initiating deletion of profile files "+fileIDs+" for user " + userID );
				
				//Remove image from the profile.
				ConnectionWrapper<Void> imageRemovalConnWrap = ConnectionWrapper.createConnectionWrapper( new DeleteProfileFilesExecutor( userID, fileIDs ) );
				imageRemovalConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
				
				for(int index = 0; index < fileIDs.size(); index++) {
					//Get the deleted file id
					final int fileID = fileIDs.get( index );
					//Update the user profile in the UserSessionManager, remove the file descriptor
					UserSessionManager.removeUserProfileFile(userID, fileID);
					//Remove the profile file from the cache
					ProfileFilesManager.filesCache.remove( fileID );
				}
				
				//Flush the TOP10 user files cache in order to update the cached data
				Top10UserDataCache.getInstance().flushUserFilesCachedData();
				
				//Nothing the be returned here
				return null;
			}
		}).execute();
	}
	
	/**
	 * This method allows count the users satisfying the user-search parameters
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param userSearchData the user search data object
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public Integer count( final int userID, final String userSessionId,
						final UserSearchData userSearchData ) throws SiteException {
		return (new SecureServerAccess<Integer>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Integer action() throws SiteException {
				logger.info( "Initiating user-profiles counting for the user-search dialog by user " + userID + "." );
				
				//Get the search results count, make sure that the admin user can see more
				userSearchData.isAdmin = UserSessionManager.getUserDataObject(userID).isAdmin();
				OnePageViewData<ShortUserData> foundUsers = new OnePageViewData<ShortUserData>(); 
				ConnectionWrapper<OnePageViewData<ShortUserData>> countFoundUsersConnWrap = ConnectionWrapper.createConnectionWrapper( new CountUserSearchResultsExecutor(userID, userSearchData) );
				countFoundUsersConnWrap.executeQuery( foundUsers, ConnectionWrapper.XCURE_CHAT_DB );
				
				return new Integer(foundUsers.total_size);
			}
		}).execute();
	}

	/**
	 * This method allows to browse users that satisfy the search query
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param userSearchData the user search data object
	 * @param offset the offset for the first statistical entry
	 * @param size the max number of entries to retrieve 
	 * @return an object containing requested statistical data
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	//We have to suppress warnings about custing to a generic type
	@SuppressWarnings("unchecked")
	public OnePageViewData<ShortUserData> browse( final int userID, final String userSessionId,
												final UserSearchData userSearchData, final int offset,
												final int size ) throws SiteException {
		return (new SecureServerAccess<OnePageViewData<ShortUserData>>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected OnePageViewData<ShortUserData> action() throws SiteException {
				logger.info( "Initiating user-profiles browsing for the user-search dialog by user " + userID + "." );
				
				//Check that the number of requested results is not exceding the allowed maximum
				int local_size; 
				if( size > UserSearchData.MAX_NUMBER_OF_USERS_PER_PAGE ) {
					local_size = UserSearchData.MAX_NUMBER_OF_USERS_PER_PAGE;
				} else {
					local_size = size;
				}
				
				//Get the search results, make sure that the admin user can see more
				userSearchData.isAdmin = UserSessionManager.getUserDataObject(userID).isAdmin();
				OnePageViewData<ShortUserData> foundUsers = new OnePageViewData<ShortUserData>(); 
				ConnectionWrapper countFoundUsersConnWrap = ConnectionWrapper.createConnectionWrapper( new BrowseUserSearchResultsExecutor(userID, userSearchData, offset, local_size) );
				countFoundUsersConnWrap.executeQuery( foundUsers, ConnectionWrapper.XCURE_CHAT_DB );
				
				return foundUsers;
			}
		}).execute();
	}
	/**
	 * Allows to set the user status to the server
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param userStatus the new user status
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void setUserStatus( final int userID, final String userSessionId, final UserStatusType userStatus ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Setting the new user status "+userStatus+" for user " + userID );
				
				//Set the user status
				MainUserData userData = UserSessionManager.getUserDataObject( userID );
				if( userData.getUserStatus() != userStatus ) {
					//Set the new user status if it is different from the old one
					userData.setUserStatus( userStatus );
					//Send the chat room notifications
					ChatRoomsManager.getInstance().notifyUserStatusChange( userData, userStatus );
				}
				
				//Nothing the be returned here
				return null;
			}
		}).execute();		
	}
	
	/**
	 * Allows to get the user status from the server
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @return the current user status
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public UserStatusType getUserStatus( final int userID, final String userSessionId ) throws SiteException {
		return (new SecureServerAccess<UserStatusType>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected UserStatusType action() throws SiteException {
				logger.info( "Retrieving the user status for user " + userID + "." );
				
				//Return the user status
				return UserSessionManager.getUserDataObject( userID ).getUserStatus( );
			}
		}).execute();
	}

	@Override
	public SiteInfoData getSiteUsersStatistics(int userID, String userSessionId) throws SiteException {
		return (new SecureServerAccess<SiteInfoData>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected SiteInfoData action() throws SiteException {
				logger.debug( "The user " + userID + ", here as " +
							 ( userID == UserData.UNKNOWN_UID || userID == UserData.DEFAULT_UID ? "<anonimous>" : "<regular>" ) +
							 ", tries to retrieve the user-site statistics" );
				return SiteUsersStatisticsManager.getInstance().getStatistics( userID );
			}	
		}).execute( true, false, false);		
	}
	
	/**
	 * This method allows to enable/disable bot for the user with the ID being equal to botID   
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @param botID the unique ID of the user who will be set/unset as a bot
	 * @param isBot true if we want to set the user as a bot, otherwise false
	 */
	@Override
	public void enableBot( final int userID, final String userSessionId,
								final int botID, final boolean isBot ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Setting the user " + botID + " as a " + (isBot ? "BOT" : "HUMAN" ) );
				
				//0. This is only allowed to do to the admin 
				if( !UserSessionManager.getUserDataObject(userID).isAdmin() ) {
					throw new InternalSiteException(InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR);
				}
				
				//1. Mark the user as a bot/human in the DB
				ConnectionWrapper<Void> markUseBotOrHumanConnWrap = ConnectionWrapper.createConnectionWrapper( new MarkAsBotOrHumanProfileExecutor( botID, isBot ) );
				markUseBotOrHumanConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
				
				//2. Mark he user as a bot/human in the session manager if the user is logged in
				MainUserData botUserData = UserSessionManager.getUserDataObject( botID );
				if( botUserData != null ) {
					botUserData.setBot( isBot );
				}
				
				//3. Notify the bot manager about the new bot/human
				ChatBotManager.getInstance().enableBot( botID, isBot ) ;
				
				//Nothing the be returned here
				return null;
			}
		}).execute();
	}

	@Override
	public Date setAvatarPrank( final int userID, final String userSessionId,
								final int prankedUserId, final int prankID,
								final boolean isRemove) throws SiteException {
		return (new SecureServerAccess<Date>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Date action() throws SiteException {
				logger.info( (isRemove ? "Removing": "Setting") + " prank " + prankID + " for user" + prankedUserId + " by user " + userID );
				Date prankExpDate = null;
				
				//Make the user pay
				final UserData prankedUserData = getUserProfile( prankedUserId, false );
				final UserData userData = UserSessionManager.getUserDataObject(userID);
				final Object synchObj = userPrankSynchFactory.getSynchObject( userID );
				try {
					synchronized( synchObj ) {
						//Check if the prank is there to be cleaned
						if( isRemove && ( ! AvatarSpoilersHelper.isAvatarSpoilerActive( prankedUserData ) ||
							prankedUserData.getAvatarSpoilerId() != prankID ) ) {
							/*The prank is not active, so we pretend that we removed it*/
							return null;
						}
						//Check if the user has enough money to set/clear the prank
						final int spoilerPrice = AvatarSpoilersHelper.getSpoilerPrice( prankID );
						userData.decrementGoldPiecesCount( spoilerPrice );
						
						//Mark the user data as pranked
						if( isRemove ) {
							prankedUserData.setAvatarSpoilerId( AvatarSpoilersHelper.UNDEFILED_AVATAR_SPOILER_ID );
							prankedUserData.setAvatarSpoilerExpDate(null);
						} else {
							prankedUserData.setAvatarSpoilerId( prankID );
							prankExpDate = AvatarSpoilersHelper.getNewSpoilerExpirationDate();
							prankedUserData.setAvatarSpoilerExpDate( prankExpDate );
						}
						
						//Update the prank status in the DB, even if the user is online
						ConnectionWrapper<Void>  attributesUpdater =  ConnectionWrapper.createConnectionWrapper( new UpdateUserAttributesExecutor( prankedUserData ) );
						attributesUpdater.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
						
						//Update the forum cache via the short user data cache
						ShortUserDataCache.getInstance( ShortUserDataCache.CacheIds.FORUM_CACHE_ID ).updateCachedUserData( prankedUserData );
						//Update the TOP10 cache via the short user data cache
						Top10UserDataCache.getInstance().updateUserDataPrank( prankedUserData );
					}
				} finally {
					userPrankSynchFactory.releaseSynchObject( userID );
				}
				
				//Nothing the be returned here
				return prankExpDate;
			}
		}).execute();
	}
	
	@Override
	public OnePageViewData<?> searchTop10( final int userID, final String userSessionId,
										   final Top10SearchData searchParams ) throws SiteException {
		return (new SecureServerAccess<OnePageViewData<?>>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected OnePageViewData<?> action() throws SiteException {
				logger.info( "Searching the Top10 of type " + searchParams.search_type + " forum by user " + userID + ", he is " +
							 ( userID == UserData.UNKNOWN_UID || userID == UserData.DEFAULT_UID ? "<anonimous>" : "<regular>" ) );
				
				//Try to get the search results from the cache
				OnePageViewData<?> result = Top10UserDataCache.getInstance().getSearchResults( searchParams );
				
				//If the search results are not in the cache then try to get them from the database
				if( result == null ) {
					switch( searchParams.search_type ) {
						case TOP_USER_FILES_SEARH_TYPE:
							final OnePageViewData<ShortUserFileDescriptor> local_result_files = new OnePageViewData<ShortUserFileDescriptor>();  
							ConnectionWrapper<OnePageViewData<ShortUserFileDescriptor>> getTop10FilesConnWrap = ConnectionWrapper.createConnectionWrapper( new SelectTop10FileDescriptorsExecutor( searchParams ) );
							getTop10FilesConnWrap.executeQuery( local_result_files, ConnectionWrapper.XCURE_CHAT_DB );
							result = local_result_files;
							break;
						case TOP_CHAT_MESSAGES_SEARH_TYPE:
						case TOP_FORUM_POSTS_SEARH_TYPE:
						case TOP_REGISTRATIONS_SEARH_TYPE:
						case TOP_USER_VISITS_SEARCH_TYPE:
						case TOP_MONEY_SEARH_TYPE:
						case TOP_TIME_ON_SITE_SEARH_TYPE:
							final OnePageViewData<ShortUserData> local_result_user = new OnePageViewData<ShortUserData>();  
							ConnectionWrapper<OnePageViewData<ShortUserData>> getTop10UserDataConnWrap = ConnectionWrapper.createConnectionWrapper( new SelectTop10UserProfilesExecutor( searchParams ) );
							getTop10UserDataConnWrap.executeQuery( local_result_user, ConnectionWrapper.XCURE_CHAT_DB );
							result = local_result_user;
							break;
						case TOP_UNKNOWN_SEARH_TYPE:
						default:
							//This should no be happening
							throw new InternalSiteException( InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR );
					}
					//Place the search result into the cache
					Top10UserDataCache.getInstance().placeSearchResults( searchParams, result );
				}
				
				//Return the result 
				return result;
			}	
		}).execute( true, true, false);
	}
}
