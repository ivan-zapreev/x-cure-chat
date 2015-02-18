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

import java.util.Map;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor allows retrieve the number of room visitors.
 */
public class SelectRoomVisirotsExecutor extends QueryExecutor<Map<Integer, Integer>> {

	public SelectRoomVisirotsExecutor(){
	}
	
	@Override
	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		//Select the room if it is either permanent or it belongs to a non-admin user that
		//is online and this room is either not expired or there are still users in it.
		final String selectQuery = "SELECT " + RID_FIELD_NAME_ROOMS_TABLE + ", " + 
									VISITORS_FIELD_NAME_ROOMS_TABLE +
									" FROM " + ROOMS_TABLE + " WHERE ( " +
									IS_PERM_FIELD_NAME_ROOMS_TABLE + " = true ) OR " +
									"( ( " + EXP_DATE_FIELD_NAME_ROOMS_TABLE + " > NOW() ) AND ( " +
									UID_FIELD_NAME_ROOMS_TABLE + " IN ( SELECT " + UID_FIELD_NAME_USERS_TABLE +
									" FROM " + USERS_TABLE + " WHERE " + IS_ONLINE_FIELD_NAME_USERS_TABLE +
									"=true ) ) ) OR ( " + VISITORS_FIELD_NAME_ROOMS_TABLE + " > 0 ) ";
		return connection.prepareStatement( selectQuery );
	}

	@Override
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		// TODO There are no parameters that need binding
	}

	@Override
	public ResultSet executeQuery(PreparedStatement pstmt, Map<Integer, Integer> result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	@Override
	public void processResultSet(ResultSet resultSet, Map<Integer, Integer> roomVisitors) throws SQLException, SiteException {
		while( resultSet.next() ) {
			roomVisitors.put( resultSet.getInt( RID_FIELD_NAME_ROOMS_TABLE ) , resultSet.getInt( VISITORS_FIELD_NAME_ROOMS_TABLE ));
		}
	}
}
