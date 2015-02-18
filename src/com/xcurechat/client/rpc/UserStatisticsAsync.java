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
 * The user interface package.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.xcurechat.client.data.UserStatsEntryData;
import com.xcurechat.client.data.search.OnePageViewData;

/**
 * @author zapreevis
 * The RPC interface that allows users to browse
 * and clean their login, logout statistics.
 */
public interface UserStatisticsAsync {

	/**
	 * This method allows count the number of statistical entries for the given user
	 */
	public void count( final int userID, final String userSessionId, AsyncCallback<Integer> callback );

	/**
	 * This method allows to browse user site login/logout statistics
	 */
	public void browse( final int userID, final String userSessionId,
						final int offset, final int size,
						AsyncCallback<OnePageViewData<UserStatsEntryData>> callback );
	
	/**
	 * This method allows to delete all user site login/logout statistics
	 */
	public void delete( final int userID, final String userSessionId,
						final String password, AsyncCallback<Void> callback );

}
