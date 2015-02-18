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
 * The server side (RPC, servlet) access package.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;

import com.xcurechat.client.data.UserStatsEntryData;
import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.rpc.exceptions.SiteException;

/**
 * @author zapreevis
 * The RPC interface that allows users to browse
 * and clean their login, logout statistics.
 */
public interface UserStatistics extends RemoteService {

	/**
	 * This method allows count the number of statistical entries for the given user
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public int count( final int userID, final String userSessionId ) throws SiteException;

	/**
	 * This method allows to browse user site login/logout statistics
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param offset the offset for the first statistical entry
	 * @param size the max number of entries to retrieve 
	 * @return an object containing requested statistical data
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public OnePageViewData<UserStatsEntryData> browse( final int userID, final String userSessionId,
														final int offset, final int size ) throws SiteException;
	
	/**
	 * This method allows to delete all user site login/logout statistics
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param password is needed for deleting statistics
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void delete( final int userID, final String userSessionId,
						final String password ) throws SiteException;
	
}
