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
package com.xcurechat.server.jdbc.chat.images;

import java.util.Set;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ChatMessage;

import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor allows to check that we can retrieve the given chat-message file, i.e.
 * the file is attached to a message that is is public, or it is private and is addressed
 * to the user. If none of these hold then we throw an exception.
 */
public class CanViewChatFileExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( CanViewChatFileExecutor.class );
	
	private static final String MESSAGES_TMP_TABLE_NAME = "msgs_tmp_tbl";
	private static final String MESSAGE_FILES_TMP_TABLE_NAME = "files_tmp_tbl";
		
	final int roomID;
	final int userID;
	final int fileID;
	
	public CanViewChatFileExecutor( final int userID, final int roomID, final int fileID) {
		this.roomID = roomID;
		this.userID = userID;
		this.fileID = fileID;
		logger.debug("Trying to check if the user " + userID + " can retrieve the file " + fileID + " from roomID " + roomID );
	}
	
	@Override
	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		//Select the chat message ID, chat message type, chat message
		//recipients, for the chat message with the given imageID, left 
		//joined with the corresponding image or its thumbnail.
		final String selectQuery = "SELECT " +
										MESSAGES_TMP_TABLE_NAME + "." + MESSAGE_TYPE_FIELD_NAME_CHAT_MESSAGES_TABLE +
										", " + MESSAGES_TMP_TABLE_NAME + "." + SENDER_ID_FIELD_NAME_CHAT_MESSAGES_TABLE +
										", " + MESSAGE_FILES_TMP_TABLE_NAME + "." + FILE_ID_FIELD_NAME_CHAT_FILES_TABLE +
										", GROUP_CONCAT( " + CHAT_MSG_RECEPIENT_TABLE + "." + USR_ID_FIELD_NAME_CHAT_MSG_RECEPIENT_TABLE +
											" SEPARATOR '" + MESSAGE_RECEPIENT_IDS_DELIMITER + "' ) " + " AS " + MESSAGE_RECEPIENT_IDS +
									" FROM " + CHAT_MSG_RECEPIENT_TABLE +
									" RIGHT JOIN " + CHAT_MESSAGES_TABLE + " AS " + MESSAGES_TMP_TABLE_NAME + " ON " +
										 MESSAGES_TMP_TABLE_NAME + "." + MESSAGE_ID_FIELD_NAME_CHAT_MESSAGES_TABLE
										 + "=" +
										 CHAT_MSG_RECEPIENT_TABLE + "." + MESSAGE_ID_FIELD_NAME_CHAT_MSG_RECEPIENT_TABLE +
									" INNER JOIN " + CHAT_FILES_TABLE + " AS " + MESSAGE_FILES_TMP_TABLE_NAME + " ON " +
										MESSAGES_TMP_TABLE_NAME + "." + FILE_ID_FIELD_NAME_CHAT_MESSAGES_TABLE
										+ "=" +
										MESSAGE_FILES_TMP_TABLE_NAME + "." + FILE_ID_FIELD_NAME_CHAT_FILES_TABLE + 
									" WHERE " + 
										"( " + MESSAGES_TMP_TABLE_NAME + "." + ROOM_ID_FIELD_NAME_CHAT_MESSAGES_TABLE + " = ? )" +
										" AND " +
										"( " + MESSAGES_TMP_TABLE_NAME + "." + FILE_ID_FIELD_NAME_CHAT_MESSAGES_TABLE + " =? ) " + 
									" GROUP BY " + MESSAGES_TMP_TABLE_NAME + "." + MESSAGE_ID_FIELD_NAME_CHAT_MESSAGES_TABLE;

		logger.debug("Checking that the file " + fileID + " can be viewed by the user " + userID + " in room " + roomID + " is, the query is: " + selectQuery);
		return connection.prepareStatement( selectQuery );
	}

	@Override
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		pstmt.setInt( counter++, roomID );
		pstmt.setInt( counter++, fileID );
	}

	@Override
	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}
	
	@Override
	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//If there is a result found then we return no image
		if( resultSet.next() ) {
			ChatMessage.Types messageType = ChatMessage.Types.values()[resultSet.getInt( MESSAGES_TMP_TABLE_NAME + "." + MESSAGE_TYPE_FIELD_NAME_CHAT_MESSAGES_TABLE )];
			final int senderID = resultSet.getInt( MESSAGES_TMP_TABLE_NAME + "." + SENDER_ID_FIELD_NAME_CHAT_MESSAGES_TABLE );
			if( ( messageType != ChatMessage.Types.SIMPLE_MESSAGE_TYPE ) && ( senderID != userID ) ) {
				//The message is not public and the user is not its sender, then check the list of recipients
				Set<Integer> recipientIDs = getChatMessageRecipients( resultSet, ChatMessage.UNKNOWN_MESSAGE_ID, logger );
				if( ! recipientIDs.contains( userID ) ) {
					//The user is also not in the list of the message recipients, thus we throw an exception
					throw new InternalSiteException( InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR );
				}
			}
		} else {
			//If there is no result found then the user is definitely not allowed to view the file
			throw new InternalSiteException( InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR );
		}
	}
}
