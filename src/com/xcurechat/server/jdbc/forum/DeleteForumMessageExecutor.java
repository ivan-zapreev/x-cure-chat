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

package com.xcurechat.server.jdbc.forum;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;
import com.xcurechat.server.jdbc.QuerySetExecutor;

/**
 * @author zapreevis
 * This class is responsible for deleting one forum message,
 * if the user is admin then the message sender is not checked.
 */
public class DeleteForumMessageExecutor extends QuerySetExecutor<Void> {

	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( DeleteForumMessageExecutor.class );
	
	private final int userID;
	private final ForumMessageData message;
	private final boolean isAdmin;
	
	/**
	 * Deletes the forum message sent by the user, if isAdmin == true then the message sender is not checked
	 * @param userID the user who is either an admin or is the message sender
	 * @param message the message to be deleted
	 * @param isAdmin if true then the message is deleted by the admin, this way the message sender is not checked
	 */
	public DeleteForumMessageExecutor( final int userID, final ForumMessageData message, final boolean isAdmin ){
		this.userID = userID;
		this.message = message;
		this.isAdmin = isAdmin;
	}

	@Override
	public void executeQuerySet(Connection connection, Void result)
			throws SQLException, SiteException {
		//Instantiate transaction and its executor
		TransactionExecutor executor = new TransactionExecutor( new Transaction(){
			@Override
			public void execute(Statement sqlStatement, Logger logger) throws SQLException, SiteException {
				//First, delete the message itself
				final int count = sqlStatement.executeUpdate( "DELETE FROM " + FORUM_MESSAGES_TABLE + " WHERE " +
						MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + " = " + message.messageID +
						( isAdmin ? "" : " AND " + SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + " = " + userID ) );

				//Second, if the message was deleted then update its parents in case
				if( count > 0) {
					//Update the parent's reply data, decrement the replies count
					//decrement by the number of replies to the deleted message + 1,
					//i.e. counting the deleted message itself
					updateParentReplyData( sqlStatement, ForumMessageData.getMessageParentIds( message ),
										   true, message.numberOfReplies + 1 );
				} else {
					logger.warn( "Unable to delete forum message" + message.messageID +
							 ", the request came from user " + userID +
							 " who is " + (isAdmin? "" : " NOT ") + "an admin!" );
				}
			}
			@Override
			public String getDescription() {
				return "deleting forum message " + message.messageID;
			}
			
		}, logger );
		executor.execute( connection );
	}
	
	/**
	 * Allows to update the last reply sender, last reply date and reply count
	 * data in the parent messages as indicated by the path i data stored in the 
	 * first method's argument, there we expect the parent ids ordered the same
	 * way as in the messagePathID column of the forum messages table
	 * @param sqlStatement the sql statement object to work with
	 * @param parentIds the ordered list of message path ids
	 * @param isDecrementCount if true then the reply count for the parent
	 *                         messages will be decremented, otherwise
	 *                         incremented, by one.
	 * @param decrement the number by which we decrement/increment the number of replies
	 * @throws SQLException if smth bad happens
	 */
	public static void updateParentReplyData( Statement sqlStatement, final List<Integer> parentIds,
											  final boolean isDecrementCount, final int decrement ) throws SQLException {
		String parentPathId = "";
		for( int index = 0; index < parentIds.size(); index++ ) {
			//Get the first parent id from the back
			final int parentId = parentIds.get( index );
			//Construct the current path id
			parentPathId = parentPathId + parentId + ForumMessageData.MESSAGE_PATH_ID_DELIMITER;
			ResultSet resultSet = sqlStatement.executeQuery( "SELECT " + SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + ", " +
									   			   						 UPDATE_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE + " " +
									   			   			 "FROM " + FORUM_MESSAGES_TABLE + " " +
									   			   			 "WHERE " + MESSAGE_PATH_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + " " +
									   			   			 			"LIKE CONCAT( \"" + parentPathId + "\", \"%\") " +
									   			   			 "ORDER BY " + UPDATE_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE + " DESC LIMIT 0,1" );
			//If the latest reply sender and reply date was found then use them, otherwise use the default
			final Date replyDate;
			final int replySenderId;
			if( resultSet.next() ) {
				replySenderId = resultSet.getInt( SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE );
				replyDate = QueryExecutor.getTime(resultSet, UPDATE_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE );
			} else {
				replySenderId = ShortUserData.UNKNOWN_UID;
				replyDate = null; 
			}
			//If the message does not have replies set the last reply date to be the message's update date.
			sqlStatement.executeUpdate( "UPDATE " + FORUM_MESSAGES_TABLE + " SET " +
										 	NUM_REPLIES_FIELD_NAME_FORUM_MESSAGES_TABLE +
										 	" = " + NUM_REPLIES_FIELD_NAME_FORUM_MESSAGES_TABLE +
										 	( isDecrementCount? "-" : "+" ) + decrement + ", " +
										 LAST_SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + " = " + replySenderId + ", " +  
										 ( replyDate != null ?
											   LAST_REPLY_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE + "='" + new Timestamp( replyDate.getTime() ) + "'"
										   :
											   LAST_REPLY_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE + "=" + UPDATE_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE
										  ) + " " +
										  "WHERE " + MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "=" + parentId );
		}
	}
}
