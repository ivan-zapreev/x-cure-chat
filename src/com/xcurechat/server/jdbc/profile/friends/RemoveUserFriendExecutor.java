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

package com.xcurechat.server.jdbc.profile.friends;

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
public class RemoveUserFriendExecutor extends QueryExecutor<Void> {
	
	private final int userID;
	private final int friendUserID;

	public RemoveUserFriendExecutor( final int userID, final int friendUserID ){
		this.userID = userID;
		this.friendUserID = friendUserID;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String updateQuery = "DELETE FROM " + FRIENDS_TABLE + " WHERE " +
									FROM_UID_FIELD_NAME_FRIENDS_TABLE + " = ? AND " + 
									TO_UID_FIELD_NAME_FRIENDS_TABLE + " = ?";
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		pstmt.setInt( counter++, userID );
		pstmt.setInt( counter++, friendUserID );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		if( pstmt.executeUpdate() == 0) {
			//Well, we could of cource throw an exception saying that the 
			//user was not a friend but in general this is unnecessary,
			//since we will still tell the user that the friend was removed.
		}
		return null;
	}

	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//DO NOTHING
	}

}
