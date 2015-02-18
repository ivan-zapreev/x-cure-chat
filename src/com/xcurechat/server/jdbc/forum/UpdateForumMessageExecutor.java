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
import java.util.Date;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ShortForumMessageData;

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.MessageException;

import com.xcurechat.server.jdbc.QueryExecutor;

import com.mysql.jdbc.MysqlDataTruncation;

/**
 * @author zapreevis
 * This executor class is responsible for updating a forum message.
 * Updates the updateDate of the message.
 */
public class UpdateForumMessageExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( UpdateForumMessageExecutor.class );

	private final ShortForumMessageData message;
	private final boolean disapprove;
	
	/**
	 * The basic constructor
	 * @param message the message update
	 * @param disapprove if true then the message will be set to disapproved.
	 */
	public UpdateForumMessageExecutor( final ShortForumMessageData message, final boolean disapprove ){
		this.message = message;
		this.disapprove = disapprove;
		this.message.updateDate = new Date();
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		String updateQuery = "UPDATE " + FORUM_MESSAGES_TABLE + " SET " +
							MESSAGE_TITLE_FIELD_NAME_FORUM_MESSAGES_TABLE + "=?, " +
							MESSAGE_BODY_FIELD_NAME_FORUM_MESSAGES_TABLE + "=?, " +
							( disapprove ? IS_APPROVED_FIELD_NAME_FORUM_MESSAGES_TABLE + "=?, " : "" ) +
							UPDATE_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE + "=?" +
							" WHERE "+
							MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "=? AND " +
							SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "=?";
		
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setString( counter++, message.messageTitle );
		pstmt.setString( counter++, message.messageBody );
		if( disapprove ) {
			//Force the message disapproval
			pstmt.setBoolean( counter++, false );
		}
		pstmt.setTimestamp( counter++, new Timestamp( message.updateDate.getTime() )  );
		pstmt.setInt( counter++, message.messageID );
		pstmt.setInt( counter++, message.senderID );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		try {
			if( pstmt.executeUpdate() == 0 ) {
				logger.error("Could not update the forum message " +  message.messageID + " by user " + message.senderID );
				throw new MessageException( MessageException.THE_MESSAGE_YOU_UPDATE_DOES_NOT_EXIST );
			}
		} catch( MysqlDataTruncation e ) {
			logger.error( "An unhandled exception when sending a forum message by " + message.senderID, e);
			throw new MessageException( MessageException.THE_RESULTING_MESSAGE_IS_TOO_LONG );
		}
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
