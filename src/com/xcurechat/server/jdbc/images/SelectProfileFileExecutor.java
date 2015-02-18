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

import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.data.UserFileData;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor allows to retrieve the user profile-file data by the user and file id
 */
public class SelectProfileFileExecutor extends QueryExecutor<UserFileData> {
	
	private final int userID;
	private final int fileID;
	
	public SelectProfileFileExecutor( final int userID, final int fileID ){
		this.userID = userID;
		this.fileID = fileID;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT * FROM " + PROFILE_FILES_TABLE + " WHERE " +
									OWNER_ID_PROFILE_FILES_TABLE + "=? AND " +
									FILE_ID_FIELD_PROFILE_FILES_TABLE + "=?";
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		pstmt.setInt( counter++, userID );
		pstmt.setInt( counter++, fileID);
	}

	public ResultSet executeQuery(PreparedStatement pstmt, UserFileData result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	public void processResultSet(ResultSet resultSet, UserFileData fileData) throws SQLException, SiteException {
		//Check if there is required image for the given user
		if( resultSet.first() ){
			fileData.fileID = fileID;
			fileData.ownerID = userID;
			fileData.mimeType = resultSet.getString( MIME_TYPE_PROFILE_FILES_TABLE );
			fileData.fileName = resultSet.getString( FILE_NAME_PROFILE_FILES_TABLE );
			fileData.thumbnailData = resultSet.getBytes( THUMBNAIL_FIELD_PROFILE_FILES_TABLE );
			fileData.fileData = resultSet.getBytes( DATA_FIELD_PROFILE_FILES_TABLE );
			fileData.widthPixels = resultSet.getInt( IMG_WIDTH_PROFILE_FILES_TABLE );
			fileData.heightPixels = resultSet.getInt( IMG_HEIGHT_PROFILE_FILES_TABLE );
		} else {
			//There are no rows in the result set, there is not required image for the given user
			fileData.thumbnailData = null;
			fileData.fileData = null;
			fileData.fileID = ShortFileDescriptor.UNKNOWN_FILE_ID;
		}
	}
}
