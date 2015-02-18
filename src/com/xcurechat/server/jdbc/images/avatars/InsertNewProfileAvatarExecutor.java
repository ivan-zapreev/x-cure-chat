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

package com.xcurechat.server.jdbc.images.avatars;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import com.xcurechat.client.data.UserFileData;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor class is responsible for inserting a new user avatar.
 */
public class InsertNewProfileAvatarExecutor extends QueryExecutor<Void> {
	
	private final UserFileData fileData;
	
	public InsertNewProfileAvatarExecutor( UserFileData fileData ) {
		this.fileData = fileData;
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		final String insertQuery = "INSERT INTO " + AVATAR_IMAGES_TABLE + " SET " +
									USER_ID_AVATAR_IMAGES_TABLE + "=?, " +
									IMAGE_FIELD_AVATAR_IMAGES_TABLE + "=?, " +
									MIME_TYPE_AVATAR_IMAGES_TABLE + "=?";
		return connection.prepareStatement( insertQuery );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setInt( counter++, fileData.ownerID );
		pstmt.setBytes( counter++, fileData.fileData );
		pstmt.setString( counter++, fileData.mimeType );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		try {
			pstmt.executeUpdate();
		}catch( MySQLIntegrityConstraintViolationException e ){
			//This exception is thrown in case there is already one avatar image for 
			//the user. Note that, it should be caught before being passed to the client!!!
			throw new InternalSiteException("The user Avatar image already exists!");
		}
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
