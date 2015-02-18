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

import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This class is responsible for hiding the user messages from beign viewable.
 * This emulates the deletion of the messages for the user.
 */
public class HideMessagesExecutor extends QueryExecutor<Void> {

	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( HideMessagesExecutor.class );
	
	private final int userID;
	private final List<Integer> messageIDS;
	private final boolean fromUser;

	/**
	 * Deletes/Hides the messages belonging to the user
	 * @param userID the user who sent or received the messages
	 * @param messageIDS the message IDs
	 * @param fromUser if true then we hide the messages from
	 * this user, otherwise the messages to the user
	 */
	public HideMessagesExecutor( final int userID, final List<Integer> messageIDS, final boolean fromUser ){
		this.userID = userID;
		this.messageIDS = messageIDS;
		this.fromUser = fromUser;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		String hideQuery = "UPDATE " + MESSAGES_TABLE + " SET ";
		if( fromUser ) {
			hideQuery += FROM_HIDDEN_FIELD_NAME_MESSAGES_TABLE + "=true WHERE " +
							FROM_UID_FIELD_NAME_MESSAGES_TABLE + " = ? ";
		} else {
			hideQuery += TO_HIDDEN_FIELD_NAME_MESSAGES_TABLE + "=true WHERE " +
							TO_UID_FIELD_NAME_MESSAGES_TABLE + " = ? ";  
		}
		hideQuery += "AND " + MSG_ID_FIELD_NAME_MESSAGES_TABLE + createINQuerySet( messageIDS );
		logger.debug("The hide messages query is: "+hideQuery);
		return connection.prepareStatement( hideQuery );
	}
	
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		pstmt.setInt( counter++, userID );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		final int count = pstmt.executeUpdate();
		if( count == 0) {
			//NOTE: We do not throw any exception, because it does not really make much sence to do it.
			logger.error("Unable to hide the given messages, for user "+userID+"!");
		} else {
			logger.debug("made "+count+" message(s) hidden for user "+userID);
		}
		return null;
	}

	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//DO NOTHING
	}

}
