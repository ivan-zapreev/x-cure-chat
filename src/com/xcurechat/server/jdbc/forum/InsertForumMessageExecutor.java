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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ShortForumMessageData;
import com.xcurechat.client.data.ShortUserData;

import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.MessageException;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.MysqlDataTruncation;

/**
 * @author zapreevis
 * This executor class is responsible for sending a new forum message.
 *  Updates the updateDate and sentDate of the message.
 */
public class InsertForumMessageExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( InsertForumMessageExecutor.class );
	
	private final ShortForumMessageData forumMessage;
	
	public InsertForumMessageExecutor( final ShortForumMessageData forumMessage ){
		this.forumMessage = forumMessage;
		//Initialize the sent date/update date
		final Date now = new Date();
		this.forumMessage.sentDate = now;
		this.forumMessage.updateDate = now;
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		final String insertQuery = "INSERT INTO " + FORUM_MESSAGES_TABLE +
									" SET " + PARENT_MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + " = ?, " +
									SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + " = ?, " +
									SENT_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE + "=?, " +
									UPDATE_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE + "=?, " +
									MESSAGE_TITLE_FIELD_NAME_FORUM_MESSAGES_TABLE + "=?, " +
									MESSAGE_BODY_FIELD_NAME_FORUM_MESSAGES_TABLE + "=?, " +
									NUM_REPLIES_FIELD_NAME_FORUM_MESSAGES_TABLE + "=?, " +
									LAST_SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "=?, " +
									LAST_REPLY_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE + "=?, " +
									MESSAGE_PATH_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + " = ( SELECT CONCAT(" +
										MESSAGE_PATH_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + ", ?, \"" +
										ShortForumMessageData.MESSAGE_PATH_ID_DELIMITER + "\" ) from " + FORUM_MESSAGES_TABLE +
										" AS tmp WHERE tmp." + MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + " = ? )";
		return connection.prepareStatement( insertQuery, PreparedStatement.RETURN_GENERATED_KEYS );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setInt( counter++, forumMessage.parentMessageID );
		pstmt.setInt( counter++, forumMessage.senderID );
		pstmt.setTimestamp( counter++, new Timestamp( forumMessage.sentDate.getTime() ) );
		pstmt.setTimestamp( counter++, new Timestamp( forumMessage.updateDate.getTime() )  );
		pstmt.setString( counter++, forumMessage.messageTitle );
		pstmt.setString( counter++, forumMessage.messageBody );
		pstmt.setInt( counter++, 0 ); 											 //NUM_REPLIES_FIELD_NAME_FORUM_MESSAGES_TABLE
		pstmt.setInt( counter++, ShortUserData.UNKNOWN_UID );					 //LAST_SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE
		pstmt.setTimestamp( counter++, new Timestamp( (new Date()).getTime()) ); //LAST_REPLY_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE
		pstmt.setInt( counter++, forumMessage.parentMessageID );
		pstmt.setInt( counter++, forumMessage.parentMessageID );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		try{
			//Execute the insert
			pstmt.executeUpdate();
			//Get back the id of the inserted row  
			ResultSet resultSet = pstmt.getGeneratedKeys();
			if ( resultSet != null && resultSet.next() ) {
				//Store the message ID
				forumMessage.messageID =  resultSet.getInt(1); 
			}
		} catch( MySQLIntegrityConstraintViolationException e ) {
			logger.error( "An unhandled exception when sending a forum message by " + forumMessage.senderID, e);
			throw new MessageException( MessageException.THE_MESSAGE_YOU_REPLY_TO_DOES_NOT_EXIST );
		} catch( MysqlDataTruncation e ) {
			logger.error( "An unhandled exception when sending a forum message by " + forumMessage.senderID, e);
			throw new MessageException( MessageException.THE_RESULTING_MESSAGE_IS_TOO_LONG );
		} catch( SQLException e ){
			logger.error( "An unhandled exception when sending a forum message by " + forumMessage.senderID, e);
			throw new InternalSiteException(InternalSiteException.UNKNOWN_INTERNAL_SITE_EXCEPTION_ERR);
		}
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
