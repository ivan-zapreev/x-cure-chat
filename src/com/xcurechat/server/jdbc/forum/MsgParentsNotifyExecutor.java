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

package com.xcurechat.server.jdbc.forum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ShortForumMessageData;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor class is responsible for updating the last reply by and
 * last reply date fields in the parent messages. Also increment the
 * replies counter if needed.
 */
public class MsgParentsNotifyExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( MsgParentsNotifyExecutor.class );

	private final ShortForumMessageData message;
	private final List<Integer> parentMsgIds;
	private final boolean incrementRepliesCounter;
	
	/**
	 * The basic constructor
	 * @param message the message which parents we need to notify, it should have the path id set and also the update date
	 * @param incrementRepliesCounter true if we need to increment the number of replies for the parent messages, i.e. this
	 * 								  is for the case when we use this class after a new message was posted
	 */
	public MsgParentsNotifyExecutor( final ShortForumMessageData message , final boolean incrementRepliesCounter ){
		this.message = message;
		this.parentMsgIds = ShortForumMessageData.getMessageParentIds( message );
		this.incrementRepliesCounter = incrementRepliesCounter;
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		String updateQuery = "UPDATE " + FORUM_MESSAGES_TABLE + " SET " +
							( incrementRepliesCounter ? NUM_REPLIES_FIELD_NAME_FORUM_MESSAGES_TABLE + "=" +
														NUM_REPLIES_FIELD_NAME_FORUM_MESSAGES_TABLE + "+1, " : "" ) +
							LAST_SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "=?, " +
							LAST_REPLY_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE + "=? " +
							" WHERE "+
							MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + createINQuerySet( parentMsgIds );
		
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setInt( counter++, message.senderID );
		pstmt.setTimestamp( counter++, new Timestamp( message.updateDate.getTime() ) );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		if( pstmt.executeUpdate() == 0 ) {
			logger.error("Could not update the replies-related data in the parents of the new message " +  message.messageID );
		}
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
