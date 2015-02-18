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

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This class is responsible for updating user statistics in the database
 * At the moment the statistics is simple: the last online date/time.
 * Every time a user loggs in or is logged out, this date/time is updated
 */
public class InsertUserStatisticsExecutor extends QueryExecutor<Void> {
	
	private final int userID;
	private final boolean is_login;
	private final boolean is_auto;
	private final String host;
	private final String location;

	public InsertUserStatisticsExecutor( final int userID, final boolean is_login,
										 final boolean is_auto, final String host,
										 final String location ){
		this.userID = userID;
		this.is_login = is_login;
		this.is_auto = is_auto;
		this.host = host;
		this.location = location;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String insertQuery = "INSERT INTO " + LOGIN_STATS_TABLE + " SET " +
									USER_ID_PROFILE_LOGIN_STATS_TABLE + "=?, " +
									IS_LOGIN_FIELD_NAME_LOGIN_STATS_TABLE + "=?, "+
									IS_AUTO_FIELD_NAME_LOGIN_STATS_TABLE + "=?, "+
									DATE_FIELD_NAME_LOGIN_STATS_TABLE + "=NOW(), "+
									HOST_FIELD_NAME_LOGIN_STATS_TABLE + "=?, "+
									LOCATION_FIELD_NAME_LOGIN_STATS_TABLE + "=?";
		return connection.prepareStatement( insertQuery );
	}
	
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		pstmt.setInt( 1, userID );
		pstmt.setBoolean(2, is_login);
		pstmt.setBoolean(3, is_auto);
		pstmt.setString(4, host);
		pstmt.setString(5, location);
	}

	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		pstmt.executeUpdate();
		return null;
	}

	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//DO NOTHING
	}

}
