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
package com.xcurechat.server.jdbc.profile.friends;

import java.util.Set;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor allows retrieve the set of user's friends
 */
public class SelectFriendsExecutor extends QueryExecutor<Set<Integer>> {
	
	private final int userID;
	
	public SelectFriendsExecutor( final int userID ){
		this.userID = userID;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT " + TO_UID_FIELD_NAME_FRIENDS_TABLE + " FROM " +
									FRIENDS_TABLE + " WHERE " +
									FROM_UID_FIELD_NAME_FRIENDS_TABLE + "=?";
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		pstmt.setInt( counter++, userID );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, Set<Integer> result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	public void processResultSet(ResultSet resultSet, Set<Integer> friedIds) throws SQLException, SiteException {
		while( resultSet.next() ) {
			friedIds.add( resultSet.getInt( TO_UID_FIELD_NAME_FRIENDS_TABLE ) );
		}
	}
}
