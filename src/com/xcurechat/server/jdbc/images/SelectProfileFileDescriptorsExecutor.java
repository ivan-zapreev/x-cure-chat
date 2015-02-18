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
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor selects the short file descriptors for the files uploaded to the given user profile.
 * The actual file's data is not retrieved, just the info
 */
public class SelectProfileFileDescriptorsExecutor extends QueryExecutor<Void> {
	
	final UserData userData;
	
	public SelectProfileFileDescriptorsExecutor( final UserData userData ){
		this.userData = userData;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT "+
									FILE_ID_FIELD_PROFILE_FILES_TABLE + ", " +
									MIME_TYPE_PROFILE_FILES_TABLE + ", " +
									FILE_NAME_PROFILE_FILES_TABLE + ", " +
									IMG_WIDTH_PROFILE_FILES_TABLE + ", " +
									IMG_HEIGHT_PROFILE_FILES_TABLE +
									" FROM " + PROFILE_FILES_TABLE + " WHERE " +
									OWNER_ID_PROFILE_FILES_TABLE + "=? " +
									"ORDER BY " + UPLOAD_DATE_PROFILE_FILES_TABLE + " ASC";
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int index = 1;
		pstmt.setInt( index++, userData.getUID() );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//Get the user profile image files descriptors
		while( resultSet.next() ) {
			userData.addUserProfileFileDescr( getShortFileDescriptorData( resultSet, new ShortFileDescriptor() ) );
		}
	}
}
