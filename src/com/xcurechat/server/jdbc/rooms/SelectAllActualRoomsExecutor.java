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

import java.util.Date;
import java.util.Map;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.xcurechat.client.data.ChatRoomData;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor allows to select all chat rooms that have to be online.
 */
public class SelectAllActualRoomsExecutor extends QueryExecutor<Map<Integer, ChatRoomData>> {

	public SelectAllActualRoomsExecutor(){
	}
	
	@Override
	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT * FROM " + ROOMS_TABLE + " WHERE ( " +
									IS_PERM_FIELD_NAME_ROOMS_TABLE + " = true ) OR " +
									"( ( " + EXP_DATE_FIELD_NAME_ROOMS_TABLE + " > NOW() ) AND ( " +
									UID_FIELD_NAME_ROOMS_TABLE + " IN ( SELECT " + UID_FIELD_NAME_USERS_TABLE +
									" FROM " + USERS_TABLE + " WHERE " + IS_ONLINE_FIELD_NAME_USERS_TABLE +
									"=true ) ) ) OR ( " + VISITORS_FIELD_NAME_ROOMS_TABLE + " > 0 ) " + 
									"ORDER BY " + IS_MAIN_FIELD_NAME_ROOMS_TABLE + " DESC, " +
									IS_PERM_FIELD_NAME_ROOMS_TABLE + " DESC, " + 
									EXP_DATE_FIELD_NAME_ROOMS_TABLE + " DESC";
		return connection.prepareStatement( selectQuery );
	}

	@Override
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		// TODO There are no parameters that need binding
	}

	@Override
	public ResultSet executeQuery(PreparedStatement pstmt, Map<Integer, ChatRoomData> result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	@Override
	public void processResultSet(ResultSet resultSet, Map<Integer, ChatRoomData> chatRooms) throws SQLException, SiteException {
		while( resultSet.next() ) {
			final int rid = resultSet.getInt( RID_FIELD_NAME_ROOMS_TABLE ); 
			final String name = resultSet.getString( NAME_FIELD_NAME_ROOMS_TABLE );
			final String desc = resultSet.getString( DESC_FIELD_NAME_ROOMS_TABLE );
			final int uid = resultSet.getInt( UID_FIELD_NAME_ROOMS_TABLE );
			final String ownerLoginName = resultSet.getString( OWNER_NAME_FIELD_NAME_ROOMS_TABLE );
			final boolean is_perm = resultSet.getBoolean( IS_PERM_FIELD_NAME_ROOMS_TABLE );
			final boolean is_main = resultSet.getBoolean( IS_MAIN_FIELD_NAME_ROOMS_TABLE );
			final Date exp_date = QueryExecutor.getTime(resultSet, EXP_DATE_FIELD_NAME_ROOMS_TABLE );
			final int type = resultSet.getInt( TYPE_FIELD_NAME_ROOMS_TABLE );
			chatRooms.put( rid, new ChatRoomData( rid, name, desc, uid, ownerLoginName, is_perm, is_main, exp_date, type) );  
		}
	}
}
