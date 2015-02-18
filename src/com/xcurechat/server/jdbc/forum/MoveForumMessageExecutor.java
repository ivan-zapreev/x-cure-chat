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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.ShortForumMessageData;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QuerySetExecutor;

/**
 * @author zapreevis
 * This class implements a transaction for moving the forum message from one parent to another
 * WARNING: Here we do not check the validity of the pathMessageID, we assume that it is correct,
 * in other words we should never use this class on messages other than the ones that just came
 * from the DB, note that the messagePathID is set by the server side only when the message is
 * created, so we are in a sefe situation and the SQL penetration should not happen. 
 */
public class MoveForumMessageExecutor extends QuerySetExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( MoveForumMessageExecutor.class );
			
	private final ForumMessageData forumMessage;
	private final ForumMessageData newParentForumMessage;
	
	/**
	 * The basic constructor
	 * @param forumMessage the message to be moved
	 * @param newParentForumMessage the message to become a new parent of the moved message
	 */
	public MoveForumMessageExecutor( final ForumMessageData forumMessage, final ForumMessageData newParentForumMessage ) {
		this.forumMessage = forumMessage;
		this.newParentForumMessage = newParentForumMessage; 
	}
	
	@Override
	public void executeQuerySet(Connection connection, Void result) throws SQLException, SiteException {
		//Instantiate transaction and its executor
		TransactionExecutor executor = new TransactionExecutor( new Transaction(){
			@Override
			public void execute(Statement sqlStatement, Logger logger) throws SQLException, SiteException {
				//First change the parent of the message we are going to move
				logger.debug("First change the parent of the message we are going to move");
				sqlStatement.execute( "UPDATE " + FORUM_MESSAGES_TABLE + " SET " +
									  PARENT_MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "=" + newParentForumMessage.messageID +
									  " WHERE " + MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "=" + forumMessage.messageID );
				
				//Next, change the messagePathID of the message and all its children
				logger.debug("Next, change the messagePathID of the message and all its children");
				sqlStatement.execute( "UPDATE " + FORUM_MESSAGES_TABLE + " SET " +
						MESSAGE_PATH_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "=REPLACE(" + MESSAGE_PATH_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + ",\"" +
																					 forumMessage.messagePathID + "\",\"" +
																					 newParentForumMessage.messagePathID +
																					 newParentForumMessage.messageID +
																					 ShortForumMessageData.MESSAGE_PATH_ID_DELIMITER + "\")" +
						  " WHERE " + MESSAGE_PATH_ID_FIELD_NAME_FORUM_MESSAGES_TABLE +
						  			  " LIKE \"" + forumMessage.messagePathID + forumMessage.messageID +
						  			  ShortForumMessageData.MESSAGE_PATH_ID_DELIMITER +"%\" OR " +
						  			  MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "=" + forumMessage.messageID );
				
				//Update the old parent's reply data, decrement the replies count
				//decrement by the number of replies to the moved message + 1,
				//i.e. counting the moved message itself
				logger.debug("Update the old parent's reply data, decrement the replies count");
				DeleteForumMessageExecutor.updateParentReplyData( sqlStatement, ForumMessageData.getMessageParentIds( forumMessage ),
									   							  true, forumMessage.numberOfReplies + 1 );
				
				//Update the new parent's reply data, incrementing the replies count
				//decrement by the number of replies to the moved message + 1,
				//i.e. counting the moved message itself. Also note that we want all
				//of the new message's location parents, thus it is the new parent
				//message parent ids plus the id of the new parent itself
				logger.debug("Update the new parent's reply data, incrementing the replies count");
				final List<Integer> newMessageParentIds = ForumMessageData.getMessageParentIds( newParentForumMessage );
				newMessageParentIds.add( newParentForumMessage.messageID );
				DeleteForumMessageExecutor.updateParentReplyData( sqlStatement, newMessageParentIds,
									   							  false, forumMessage.numberOfReplies + 1 );
			}
			@Override
			public String getDescription() {
				return "moving the forum message " + forumMessage.messageID;
			}
			
		}, logger );
		executor.execute( connection );
	}
}
