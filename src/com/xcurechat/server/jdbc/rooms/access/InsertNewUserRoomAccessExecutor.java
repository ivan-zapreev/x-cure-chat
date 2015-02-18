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
package com.xcurechat.server.jdbc.rooms.access;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.RoomUserAccessData;

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor class is responsible for executing user-room access rule insert into the database.
 * NOTE: This executor updates the provided RoomUserAccessData object with the room-user access right id 
 */
public class InsertNewUserRoomAccessExecutor extends QueryExecutor<Void> {
	private final RoomUserAccessData userAccess;
	private final boolean insertTime;

	public InsertNewUserRoomAccessExecutor( final RoomUserAccessData userAccess ) {
		this.userAccess = userAccess;
		this.insertTime = (userAccess.getReadAllDurationTimeHours() != ChatRoomData.UNKNOWN_HOURS_DURATION) 
							&& userAccess.isReadAll(); 
	}

	@Override
	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String query = "INSERT INTO " + ROOM_ACCESS_TABLE + " SET " +
								RID_FIELD_NAME_ROOM_ACCESS_TABLE + " = ?, " +
								UID_FIELD_NAME_ROOM_ACCESS_TABLE + " = ?, " +
								LOGIN_FIELD_NAME_ROOM_ACCESS_TABLE + " = (SELECT " +
								LOGIN_FIELD_NAME_USERS_TABLE +" FROM "+ USERS_TABLE +" WHERE "+
								UID_FIELD_NAME_USERS_TABLE+" = ? ), " +
								IS_SYSTEM_FIELD_NAME_ROOM_ACCESS_TABLE + " = ?, " +
								IS_WRITE_FIELD_NAME_ROOM_ACCESS_TABLE + " = ?, " +
								IS_READ_FIELD_NAME_ROOM_ACCESS_TABLE + " = ?, " + 
								IS_READ_ALL_FIELD_NAME_ROOM_ACCESS_TABLE + " = ?" + 
								( insertTime ? ", " + READ_ALL_EXP_DATE_FIELD_NAME_ROOM_ACCESS_TABLE + " = ?" : "" ); 

		return connection.prepareStatement( query, PreparedStatement.RETURN_GENERATED_KEYS );
	}
	
	@Override
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int index = 1;
		pstmt.setInt( index++, userAccess.getRID() );
		pstmt.setInt( index++, userAccess.getUID() );
		pstmt.setInt( index++, userAccess.getUID() );
		pstmt.setBoolean( index++, userAccess.isSystem() );
		pstmt.setBoolean( index++, userAccess.isWrite() );
		pstmt.setBoolean( index++, userAccess.isRead() );
		pstmt.setBoolean( index++, userAccess.isReadAll() );
		if( insertTime ) {
			QueryExecutor.setTime( pstmt, index++, userAccess.getReadAllDurationTimeHours() );
		}
	}

	@Override
	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		pstmt.executeUpdate();
		//Get back the id of the inserted access right  
		ResultSet resultSet = pstmt.getGeneratedKeys();
		if ( resultSet != null && resultSet.next() ) { 
			userAccess.setRAID( resultSet.getInt(1) ); 
		}
		return null;
	}

	@Override
	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//DO NOTHING
	}

}
