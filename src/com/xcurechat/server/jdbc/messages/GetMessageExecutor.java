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
package com.xcurechat.server.jdbc.messages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.PrivateMessageData;

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.MessageException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This class allows to browse user messages page by page
 */
public class GetMessageExecutor extends QueryExecutor<PrivateMessageData> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( GetMessageExecutor.class );
	
	private static final String FROM_LOGIN_NAME_FIELD_NAME_MESSAGES_TABLE = "login_send";
	private static final String TO_LOGIN_NAME_FIELD_NAME_MESSAGES_TABLE = "login_receive";
	private static final String USERS_FROM_TMP_TABLE_NAME = "u_send_tmp_tbl";
	private static final String USERS_TO_TMP_TABLE_NAME = "u_receive_tmp_tbl";
	private static final String MESSAGES_TMP_TABLE_NAME = "msgs_tmp_tbl";
	private static final String ROOMS_TMP_TABLE = "rooms_tmp_tbl";
	private static final String ROOM_NAME_FIELD_NAME_MESSAGES_TABLE = "room_name";
	
	private final int userID;
	private final int messageID;
	
	/**
	 * A simple constructor
	 * @param userID the ID of the user who is either a sender or a receiver of the message
	 * @param messageID the id of the message we want to retrieve
	 */
	public GetMessageExecutor( final int userID, final int messageID ){
		this.userID = userID;
		this.messageID = messageID;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT " + 
									MESSAGES_TMP_TABLE_NAME + "." + MSG_ID_FIELD_NAME_MESSAGES_TABLE + ", " +
									MESSAGES_TMP_TABLE_NAME + "." + FROM_UID_FIELD_NAME_MESSAGES_TABLE + ", " +
									MESSAGES_TMP_TABLE_NAME + "." + TO_UID_FIELD_NAME_MESSAGES_TABLE + ", " +
									MESSAGES_TMP_TABLE_NAME + "." + IS_READ_FIELD_NAME_MESSAGES_TABLE + ", " +
									MESSAGES_TMP_TABLE_NAME + "." + SENT_DATE_TIME_FIELD_NAME_MESSAGES_TABLE + ", " +
									MESSAGES_TMP_TABLE_NAME + "." + MSG_TYPE_FIELD_NAME_MESSAGES_TABLE + ", " +
									MESSAGES_TMP_TABLE_NAME + "." + MSG_TITLE_FIELD_NAME_MESSAGES_TABLE + ", " +
									MESSAGES_TMP_TABLE_NAME + "." + MSG_BODY_FIELD_NAME_MESSAGES_TABLE + ", " +
									MESSAGES_TMP_TABLE_NAME + "." + ROOM_ID_FIELD_NAME_MESSAGES_TABLE + ", " +
										USERS_FROM_TMP_TABLE_NAME + "." + LOGIN_FIELD_NAME_USERS_TABLE +
										" AS " + FROM_LOGIN_NAME_FIELD_NAME_MESSAGES_TABLE + ", " +
										USERS_TO_TMP_TABLE_NAME + "." + LOGIN_FIELD_NAME_USERS_TABLE +
										" AS " + TO_LOGIN_NAME_FIELD_NAME_MESSAGES_TABLE + ", " + 
										ROOMS_TMP_TABLE + "." + NAME_FIELD_NAME_ROOMS_TABLE +  
										" AS " + ROOM_NAME_FIELD_NAME_MESSAGES_TABLE +
									" FROM " + USERS_TABLE + " AS " + USERS_TO_TMP_TABLE_NAME +
									" INNER JOIN " + MESSAGES_TABLE + " AS " + MESSAGES_TMP_TABLE_NAME + " ON " +
										USERS_TO_TMP_TABLE_NAME + "." + UID_FIELD_NAME_USERS_TABLE +
										" = " +
										MESSAGES_TMP_TABLE_NAME + "." + TO_UID_FIELD_NAME_MESSAGES_TABLE +
									" INNER JOIN " + USERS_TABLE + " AS " + USERS_FROM_TMP_TABLE_NAME + " ON " +
										USERS_FROM_TMP_TABLE_NAME + "." + UID_FIELD_NAME_USERS_TABLE +
										" = " +
										MESSAGES_TMP_TABLE_NAME + "." + FROM_UID_FIELD_NAME_MESSAGES_TABLE +
									" LEFT JOIN " + ROOMS_TABLE + " AS " + ROOMS_TMP_TABLE + " ON " +
										MESSAGES_TMP_TABLE_NAME + "." + ROOM_ID_FIELD_NAME_MESSAGES_TABLE +
										" = " +
										ROOMS_TMP_TABLE + "." + RID_FIELD_NAME_ROOMS_TABLE +
									" WHERE ( ( " + MESSAGES_TMP_TABLE_NAME + "." + FROM_UID_FIELD_NAME_MESSAGES_TABLE + "=? AND " +
										MESSAGES_TMP_TABLE_NAME + "." + FROM_HIDDEN_FIELD_NAME_MESSAGES_TABLE + "=false ) OR ( "+
										MESSAGES_TMP_TABLE_NAME + "." + TO_UID_FIELD_NAME_MESSAGES_TABLE + "=? AND " +
										MESSAGES_TMP_TABLE_NAME + "." + TO_HIDDEN_FIELD_NAME_MESSAGES_TABLE + "=false ) ) AND " +
										MESSAGES_TMP_TABLE_NAME + "." + MSG_ID_FIELD_NAME_MESSAGES_TABLE + "=?";
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int index = 1;
		pstmt.setInt( index++, userID );
		pstmt.setInt( index++, userID );
		pstmt.setInt( index++, messageID );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, PrivateMessageData result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	@Override
	public void processResultSet(ResultSet resultSet, PrivateMessageData messageData) throws SQLException, SiteException {
		if( resultSet.next() ) {
			messageData.setMsgID( resultSet.getInt( MESSAGES_TMP_TABLE_NAME + "." + MSG_ID_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setFromUID( resultSet.getInt( MESSAGES_TMP_TABLE_NAME + "." + FROM_UID_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setFromUserName( resultSet.getString( FROM_LOGIN_NAME_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setToUID( resultSet.getInt( MESSAGES_TMP_TABLE_NAME + "." + TO_UID_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setToUserName( resultSet.getString( TO_LOGIN_NAME_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setRead( resultSet.getBoolean( MESSAGES_TMP_TABLE_NAME + "." + IS_READ_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setSendReceiveDate( QueryExecutor.getTime( resultSet, MESSAGES_TMP_TABLE_NAME + "." + SENT_DATE_TIME_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setMessageType( resultSet.getInt( MESSAGES_TMP_TABLE_NAME + "." + MSG_TYPE_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setMessageTitle( resultSet.getString( MESSAGES_TMP_TABLE_NAME + "." + MSG_TITLE_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setMessageBody( resultSet.getString( MESSAGES_TMP_TABLE_NAME + "." + MSG_BODY_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setRoomID( resultSet.getInt( MESSAGES_TMP_TABLE_NAME + "." + ROOM_ID_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setRoomName( resultSet.getString( ROOM_NAME_FIELD_NAME_MESSAGES_TABLE ) );
		} else {
			logger.warn("Could not find the message " + messageID + " for user "+userID);
			throw new MessageException( MessageException.UNABLE_TO_RETRIEVE_MESSAGE );
		}
	}
}
