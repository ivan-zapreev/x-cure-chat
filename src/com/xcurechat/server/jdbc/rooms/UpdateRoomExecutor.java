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

package com.xcurechat.server.jdbc.rooms;

import java.util.Date;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor class is responsible for updating room's data in the database.
 */
public class UpdateRoomExecutor extends QueryExecutor<Void> {
	
	private final ChatRoomData roomData;
	private final boolean updateTime;
	private final boolean resetTime;
	
	public UpdateRoomExecutor( ChatRoomData roomData ){
		this.roomData = roomData;
		this.updateTime = !roomData.isPermanent() && !roomData.isMain() &&
							(roomData.getRoomDurationTimeHours() != ChatRoomData.UNKNOWN_HOURS_DURATION);
		this.resetTime = ( roomData.getRoomDurationTimeHours() == ChatRoomData.CLEAN_HOURS_DURATION );
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		String updateQuery = "UPDATE " + ROOMS_TABLE + " SET " +
							NAME_FIELD_NAME_ROOMS_TABLE + "=?, " +
							DESC_FIELD_NAME_ROOMS_TABLE + "=?, " +
							IS_PERM_FIELD_NAME_ROOMS_TABLE + "=?, " +
							IS_MAIN_FIELD_NAME_ROOMS_TABLE +"=?, "+
							(updateTime ? EXP_DATE_FIELD_NAME_ROOMS_TABLE + "=" + ( resetTime? "NOW()" : "?") + ", " : "") +
							TYPE_FIELD_NAME_ROOMS_TABLE + "=? " +
							"WHERE "+ UID_FIELD_NAME_ROOMS_TABLE + "=? AND "+
							RID_FIELD_NAME_ROOMS_TABLE + "=?";
		
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setString( counter++, roomData.getRoomName() );
		pstmt.setString( counter++, roomData.getRoomDesc() );
		pstmt.setBoolean( counter++, roomData.isPermanent() );
		pstmt.setBoolean( counter++, roomData.isMain() );
		if( updateTime ) {
			if( resetTime ) {
				//If we reset time then we put the current time minus some seconds, to make the room go offline
				roomData.setExpirationDate( new Date( System.currentTimeMillis() - 1000 ) );
			} else {
				roomData.setExpirationDate( QueryExecutor.setTime( pstmt, counter++, roomData.getRoomDurationTimeHours() ) );
			}
		}
		pstmt.setInt( counter++, roomData.getRoomType() );
		pstmt.setInt( counter++, roomData.getOwnerID() );
		pstmt.setInt( counter++, roomData.getRoomID() );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		pstmt.executeUpdate();
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
