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

package com.xcurechat.server.jdbc.messages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This class is responsible for changing the user who sent/received the private messages.
 * This should happen when a user deleted his profile, then the message's sender/receiver
 * is mapped to a 'deleted' user profile.
 */
public class RerouteMessagesToDoletedUserExecutor extends QueryExecutor<Void> {

	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( RerouteMessagesToDoletedUserExecutor.class );
	
	private final int userID;
	private final boolean fromUser;

	/**
	 * Simple constructor
	 * @param userID the user who sent or received the messages
	 * @param fromUser if true then we reroute messages for the sender, otherwise for a receiver
	 * this user, otherwise the messages to the user
	 */
	public RerouteMessagesToDoletedUserExecutor( final int userID, final boolean fromUser ){
		this.userID = userID;
		this.fromUser = fromUser;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		String hideQuery = "UPDATE " + MESSAGES_TABLE + " SET ";
		if( fromUser ) {
			hideQuery += FROM_UID_FIELD_NAME_MESSAGES_TABLE + " = ";
		} else {
			hideQuery += TO_UID_FIELD_NAME_MESSAGES_TABLE + " = ";  
		}
		hideQuery += "( SELECT " + UID_FIELD_NAME_USERS_TABLE + " FROM " +
					USERS_TABLE + " WHERE " + TYPE_FIELD_NAME_USERS_TABLE + "=" +
					MainUserData.DELETED_USER_TYPE + " ) WHERE ";
		if( fromUser ) {
			hideQuery += FROM_UID_FIELD_NAME_MESSAGES_TABLE + " = ?";
		} else {
			hideQuery += TO_UID_FIELD_NAME_MESSAGES_TABLE + " = ?";  
		}
		logger.debug("The messages sender/receiver rerouting query is: "+hideQuery);
		return connection.prepareStatement( hideQuery );
	}
	
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		pstmt.setInt( counter++, userID );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		pstmt.executeUpdate();
		return null;
	}

	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//DO NOTHING
	}

}
