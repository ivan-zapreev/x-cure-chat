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

import java.util.Date;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.server.jdbc.QuerySetExecutor;

/**
 * @author zapreevis
 * This executor class is responsible for inserting a new chat message into the database.
 * Note that the chat message and its recepiens are inserted with the same send time
 * to make sure that they are all deleted from the DB synchronously.
 */
public class InsertNewChatMessageExecutor extends QuerySetExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( InsertNewChatMessageExecutor.class );
	
	private final ChatMessage message;
	//Stores true if we need to enter message recipients
	//This should only be needed for private messages
	private final boolean needRecepients;
	//The date when the message was sent
	private final Timestamp msgSendTimestamp = new Timestamp( (new Date()).getTime() );
	
	public InsertNewChatMessageExecutor( final ChatMessage message ){
		this.message = message;
		
		needRecepients = ( message.recipientIDs != null ) && ( message.recipientIDs.size() > 0 );
	}
	
	/**
	 * If the message is private and there are recipients set, then
	 * make a locked table insert of the message and the recipients
	 * we can not use transactions here because we use memory tables:
	 * LOCK TABLES private_chat_msg_recepient LOW_PRIORITY WRITE, chat_messages LOW_PRIORITY WRITE;
	 * INSERT INTO chat_messages SET ...;
	 * INSERT INTO private_chat_msg_recepient SET ...;
	 * UNLOCK TABLES;
	 */
	public void executeQuerySet( Connection connection, Void result ) throws SQLException, SiteException {
		//Create the statement for executing the DB queries
		Statement sqlStatement = null;
		
		//Execute queries
		try {
			if( needRecepients ) {
				sqlStatement = connection.createStatement();
				//Lock the tables to make the insertion of the message and recipients atomic
				sqlStatement.execute( "LOCK TABLES " + CHAT_MSG_RECEPIENT_TABLE + " LOW_PRIORITY WRITE, " +
										CHAT_MESSAGES_TABLE + " LOW_PRIORITY WRITE");
			}
			
			int messageID = insertChatMessage( connection );
			
			//Process (insert) the message recipients
			if( needRecepients ) {
				insertChatMessageRecipients( connection, messageID );
			}
		} catch ( SQLException e ){
			logger.error( "An SQL exception while inserting a chat message", e );
			throw new InternalSiteException( InternalSiteException.DATABASE_EXCEPTION_ERR );
		} finally {
			//If the table was locked because of the present
			//message recipients, then unlock it here.
			if( needRecepients && ( sqlStatement != null ) ) {
				try {
					sqlStatement.execute( "UNLOCK TABLES" );
				} catch ( SQLException e ) {
					logger.error( "An SQL exception while unlocking tables after inserting new a chat message", e );
				} finally {
					//Close the statement
					try {
						sqlStatement.close();
					} catch ( SQLException e ) {
						logger.error( "An exception while closing the SQL statement", e);
					} finally {
						sqlStatement = null;
					}
				}
			}
		}
	}
	
	//This method allows to insert message recipients
	private void insertChatMessageRecipients( Connection connection, final int messageID ) throws InternalSiteException {
		String insertRecepientQuery = "INSERT INTO " + CHAT_MSG_RECEPIENT_TABLE + " SET " + 
										SENT_DATE_FIELD_NAME_CHAT_MSG_RECEPIENT_TABLE + "=?, " +
										MESSAGE_ID_FIELD_NAME_CHAT_MSG_RECEPIENT_TABLE + "=?, " +
										USR_ID_FIELD_NAME_CHAT_MSG_RECEPIENT_TABLE + "=?";
		int counter = 1;
		PreparedStatement insertChatMsgRecepPstmt = null;
		//Insert the recipients
		try {
			//Prepare the insert statement
			insertChatMsgRecepPstmt = connection.prepareStatement( insertRecepientQuery );
			//Set the message creation date
			insertChatMsgRecepPstmt.setTimestamp( counter++, msgSendTimestamp );
			//Set the message ID
			insertChatMsgRecepPstmt.setInt( counter++, messageID );
			//Add the recipients one by one
			Iterator<Integer> recepients = message.recipientIDs.iterator();
			while( recepients.hasNext() ) {
				int recipientID = recepients.next();
				try {
					insertChatMsgRecepPstmt.setInt( counter, recipientID );
					insertChatMsgRecepPstmt.executeUpdate();
				} catch ( SQLException e ) {
					logger.error("Unable to insert the recipient user ID " + recipientID +
								 " for the chat message with ID " + messageID, e );
				}
			}
		} catch (SQLException e) {
			logger.error( "An exception while inserting chat message recepients" , e);
			throw new InternalSiteException( InternalSiteException.DATABASE_EXCEPTION_ERR );			
		} finally {
			//Close the insert message recipient prepared statement
			if( insertChatMsgRecepPstmt != null ) {
				try {
					insertChatMsgRecepPstmt.close();
				} catch ( SQLException e) {
					logger.error( "Error while closing the prepared statement for adding message recepients" , e);
				} finally {
					insertChatMsgRecepPstmt = null;
				}
			}
		}
	}
	
	//Allows to insert a new chat message into the database,
	//no recipients. This method returns the inserted message ID.
	private int insertChatMessage( Connection connection ) throws InternalSiteException {
		//Insert the chat message
		final String insertMsgQuery =   "INSERT INTO " + CHAT_MESSAGES_TABLE +
										" SET " + SENT_DATE_FIELD_NAME_CHAT_MESSAGES_TABLE + " = ?, " + 
										SENDER_ID_FIELD_NAME_CHAT_MESSAGES_TABLE + " =?, " + 
										ROOM_ID_FIELD_NAME_CHAT_MESSAGES_TABLE + "=?, " +
										FILE_ID_FIELD_NAME_CHAT_MESSAGES_TABLE + "=?, " +
										MESSAGE_BODY_FIELD_NAME_CHAT_MESSAGES_TABLE + "=?, " +
										MESSAGE_TYPE_FIELD_NAME_CHAT_MESSAGES_TABLE +"=?, "+
										INFO_USER_ID_FIELD_NAME_CHAT_MESSAGES_TABLE + "=?, "+
										INFO_USER_LOGIN_FIELD_NAME_CHAT_MESSAGES_TABLE + "=?, " + 
										FONT_TYPE_FIELD_NAME_CHAT_MESSAGES_TABLE + "=?, " +
										FONT_SIZE_FIELD_NAME_CHAT_MESSAGES_TABLE + "=?, " +
										FONT_COLOR_FIELD_NAME_CHAT_MESSAGES_TABLE + "=?";
		PreparedStatement insertChatMsgPstmt = null;
		int messageID = ChatMessage.UNKNOWN_MESSAGE_ID;
		try {
			//Create statement
			insertChatMsgPstmt = connection.prepareStatement( insertMsgQuery, PreparedStatement.RETURN_GENERATED_KEYS );
			//Bind parameters
			int counter = 1;
			insertChatMsgPstmt.setTimestamp( counter++, msgSendTimestamp );
			insertChatMsgPstmt.setInt( counter++, message.senderID );
			insertChatMsgPstmt.setInt( counter++, message.roomID );
			insertChatMsgPstmt.setInt( counter++, (message.fileDesc != null) ? message.fileDesc.fileID : ShortFileDescriptor.UNKNOWN_FILE_ID );
			insertChatMsgPstmt.setString( counter++, message.messageBody );
			insertChatMsgPstmt.setInt( counter++, message.messageType.ordinal() );
			insertChatMsgPstmt.setInt( counter++, message.infoUserID );
			insertChatMsgPstmt.setString( counter++, message.infoUserLogin );
			insertChatMsgPstmt.setInt( counter++, message.fontType );
			insertChatMsgPstmt.setInt( counter++, message.fontSize );
			insertChatMsgPstmt.setInt( counter++, message.fontColor );
			//Execute query
			insertChatMsgPstmt.executeUpdate();
			//Get the message ID
			ResultSet resultSet = insertChatMsgPstmt.getGeneratedKeys();
			if ( resultSet != null && resultSet.next() ) { 
				messageID = resultSet.getInt(1); 
			}
		} catch ( SQLException e ) {
			logger.error( "An exception while inserting a new chat message", e);
			throw new InternalSiteException( InternalSiteException.DATABASE_EXCEPTION_ERR );
		} finally {
			if( insertChatMsgPstmt != null ) {
				try{
					insertChatMsgPstmt.close();
				} catch ( SQLException e ) {
					logger.error( "An exception while closing the SQL prepared statement", e);
				} finally {
					insertChatMsgPstmt = null;
				}
			}
		}
		return messageID;
	}
}
