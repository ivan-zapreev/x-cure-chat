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

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import com.xcurechat.client.data.UserStatsEntryData;
import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.rpc.UserStatistics;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.UserLoginException;

import com.xcurechat.server.core.SecureServerAccess;

import com.xcurechat.server.jdbc.ConnectionWrapper;
import com.xcurechat.server.jdbc.profile.VerifyPasswordExecutor;
import com.xcurechat.server.jdbc.statistics.DeleteUserStatisticsExecutor;
import com.xcurechat.server.jdbc.statistics.CountUserStatisticsEntriesExecutor;
import com.xcurechat.server.jdbc.statistics.SelectUserStatisticsExecutor;

import com.xcurechat.server.security.statistics.StatisticsSecurityManager;

/**
 * @author zapreevis
 * The RPC interface that allows users to browse
 * and clean their login, logout statistics.
 */
public class UserStatisticsImpl extends RemoteServiceServlet implements UserStatistics {
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;

	/**
	 * Retrieves the HttpSession object if any.
	 * @return the HttpSession object, either an old or a newly created one
	 */
	private HttpSession getLocalHttpSession() {
		return this.getThreadLocalRequest().getSession( true );
	}
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( UserStatisticsImpl.class );
	
	/**
	 * This method allows count the number of statistical entries for the given user
	 * @param userID the unique user ID
	 * @param userSessionId the user's session id
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public int count(final int userID, final String userSessionId ) throws SiteException {
		return (new SecureServerAccess<Integer>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Integer action() throws SiteException {
				logger.info( "Initiating retrieval of login/logout statistics for user '" + userID +"'");
				
				OnePageViewData<UserStatsEntryData> data = new OnePageViewData<UserStatsEntryData>();

				//Retrieve the size of user statistics
				CountUserStatisticsEntriesExecutor countUserStatsExecutor = new CountUserStatisticsEntriesExecutor( userID ); 
				ConnectionWrapper<OnePageViewData<UserStatsEntryData>> countUserStatusConnWrap = ConnectionWrapper.createConnectionWrapper( countUserStatsExecutor );
				countUserStatusConnWrap.executeQuery( data, ConnectionWrapper.XCURE_CHAT_DB );
				
				return data.total_size;
			}
		}).execute();
	}
	
	/**
	 * This method allows to browse user site login/logout statistics
	 * @param userID the unique user ID
	 * @param userSessionId the user's session id
	 * @param offset the offset for the first statistical entry
	 * @param size the max number of entries to retrieve 
	 * @return an objects containing requested statistical data
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	@SuppressWarnings("unchecked")
	public OnePageViewData<UserStatsEntryData> browse(final int userID, final String userSessionId,
								final int offset, final int size ) throws SiteException {
		return (OnePageViewData<UserStatsEntryData>) (new SecureServerAccess( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Object action() throws SiteException {
				logger.info( "Initiating retrieval of login/logout statistics for user '" + userID +"'");
				OnePageViewData<UserStatsEntryData> data = new OnePageViewData<UserStatsEntryData>();
				
				SelectUserStatisticsExecutor selectUserStatsExecutor = new SelectUserStatisticsExecutor(userID, offset, size);
				ConnectionWrapper selectUserStatusConnWrap = ConnectionWrapper.createConnectionWrapper( selectUserStatsExecutor );
				selectUserStatusConnWrap.executeQuery( data, ConnectionWrapper.XCURE_CHAT_DB );
				
				return data;
			}
		}).execute();
	}
	
	/**
	 * This method allows to delete all user site login/logout statistics
	 * @param userID the unique user ID
	 * @param userSessionId the user's session id
	 * @param password is needed for deleting statistics
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void delete(final int userID, final String userSessionId, final String password ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Initiating deletion of login/logout statistics for user '" + userID +"'");

				try {
					//Check that the password is correct!
					ConnectionWrapper<Void> verifyPasswordConnWrap = ConnectionWrapper.createConnectionWrapper( new VerifyPasswordExecutor( userID, password ) );
					//Verify the old login-password pare
					verifyPasswordConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
				} catch ( UserLoginException exception ){
					StatisticsSecurityManager.reportFailedLogin( remoteAddr, userID );
					throw exception;
				}
				
				//Delete user login/Logout statistics
				DeleteUserStatisticsExecutor deleteUserStatsExecutor = new DeleteUserStatisticsExecutor( userID ); 
				ConnectionWrapper<Void> deleteUserStatusConnWrap = ConnectionWrapper.createConnectionWrapper( deleteUserStatsExecutor );
				deleteUserStatusConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
				
				//Nothing the be returned here
				return null;
			}
		}).execute();
	}
}
