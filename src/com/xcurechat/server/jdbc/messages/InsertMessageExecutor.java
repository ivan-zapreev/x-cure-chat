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

package com.xcurechat.server.jdbc.messages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ChatRoomData;

import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.UserStateException;
import com.xcurechat.client.rpc.exceptions.IncorrectRoomDataException;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

/**
 * @author zapreevis
 * This executor class is responsible for sending a simple personal message
 */
public class InsertMessageExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( InsertMessageExecutor.class );
	
	private final int userID;
	private final int toUserID;
	private final int roomId;
	private final int messageType;
	private final String messageTitle;
	private final String messageBody;
	private final boolean isRoomAccessMsg;
	
	public InsertMessageExecutor( final int userID, final int toUserID, final int roomId, final int messageType,
									final String messageTitle, final String messageBody ){
		this.userID = userID;
		this.toUserID = toUserID;
		this.roomId = roomId;
		this.messageType = messageType;
		this.messageTitle = messageTitle;
		this.messageBody = messageBody;
		this.isRoomAccessMsg = ( roomId != ChatRoomData.UNKNOWN_ROOM_ID );
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		final String insertQuery = "INSERT INTO " + MESSAGES_TABLE +
									" SET " + FROM_UID_FIELD_NAME_MESSAGES_TABLE + " = ?, " + 
									TO_UID_FIELD_NAME_MESSAGES_TABLE + " = ?, " +
									( isRoomAccessMsg ? ROOM_ID_FIELD_NAME_MESSAGES_TABLE + " = ?, ": "" ) +
									SENT_DATE_TIME_FIELD_NAME_MESSAGES_TABLE + "=NOW(), " +
									MSG_TYPE_FIELD_NAME_MESSAGES_TABLE + "=?, " +
									MSG_TITLE_FIELD_NAME_MESSAGES_TABLE + "=?, " +
									MSG_BODY_FIELD_NAME_MESSAGES_TABLE +"=?";
		return connection.prepareStatement( insertQuery );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setInt( counter++, userID );
		pstmt.setInt( counter++, toUserID );
		if( isRoomAccessMsg ) {
			pstmt.setInt( counter++, roomId );
		}
		pstmt.setInt( counter++, messageType );
		pstmt.setString( counter++, messageTitle );
		pstmt.setString( counter++, messageBody );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		try{
			//Execute the insert
			pstmt.executeUpdate();
		} catch(MySQLIntegrityConstraintViolationException e ) {
			if( isRoomAccessMsg ) {
				throw new IncorrectRoomDataException( IncorrectRoomDataException.ROOM_DOES_NOT_EXIST_ERR );
			} else {
				throw new UserStateException( UserStateException.USER_DOES_NOT_EXIST );
			}
		}catch( SQLException e ){
			logger.error( "An unhandled exception when sending a simple personal message from " + userID + " to user " + toUserID, e);
			throw new InternalSiteException(InternalSiteException.UNKNOWN_INTERNAL_SITE_EXCEPTION_ERR);
		}
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
