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
 * This executor class is responsible for updating the user avatar in the database.
 */
public class UpdateProfileAvatarExecutor extends QueryExecutor<Void> {
	
	private final UserFileData fileData;
	
	public UpdateProfileAvatarExecutor( UserFileData fileData ){
		this.fileData = fileData;
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		String updateQuery = "UPDATE " +  AVATAR_IMAGES_TABLE + " SET " +
							IMAGE_FIELD_AVATAR_IMAGES_TABLE + "=?, " +
							MIME_TYPE_AVATAR_IMAGES_TABLE + "=? " +
							"WHERE "+ USER_ID_AVATAR_IMAGES_TABLE + "=?";

		
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setBytes( counter++, fileData.fileData );
		pstmt.setString( counter++, fileData.mimeType );
		pstmt.setInt( counter++, fileData.ownerID );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		pstmt.executeUpdate();
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
