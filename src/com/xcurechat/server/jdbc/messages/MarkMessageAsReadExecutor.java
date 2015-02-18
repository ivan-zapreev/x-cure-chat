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

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This class is responsible for marking a message as read.
 */
public class MarkMessageAsReadExecutor extends QueryExecutor<Void> {

	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( MarkMessageAsReadExecutor.class );
	
	private final int userID;
	private final int messageID;

	/**
	 * Marks a message sent to a user as read
	 * @param userID the user who received the messages
	 * @param messageID the message which should be marked as read
	 */
	public MarkMessageAsReadExecutor( final int userID, final int messageID ){
		this.userID = userID;
		this.messageID = messageID;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		String markAsReadQuery = "UPDATE " + MESSAGES_TABLE + " SET " +
							IS_READ_FIELD_NAME_MESSAGES_TABLE + "=true WHERE " +
							TO_UID_FIELD_NAME_MESSAGES_TABLE + " = ? AND " +
							MSG_ID_FIELD_NAME_MESSAGES_TABLE + " = ?" ;  
		logger.debug("The mark as read message query is: " + markAsReadQuery);
		return connection.prepareStatement( markAsReadQuery );
	}
	
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		pstmt.setInt( counter++, userID );
		pstmt.setInt( counter++, messageID );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		final int count = pstmt.executeUpdate();
		if( count == 0) {
			//NOTE: We do not throw any exception, because it does not really make much sence to do it.
			logger.error("Unable to mark 'as read' the message " + messageID + " sent to user "+userID+"!");
		}
		return null;
	}

	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//DO NOTHING
	}

}
