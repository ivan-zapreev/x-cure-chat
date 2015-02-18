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

package com.xcurechat.server.jdbc.profile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor class is responsible for updating user profile in the database.
 * In this case we only save the user-managed fiels of the profile.
 */
public class UpdateUserProfileExecutor extends QueryExecutor<Void> {
	
	private final MainUserData userData;
	private final String newPasswordHash;
	
	public UpdateUserProfileExecutor( final MainUserData userData, final String newUserPassword ){
		this.userData = userData;
		
		//Generate a new password hash
		if( ( newUserPassword != null ) && !newUserPassword.isEmpty() ) {
			this.newPasswordHash = hashPassword( newUserPassword );
		} else {
			this.newPasswordHash = null;
		}
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		String updateQuery = "UPDATE " + USERS_TABLE + " SET ";
		if( newPasswordHash != null ){
			updateQuery	+=  PASSWORD_HASH_FIELD_NAME_USERS_TABLE + " = ?, ";
		}
		updateQuery	+=  GENDER_FIELD_NAME_USERS_TABLE + "=?, " +
						AGE_FIELD_NAME_USERS_TABLE + "=?, " +
						FIRST_NAME_FIELD_NAME_USERS_TABLE + "=?, " +
						LAST_NAME_FIELD_NAME_USERS_TABLE +"=?, "+
						COUNTRY_FIELD_NAME_USERS_TABLE + "=?, "+
						CITY_FIELD_NAME_USERS_TABLE + "=?, " +
						ABOUT_ME_FIELD_NAME_USERS_TABLE + "=? " +
						"WHERE "+ UID_FIELD_NAME_USERS_TABLE + "=?";
		
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		if( newPasswordHash != null ){
			pstmt.setString( counter++, newPasswordHash );
		}
		pstmt.setBoolean( counter++, userData.isMale() );
		pstmt.setInt( counter++, userData.getUserAge() );
		pstmt.setString( counter++, userData.getFirstName() );
		pstmt.setString( counter++, userData.getLastName() );
		pstmt.setString( counter++, userData.getCountryName() );
		pstmt.setString( counter++, userData.getCityName() );
		pstmt.setString( counter++, userData.getAboutMe() );
		pstmt.setInt( counter++, userData.getUID() );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		pstmt.executeUpdate();
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
