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

import com.xcurechat.client.data.UserFileData;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor allows to retrieve the user profile avatar image for the given user id
 */
public class SelectProfileAvatarExecutor extends QueryExecutor<UserFileData> {
	
	private final int userID;
	
	public SelectProfileAvatarExecutor( final int userID ){
		this.userID = userID;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT "+ IMAGE_FIELD_AVATAR_IMAGES_TABLE + ", " +
									MIME_TYPE_AVATAR_IMAGES_TABLE +
									" FROM " + AVATAR_IMAGES_TABLE + " WHERE " +
									USER_ID_AVATAR_IMAGES_TABLE + "=?";
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		pstmt.setInt( 1, userID );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, UserFileData result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	public void processResultSet(ResultSet resultSet, UserFileData fileData) throws SQLException, SiteException {
		//Check if there is required image for the given user
		fileData.ownerID = userID;
		if( resultSet.first() ){
			fileData.fileData = resultSet.getBytes( IMAGE_FIELD_AVATAR_IMAGES_TABLE );
			fileData.mimeType = resultSet.getString( MIME_TYPE_AVATAR_IMAGES_TABLE );
		} else {
			//There are no rows in the result set, there is not required image for the given user
			fileData.fileData = null;
		}
	}
}
