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
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Date;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.rpc.exceptions.IncorrectUserDataException;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;
import com.xcurechat.server.utils.LoginUnifier;

/**
 * @author zapreevis
 * This executor class is responsible for registering the new used in the database
 */
public class InsertNewUserExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( InsertNewUserExecutor.class );
	
	private final MainUserData userData;
	private final String passwordHash;
	private final String unifiedLogin;
	
	public InsertNewUserExecutor( final MainUserData userData, final String userPassword ){
		this.userData = userData;
		this.unifiedLogin = LoginUnifier.getUnifiedLogin( userData.getUserLoginName() );
		this.passwordHash = hashPassword( userPassword );
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		final String insertQuery = "INSERT INTO " + USERS_TABLE +
									" SET " + LOGIN_FIELD_NAME_USERS_TABLE + " = ?, " + 
									UNI_LOGIN_FIELD_NAME_USERS_TABLE + " = ?, " +
									PASSWORD_HASH_FIELD_NAME_USERS_TABLE + " = ?, " + 
									GENDER_FIELD_NAME_USERS_TABLE + "=?, " +
									AGE_FIELD_NAME_USERS_TABLE + "=?, " +
									FIRST_NAME_FIELD_NAME_USERS_TABLE + "=?, " +
									LAST_NAME_FIELD_NAME_USERS_TABLE +"=?, "+
									COUNTRY_FIELD_NAME_USERS_TABLE + "=?, "+
									CITY_FIELD_NAME_USERS_TABLE + "=?, " +
									ABOUT_ME_FIELD_NAME_USERS_TABLE + "=?, " +
									REG_DATE_FIELD_NAME_USERS_TABLE + "=?, " + 
									LAST_ONLINE_FIELD_NAME_USERS_TABLE + "=?" ;
		return connection.prepareStatement( insertQuery, PreparedStatement.RETURN_GENERATED_KEYS );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setString( counter++, userData.getUserLoginName() );
		pstmt.setString( counter++, unifiedLogin );
		pstmt.setString( counter++, passwordHash );
		pstmt.setBoolean( counter++, userData.isMale() );
		pstmt.setInt( counter++, userData.getUserAge() );
		pstmt.setString( counter++, userData.getFirstName() );
		pstmt.setString( counter++, userData.getLastName() );
		pstmt.setString( counter++, userData.getCountryName() );
		pstmt.setString( counter++, userData.getCityName() );
		pstmt.setString( counter++, userData.getAboutMe() );
		setTime( pstmt, counter++, userData.getUserRegistrationDate() );
		setTime( pstmt, counter++, userData.getUserLastOnlineDate() );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		try{
			//Execute the insert
			pstmt.executeUpdate();
			//Get back the id of the inserted row  
			ResultSet resultSet = pstmt.getGeneratedKeys();
			if ( resultSet != null && resultSet.next() ) {
				//Store the user ID
			    userData.setUID( resultSet.getInt(1) ); 
			}
			//Set the user's last online and registration date
			userData.setUserRegistrationDate( new Date() );
		    userData.setUserLastOnlineDate( new Date() );
		}catch( SQLException e ){
			if( e instanceof SQLIntegrityConstraintViolationException ){
				logger.warn( "Trying to register a new user '" + userData.getUserLoginName() +
							"' but the login name or a similar (" + unifiedLogin + ") already exists, registration is skipped.");
				//It is likely to be the case that the user login already exists.
				//There is only one other field that has unique value but it is
				//an auto increment field, so nothing should go wrong there.
				throw new IncorrectUserDataException( IncorrectUserDataException.LOGIN_NAME_IN_USE_ERR );
			} else {
				logger.error( "An unhandled exception when adding a new user to the database." , e);
				throw e;
			}
		}
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
