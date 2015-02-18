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
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ShortUserFileDescriptor;
import com.xcurechat.client.data.search.OnePageViewData;
import com.xcurechat.client.data.search.Top10SearchData;

import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor selects the short file descriptors for the top10 user section
 */
public class SelectTop10FileDescriptorsExecutor extends QueryExecutor<OnePageViewData<ShortUserFileDescriptor>> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( SelectTop10FileDescriptorsExecutor.class );
	
	private final static String PROFILE_FILES_SUB_TABLE = "tmp_" + PROFILE_FILES_TABLE;
	private final static String USERS_SUB_TABLE = "tmp_" + USERS_TABLE;
	private final static String TOTAL_COUNT_SUB_TABLE = "total_count";
	private final static String TOTAL_FIELD_NAME_TOTAL_COUNT_SUB_TABLE = "total";
	
	//THe search request data
	private final Top10SearchData searchData;
	
	public SelectTop10FileDescriptorsExecutor( final Top10SearchData searchData ) throws InternalSiteException{
		//Store the data
		this.searchData = searchData;
		
		//Perform a sanity check
		if( searchData.search_type != Top10SearchData.SearchTypes.TOP_USER_FILES_SEARH_TYPE ) {
			//This should not be happening
			throw new InternalSiteException( InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR );
		}
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT SQL_CALC_FOUND_ROWS " + PROFILE_FILES_SUB_TABLE +
									".*, " + TOTAL_COUNT_SUB_TABLE + ".*, " + USERS_SUB_TABLE + ".* FROM " +
								    "( SELECT " +
								    	OWNER_ID_PROFILE_FILES_TABLE		 + ", " +
										FILE_ID_FIELD_PROFILE_FILES_TABLE 	 + ", " +
										MIME_TYPE_PROFILE_FILES_TABLE 		 + ", " +
										FILE_NAME_PROFILE_FILES_TABLE 		 + ", " +
										IMG_WIDTH_PROFILE_FILES_TABLE 		 + ", " +
										IMG_HEIGHT_PROFILE_FILES_TABLE 		 + ", " +
										UPLOAD_DATE_PROFILE_FILES_TABLE 	 +
									" FROM " + PROFILE_FILES_TABLE + " WHERE TRUE ) AS " + PROFILE_FILES_SUB_TABLE +
								    " LEFT JOIN ( " +
								    				"SELECT FOUND_ROWS() AS " + TOTAL_FIELD_NAME_TOTAL_COUNT_SUB_TABLE +
								    			" ) AS " + TOTAL_COUNT_SUB_TABLE + " ON TRUE " + 
								    " LEFT JOIN ( " +
							    					"SELECT " + UID_FIELD_NAME_USERS_TABLE + ", " +
							    					LOGIN_FIELD_NAME_USERS_TABLE + " FROM " + USERS_TABLE +
							    				" ) AS " + USERS_SUB_TABLE + " ON " +
							    					PROFILE_FILES_SUB_TABLE + "." + OWNER_ID_PROFILE_FILES_TABLE +
							    				"=" +
							    					USERS_SUB_TABLE + "." + UID_FIELD_NAME_USERS_TABLE +
								    " ORDER BY " + PROFILE_FILES_SUB_TABLE + "." + UPLOAD_DATE_PROFILE_FILES_TABLE +
								    " DESC LIMIT ? OFFSET ?";
		logger.debug("The TOP10 user files selector query is: " + selectQuery );
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int index = 1;
		final int entriesPerPage = Math.min( Top10SearchData.MAX_NUMBER_OF_RESULTS_PER_PAGE, OnePageViewData.MAX_NUMBER_OF_ENTRIES_PER_PAGE );
		pstmt.setInt( index++, entriesPerPage );
		pstmt.setInt( index++, entriesPerPage * ( searchData.pageIndex - 1 ) );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, OnePageViewData<ShortUserFileDescriptor> result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	public void processResultSet(ResultSet resultSet, OnePageViewData<ShortUserFileDescriptor> result) throws SQLException, SiteException {
		boolean isFirst = true;
		result.entries = new ArrayList<ShortUserFileDescriptor>();
		//Get the user short data
		while( resultSet.next() ) {
			if( isFirst ) {
				//Get the total number of found entries satisfying the search query
				result.total_size = resultSet.getInt( TOTAL_FIELD_NAME_TOTAL_COUNT_SUB_TABLE );
				isFirst = false;
			}
			
			//Get the sort user data
			ShortUserFileDescriptor fileDescr = new ShortUserFileDescriptor();
			getShortFileDescriptorData( resultSet, fileDescr );
			//Get the owner id
			fileDescr.ownerID = resultSet.getInt( OWNER_ID_PROFILE_FILES_TABLE );
			//Get the owner login name
			fileDescr.ownerLoginName = resultSet.getString( LOGIN_FIELD_NAME_USERS_TABLE );
			//Get the upload date
			fileDescr.uploadDate = getTime( resultSet, UPLOAD_DATE_PROFILE_FILES_TABLE );
			
			result.entries.add( fileDescr );
		}
	}
}
