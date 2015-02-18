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
package com.xcurechat.server.jdbc.rooms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.xcurechat.client.data.ChatRoomData;

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.IncorrectRoomDataException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor allows to retrieve a one room's data.
 */
public class GetRoomExecutor extends QueryExecutor<Void> {

	//The room data object with a set roomId for which we will fill the object with the data
	final ChatRoomData roomData;
	
	public GetRoomExecutor( final ChatRoomData roomData ){
		this.roomData = roomData;
	}
	
	@Override
	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT * FROM " + ROOMS_TABLE + " WHERE " +
									RID_FIELD_NAME_ROOMS_TABLE + " = ?";
		return connection.prepareStatement( selectQuery );
	}

	@Override
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int index = 1;
		pstmt.setInt( index++, roomData.getRoomID() );
	}

	@Override
	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	@Override
	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		if( resultSet.next() ) {
			roomData.setRoomID( resultSet.getInt( RID_FIELD_NAME_ROOMS_TABLE ) );
			roomData.setRoomName( resultSet.getString( NAME_FIELD_NAME_ROOMS_TABLE ) );
			roomData.setRoomDesc( resultSet.getString( DESC_FIELD_NAME_ROOMS_TABLE ) );
			roomData.setOwnerID( resultSet.getInt( UID_FIELD_NAME_ROOMS_TABLE ) );
			roomData.setOwnerName( resultSet.getString( OWNER_NAME_FIELD_NAME_ROOMS_TABLE ) );
			roomData.setPermanent( resultSet.getBoolean( IS_PERM_FIELD_NAME_ROOMS_TABLE ) );
			roomData.setMain( resultSet.getBoolean( IS_MAIN_FIELD_NAME_ROOMS_TABLE ) );
			roomData.setExpirationDate( QueryExecutor.getTime(resultSet, EXP_DATE_FIELD_NAME_ROOMS_TABLE ) );
			roomData.setRoomType( resultSet.getInt( TYPE_FIELD_NAME_ROOMS_TABLE ) );
		} else {
			//There is no room found for this roomId, this means that the room was deleted.
			throw new IncorrectRoomDataException( IncorrectRoomDataException.ROOM_DOES_NOT_EXIST_ERR );
		}
	}
}
