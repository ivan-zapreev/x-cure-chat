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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This class is responsible for deleting user login/logout statistics
 */
public class DeleteUserStatisticsExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( DeleteUserStatisticsExecutor.class );
	
	private final int userID;

	public DeleteUserStatisticsExecutor( final int userID ){
		this.userID = userID;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String updateQuery = "DELETE FROM " + LOGIN_STATS_TABLE + " WHERE " +
									USER_ID_PROFILE_LOGIN_STATS_TABLE + "=?";
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		pstmt.setInt( 1, userID );
	}
	
	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		if( pstmt.executeUpdate() == 0) {
			logger.warn("Removal of login/logout statistics for user '"+userID+"' was unsuccessfull, no table entries were deleted.");
		}
		return null;
	}

	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//DO NOTHING
	}

}
