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
package com.xcurechat.server.jdbc.chat;

import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.ShortFileDescriptor;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor allows to retrieve an update of the chat room messages for the given user
 * The messages are retrieved in the ascending order by the message ID, i.e. the auto
 * increment primary key. This ensures that the messages are ordered from the oldest
 * ones in the beginning to the newest ones in the end. Note that each message
 * recipients are kept ordered. I.e. are retrieved in the same order as they
 * were placed in DB
 */
public class GetRoomMessagesUpdateExecutor extends QueryExecutor<List<ChatMessage>> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( GetRoomMessagesUpdateExecutor.class );
			
	public static final String MESSAGE_RECEPIENT_IDS = "message_recepient_ids";
	public static final String MESSAGE_RECEPIENT_IDS_DELIMITER = ":";
	
	final int roomID;
	final int userID;
	final int lastMessageID;
	//If false then we retrieve all messages from the room, regardless the user they were sent to
	final boolean isNotAll;
	//True if the last message ID is undefined then we retrieve messages for the previous minute
	final boolean undefinedLastMsgID;
	
	private GetRoomMessagesUpdateExecutor( final int roomID, final int userID,
								   final int lastMessageID, final boolean isNotAll) {
		this.roomID = roomID;
		this.userID = userID;
		this.lastMessageID = lastMessageID;
		this.isNotAll = isNotAll;
		logger.debug("The last message ID retrieved from room " + roomID + " is " + lastMessageID );
		undefinedLastMsgID = ( lastMessageID == ChatMessage.UNKNOWN_MESSAGE_ID );
	}
	
	/**
	 * Get the new messages in the given room sent to all or to this user
	 * @param roomID the ID of the room we retrieve messages from
	 * @param userID the ID of the user we retrieve messages for
	 * @param lastMessageID the last retrieved message for this user from this room.
	 */
	public GetRoomMessagesUpdateExecutor(final int roomID, final int userID, final int lastMessageID){
		this( roomID, userID, lastMessageID, true);
	}
	
	/**
	 * Get all new messages in the given chat room
	 * @param roomID the ID of the room we retrieve messages from
	 * @param lastMessageID the last retrieved message for this user from this room.
	 */
	public GetRoomMessagesUpdateExecutor(final int roomID, final int lastMessageID){
		this( roomID, MainUserData.UNKNOWN_UID, lastMessageID, false);
	}
	
	@Override
	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		//If the value of lastMessageID is not set, i.e. it is 0 then we retrieve
		//all messages that are not older than one minute
		
		//Select all messages after the given one in the room such that either
		//the message is not private or the user sent the message or it is
		//private message and it was addressed to the given user
		String selectQuery = "SELECT " + CHAT_MESSAGES_TABLE + ".*, " +
							 "GROUP_CONCAT( " +
								CHAT_MSG_RECEPIENT_TABLE + "." + USR_ID_FIELD_NAME_CHAT_MSG_RECEPIENT_TABLE +
								" ORDER BY " + CHAT_MSG_RECEPIENT_TABLE + "." + ENTRY_ID_CHAT_MSG_RECEPIENT_TABLE +
								" SEPARATOR '" + MESSAGE_RECEPIENT_IDS_DELIMITER + "'" +
							 " ) " + " AS " + MESSAGE_RECEPIENT_IDS + ", " +
							 CHAT_FILES_TABLE + "." + FILE_ID_FIELD_NAME_CHAT_FILES_TABLE + ", " +
							 CHAT_FILES_TABLE + "." + MIME_TYPE_CHAT_FILES_TABLE + ", " +
							 CHAT_FILES_TABLE + "." + FILE_NAME_CHAT_FILES_TABLE + ", " +
							 CHAT_FILES_TABLE + "." + IMG_WIDTH_CHAT_FILES_TABLE + ", " +
							 CHAT_FILES_TABLE + "." + IMG_HEIGHT_CHAT_FILES_TABLE +
							 " FROM " + CHAT_MSG_RECEPIENT_TABLE +
							 " RIGHT JOIN " + CHAT_MESSAGES_TABLE + " ON " +
							 		CHAT_MESSAGES_TABLE + "." + MESSAGE_ID_FIELD_NAME_CHAT_MESSAGES_TABLE + "=" +
									CHAT_MSG_RECEPIENT_TABLE + "." + MESSAGE_ID_FIELD_NAME_CHAT_MSG_RECEPIENT_TABLE +
							//Get the message file descriptor if any
							" LEFT JOIN " + CHAT_FILES_TABLE + " ON " +
									CHAT_MESSAGES_TABLE + "." + FILE_ID_FIELD_NAME_CHAT_MESSAGES_TABLE + " = " + 
									CHAT_FILES_TABLE + "." + FILE_ID_FIELD_NAME_CHAT_FILES_TABLE + 
							" WHERE ( " + ROOM_ID_FIELD_NAME_CHAT_MESSAGES_TABLE + " = ? ) AND ";
		
		//If the last message ID is undefined then we retrieve the messages for the last minute
		if( undefinedLastMsgID ) {
			selectQuery += "( " + CHAT_MESSAGES_TABLE + "." + SENT_DATE_FIELD_NAME_CHAT_MESSAGES_TABLE + " > ? ) ";
		} else {
			selectQuery += "( " + CHAT_MESSAGES_TABLE + "." + MESSAGE_ID_FIELD_NAME_CHAT_MESSAGES_TABLE + " > ? ) ";
		}
		
		if( isNotAll ) {
			selectQuery += " AND  ( ( " + CHAT_MESSAGES_TABLE + "." + MESSAGE_TYPE_FIELD_NAME_CHAT_MESSAGES_TABLE + " != " +
										ChatMessage.Types.PRIVATE_MESSAGE_TYPE.ordinal() + " ) OR ( " +
										CHAT_MESSAGES_TABLE + "." + SENDER_ID_FIELD_NAME_CHAT_MESSAGES_TABLE + " = ? ) OR ( ( " +
										CHAT_MESSAGES_TABLE + "." + MESSAGE_TYPE_FIELD_NAME_CHAT_MESSAGES_TABLE + " = " +
										ChatMessage.Types.PRIVATE_MESSAGE_TYPE.ordinal() +
										" ) AND ( ? IN ( SELECT " + USR_ID_FIELD_NAME_CHAT_MSG_RECEPIENT_TABLE + "" +
													" FROM " + CHAT_MSG_RECEPIENT_TABLE + " WHERE " +
													MESSAGE_ID_FIELD_NAME_CHAT_MSG_RECEPIENT_TABLE + " = " +
													CHAT_MESSAGES_TABLE + "." + MESSAGE_ID_FIELD_NAME_CHAT_MESSAGES_TABLE + " ) ) ) )";
		}
		selectQuery += " GROUP BY " + CHAT_MESSAGES_TABLE + "." + MESSAGE_ID_FIELD_NAME_CHAT_MESSAGES_TABLE;

		logger.debug("The new chat message retrieval query for user " + userID + " and room " + roomID + " is " + selectQuery);
		return connection.prepareStatement( selectQuery );
	}

	@Override
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		pstmt.setInt( counter++, roomID );
		if( undefinedLastMsgID ) {
			//Retrieve the messages that are not more than one minute old
			pstmt.setTimestamp( counter++, new Timestamp( System.currentTimeMillis() - 60 * 1000 ) );
		} else {
			pstmt.setInt( counter++, lastMessageID );
		}
		if( isNotAll ) {
			pstmt.setInt( counter++, userID );
			pstmt.setInt( counter++, userID );
		}
	}

	@Override
	public ResultSet executeQuery(PreparedStatement pstmt, List<ChatMessage> result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	@Override
	public void processResultSet(ResultSet resultSet, List<ChatMessage> chatMessages) throws SQLException, SiteException {
		while( resultSet.next() ) {
			ChatMessage message = new ChatMessage();
			message.messageID = resultSet.getInt( MESSAGE_ID_FIELD_NAME_CHAT_MESSAGES_TABLE );
			message.sentDate = QueryExecutor.getTime(resultSet, SENT_DATE_FIELD_NAME_CHAT_MESSAGES_TABLE );
			message.senderID = resultSet.getInt( SENDER_ID_FIELD_NAME_CHAT_MESSAGES_TABLE );
			message.roomID = resultSet.getInt( ROOM_ID_FIELD_NAME_CHAT_MESSAGES_TABLE );
			//If the files is attached
			if( resultSet.getObject( CHAT_FILES_TABLE + "." + FILE_ID_FIELD_NAME_CHAT_FILES_TABLE ) != null  ) {
				message.fileDesc = new ShortFileDescriptor();
				message.fileDesc.fileID = resultSet.getInt( CHAT_FILES_TABLE + "." + FILE_ID_FIELD_NAME_CHAT_FILES_TABLE );
				message.fileDesc.mimeType = resultSet.getString( CHAT_FILES_TABLE + "." + MIME_TYPE_CHAT_FILES_TABLE );
				message.fileDesc.fileName = resultSet.getString( CHAT_FILES_TABLE + "." + FILE_NAME_CHAT_FILES_TABLE ); 
				message.fileDesc.widthPixels = resultSet.getInt( CHAT_FILES_TABLE + "." + IMG_WIDTH_CHAT_FILES_TABLE ); 
				message.fileDesc.heightPixels = resultSet.getInt( CHAT_FILES_TABLE + "." + IMG_HEIGHT_CHAT_FILES_TABLE );
			}
			message.messageBody = resultSet.getString( MESSAGE_BODY_FIELD_NAME_CHAT_MESSAGES_TABLE );
			message.messageType = ChatMessage.Types.values()[resultSet.getInt( MESSAGE_TYPE_FIELD_NAME_CHAT_MESSAGES_TABLE )];
			message.infoUserID = resultSet.getInt( INFO_USER_ID_FIELD_NAME_CHAT_MESSAGES_TABLE );
			message.infoUserLogin = resultSet.getString( INFO_USER_LOGIN_FIELD_NAME_CHAT_MESSAGES_TABLE );
			message.fontType = resultSet.getInt( FONT_TYPE_FIELD_NAME_CHAT_MESSAGES_TABLE );
			message.fontSize = resultSet.getInt( FONT_SIZE_FIELD_NAME_CHAT_MESSAGES_TABLE );
			message.fontColor = resultSet.getInt( FONT_COLOR_FIELD_NAME_CHAT_MESSAGES_TABLE );
			//Get and process message recipient IDs
			message.recipientIDs = getChatMessageRecipients( resultSet, message.messageID, logger );
			chatMessages.add( message );  
		}
	}
}
