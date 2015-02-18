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

package com.xcurechat.server.jdbc.images;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.xcurechat.client.data.FileData;
import com.xcurechat.client.data.UserFileData;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor class is responsible for inserting a new user-profile file into the database.
 */
public class InsertNewProfileFileExecutor extends QueryExecutor<Void> {
	
	private final UserFileData fileData;
	
	public InsertNewProfileFileExecutor( final UserFileData fileData ) {
		this.fileData = fileData;
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		final String insertQuery = "INSERT INTO " + PROFILE_FILES_TABLE + " SET " +
									OWNER_ID_PROFILE_FILES_TABLE + "=?, " +
									MIME_TYPE_PROFILE_FILES_TABLE + "=?, " +
									FILE_NAME_PROFILE_FILES_TABLE + "=?, " +
									IMG_WIDTH_PROFILE_FILES_TABLE + "=?, " +
									IMG_HEIGHT_PROFILE_FILES_TABLE + "=?, " +
									( fileData.thumbnailData != null ? THUMBNAIL_FIELD_PROFILE_FILES_TABLE + "=?, " : "" ) + 
									DATA_FIELD_PROFILE_FILES_TABLE + "=?, " + 
									UPLOAD_DATE_PROFILE_FILES_TABLE + "=NOW()";
									
		return connection.prepareStatement( insertQuery, PreparedStatement.RETURN_GENERATED_KEYS );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int index = 1;
		pstmt.setInt( index++, fileData.ownerID );
		pstmt.setString( index++, fileData.mimeType );
		pstmt.setString( index++, fileData.fileName );
		pstmt.setInt( index++, fileData.widthPixels );
		pstmt.setInt( index++, fileData.heightPixels );
		if( fileData.thumbnailData != null ) {
			pstmt.setBytes( index++, fileData.thumbnailData );
		}
		pstmt.setBytes( index++, fileData.fileData );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		pstmt.executeUpdate();
		//Get back the id of the inserted file  
		ResultSet resultSet = pstmt.getGeneratedKeys();
		if ( resultSet != null && resultSet.next() ) {
			fileData.fileID = resultSet.getInt(1); 
		} else {
			fileData.fileID = FileData.UNKNOWN_FILE_ID;
		}
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
