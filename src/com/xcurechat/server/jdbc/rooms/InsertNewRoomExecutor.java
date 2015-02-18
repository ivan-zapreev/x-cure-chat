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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor class is responsible for executing room insert into the database.
 */
public class InsertNewRoomExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( InsertNewRoomExecutor.class );
	
	private final ChatRoomData roomData;
	final boolean insertTime;
	
	public InsertNewRoomExecutor( ChatRoomData roomData ){
		this.roomData = roomData;
		this.insertTime = !roomData.isPermanent() && !roomData.isMain();
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		final String insertQuery = "INSERT INTO " + ROOMS_TABLE +
									" SET " + NAME_FIELD_NAME_ROOMS_TABLE + " = ?, " + 
									DESC_FIELD_NAME_ROOMS_TABLE + " = ?, " + 
									UID_FIELD_NAME_ROOMS_TABLE + "=?, " +
									OWNER_NAME_FIELD_NAME_ROOMS_TABLE + "=?, " +
									IS_PERM_FIELD_NAME_ROOMS_TABLE + "=?, " +
									IS_MAIN_FIELD_NAME_ROOMS_TABLE +"=?, "+
									( insertTime ? EXP_DATE_FIELD_NAME_ROOMS_TABLE + "=?, " : "" )+
									TYPE_FIELD_NAME_ROOMS_TABLE + "=?";
		return connection.prepareStatement( insertQuery, PreparedStatement.RETURN_GENERATED_KEYS );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setString( counter++, roomData.getRoomName() );
		pstmt.setString( counter++, roomData.getRoomDesc() );
		pstmt.setInt( counter++, roomData.getOwnerID() );
		pstmt.setString( counter++, roomData.getOwnerName() );
		pstmt.setBoolean( counter++, roomData.isPermanent() );
		pstmt.setBoolean( counter++, roomData.isMain() );
		if( insertTime ) {
			roomData.setExpirationDate( QueryExecutor.setTime( pstmt, counter++, roomData.getRoomDurationTimeHours() ) );
		}
		pstmt.setInt( counter++, roomData.getRoomType() );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		try{
			//Execute the insert
			pstmt.executeUpdate();
			//Get back the id of the inserted row  
			ResultSet resultSet = pstmt.getGeneratedKeys();
			if ( resultSet != null && resultSet.next() ) { 
			    roomData.setRoomID( resultSet.getInt(1) ); 
			}
		}catch( SQLException e ){
			logger.error( "An unhandled exception when adding a new room to the database for user " + roomData.getOwnerID(), e);
			throw new InternalSiteException(InternalSiteException.UNKNOWN_INTERNAL_SITE_EXCEPTION_ERR);
		}
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
