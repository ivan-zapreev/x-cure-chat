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

import java.util.HashMap;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.data.ShortFileDescriptor;

import com.xcurechat.client.rpc.exceptions.UserStateException;
import com.xcurechat.client.rpc.exceptions.AccessBlockedException;

import com.xcurechat.server.security.bcrypt.BCrypt;

import com.xcurechat.server.security.statistics.StatisticsSecurityManager;

/**
 * @author zapreevis
 * This class provides synchronized methods for managing user session, namely
 * It allows to verify that the user session is valid, to register new user session,
 * to unregister user session, to provide a list of logged-on users and etc.
 */
public final class UserSessionManager {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( UserSessionManager.class );
	
	//Mapping from the user sessionId to the user session object
	private static HashMap<String, UserSession> sessionIdToUserSession = new HashMap<String, UserSession>();
	//Mapping from the user ID to the user sessionId
	private static HashMap<Integer, String> loginToSessionId = new HashMap<Integer, String>();
	//Mapping from the user login to the counter for the number of times the user has logged in
	private static HashMap<Integer, Integer> loginToLoginCounter = new HashMap<Integer, Integer>();
	
	//The object to synchronize the methods of this class
	private static final Object synchObj = new Object();
	
	/**
	 * Returns the user's online/offline status
	 * @param userID the user ID we want to know status for
	 * @return true if user is online, otherwise false
	 */
	public static boolean isUserOnline( final Integer userID ) {
		synchronized( synchObj ) {
			return ( loginToSessionId.get( userID ) != null);
		}
	}
	
	/**
	 * Allows to retrieve the number of currently logged-in users, for each user we count the number of times it is logged-in
	 * Because the same user can be logged in several times from different browsers or even computers
	 * @return the number of currently logged-in users
	 */
	public static int getOnlineUsersCount() {
		synchronized( synchObj ) {
			Integer total = 0;
			for( Integer count : loginToLoginCounter.values() ) {
				total += count;
			}
			return total;
		}
	}
	
	/**
	 * Allows to add a new file descriptor into the user profile
	 * @param userID the if of the user to set the image to
	 * @param profileFileDescr the profile-file descriptor
	 */
	public static void addUserProfileFile( final int userID, final ShortFileDescriptor profileFileDescr ) {
		synchronized( synchObj ) {
			MainUserData userData = getUserDataObject( userID );
			if( userData != null ) {
				userData.addUserProfileFileDescr( profileFileDescr );
			}
		}
	}
	
	/**
	 * Allows to remove the user file descriptor from the profile
	 * @param userID the id of the user we want to remove the profile for
	 * @param fileID the id of the file that we want to remove from the profile data
	 */
	public static void removeUserProfileFile( final int userID, final int fileID ) {
		synchronized( synchObj ) {
			MainUserData userData = getUserDataObject( userID );
			if( userData != null ) {
				userData.removeUserProfileFileDescr( fileID );
			}
		}
	}
	
	/**
	 * Returns the user session data
	 * @param userID the unique user ID
	 * @return the user data for the logged on user
	 */
	public static MainUserData getUserDataObject( final int userID ) {
		synchronized( synchObj ) {
			final String sessionId = loginToSessionId.get( userID );
			//Retrieve the user data object from memory
			return ( sessionId != null ? sessionIdToUserSession.get( sessionId ).getUserData() : null );
		}
	}

	/**
	 * Allows to update the user object stores in the user session
	 * @param sessionId a valid id of a user session
	 * @param userData the user data 
	 * @param preserveProfileType true if we should preserve the user
	 * profile type, or otherwise we use the type from the newly
	 * given user data object 
	 */
	public static void setUserDataObject( final String sessionId, MainUserData userData,
											final boolean preserveProfileType ) {
		synchronized( synchObj ) {
			//Here we want to keep using the same MainUserData as stored in the userSession
			//This is done for optimizing the retrieval of user's data in the chat rooms
			
			//0. Update the needed fields of userData
			userData.setUserSessionId( sessionId );
			userData.setOnline(true);
			
			//1. Get the user session object and the previously stored MainUserData
			UserSession userSessionObject = sessionIdToUserSession.get( sessionId );
			MainUserData oldUserData = userSessionObject.getUserData();
			
			//2. If we need to preserve the user type, then we copy the type data
			if( preserveProfileType ) {
				if( oldUserData != null ) {
					//WARNING: The following assignments are Important, otherwise the
					//security can be breached!!!
					userData.setUserProfileType( oldUserData.getUserProfileType() );
				} else {
					logger.warn( "Unable to preserve the user profile type, "
									+ "because the old UserData object for user "
									+ userData.getUID() + " is null.");
					//WARNING: Set the default user profile type, just in case because
					///this is an exceptional situation!!!
					userData.setUserProfileType( MainUserData.SIMPLE_USER_TYPE );
				}
			}
			
			//3. Copy the user-editable profile information from the provided 
			//   MainUserData to the old one, or update user data in the
			//   session object only if there was no old MainUserData there. 
			if( oldUserData != null ) {
				oldUserData.setUserManagedData( userData );
			} else {
				userSessionObject.setUserData( userData );
			}
		}
	}
	
	/**
	 * This mehtod registeres a new user session. If a user with the same login name is already in
	 * then, since the login name is unique, we map it to the existing session id and user session.
	 * @param httpSession the session object for the servlet session from which the call comes
	 * @param userData the user object that is filled with (at least) the login name
	 * @param remoteAddr the user's remote address
	 * @param isOnLogin must be true if this is an after login session registration otherwise must be false!
	 * this object is updated with the sessionId and the hash code of the UserSession object
	 */
	public static void registerUserSession( HttpSession httpSession, final MainUserData userData,
											final String remoteAddr, final boolean isOnLogin ) {
		synchronized( synchObj ) {
			//Add user sessionId/login/UserSession mappings
			final int userID = userData.getUID();
			logger.debug("Registering a new user session for user '"+userID+"'");
			
			//First check if someone is already logged in within this session, if yes, remove this person
			//because we do not support two different persons being registered in the same http session
			HttpUserSessionListener sessionListener = (HttpUserSessionListener) httpSession.getAttribute( HttpUserSessionListener.USER_SERVER_SESSION_LISTENER );
			if( sessionListener != null ) {
				if( sessionListener.isUserSet() ) {
					unregisterUserSession( sessionListener, sessionListener.getUserID(), sessionListener.getUserSessionId() );
				}
			} else {
				logger.fatal("Somehow the HttpUserSessionListener is missing the session registration of user '"+userID+"'!" );
			}
			
			//This variable will store the user session ID, a new or an old one
			String userSessionId = null;
			Integer loginCounter = null;
			//If the user is already logged on
			if( loginToSessionId.keySet().contains( userID ) ){
				logger.debug("The user session for '"+userID+"' already exists");
				//Retrieve the session id
				userSessionId = loginToSessionId.get( userID );
				//Get the current number of user logins
				loginCounter = loginToLoginCounter.get( userID );
			} else {
				logger.debug("Allocating a new user session for user '"+userID+"'");
				//Generate new session id
				userSessionId = BCrypt.hashpw( BCrypt.gensalt() + userData.getUserLoginName() + System.currentTimeMillis() , BCrypt.gensalt() );
				//Store new session mappings, we do not fill the user session with the profile data yet
				//we do this for security reasons, so that the profile type is set to default user type
				sessionIdToUserSession.put( userSessionId, new UserSession( null ) );
				loginToSessionId.put( userID, userSessionId );
				//Initialize the login counter
				loginCounter = new Integer(0);
			}
			//Update the user session with the user profile data, preserving the
			//old profile type, in case it is not registration on login, because
			//then the user profile comes straight from the database
			setUserDataObject( userSessionId, userData, !isOnLogin );
			
			//Increase the counter for the number of user logins
			loginCounter = loginCounter + 1;
			loginToLoginCounter.put( userID, loginCounter );
			
			//Update the user object with the session Id!
			userData.setUserSessionId( userSessionId );
			
			//Update the HttpUserSessionListener with the user name and user session id;
			if( sessionListener != null ) {
				sessionListener.setUser( userID, userSessionId, remoteAddr );
			}
			
			logger.debug("The user '" + userID + "' is now logged in " + loginCounter + " time(s)");
		}
	}
	
	/**
	 * Checks that the login-sessionId pare is valid, i.e. the user is logged on
	 * @param httpSession the session object for the servlet session from which the call comes
	 * @param remoteAddr the IP address of the user making the request
	 * @param userID the unique user ID
	 * @param userSessionId the user sessionId
	 * @throws UserStateException if the user login and sessionId do not match
	 * @throws AccessBlockedException if there are too many requests within
	 * this http sesion or the number/frequency of user login/session validation
	 * has exceeeded a certain value.  
	 */
	public static void validateLoginVsSessionId( final HttpSession httpSession, final String remoteAddr,
												final int userID, final String userSessionId )
												throws UserStateException, AccessBlockedException {
		logger.debug( "Trying to validate user login - session pare for user '" + userID + "' from host " + remoteAddr );
		
		synchronized( synchObj ) {
			if( ( userID != UserData.UNKNOWN_UID ) && ( userID != UserData.DEFAULT_UID ) && loginToSessionId.keySet().contains( userID ) ) {
				if ( ! userSessionId.equals( loginToSessionId.get( userID ) ) ) {
					logger.error("The login for user '" + userID + "' does not match the provided user session id.");
					//Report that the validation has failed
					StatisticsSecurityManager.reportFailedLogin(remoteAddr, userID);
					//If the user session does not match the user login we report an exception
					throw new UserStateException( UserStateException.USER_IS_NOT_LOGGED_IN_ERR );
				}
			} else {
				logger.error( "The user-session validation has failed for user " + userID + ", is UNKNOWN_ID: " +
							  ( userID == UserData.UNKNOWN_UID ) + ", is DEFAULT_ID: " +
							  ( userID == UserData.DEFAULT_UID ) + ", is logged in: " +
							  loginToSessionId.keySet().contains( userID ) );
				throw new UserStateException( UserStateException.USER_IS_NOT_LOGGED_IN_ERR );
			}
		}
	}
	
	/**
	 * Validates that the user login maps to the user session and then removes user session
	 * @param sessionListener the session listener object, the reason we do not pass session
	 * itself is that in case of automatic logout the session is already invalid and we can
	 * not possibly get any attriputes put into the session
	 * @param userID the unique user ID
	 * @param sessionId the valid id of the user session
	 */
	public static void unregisterUserSession( HttpUserSessionListener sessionListener, final int userID, final String sessionId ) {
		synchronized( synchObj ) {
			logger.debug("Unregstering user session for user '" + userID + "'");
			
			//Reset the user login/session in the session listener, to avoiding another (automatic) logout
			if( sessionListener != null ) {
				sessionListener.setUser( UserData.UNKNOWN_UID, null, null );
			}
			
			//Logout the user completely if he is not longer logged
			if( loginToLoginCounter.containsKey( userID ) ) {
				Integer loginCounter = loginToLoginCounter.get( userID ) - 1;
				
				if( loginCounter == 0 ) {
					//Remove user session mappings if the user is
					//no longer logged infrom other places
					loginToSessionId.remove( userID );
					loginToLoginCounter.remove( userID );
					sessionIdToUserSession.remove( sessionId );
				} else {
					//Put the decremented counter back
					loginToLoginCounter.put( userID, loginCounter );
				}
				
				logger.debug("The user '" + userID + "' is now logged in " + loginCounter + " time(s)");
			} else {
				logger.warn("The user '" + userID + "' is not logged in.");
			}
		}
	}
	
	/**
	 * Allows to get the number of clients the user is logged on from
	 * @param userID the if of the user we want to get information for
	 * @return the number of times the user is logged in
	 */
	public static int getUserLoginCounter( final int userID ) {
		synchronized( synchObj ) {
			Integer loginCounter = loginToLoginCounter.get( userID );
			if( loginCounter == null ) {
				return 0;
			} else {
				return loginCounter;
			}
		}
	}
	
	/**
	 * Allows to test if the user is online
	 * @param userID the id we want to get the information for
	 * @return true if the user is online
	 */
	public static boolean isUserOnline( final int userID ) {
		return getUserLoginCounter( userID ) > 0;
	}
	
	/**
	 * Allows to test if the user is offline
	 * @param userID the id we want to get the information for
	 * @return true if the user is offline
	 */
	public static boolean isUserOffline( final int userID ) {
		return getUserLoginCounter( userID ) == 0;
	}
	
	/**
	 * Validates that the user login maps to the user session and then removes all user sessions
	 * @param sessionListener the session listener object, the reason we do not pass session
	 * itself is that in case of automatic logout the session is already invalid and we can
	 * not possibly get any attriputes put into the session
	 * @param userID the unique user ID
	 * @param sessionId the valid id of the user session
	 */
	public static void unregisterAllUserSessions( HttpUserSessionListener sessionListener, final int userID, final String sessionId ) {
		synchronized( synchObj ) {
			logger.debug("Unregstering all user sessions for user '" + userID + "'");
			
			//Reset the user login/session in the session listener, to avoing another (automatic) logout
			if( sessionListener != null ) {
				sessionListener.setUser( UserData.UNKNOWN_UID, null, null );
			}
			
			//Remove all user session mappings
			loginToSessionId.remove( userID );
			loginToLoginCounter.remove( userID );
			sessionIdToUserSession.remove( sessionId );
			
			logger.debug("The user '" + userID + "' is now logged in 0 time(s)");
		}
	}
}
