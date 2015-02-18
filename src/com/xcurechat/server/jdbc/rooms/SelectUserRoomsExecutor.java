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

import java.util.ArrayList;
import java.util.Date;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This class allows to browsr user rooms page by page
 */
public class SelectUserRoomsExecutor extends QueryExecutor<OnePageViewData<ChatRoomData>> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( SelectUserRoomsExecutor.class );
	
	private final int userID;
	private final int offset;
	private final int size;
	
	/**
	 * A simple constructor
	 * @param userID the unique user ID
	 * @param offset the offset, from which to start retrieving entries
	 * @param size the number of entries to retrieve
	 */
	public SelectUserRoomsExecutor( final int userID, final int offset, final int size ){
		this.userID = userID;
		this.offset = offset;
		this.size = size;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT * FROM " + ROOMS_TABLE + " WHERE " +
									UID_FIELD_NAME_ROOMS_TABLE + "=? "+
									"ORDER BY " + IS_MAIN_FIELD_NAME_ROOMS_TABLE + " DESC, " +
									EXP_DATE_FIELD_NAME_ROOMS_TABLE + " DESC, " +
									IS_PERM_FIELD_NAME_ROOMS_TABLE + " DESC LIMIT ? OFFSET ?";
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int index = 1;
		pstmt.setInt( index++, userID );
		pstmt.setInt( index++, size );
		pstmt.setInt( index++, offset );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, OnePageViewData<ChatRoomData> result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	@Override
	public void processResultSet(ResultSet resultSet, OnePageViewData<ChatRoomData> dataObj) throws SQLException, SiteException {
		dataObj.offset = offset;
		
		ArrayList<ChatRoomData> list = new ArrayList<ChatRoomData>();
		while( resultSet.next() ){
			final int rid = resultSet.getInt( RID_FIELD_NAME_ROOMS_TABLE ); 
			final String name = resultSet.getString( NAME_FIELD_NAME_ROOMS_TABLE );
			final String desc = resultSet.getString( DESC_FIELD_NAME_ROOMS_TABLE );
			final int uid = resultSet.getInt( UID_FIELD_NAME_ROOMS_TABLE );
			final String ownerLoginName = resultSet.getString( OWNER_NAME_FIELD_NAME_ROOMS_TABLE );
			final boolean is_perm = resultSet.getBoolean( IS_PERM_FIELD_NAME_ROOMS_TABLE );
			final boolean is_main = resultSet.getBoolean( IS_MAIN_FIELD_NAME_ROOMS_TABLE );
			
			final Date exp_date = QueryExecutor.getTime( resultSet, EXP_DATE_FIELD_NAME_ROOMS_TABLE );
			final int type = resultSet.getInt( TYPE_FIELD_NAME_ROOMS_TABLE );
			
			list.add( new ChatRoomData( rid, name, desc, uid, ownerLoginName, is_perm, is_main, exp_date, type ) );
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
