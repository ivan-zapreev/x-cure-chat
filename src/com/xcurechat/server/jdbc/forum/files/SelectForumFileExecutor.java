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

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;
import com.xcurechat.client.data.MessageFileData;
import com.xcurechat.client.data.ShortFileDescriptor;

/**
 * @author zapreevis
 * Allows to retrieve forum file by is ID
 */
public class SelectForumFileExecutor extends QueryExecutor<MessageFileData> {
	
	//The id of the file that we want to retrieve
	private final int fileID;

	/**
	 * Basic constructor
	 * @param fileID the id of the file that we want to retrieve
	 */
	public SelectForumFileExecutor( final int fileID ){
		this.fileID = fileID;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT "+ FILE_THUMBNAIL_FIELD_NAME_FORUM_FILES_TABLE + ", " +
											  DATA_FIELD_NAME_FORUM_FILES_TABLE + ", " +
											  MIME_TYPE_FIELD_NAME_FORUM_FILES_TABLE + ", " +
											  FILE_NAME_FIELD_NAME_FORUM_FILES_TABLE +
									" FROM " + FORUM_FILES_TABLE + " WHERE " +
									FILE_ID_FIELD_NAME_FORUM_FILES_TABLE + "=?";
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		pstmt.setInt(counter++, fileID);
	}

	public ResultSet executeQuery(PreparedStatement pstmt, MessageFileData result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	public void processResultSet(ResultSet resultSet, MessageFileData fileData) throws SQLException, SiteException {
		//Check if there is required image for the given user
		if( resultSet.first() ){
			fileData.fileID = fileID;
			fileData.mimeType = resultSet.getString( MIME_TYPE_FIELD_NAME_FORUM_FILES_TABLE );
			fileData.fileName = resultSet.getString( FILE_NAME_FIELD_NAME_FORUM_FILES_TABLE );
			fileData.thumbnailData = resultSet.getBytes( FILE_THUMBNAIL_FIELD_NAME_FORUM_FILES_TABLE );
			fileData.fileData = resultSet.getBytes( DATA_FIELD_NAME_FORUM_FILES_TABLE );
		} else {
			//There are no rows in the result set, the required file was not found
			fileData.thumbnailData = null;
			fileData.fileData = null;
			fileData.fileID = ShortFileDescriptor.UNKNOWN_FILE_ID;
		}
	}
}
