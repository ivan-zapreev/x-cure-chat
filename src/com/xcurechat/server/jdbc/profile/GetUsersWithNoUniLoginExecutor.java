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
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.server.jdbc.profile;

import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor allows retrieve the set of user's that have no unified login name set.
 * The list of users is ordered by the last visit in the descending order. The method
 * assigns the list of ShortUserData objects but in each of them only the user ID and
 * login name are set to the right values! 
 */
public class GetUsersWithNoUniLoginExecutor extends QueryExecutor<List<ShortUserData>> {
	
	public GetUsersWithNoUniLoginExecutor( ){
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT " + UID_FIELD_NAME_USERS_TABLE + ", " +
											   LOGIN_FIELD_NAME_USERS_TABLE + ", " +
											   LAST_ONLINE_FIELD_NAME_USERS_TABLE + " FROM " +
								   USERS_TABLE + " WHERE " +
								   UNI_LOGIN_FIELD_NAME_USERS_TABLE + " IS NULL " +
								   "ORDER BY " + LAST_ONLINE_FIELD_NAME_USERS_TABLE + " DESC";
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
	}

	public ResultSet executeQuery(PreparedStatement pstmt, List<ShortUserData> result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	public void processResultSet(ResultSet resultSet, List<ShortUserData> users) throws SQLException, SiteException {
		while( resultSet.next() ) {
			ShortUserData userData = new ShortUserData();
			userData.setUID( resultSet.getInt( UID_FIELD_NAME_USERS_TABLE ) );
			userData.setUserLoginName( resultSet.getString( LOGIN_FIELD_NAME_USERS_TABLE ) );
			users.add( userData );
		}
	}
}
