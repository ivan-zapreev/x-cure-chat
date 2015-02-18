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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor allows to check that one user is a friend of another
 */
public class IsFriendCheckExecutor extends QueryExecutor<IsFriendCheckExecutor.ResultHolder> {

	public class ResultHolder {
		public Boolean isFriend = false;
	}
	
	private final int userID;
	private final int friendUserID;
	
	public IsFriendCheckExecutor( final int userID, final int friendUserID ){
		this.userID = userID;
		this.friendUserID = friendUserID;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT * FROM " +
									FRIENDS_TABLE + " WHERE " +
									FROM_UID_FIELD_NAME_FRIENDS_TABLE + "=? AND " + 
									TO_UID_FIELD_NAME_FRIENDS_TABLE + "=?";
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		pstmt.setInt( counter++, userID );
		pstmt.setInt( counter++, friendUserID );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, ResultHolder result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	public void processResultSet(ResultSet resultSet, ResultHolder result) throws SQLException, SiteException {
		if( resultSet.first() ) {
			//The userID is a friend of friendUserID
			result.isFriend = true;
		} else {
			//The userID is NOT a friend of friendUserID
			result.isFriend = false;
		}
	}

}
