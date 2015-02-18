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
 * The server-side security package for access statistics.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.server.security.statistics;

import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.xcurechat.client.rpc.exceptions.AccessBlockedException;
import com.xcurechat.client.rpc.exceptions.LoginAccessBlockedException;
import com.xcurechat.client.rpc.exceptions.UserStateException;

import com.xcurechat.server.core.HttpUserSessionListener;

import com.xcurechat.client.data.UserData;

/**
 * @author zapreevis
 * This class is supposed to test how frequently the session is accessed
 * and to monitore the number of incorrect userID/session validations
 * for every given IP address and to block sessions and IP adresses
 */
public class StatisticsSecurityManager {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( StatisticsSecurityManager.class );
	
	//The number of milliseconds between clean ups of the StatisticsSecurityManager
	public static final long MIN_CLEAN_UP_INTERVAL_MILLISEC = 1800000; //30*60*1000
	
	//The max time since the last access from the given host
	public static final long MAX_TIME_SINCE_LAST_ACCESS_MILLISEC = 900000; //15*60*1000
	
	//Mappings from the remote address to the (login,login/session validation failure stats )  
	private static HashMap<String, HashMap<Integer, SessionAccessStatistics> > remoteAddrToLoginStats = new HashMap<String, HashMap<Integer, SessionAccessStatistics> >();
	
	//Mapping from the remote address plus the last access (session stats and login validation) time
	private static ConcurrentHashMap<String, Date> removeAddrToLastAccDate = new ConcurrentHashMap<String, Date>();
	
	//The date and time of the last clean up of this object's data
	private static Date lastCleanUpTime = new Date();
	
	/**
	 * Validating access statistics for the session and user login/session validation
	 * This method makes the client's access to be counted. I.e. we update the session
	 * access counter.
	 * @param httpSession the http session corresponding to this request
	 * @param remoteAddr the IP address fro mwhich the request has come, should ne non NULL
	 * 					only if userLoginName is not null 
	 * @param userID must be UserData.UNKNOWN_UID if we only need to validate the session access statistics! 
	 * @param populateSessionStat if true then, if HttpUserSessionListener is not present in the
	 * 								session object, instead of throwing an exception, we create
	 * 								a new one and exit.  
	 * @throws UserStateException If populateSessionStat==false and HttpUserSessionListener is not
	 * 								present in httpSession
	 * @throws AccessBlockedException If the session is overloaded with requests or if there were
	 * 									too many incorrect logins/session validations for the
	 * 									userLoginName from the remoteAddr. 
	 */
	public static void validateAccess( final HttpSession httpSession, final String remoteAddr,
										final int userID, final boolean populateSessionStat )
										throws UserStateException, AccessBlockedException {
		validateAccess( httpSession, remoteAddr, userID, populateSessionStat, true );
	}
	
	/**
	 * Validating access statistics for the session and user login/session validation
	 * @param httpSession the http session corresponding to this request
	 * @param remoteAddr the IP address fro mwhich the request has come, should ne non NULL
	 * 					only if userLoginName is not null 
	 * @param userID must be UserData.UNKNOWN_UID if we only need to validate the session access statistics! 
	 * @param populateSessionStat if true then, if HttpUserSessionListener is not present in the
	 * 								session object, instead of throwing an exception, we create
	 * 								a new one and proceed with the validation. 
	 * @param isUserActionCall if set to false then we do not update the session access counter
	 * 						this is done to reduce user blocking in case of the slow internet
	 *						connection. basically the automatic calls from the client should not
	 *						be counted, to ount only user actions.
	 * @param skipLoginValidationStat if true then we skip the login/session validation statistics check
	 * @throws UserStateException If populateSessionStat==false and HttpUserSessionListener is not
	 * 								present in httpSession
	 * @throws AccessBlockedException If the session is overloaded with requests or if there were
	 * 									too many incorrect logins/session validations for the
	 * 									userLoginName from the remoteAddr. 
	 */
	public static void validateAccess( final HttpSession httpSession, final String remoteAddr,
										final int userID, final boolean populateSessionStat,
										final boolean isUserActionCall )
										throws UserStateException, AccessBlockedException {
		validateAccess( httpSession, remoteAddr, userID, populateSessionStat, isUserActionCall, true );
	}
	/**
	 * Validating access statistics for the session and user login/session validation
	 * @param httpSession the http session corresponding to this request
	 * @param remoteAddr the IP address fro mwhich the request has come, should ne non NULL
	 * 					only if userLoginName is not null 
	 * @param userID must be UserData.UNKNOWN_UID if we only need to validate the session access statistics! 
	 * @param populateSessionStat if true then, if HttpUserSessionListener is not present in the
	 * 								session object, instead of throwing an exception, we create
	 * 								a new one and proceed with the validation. 
	 * @param isUserActionCall if set to false then we do not update the session access counter
	 * 						this is done to reduce user blocking in case of the slow internet
	 *						connection. basically the automatic calls from the client should not
	 *						be counted, to ount only user actions.
	 * @param validateLoginSessionStat if true then we check on the login/session validation statistics
	 * @throws UserStateException If populateSessionStat==false and HttpUserSessionListener is not
	 * 								present in httpSession
	 * @throws AccessBlockedException If the session is overloaded with requests or if there were
	 * 									too many incorrect logins/session validations for the
	 * 									userLoginName from the remoteAddr. 
	 */
	public static void validateAccess( final HttpSession httpSession, final String remoteAddr,
										final int userID, final boolean populateSessionStat,
										final boolean isUserActionCall, final boolean validateLoginSessionStat )
										throws UserStateException, AccessBlockedException {
		//NOTE: Here, we only synchronize on httpSession because SessionAccessStatistics is all synchronized 
		
		logger.debug( "Validating session access and login/session validation statistics for user '" +
						userID + "' from host '" + remoteAddr + "'" );
		//Get the user session statistics
		HttpUserSessionListener sessionListener = null; 
		synchronized( httpSession ) {
			sessionListener = (HttpUserSessionListener) httpSession.getAttribute( HttpUserSessionListener.USER_SERVER_SESSION_LISTENER );
			if( sessionListener == null ){
				if( ! populateSessionStat ) {
					logger.error( "The session of '"+userID+"' is not filled with the sessionListener" );
					//If the HTTP session is not populated with the Listener, then the user is not registered!  
					throw new UserStateException( UserStateException.USER_IS_NOT_LOGGED_IN_ERR );
				} else {
					logger.debug( "Creating a new sessionListener for user '"+userID+"'" );
					//NOTE: This session listener is created with and UNKNOWN_UID, no session ID, and no
					//remote address. This is fine because if the user is not logged in this will be found
					//out, later when the userID and the sessionID are validated against each other.
					sessionListener = new HttpUserSessionListener();
					httpSession.setAttribute( HttpUserSessionListener.USER_SERVER_SESSION_LISTENER, sessionListener );
				}
			}
		}
		
		//Check if the statistics says that we are under an attack if yet, throw an exception
		SessionAccessStatistics sessionAccStat =  sessionListener.getAccessStatistics();
		if( sessionAccStat.isAccessBlocked() ) {
			logger.error( "It is reported that the server session is overloaded with requests." );
			//If we are under an attack and the session access is blocked
			AccessBlockedException exception = new AccessBlockedException(AccessBlockedException.TOO_MANU_SITE_REQUESTS_EXCEPTION_ERR);
			exception.setRemainingBlockingTime( sessionAccStat.getRemainingBlockingTime() );
			throw exception;
		} else {
			//If this is a user action call from the client then we update the counter
			if( isUserActionCall ) {
				//If we are not under an attack yet then update the access statistics
				sessionAccStat.updateCounter();
			} else {
				logger.debug( "A system call to the server from IP: " + remoteAddr + " by user " +
							 userID + ", the session acccess statistics counter increase is skipped!" );
			}
		}
		
		if( validateLoginSessionStat ) {
			validateLoginValidationStats( remoteAddr, userID );
		} else {
			logger.debug( "Skipping the login/session validation statistics check here, for user " + userID +
						  " from hosr " + remoteAddr + ", due to the method's arguments");
		}
	}
	
	/**
	 * Checks if the session access for user is blocked due to too many invalid login/session validations
	 * @param remoteAddr the IP address fro mwhich the request has come, should be non null,
	 * only if userLoginName is not null. 
	 * @param userID must be UserData.UNKNOWN_UID if we only need to validate the session access statistics!
	 * @throws LoginAccessBlockedException if there were too many incorrect logins/session validations for the
	 * userLoginName from the remoteAddr. 
	 */
	private static void validateLoginValidationStats(final String remoteAddr, final int userID ) throws AccessBlockedException {
		logger.debug( "Validating login/session validation statistics for user '" + userID + "' from host '" + remoteAddr + "'" );
		//WARNING: Every user, before he tries to log in, or register is identified by UserData.DEFAULT_UID
		//Therefore we do not exclude the case of userID == UserData.DEFAULT_UID
		if( ( userID != UserData.UNKNOWN_UID ) ) {
			if( remoteAddr != null ) {
				//Get mappings for the given local address
				SessionAccessStatistics loginValidStat = getLoginValidationStatistics( remoteAddr, userID );
				//Synchronize on the particular statistics, since it is not necessarily unique for each http session
				synchronized( loginValidStat ) {
					if( loginValidStat.isAccessBlocked() ) {
						logger.error( "It is reported that user '" + userID + "' from host '" + remoteAddr + "' is blocked" );
						//Some one is trying to hack the password or the session
						LoginAccessBlockedException exception = new LoginAccessBlockedException(LoginAccessBlockedException.TOO_MANU_FAILED_LOGINS_EXCEPTION_ERR);
						exception.setRemainingBlockingTime( loginValidStat.getRemainingBlockingTime() );
						throw exception;
					} else {
						//We do not update the counter here, because it has to be
						//updated only if there was an incorrect login/session validation attempt
					}
				}
			} else {
				logger.error( "Impossible to check login/session validation statistics for user '" +
								userID + "' because the provided remote address is NULL." );
			}
		} else {
			logger.warn("The user ID is UNKNOWN_UID, checking of the userID/sessionID validation statistics is skipped");
		}
	}
	
	/**
	 * Allows to report that login/ session validation failed for user userLoginName from remoteAddr 
	 * @param remoteAddr the address from which an incorrect login/session validatin was initiated
	 * @param userID the unique user ID for which this login/session validation was initiated
	 */
	public static void reportFailedLogin( final String remoteAddr, final int userID ) {
		logger.error( "Reporting failed login/session validation for user '"
						+ userID + "' using host '" + remoteAddr + "'." );
		//Get mappings for the given local address
		SessionAccessStatistics loginValidStat = getLoginValidationStatistics( remoteAddr, userID );
		//Synchronize on the particular statistics, since it is not necessarily unique for each http session
		synchronized( loginValidStat ) {
			//Update the counter here
			loginValidStat.updateCounter();
		}
	}
	
	/**
	 * Allows to clean up the unnecessary statistics from the manager. We first
	 * look when the last clean up was then if the last access from the given
	 * host was sufficiently long ago, then we check if some logins were locked
	 * for this host, if not then we remove the data.
	 */
	public static void cleanUp() {
		logger.info( "Cleaning up the data in StatisticsSecurityManager" );
		synchronized( lastCleanUpTime ) {
			logger.debug( "The last StatisticsSecurityManager clean up was done at " +
							lastCleanUpTime + " and now it is " + new Date() );
			if( ( System.currentTimeMillis() - MIN_CLEAN_UP_INTERVAL_MILLISEC ) > lastCleanUpTime.getTime() ) {
				//If the previous clean up was too long ago then do it
				synchronized( remoteAddrToLoginStats ) {
					Iterator<String> hostIter = remoteAddrToLoginStats.keySet().iterator();
					while( hostIter.hasNext() ) {
						String remoteAddr = hostIter.next();
						//Apdate the last access date
						Date lastAccesTime = removeAddrToLastAccDate.get( remoteAddr );
						
						logger.debug( "The last access from host " + remoteAddr + " was done at " +
										lastAccesTime + " and now it is " + new Date() );
						//Check if the last access from the given host was sufficiently long ago
						if( ( System.currentTimeMillis() - MAX_TIME_SINCE_LAST_ACCESS_MILLISEC ) > lastAccesTime.getTime() ) {
							//If the last access from this host was sufficiently long ago then do the clean up
							HashMap<Integer, SessionAccessStatistics> loginToStats = remoteAddrToLoginStats.get( remoteAddr );
							synchronized( loginToStats ) {
								//Check if there are no blocked logins for this host
								Iterator<Integer> loginIter = loginToStats.keySet().iterator();
								boolean isNoOneBlocked = true;
								while( loginIter.hasNext() && isNoOneBlocked ) {
									SessionAccessStatistics stat = ( loginToStats.get( loginIter.next() ) );
									isNoOneBlocked = ! stat.isAccessBlocked();
								}
								
								//If there are no blocked logins then remove information for this host
								if( isNoOneBlocked ) {
									logger.info( "Removing failed login/session validation statistics for host '"+ remoteAddr +"'" );
									removeAddrToLastAccDate.remove( remoteAddr );
									hostIter.remove();
								}
							}
						}
					}
				}
				
				//Update the last clean up time
				lastCleanUpTime.setTime( System.currentTimeMillis() );
			}
		}
	}
	
	private static HashMap<Integer, SessionAccessStatistics> getAddressToStatsMapping( final String remoteAddr ) {
		HashMap<Integer, SessionAccessStatistics> loginToStats = null;
		logger.debug("Retrieving the login-statistics object mappings for host '" + remoteAddr + "'");
		//Synchronize on the remoteAddrToLoginStats mapping
		synchronized( remoteAddrToLoginStats ){
			//Fisrt update the last access date
			removeAddrToLastAccDate.put(remoteAddr, new Date());
			
			//Then get the mappings for this host 
			loginToStats = remoteAddrToLoginStats.get( remoteAddr );
			if( loginToStats == null ) {
				logger.debug("The mapping for host '" + remoteAddr + "' does not exist, creating a new one.");
				loginToStats = new HashMap< Integer, SessionAccessStatistics>();
				remoteAddrToLoginStats.put( remoteAddr, loginToStats );
			}
		}
		return loginToStats;
	}
	
	private static SessionAccessStatistics getLoginValidationStatistics( final String remoteAddr, final int userID ) {
		HashMap<Integer, SessionAccessStatistics> addrToStats = getAddressToStatsMapping( remoteAddr );
		SessionAccessStatistics loginValidStat = null;
		logger.debug("Retrieving the login/session validation statistics object for user '" + userID + "'");
		//Synchronize on the mappings for login/stat for a particular remote address
		synchronized( addrToStats ){
			loginValidStat = addrToStats.get( userID );
			if( loginValidStat == null ) {
				logger.debug("The mapping for user '" + userID + "' does not exist, creating a new one.");
				loginValidStat = new SessionAccessStatistics(false, userID);
				addrToStats.put( userID, loginValidStat );
			}
		}
		
		return loginValidStat;
	}
}
