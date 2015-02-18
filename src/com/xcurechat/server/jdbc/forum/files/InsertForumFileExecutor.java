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

package com.xcurechat.server.jdbc.forum.files;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.xcurechat.client.data.FileData;
import com.xcurechat.client.data.ShortForumMessageData;
import com.xcurechat.client.data.UserFileData;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor class is responsible for executing an insertion
 * of a new forum-message file into the database.
 * NOTE: The messageID is set to the root forum message and is updated
 * after the message to which the file is added is created/updated
 */
public class InsertForumFileExecutor extends QueryExecutor<Void> {
	
	private final UserFileData fileDesc;
	
	public InsertForumFileExecutor( final UserFileData fileDesc ) {
		this.fileDesc = fileDesc;
	}
	
	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		final String insertQuery = "INSERT INTO " + FORUM_FILES_TABLE + " SET " +
									MESSAGE_ID_FIELD_NAME_FORUM_FILES_TABLE + "=?, " +
									OWNER_ID_FIELD_NAME_FORUM_FILES_TABLE + "=?, " +
									MIME_TYPE_FIELD_NAME_FORUM_FILES_TABLE + "=?, " +
									FILE_NAME_FIELD_NAME_FORUM_FILES_TABLE + "=?, " + 
									IMG_WIDTH_PIXELS_NAME_FORUM_FILES_TABLE + "=?, " +
									IMG_HEIGHT_PIXELS_NAME_FORUM_FILES_TABLE + "=?, " +
									( fileDesc.thumbnailData != null ? FILE_THUMBNAIL_FIELD_NAME_FORUM_FILES_TABLE + "=?, " : "" ) + 
									DATA_FIELD_NAME_FORUM_FILES_TABLE + "=?, " + 
									UPLOAD_DATE_FIELD_NAME_FORUM_FILES_TABLE + "=NOW()";
		return connection.prepareStatement( insertQuery, PreparedStatement.RETURN_GENERATED_KEYS );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		//The true message id set after the message is created/updated
		pstmt.setInt( counter++, ShortForumMessageData.ROOT_FORUM_MESSAGE_ID );
		pstmt.setInt( counter++, fileDesc.ownerID );
		pstmt.setString( counter++, fileDesc.mimeType );
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
			fileDesc.fileID = FileData.UNKNOWN_FILE_ID;
		}
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
