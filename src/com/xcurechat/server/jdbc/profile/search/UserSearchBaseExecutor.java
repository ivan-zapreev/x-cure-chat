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
package com.xcurechat.server.jdbc.profile.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.data.search.OnePageViewData;
import com.xcurechat.client.data.search.UserSearchData;
import com.xcurechat.client.data.search.NonRoomUserSearchData;
import com.xcurechat.client.data.search.MessageRecepientSearchData;

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This is the base class for all user search queries.
 * It is responsible for creating the search query and
 * binding the search arguments.
 */
public abstract class UserSearchBaseExecutor extends QueryExecutor<OnePageViewData<ShortUserData>> {
	//Get the Log4j logger object
	protected static final Logger logger = Logger.getLogger( UserSearchBaseExecutor.class );
	
	//The id of the user that performs the data search, this is needed for searching friends
	protected final int userID;
	//The user search query object
	protected final UserSearchData userSearchQueryData;
	//The indicator that says whether we need to count users (true) or browse them (false)
	protected final boolean isCount;
	//THe offset and the size for the browsing query
	private final int offset;
	private final int size;
	
	/**
	 * This is the main constructor
	 * @param userID is the ID of the user making search, it is only needed for searching user's friends
	 * @param isCount true if we need to count users, if we need to browse, then false
	 * @param userSearchQueryData the user search query data object
	 * @param offset the offset, from which to start retrieving entries (is needed when isCount == false)
	 * @param size the number of entries to retrieve (is needed when isCount == false)
	 */
	public UserSearchBaseExecutor( final int userID, final boolean isCount, final UserSearchData userSearchQueryData,
									final int offset, final int size ){
		//Store the user ID
		this.userID = userID;
		//Make sure that the search query is not too long, if it is then truncate.
		//This is just a safety check since the client should not allow sending long queries.
		if( ( userSearchQueryData.searchString != null ) &&
			( userSearchQueryData.searchString.length() > UserSearchData.MAX_SEARCH_STRING_LENGTH ) ) {
			userSearchQueryData.searchString = userSearchQueryData.searchString.substring(0, UserSearchData.MAX_SEARCH_STRING_LENGTH);
		} 
		//Store the search query object
		this.userSearchQueryData = userSearchQueryData;
		//Store the count or not indicator
		this.isCount = isCount;
		//Store the offset and the size
		this.offset = offset;
		this.size = size;
	}

	/**
	 * This method helps to build up the selector part of the query.
	 * Here we add the "LIKE" query selector.
	 * @param currSelectorQuery the current selector part of the query
	 * @param fieldName the field we want to add search on
	 * @param isOr if true then we add " OR fieldName=?" otherwise " AND fieldName=?"
	 * @param isExact true for using "=?" , false for using "LIKE ?"  
	 * @return the updated currSelectorQuery, i.e. with an appended selector 
	 */
	private String addSelectorToQuery( String currSelectorQuery, final String fieldName,
										final boolean isOr, final boolean isExact) {
		String newCurrSelectorQuery = currSelectorQuery;
		if( !currSelectorQuery.isEmpty() ) {
			if( isOr ) {
				newCurrSelectorQuery += " OR ";
			} else {
				newCurrSelectorQuery += " AND ";
			}
		}
		if( isExact ) {
			newCurrSelectorQuery += fieldName+" =?";
		} else {
			newCurrSelectorQuery += fieldName+" LIKE ?";
		}
		return newCurrSelectorQuery;
	}
	
	@Override
	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		String query = "SELECT ";
		
		if( isCount ) {
			query += "COUNT(" + UID_FIELD_NAME_USERS_TABLE + ")";
		} else {
			query += UID_FIELD_NAME_USERS_TABLE + ", " +
					 LOGIN_FIELD_NAME_USERS_TABLE + ", " +
					 GENDER_FIELD_NAME_USERS_TABLE + ", " +
					 IS_ONLINE_FIELD_NAME_USERS_TABLE + ", " +
				     REG_DATE_FIELD_NAME_USERS_TABLE + ", " +
					 LAST_ONLINE_FIELD_NAME_USERS_TABLE + ", " +
					 FORUM_MSGS_COUNT_FIELD_NAME_USERS_TABLE + ", " + 
					 CHAT_MSGS_COUNT_FIELD_NAME_USERS_TABLE + ", " + 
					 TIME_ONLINE_FIELD_NAME_USERS_TABLE + ", " + 
					 SPOILER_ID_FIELD_NAME_USERS_TABLE + ", " + 
					 GOLD_PIECES_FIELD_NAME_USERS_TABLE + ", " + 
					 SPOILER_EXP_DATE_FIELD_NAME_USERS_TABLE;
		}
		query += " FROM " + USERS_TABLE + " WHERE ";
		
		//Add the selector part of the query
		String selectorPart = "";
		//First add the selectors for the login, first name, last name, city, country
		if( userSearchQueryData.isLogin ) {
			selectorPart = addSelectorToQuery( selectorPart, LOGIN_FIELD_NAME_USERS_TABLE, true, false );
		}
		if( userSearchQueryData.isFirstName ) {
			selectorPart = addSelectorToQuery( selectorPart, FIRST_NAME_FIELD_NAME_USERS_TABLE, true, false );
		}
		if( userSearchQueryData.isLastName ) {
			selectorPart = addSelectorToQuery( selectorPart, LAST_NAME_FIELD_NAME_USERS_TABLE, true, false );
		}
		if( userSearchQueryData.isCity ) {
			selectorPart = addSelectorToQuery( selectorPart, CITY_FIELD_NAME_USERS_TABLE, true, false );
		}
		if( userSearchQueryData.isCountry ) {
			selectorPart = addSelectorToQuery( selectorPart, COUNTRY_FIELD_NAME_USERS_TABLE, true, false );
		}
		if( userSearchQueryData.isAboutMyself ) {
			selectorPart = addSelectorToQuery( selectorPart, ABOUT_ME_FIELD_NAME_USERS_TABLE, true, false );
		}
		//Add the bracket for the set of OR selectors or reset the selector query
		if( selectorPart.isEmpty() ){
			selectorPart = "";
		} else {
			selectorPart = "(" +selectorPart+ ")";
		}
		
		//Then add the selectors for the age and gender
		if( userSearchQueryData.userAgeIntervalID != UserData.AGE_UNKNOWN ){
			selectorPart = addSelectorToQuery( selectorPart, AGE_FIELD_NAME_USERS_TABLE, false, true );
		}
		if( userSearchQueryData.userGender != UserSearchData.USER_GENDER_UNKNOWN ){
			selectorPart = addSelectorToQuery( selectorPart, GENDER_FIELD_NAME_USERS_TABLE, false, true );
		}
		//If we are only looking for online users 
		if( userSearchQueryData.isOnline ) {
			selectorPart = addSelectorToQuery( selectorPart, IS_ONLINE_FIELD_NAME_USERS_TABLE, false, true );
		}
		
		//Exclude the deleted user profile from the search results
		selectorPart += ( !selectorPart.isEmpty() ? " AND " : "") + TYPE_FIELD_NAME_USERS_TABLE + " != " + UserData.DELETED_USER_TYPE;
		
		//If we are looking for friends only 
		if( userSearchQueryData.isFriend ) {
			selectorPart += ( !selectorPart.isEmpty() ? " AND " : "") +  UID_FIELD_NAME_USERS_TABLE + " IN ( SELECT "+ TO_UID_FIELD_NAME_FRIENDS_TABLE +
							" FROM " + FRIENDS_TABLE + " WHERE " + FROM_UID_FIELD_NAME_FRIENDS_TABLE + "=? ) "; 
		}
		
		//If we are looking for people with pictures only
		if( userSearchQueryData.hasPictures ) {
			selectorPart += ( !selectorPart.isEmpty() ? " AND " : "") + UID_FIELD_NAME_USERS_TABLE + " IN ( SELECT "+ OWNER_ID_PROFILE_FILES_TABLE + 
							" FROM " + PROFILE_FILES_TABLE + ") ";
		}
		
		//Add extra selectors if we deal with a subclass of UserSearchData
		if( userSearchQueryData instanceof NonRoomUserSearchData ) {
			//Depending on the user type we get either all room access entries or just regular ones
			selectorPart += ( !selectorPart.isEmpty() ? " AND " : "") + "( "+ UID_FIELD_NAME_USERS_TABLE + " NOT IN " ;
			selectorPart += "( SELECT " + UID_FIELD_NAME_ROOM_ACCESS_TABLE + " FROM " + 
								ROOM_ACCESS_TABLE + " WHERE " + RID_FIELD_NAME_ROOM_ACCESS_TABLE + "=? AND " +
								IS_SYSTEM_FIELD_NAME_ROOM_ACCESS_TABLE + "=false )";
			if( userSearchQueryData.isAdmin ) {
				selectorPart += " OR "+ UID_FIELD_NAME_USERS_TABLE + " NOT IN "; 
				selectorPart += "( SELECT " + UID_FIELD_NAME_ROOM_ACCESS_TABLE + " FROM " + 
									ROOM_ACCESS_TABLE + " WHERE " + RID_FIELD_NAME_ROOM_ACCESS_TABLE + "=? AND " +
									IS_SYSTEM_FIELD_NAME_ROOM_ACCESS_TABLE + "=true )";
			}
			selectorPart += " )";
			//If this is not an admin who does the search then he should not 
			//be able to see himself, since he can access the room as its owner
			if( ! userSearchQueryData.isAdmin ) {
				selectorPart += " AND " + UID_FIELD_NAME_USERS_TABLE + " != ? ";
			}
		} else {
			if ( userSearchQueryData instanceof MessageRecepientSearchData ) {
				selectorPart += ( !selectorPart.isEmpty() ? " AND " : "") + UID_FIELD_NAME_USERS_TABLE + " != ? ";
			}
		}
		
		//Check if the selector is empty or not (contains only '()'), if yes then make it select all
		if( selectorPart.length() == 2 ){
			query += "true"; 
		} else {
			query += selectorPart; 
		}
		
		//Add the offset and the selection size if it is not a counting query
		if( !isCount ) {
			//Order by the last online date in the descending order
			query += " ORDER BY " + LAST_ONLINE_FIELD_NAME_USERS_TABLE + " DESC LIMIT ? OFFSET ?";
		}
		
		//Create the prepared statement
		logger.debug("The user search "+(isCount?"count":"browse")+" query is '"+query+"'");
		return connection.prepareStatement( query );
	}

	@Override
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int index = 1;
		//First add bind the strings for login, first name, last name, city, country
		final String query = "%"+userSearchQueryData.searchString+"%"; 
		if( userSearchQueryData.isLogin ) {
			pstmt.setString( index++, query );
		}
		if( userSearchQueryData.isFirstName ) {
			pstmt.setString( index++, query );
		}
		if( userSearchQueryData.isLastName ) {
			pstmt.setString( index++, query );
		}
		if( userSearchQueryData.isCity ) {
			pstmt.setString( index++, query );
		}
		if( userSearchQueryData.isCountry ) {
			pstmt.setString( index++, query );
		}
		if( userSearchQueryData.isAboutMyself ) {
			pstmt.setString( index++, query );
		}
		
		//Then bind the selectors for the age and gender
		if( userSearchQueryData.userAgeIntervalID != UserData.AGE_UNKNOWN ){
			pstmt.setInt( index++, userSearchQueryData.userAgeIntervalID );
		}
		if( userSearchQueryData.userGender != UserSearchData.USER_GENDER_UNKNOWN ){
			pstmt.setBoolean( index++, (userSearchQueryData.userGender == UserSearchData.USER_GENDER_MALE) );
		}
		//If we are looking for online users only
		if( userSearchQueryData.isOnline ) {
			pstmt.setBoolean( index++, true );
		}
		//If we are searching for user's friends
		if( userSearchQueryData.isFriend ) {
			pstmt.setInt( index++, userID );
		}
		
		//Add extra selectors if we deal with a subclass of UserSearchData
		if( userSearchQueryData instanceof NonRoomUserSearchData ) {
			pstmt.setInt( index++, ((NonRoomUserSearchData)userSearchQueryData).roomID );
			if( userSearchQueryData.isAdmin ) {
				pstmt.setInt( index++, ((NonRoomUserSearchData)userSearchQueryData).roomID );
			}
			if( ! userSearchQueryData.isAdmin ) {
				pstmt.setInt( index++, userID );
			}
		} else {
			if ( userSearchQueryData instanceof MessageRecepientSearchData ) {
				pstmt.setInt( index++, ((MessageRecepientSearchData)userSearchQueryData).searchedByUserID );
			}
		}
		
		//Add the offset and the selection size if it is not a counting query
		if( !isCount ) {
			pstmt.setInt( index++, size );
			pstmt.setInt( index++, offset );
		}		
	}

	@Override
	public ResultSet executeQuery(PreparedStatement pstmt, OnePageViewData<ShortUserData> result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}
}
