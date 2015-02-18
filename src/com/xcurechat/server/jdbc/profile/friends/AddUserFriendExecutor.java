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

import org.apache.log4j.Logger;

import com.xcurechat.client.rpc.exceptions.UserStateException;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor class is responsible for adding a friend to a user.
 */
public class AddUserFriendExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( AddUserFriendExecutor.class );
	
	private final int userID;
	private final int friendUserID;
	
	public AddUserFriendExecutor( final int userID, final int friendUserID ){
		this.userID = userID;
		this.friendUserID = friendUserID;
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		final String insertQuery = "INSERT INTO " + FRIENDS_TABLE +
									" SET " + FROM_UID_FIELD_NAME_FRIENDS_TABLE + " = ?, " + 
									TO_UID_FIELD_NAME_FRIENDS_TABLE + " = ?";
		return connection.prepareStatement( insertQuery );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setInt( counter++, userID );
		pstmt.setInt( counter++, friendUserID );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		try{
			//Execute the insert
			if( pstmt.executeUpdate() == 0 ) {
				//If this hapens then probably the user does not exist
				throw new UserStateException( UserStateException.USER_DOES_NOT_EXIST );
			}
		}catch( SQLException e ){
			logger.error("An exception while adding friendship from user "+userID+" to user " + friendUserID , e);
			//This could indeed be something else, but I am not sure about other options
			throw new UserStateException( UserStateException.USER_DOES_NOT_EXIST );
		}
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
