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
 * The server-side RPC package, managing DB queries.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.server.jdbc.statistics;

import java.util.List;
import java.util.ArrayList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.UserStatsEntryData;
import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This function counts the total number of user logins/logouts
 */
public class SelectUserStatisticsExecutor extends QueryExecutor<OnePageViewData<UserStatsEntryData>> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( SelectUserStatisticsExecutor.class );
	
	private final int userID;
	private final int offset;
	private final int size;
	
	/**
	 * A simple constructor
	 * @param userID the unique user ID
	 * @param offset the offset, from which to start retrieving stat entries
	 * @param size the number of stat entries to retrieve
	 */
	public SelectUserStatisticsExecutor( final int userID, final int offset, final int size ){
		this.userID = userID;
		this.offset = offset;
		
		if( size > OnePageViewData.MAX_NUMBER_OF_ENTRIES_PER_PAGE ){
			this.size = OnePageViewData.MAX_NUMBER_OF_ENTRIES_PER_PAGE;
			logger.warn("Trying to request access statistics of size '" + size + "' for user '"+userID+"', "+
						"resetting to default value, i.e. '" + OnePageViewData.MAX_NUMBER_OF_ENTRIES_PER_PAGE + "'");
		} else {
			this.size = size;
		}
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT * FROM " + LOGIN_STATS_TABLE + " WHERE " +
									USER_ID_PROFILE_LOGIN_STATS_TABLE + "=? "+
									"ORDER BY " + DATE_FIELD_NAME_LOGIN_STATS_TABLE +
									" DESC LIMIT ? OFFSET ?";
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		pstmt.setInt( 1, userID );
		pstmt.setInt( 2, size );
		pstmt.setInt( 3, offset );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, OnePageViewData<UserStatsEntryData> result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	public void processResultSet(ResultSet resultSet, OnePageViewData<UserStatsEntryData> dataObj) throws SQLException, SiteException {
		dataObj.offset = offset;
		
		List<UserStatsEntryData> list = new ArrayList<UserStatsEntryData>();
		while( resultSet.next() ){
			UserStatsEntryData entry = new UserStatsEntryData();
			entry.isLogin = resultSet.getBoolean( IS_LOGIN_FIELD_NAME_LOGIN_STATS_TABLE );
			entry.isAuto = resultSet.getBoolean( IS_AUTO_FIELD_NAME_LOGIN_STATS_TABLE );
			entry.date = QueryExecutor.getTime( resultSet, DATE_FIELD_NAME_LOGIN_STATS_TABLE );
			entry.host = resultSet.getString( HOST_FIELD_NAME_LOGIN_STATS_TABLE );
			entry.location = resultSet.getString( LOCATION_FIELD_NAME_LOGIN_STATS_TABLE );
			list.add( entry );
		}
		
		if( ! list.isEmpty() ){
			dataObj.entries = list;
			logger.debug( "Retrieved the statistical data for user '" + userID +
							"', size = " + (dataObj.entries != null ? ""+dataObj.entries.size() : "null") );
		} else {
			logger.warn("There is no login/logout statistics for user '"+userID+"'");
			dataObj.entries = null;
		}
	}
}
