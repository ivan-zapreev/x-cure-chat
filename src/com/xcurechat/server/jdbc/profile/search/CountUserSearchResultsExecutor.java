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

import com.xcurechat.client.data.ShortUserData;

import com.xcurechat.client.data.search.OnePageViewData;
import com.xcurechat.client.data.search.UserSearchData;

import com.xcurechat.client.rpc.exceptions.SiteException;

/**
 * @author zapreevis
 * This class is supposed to count the user search results
 */
public class CountUserSearchResultsExecutor extends UserSearchBaseExecutor {
	
	public CountUserSearchResultsExecutor(final int userID, final UserSearchData userSearchQueryData){
		//Initialize the super class
		super(userID, true, userSearchQueryData, 0, 0);
	}

	@Override
	public void processResultSet(ResultSet resultSet, OnePageViewData<ShortUserData> dataObj) throws SQLException, SiteException {
		if( resultSet.first() ){
			dataObj.total_size = resultSet.getInt( 1 );
			logger.debug("Found "+dataObj.total_size+" users that satisfy the select query.");
		} else {
			logger.info("The number of found users is zero.");
			dataObj.total_size = 0;
		}
	}

}
