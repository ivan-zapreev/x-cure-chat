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
import com.xcurechat.client.data.MessageFileData;
import com.xcurechat.client.data.UserFileData;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor allows to retrieve the chat message data, joined with the message image.
 * Then it is tested if the message is public, or it is private and is addressed to the user.
 * If none of these hold then no image is returned.
 */
public class SecureSelectChatFileExecutor extends QueryExecutor<MessageFileData> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( SecureSelectChatFileExecutor.class );
	
	private static final String MESSAGES_TMP_TABLE_NAME = "msgs_tmp_tbl";
	private static final String MESSAGE_IMAGES_TMP_TABLE_NAME = "files_tmp_tbl";
		
	final int roomID;
	final int userID;
	final int imageID;
	final boolean isThumbnail;
	
	public SecureSelectChatFileExecutor( final int userID, final int roomID,
								   		 final int imageID, final boolean isThumbnail) {
		this.roomID = roomID;
		this.userID = userID;
		this.imageID = imageID;
		this.isThumbnail = isThumbnail;
		logger.debug("Trying to securely retrieve image " + imageID + " from roomID " + roomID + " by user " + userID );
	}
	
	@Override
	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		//Select the chat message ID, chat message type, chat message
		//recipients, for the chat message with the given imageID, left 
		//joined with the corresponding image or its thumbnail.
		final String selectQuery = "SELECT " +
										MESSAGES_TMP_TABLE_NAME + "." + MESSAGE_TYPE_FIELD_NAME_CHAT_MESSAGES_TABLE +
										", " + MESSAGES_TMP_TABLE_NAME + "." + SENDER_ID_FIELD_NAME_CHAT_MESSAGES_TABLE +
										", " + MESSAGE_IMAGES_TMP_TABLE_NAME + "." + ( isThumbnail ? THUMBNAIL_FIELD_NAME_CHAT_FILES_TABLE : DATA_FIELD_NAME_CHAT_FILES_TABLE ) +
										", " + MESSAGE_IMAGES_TMP_TABLE_NAME + "." + MIME_TYPE_CHAT_FILES_TABLE +
										", GROUP_CONCAT( " + CHAT_MSG_RECEPIENT_TABLE + "." + USR_ID_FIELD_NAME_CHAT_MSG_RECEPIENT_TABLE +
											" SEPARATOR '" + MESSAGE_RECEPIENT_IDS_DELIMITER + "' ) " + " AS " + MESSAGE_RECEPIENT_IDS +
									" FROM " + CHAT_MSG_RECEPIENT_TABLE +
									" RIGHT JOIN " + CHAT_MESSAGES_TABLE + " AS " + MESSAGES_TMP_TABLE_NAME + " ON " +
										 MESSAGES_TMP_TABLE_NAME + "." + MESSAGE_ID_FIELD_NAME_CHAT_MESSAGES_TABLE
										 + "=" +
										 CHAT_MSG_RECEPIENT_TABLE + "." + MESSAGE_ID_FIELD_NAME_CHAT_MSG_RECEPIENT_TABLE +
									" INNER JOIN " + CHAT_FILES_TABLE + " AS " + MESSAGE_IMAGES_TMP_TABLE_NAME + " ON " +
										MESSAGES_TMP_TABLE_NAME + "." + FILE_ID_FIELD_NAME_CHAT_MESSAGES_TABLE
										+ "=" +
										MESSAGE_IMAGES_TMP_TABLE_NAME + "." + FILE_ID_FIELD_NAME_CHAT_FILES_TABLE + 
									" WHERE " + 
										"( " + MESSAGES_TMP_TABLE_NAME + "." + ROOM_ID_FIELD_NAME_CHAT_MESSAGES_TABLE + " = ? )" +
										" AND " +
										"( " + MESSAGES_TMP_TABLE_NAME + "." + FILE_ID_FIELD_NAME_CHAT_MESSAGES_TABLE + " =? ) " + 
									" GROUP BY " + MESSAGES_TMP_TABLE_NAME + "." + MESSAGE_ID_FIELD_NAME_CHAT_MESSAGES_TABLE;

		logger.debug("The new chat message image retrieval query for image " + imageID + " user " + userID + " and room " + roomID + " is " + selectQuery);
		return connection.prepareStatement( selectQuery );
	}

	@Override
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		pstmt.setInt( counter++, roomID );
		pstmt.setInt( counter++, imageID );
	}

	@Override
	public ResultSet executeQuery(PreparedStatement pstmt, MessageFileData result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}
	
	private void extractFileData( ResultSet resultSet, UserFileData fileData ) throws SQLException {
		if( isThumbnail ) {
			fileData.thumbnailData = resultSet.getBytes( MESSAGE_IMAGES_TMP_TABLE_NAME + "." + THUMBNAIL_FIELD_NAME_CHAT_FILES_TABLE );
		} else {
			fileData.fileData = resultSet.getBytes( MESSAGE_IMAGES_TMP_TABLE_NAME + "." + DATA_FIELD_NAME_CHAT_FILES_TABLE );
		}
	}

	@Override
	public void processResultSet(ResultSet resultSet, MessageFileData fileData) throws SQLException, SiteException {
		//Just in case we set it to null
		fileData.thumbnailData = null;
		fileData.fileData = null;
		
		//if there is a result found then we return no image
		if( resultSet.next() ) {
			ChatMessage.Types messageType = ChatMessage.Types.values()[resultSet.getInt( MESSAGES_TMP_TABLE_NAME + "." + MESSAGE_TYPE_FIELD_NAME_CHAT_MESSAGES_TABLE )];
			final int senderID = resultSet.getInt( MESSAGES_TMP_TABLE_NAME + "." + SENDER_ID_FIELD_NAME_CHAT_MESSAGES_TABLE );
			fileData.mimeType = resultSet.getString( MESSAGE_IMAGES_TMP_TABLE_NAME + "." + MIME_TYPE_CHAT_FILES_TABLE );
			if( ( messageType == ChatMessage.Types.SIMPLE_MESSAGE_TYPE ) || ( senderID == userID ) ) {
				//For a public message or a message sent by this user we just return the image
				extractFileData( resultSet, fileData );
			} else {
				//The message is not public so it should be private then
				//because we should not have any images in non-user messages
				Set<Integer> recipientIDs = getChatMessageRecipients( resultSet, ChatMessage.UNKNOWN_MESSAGE_ID, logger );
				if( recipientIDs.contains( userID ) ) {
					//If the user is in the list of recipients of the
					//private chat message then give him the image
					extractFileData( resultSet, fileData );
				}
			}
		}
	}
}
