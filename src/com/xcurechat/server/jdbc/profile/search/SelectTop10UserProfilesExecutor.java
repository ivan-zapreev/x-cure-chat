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
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.server.jdbc.profile.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.data.search.OnePageViewData;
import com.xcurechat.client.data.search.Top10SearchData;
import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor allow to perform TOP10 section search or the users
 */
public class SelectTop10UserProfilesExecutor extends QueryExecutor<OnePageViewData<ShortUserData>> {
	private final static String USER_DATA_SUB_TABLE = "tmp_" + USERS_TABLE;
	private final static String TOTAL_COUNT_SUB_TABLE = "total_count";
	private final static String TOTAL_FIELD_NAME_TOTAL_COUNT_SUB_TABLE = "total";
	
	//The search data
	private final Top10SearchData searchData;
	//The columns we are going to order by
	private final String orderByString;
	
	public SelectTop10UserProfilesExecutor( final Top10SearchData searchData ) throws InternalSiteException{
		this.searchData = searchData;
		switch( searchData.search_type ) {
			case TOP_CHAT_MESSAGES_SEARH_TYPE:
				orderByString = CHAT_MSGS_COUNT_FIELD_NAME_USERS_TABLE + " DESC";
				break;
			case TOP_FORUM_POSTS_SEARH_TYPE:
				orderByString = FORUM_MSGS_COUNT_FIELD_NAME_USERS_TABLE + " DESC";
				break;
			case TOP_MONEY_SEARH_TYPE:
				orderByString = GOLD_PIECES_FIELD_NAME_USERS_TABLE + " DESC";
				break;
			case TOP_TIME_ON_SITE_SEARH_TYPE:
				orderByString = TIME_ONLINE_FIELD_NAME_USERS_TABLE + " DESC";
				break;
			case TOP_REGISTRATIONS_SEARH_TYPE:
				orderByString = REG_DATE_FIELD_NAME_USERS_TABLE + " DESC";
				break;
			case TOP_USER_VISITS_SEARCH_TYPE:
				orderByString = IS_ONLINE_FIELD_NAME_USERS_TABLE + " DESC" + ", " +
								LAST_ONLINE_FIELD_NAME_USERS_TABLE + " DESC" ;
				break;
			default:
				//This should not be happening
				throw new InternalSiteException( InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR );
		}
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT SQL_CALC_FOUND_ROWS " + USER_DATA_SUB_TABLE +
								    ".*, " + TOTAL_COUNT_SUB_TABLE + ".* FROM " +
								    "( SELECT " +
								    	UID_FIELD_NAME_USERS_TABLE + ", " +
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
								    	SPOILER_EXP_DATE_FIELD_NAME_USERS_TABLE + 
								    	" FROM " + USERS_TABLE + " WHERE " +
								    	TYPE_FIELD_NAME_USERS_TABLE + " != " + UserData.DELETED_USER_TYPE + 
								    " ) AS " + USER_DATA_SUB_TABLE +
								    " LEFT JOIN (SELECT FOUND_ROWS() AS " + TOTAL_FIELD_NAME_TOTAL_COUNT_SUB_TABLE + ") AS " + TOTAL_COUNT_SUB_TABLE +
								    " ON TRUE ORDER BY " + orderByString + " LIMIT ? OFFSET ?";
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int index = 1;
		final int entriesPerPage = Math.min( Top10SearchData.MAX_NUMBER_OF_RESULTS_PER_PAGE, OnePageViewData.MAX_NUMBER_OF_ENTRIES_PER_PAGE );
		pstmt.setInt( index++, entriesPerPage );
		pstmt.setInt( index++, entriesPerPage * ( searchData.pageIndex - 1 ) );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, OnePageViewData<ShortUserData> result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	public void processResultSet(ResultSet resultSet, OnePageViewData<ShortUserData> result) throws SQLException, SiteException {
		boolean isFirst = true;
		result.entries = new ArrayList<ShortUserData>();
		//Get the user short data
		while( resultSet.next() ) {
			if( isFirst ) {
				//Get the total number of found entries satisfying the search query
				result.total_size = resultSet.getInt( TOTAL_FIELD_NAME_TOTAL_COUNT_SUB_TABLE );
				isFirst = false;
			}
			//Get the sort user data
			result.entries.add( getShortUserData( resultSet, FORUM_MSGS_COUNT_FIELD_NAME_USERS_TABLE , true ) );
		}
	}
}
