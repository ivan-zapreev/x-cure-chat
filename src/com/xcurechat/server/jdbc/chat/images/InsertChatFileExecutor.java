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

package com.xcurechat.server.jdbc.chat.images;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.data.UserFileData;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor class is responsible for executing an insertion
 * of a new chat message image into the database.
 */
public class InsertChatFileExecutor extends QueryExecutor<Void> {
	
	//This parameter is unused, in the future we might
	//have images table for each separate room
	@SuppressWarnings("unused")
	private final int roomID; 
	//The file data to put into the DB
	private final UserFileData fileDesc;
	//The MD5 sum of the file data
	private final String md5sum;
	
	public InsertChatFileExecutor( final int roomID, final UserFileData fileDesc, final String md5sum ) {
		this.roomID = roomID;
		this.fileDesc = fileDesc;
		this.md5sum = md5sum;
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		final String insertQuery = "INSERT INTO " + CHAT_FILES_TABLE + " SET " +
									MD5_SUM_CHAT_FILES_TABLE + "=?, " +
									OWNER_ID_FIELD_NAME_CHAT_FILES_TABLE + "=?, " +
									MIME_TYPE_CHAT_FILES_TABLE + "=?, " +
									FILE_NAME_CHAT_FILES_TABLE + "=?, " +
									IMG_WIDTH_CHAT_FILES_TABLE + "=?, " +
									IMG_HEIGHT_CHAT_FILES_TABLE + "=?, " +
									( fileDesc.thumbnailData != null ? THUMBNAIL_FIELD_NAME_CHAT_FILES_TABLE + "=?, " : "" ) + 
									DATA_FIELD_NAME_CHAT_FILES_TABLE + "=?, " + 
									UPLOAD_DATE_FIELD_NAME_CHAT_FILES_TABLE + "=NOW()";
		return connection.prepareStatement( insertQuery, PreparedStatement.RETURN_GENERATED_KEYS );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setString( counter++, md5sum );
		pstmt.setInt( counter++, fileDesc.ownerID );
		pstmt.setString( counter++, fileDesc.mimeType);
		pstmt.setString( counter++, fileDesc.fileName );
		pstmt.setInt( counter++, fileDesc.widthPixels );
		pstmt.setInt( counter++, fileDesc.heightPixels );
		if( fileDesc.thumbnailData != null ) {
			pstmt.setBytes( counter++, fileDesc.thumbnailData );
		}
		pstmt.setBytes( counter++, fileDesc.fileData );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		pstmt.executeUpdate();
		//Get back the id of the inserted file  
		ResultSet resultSet = pstmt.getGeneratedKeys();
		if ( resultSet != null && resultSet.next() ) {
			fileDesc.fileID = resultSet.getInt(1); 
		} else {
			fileDesc.fileID = ShortFileDescriptor.UNKNOWN_FILE_ID;
		}
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
