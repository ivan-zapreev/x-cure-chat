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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.search.OnePageViewData;
import com.xcurechat.client.data.search.UserSearchData;

import com.xcurechat.client.rpc.exceptions.SiteException;

/**
 * @author zapreevis
 * This class is supposed to allow browsing the user search results
 */
public class BrowseUserSearchResultsExecutor extends UserSearchBaseExecutor {
	
	public BrowseUserSearchResultsExecutor(final int userID, final UserSearchData userSearchQueryData, final int offset, final int size ){
		//Call the super class constructor
		super(userID, false, userSearchQueryData, offset, size );
	}

	@Override
	public void processResultSet(ResultSet resultSet, OnePageViewData<ShortUserData> dataObj) throws SQLException, SiteException {
		ArrayList<ShortUserData> list = new ArrayList<ShortUserData>();
		while( resultSet.next() ){
			list.add( getShortUserData( resultSet, FORUM_MSGS_COUNT_FIELD_NAME_USERS_TABLE , true ) );
		}
		
		if( ! list.isEmpty() ){
			dataObj.entries = list;
			logger.debug( "Browsing the found users resulted in " + (dataObj.entries != null ? ""+dataObj.entries.size() : "zero")+" entries." );
		} else {
			logger.info("There were no users found.");
			dataObj.entries = null;
		}
	}

}
