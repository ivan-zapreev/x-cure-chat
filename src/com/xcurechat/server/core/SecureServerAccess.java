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
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.server.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.xcurechat.server.Log4jInit;
import com.xcurechat.server.security.statistics.StatisticsSecurityManager;
import com.xcurechat.server.utils.HTTPUtils;

import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.SiteException;

/**
 * @author zapreevis
 * This class is supposed to manage the secure and synchronized
 * access to the server side functionality. It also initializes 
 * logging.
 */
public abstract class SecureServerAccess<ReturnObjectType> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( SecureServerAccess.class );
	
	protected final HttpSession httpSession;
	protected final HttpServletRequest httpRequest;
	protected final int userID;
	protected final String userSessionId;
	protected final String remoteAddr; 
	
	/**
	 * @param userID the user unique ID
	 * @return returns the remote address as specified in the http request 
	 */
	public static String getTrueRemoteAddress(final int userID, HttpServletRequest httpRequest ) {
		return HTTPUtils.getTrueRemoteAddr( userID, httpRequest );
	}

	public SecureServerAccess( final HttpSession httpSession, final HttpServletRequest httpRequest,
								final int userID, final String userSessionId ) {
		this.httpSession = httpSession;
		this.httpRequest = httpRequest;
		this.userID = userID;
		this.userSessionId = userSessionId;
		this.remoteAddr = getTrueRemoteAddress(userID, httpRequest);
	}

	/**
	 * This method initiates secure execution of the site access.
	 * It performs logging and security checks and initialization 
	 * and also synchronization for the userID. Then it executes
	 * what ever it is in the action method. Note that we never
	 * populate session with the HttpUserSessionListener.
	 * This method makes the client's access to be counted. I.e.
	 * we update the session access counter.
	 * @return what ever it is returned by the action method
	 */  
	public ReturnObjectType execute( ) throws SiteException {
		return execute( false );
	}

	//Get the synchronization factory for users
	private static final SynchFactory userSynchFactory = SynchFactory.getSynchFactory( SynchFactory.USER_FACTORY_NAME );

	/**
	 * This method initiates secure execution of the site access.
	 * It performs logging and security checks and initialization 
	 * and also synchronization for the userID. Then it executes
	 * what ever it is in the action method.
	 * This method makes the client's access to be counted. I.e.
	 * we update the session access counter.
	 * @param populateSessionStat if true then, if HttpUserSessionListener is not present in the
	 * 								session object, instead of throwing an exception, we create
	 * 								a new one and exit.
	 * @return what ever it is returned by the action method
	 */  
	public ReturnObjectType execute( final boolean populateSessionStat ) throws SiteException {
		return execute( populateSessionStat, true );
	}

	/**
	 * This method initiates secure execution of the site access.
	 * It performs logging and security checks and initialization 
	 * and also synchronization for the userID. Then it executes
	 * what ever it is in the action method.
	 * @param populateSessionStat if true then, if HttpUserSessionListener is not present in the
	 * 								session object, instead of throwing an exception, we create
	 * 								a new one and exit.
	 * @param  isUserActionCall if set to false then we do not update the session access counter
	 * 						this is done to reduce user blocking in case of the slow internet
	 *						connection. basically the automatic calls from the client should not
	 *						be counted, to ount only user actions.
	 * @return what ever it is returned by the action method
	 */  
	public ReturnObjectType execute( final boolean populateSessionStat, final boolean isUserActionCall ) throws SiteException {
		return execute( populateSessionStat, isUserActionCall, true );
	}

	/**
	 * This method initiates secure execution of the site access. It performs logging and security
	 * checks and initialization and also synchronization for the userID. Then it executes what ever
	 * it is in the action method. Note that this method does not synchronize in case the user ID is
	 * set to be unknown or default. This should reduce problems with possible deadlocks in the action
	 * methods and also speed up the access for the non-logged in users which can not do data modifications
	 * any ways, so they can have concurrent read access to all the data.
	 * @param populateSessionStat if true then, if HttpUserSessionListener is not present in the
	 * 								session object, instead of throwing an exception, we create
	 * 								a new one and exit.
	 * @param  isUserActionCall if set to false then we do not update the session access counter
	 * 						this is done to reduce user blocking in case of the slow internet
	 *						connection. basically the automatic calls from the client should not
	 *						be counted, to ount only user actions.
	 * @param validateLoggedIn if true then we validate that the user is logged in otherwise we
	 * 							do not. This is done because some of the user access should be
	 * 							allowed for non-logged in users.
	 * @return what ever it is returned by the action method
	 */  
	public ReturnObjectType execute( final boolean populateSessionStat, final boolean isUserActionCall,
						   			 final boolean validateLoggedIn ) throws SiteException {
		Log4jInit.pushDNC( httpRequest, userSessionId, userID );
		
		try{
			//Validate that we are allowed to have access to the session and
			//that the login/session validation for this IP/login name are not blocked
			StatisticsSecurityManager.validateAccess( httpSession, remoteAddr, userID, populateSessionStat, isUserActionCall, validateLoggedIn );
			//Detect if this is a true user id, i.e. an non-unknown and non-default user id
			final boolean isTrueUser = (userID != ShortUserData.DEFAULT_UID ) && ( userID != ShortUserData.UNKNOWN_UID );
			try {
				//Do actions requiring synchronization of the user in case it is a logged in user
				Object synchObj = isTrueUser ? userSynchFactory.getSynchObject( userID ) : new Object();
				synchronized( synchObj ) {
					if( validateLoggedIn ) {
						//Validate that login and sessionId match
						UserSessionManager.validateLoginVsSessionId( httpSession, remoteAddr, userID, userSessionId  );
					} else {
						logger.debug( "The validation of the userID VS. userSessionID was skipped, " + 
									  "because the called functionality allows for an anonimous access!" );
					}
					
					return action();
				}
			} finally {
				if( isTrueUser ) {
					//Release the synchronization object for the user
					userSynchFactory.releaseSynchObject( userID );
				}
			}
		} catch( Throwable exception ) {
			if( exception instanceof SiteException ) {
				//If it is our exception then we do not care, just pass it further
				throw (SiteException) exception;
			} else {
				logger.error("Centralized exception catcher, got an third-party exception: ", exception);
				throw new InternalSiteException( InternalSiteException.UNKNOWN_INTERNAL_SITE_EXCEPTION_ERR );
			}
		} finally {
			Log4jInit.cleanDNC();
		}
	}
	
	/**
	 * This method is supposed to provide code that has to be
	 * executed after user access has been authorized.
	 * @return what ever it is returned by the specified code
	 */
	protected abstract ReturnObjectType action() throws SiteException;
	
}
