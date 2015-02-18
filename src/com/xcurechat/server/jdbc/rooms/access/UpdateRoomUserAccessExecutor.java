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
 * This executor class is responsible for updating room's data in the database.
 */
public class UpdateRoomUserAccessExecutor extends QueryExecutor<Void> {
	
	private final RoomUserAccessData userAccess;
	private final boolean updateTime;
	
	public UpdateRoomUserAccessExecutor( final RoomUserAccessData userAccess ){
		this.userAccess = userAccess;
		this.updateTime = (userAccess.getReadAllDurationTimeHours() != ChatRoomData.UNKNOWN_HOURS_DURATION) 
						 	&& userAccess.isReadAll(); 
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		//Below we check that we update the access right with the given RAID, RID and UID
		//This is done for security reasons, to avoid editing of some one elsed access right 
		String updateQuery = "UPDATE " + ROOM_ACCESS_TABLE + " SET " +
							IS_SYSTEM_FIELD_NAME_ROOM_ACCESS_TABLE + "=?, " +
							IS_READ_FIELD_NAME_ROOM_ACCESS_TABLE + "=?, " +
							IS_READ_ALL_FIELD_NAME_ROOM_ACCESS_TABLE + "=?, " +
							(updateTime ? READ_ALL_EXP_DATE_FIELD_NAME_ROOM_ACCESS_TABLE + "=?, " : " ") +
							IS_WRITE_FIELD_NAME_ROOM_ACCESS_TABLE +"=? "+
							"WHERE "+ RAID_FIELD_NAME_ROOM_ACCESS_TABLE + "=? AND "+
							RID_FIELD_NAME_ROOM_ACCESS_TABLE + "=? AND "+
							UID_FIELD_NAME_ROOM_ACCESS_TABLE + "=? ";
		
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setBoolean( counter++, userAccess.isSystem() );
		pstmt.setBoolean( counter++, userAccess.isRead() );
		pstmt.setBoolean( counter++, userAccess.isReadAll() );
		if( updateTime ) {
			if( userAccess.getReadAllDurationTimeHours() == ChatRoomData.CLEAN_HOURS_DURATION ) {
				pstmt.setTimestamp( counter++, null );
			} else {
				userAccess.setReadAllExpires( QueryExecutor.setTime( pstmt, counter++, userAccess.getReadAllDurationTimeHours() ) );
			}
		}
		pstmt.setBoolean( counter++, userAccess.isWrite() );
		pstmt.setInt( counter++, userAccess.getRAID() );
		pstmt.setInt( counter++, userAccess.getRID() );
		pstmt.setInt( counter++, userAccess.getUID() );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		pstmt.executeUpdate();
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
