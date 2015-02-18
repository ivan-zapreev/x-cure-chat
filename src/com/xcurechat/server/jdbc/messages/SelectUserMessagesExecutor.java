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

import java.util.ArrayList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ShortPrivateMessageData;
import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This class allows to browse user messages page by page
 */
public class SelectUserMessagesExecutor extends QueryExecutor<OnePageViewData<ShortPrivateMessageData>> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( SelectUserMessagesExecutor.class );
	
	private static final String FROM_LOGIN_NAME_FIELD_NAME_MESSAGES_TABLE = "login_send";
	private static final String TO_LOGIN_NAME_FIELD_NAME_MESSAGES_TABLE = "login_receive";
	private static final String USERS_FROM_TMP_TABLE_NAME = "u_send_tmp_tbl";
	private static final String USERS_TO_TMP_TABLE_NAME = "u_receive_tmp_tbl";
	private static final String MESSAGES_TMP_TABLE_NAME = "msgs_tmp_tbl";
	private static final String ROOMS_TMP_TABLE = "rooms_tmp_tbl";
	private static final String ROOM_NAME_FIELD_NAME_MESSAGES_TABLE = "room_name";
	
	private final int userID;
	private final boolean isAll;
	private final boolean isReceived;
	private final int offset;
	private final int size;
	
	/**
	 * A simple constructor
	 * @param userID the unique user ID
	 * @param isAll if true then we search for all messages
	 * @param isReceived if not isAll and this is true then we
	 * get only received messages, if false then sent. 
	 * @param offset the offset, from which to start retrieving entries
	 * @param size the number of entries to retrieve
	 */
	public SelectUserMessagesExecutor( final int userID, final boolean isAll,
										final boolean isReceived, final int offset,
										final int size ) {
		this.userID = userID;
		this.isAll = isAll;
		this.isReceived = isReceived;
		this.offset = offset;
		this.size = size;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		String selectQuery = "SELECT " +  MESSAGES_TMP_TABLE_NAME + "." + MSG_ID_FIELD_NAME_MESSAGES_TABLE + ", " +
								MESSAGES_TMP_TABLE_NAME + "." + FROM_UID_FIELD_NAME_MESSAGES_TABLE + ", " +
								MESSAGES_TMP_TABLE_NAME + "." + TO_UID_FIELD_NAME_MESSAGES_TABLE + ", " +
								MESSAGES_TMP_TABLE_NAME + "." + IS_READ_FIELD_NAME_MESSAGES_TABLE + ", " +
								MESSAGES_TMP_TABLE_NAME + "." + SENT_DATE_TIME_FIELD_NAME_MESSAGES_TABLE + ", " +
								MESSAGES_TMP_TABLE_NAME + "." + MSG_TYPE_FIELD_NAME_MESSAGES_TABLE + ", " +
								MESSAGES_TMP_TABLE_NAME + "." + MSG_TITLE_FIELD_NAME_MESSAGES_TABLE + ", " +
									USERS_FROM_TMP_TABLE_NAME + "." + LOGIN_FIELD_NAME_USERS_TABLE +
									" AS " + FROM_LOGIN_NAME_FIELD_NAME_MESSAGES_TABLE + ", " +
									USERS_TO_TMP_TABLE_NAME + "." + LOGIN_FIELD_NAME_USERS_TABLE +
									" AS " + TO_LOGIN_NAME_FIELD_NAME_MESSAGES_TABLE  + ", " + 
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
								" WHERE ";
		selectQuery += ( ( isAll || !isReceived ) ? "( " + MESSAGES_TMP_TABLE_NAME + "." + FROM_UID_FIELD_NAME_MESSAGES_TABLE + "=? AND " +
								MESSAGES_TMP_TABLE_NAME + "." + FROM_HIDDEN_FIELD_NAME_MESSAGES_TABLE + "=false )" : "" );
		selectQuery += ( isAll ? " OR " : "" );
		selectQuery += ( ( isAll || isReceived ) ? "( "+ MESSAGES_TMP_TABLE_NAME + "." + TO_UID_FIELD_NAME_MESSAGES_TABLE + "=? AND " +
								MESSAGES_TMP_TABLE_NAME + "." + TO_HIDDEN_FIELD_NAME_MESSAGES_TABLE + "=false )" : "" );
		selectQuery += " ORDER BY " + MESSAGES_TMP_TABLE_NAME + "." + SENT_DATE_TIME_FIELD_NAME_MESSAGES_TABLE +
						" DESC LIMIT ? OFFSET ?";
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int index = 1;
		//There will always be one used ID needed
		pstmt.setInt( index++, userID );
		//If we get all messages then we need two user IDs
		if( isAll ) {
			pstmt.setInt( index++, userID );
		}
		pstmt.setInt( index++, size );
		pstmt.setInt( index++, offset );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, OnePageViewData<ShortPrivateMessageData> result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	@Override
	public void processResultSet(ResultSet resultSet, OnePageViewData<ShortPrivateMessageData> dataObj) throws SQLException, SiteException {
		dataObj.offset = offset;
		
		ArrayList<ShortPrivateMessageData> list = new ArrayList<ShortPrivateMessageData>();
		while( resultSet.next() ) {
			ShortPrivateMessageData messageData = new ShortPrivateMessageData();
			messageData.setMsgID( resultSet.getInt( MESSAGES_TMP_TABLE_NAME + "." + MSG_ID_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setFromUID( resultSet.getInt( MESSAGES_TMP_TABLE_NAME + "." + FROM_UID_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setFromUserName( resultSet.getString( FROM_LOGIN_NAME_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setToUID( resultSet.getInt( MESSAGES_TMP_TABLE_NAME + "." + TO_UID_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setToUserName( resultSet.getString( TO_LOGIN_NAME_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setRead( resultSet.getBoolean( MESSAGES_TMP_TABLE_NAME + "." + IS_READ_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setSendReceiveDate( QueryExecutor.getTime( resultSet, MESSAGES_TMP_TABLE_NAME + "." + SENT_DATE_TIME_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setMessageType( resultSet.getInt( MESSAGES_TMP_TABLE_NAME + "." + MSG_TYPE_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setMessageTitle( resultSet.getString( MESSAGES_TMP_TABLE_NAME + "." + MSG_TITLE_FIELD_NAME_MESSAGES_TABLE ) );
			messageData.setRoomName( resultSet.getString( ROOM_NAME_FIELD_NAME_MESSAGES_TABLE ) );
			list.add( messageData );
		}
		
		if( ! list.isEmpty() ){
			dataObj.entries = list;
			logger.debug( "Retrieved the rooms for user '" + userID +
							"', size = " + (dataObj.entries != null ? ""+dataObj.entries.size() : "null") );
		} else {
			logger.warn("There are no rooms for user '"+userID+"'");
			dataObj.entries = null;
		}
	}
}
