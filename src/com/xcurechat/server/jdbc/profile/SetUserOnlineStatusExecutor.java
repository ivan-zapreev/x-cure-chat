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

package com.xcurechat.server.jdbc.profile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor class is responsible for updating user profile
 * by setting the user's online/offline status. It also updates
 * the last online field.
 */
public class SetUserOnlineStatusExecutor extends QueryExecutor<Void> {
	
	private final int userID;
	private final boolean isOnline;
	
	public SetUserOnlineStatusExecutor( final int userID, final boolean isOnline ){
		this.userID = userID;
		this.isOnline = isOnline;
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		String updateQuery = "UPDATE " + USERS_TABLE + " SET ";
		updateQuery	+=  IS_ONLINE_FIELD_NAME_USERS_TABLE + "=?, " + 
						LAST_ONLINE_FIELD_NAME_USERS_TABLE + "=NOW() " +
						"WHERE "+ UID_FIELD_NAME_USERS_TABLE + "=?";
		
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setBoolean( counter++, isOnline );
		pstmt.setInt( counter++, userID );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		pstmt.executeUpdate();
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
