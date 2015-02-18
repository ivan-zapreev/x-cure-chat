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
 * The search related package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.data.search;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author zapreevis
 * Contains the data needed to perform the top 10 section search
 */
public class Top10SearchData implements IsSerializable {
	//The maximum allowed number of search results per page
	public static final int MAX_NUMBER_OF_RESULTS_PER_PAGE = 10;
	//The minimum page index
	public static final int MINIMUM_PAGE_INDEX = 1;
	
	//Defines the set of possible search types
	public enum SearchTypes {
		TOP_UNKNOWN_SEARH_TYPE,				//The undefined search type
		TOP_MONEY_SEARH_TYPE,				//Who has more money
		TOP_FORUM_POSTS_SEARH_TYPE,			//Who posted more forum messages
		TOP_CHAT_MESSAGES_SEARH_TYPE,		//Who sent more chat messages
		TOP_TIME_ON_SITE_SEARH_TYPE,		//Who spent more time on site
		TOP_USER_FILES_SEARH_TYPE,			//What are the latest profile files (images/videos/music)
		TOP_REGISTRATIONS_SEARH_TYPE,		//We are looking for the times of user registrations
		TOP_USER_VISITS_SEARCH_TYPE			//The search for the users' last visits
	};
	
	//The search page that we are going to look at
	public int pageIndex = MINIMUM_PAGE_INDEX;
	//The type of the search we are going to perform
	public SearchTypes search_type = SearchTypes.TOP_UNKNOWN_SEARH_TYPE;
	
	/**
	 * The default constructor
	 */
	public Top10SearchData() {
	}
	
	/**
	 * The basic constructor for the search
	 */
	public Top10SearchData(final int pageIndex, final SearchTypes search_type) {
		this.pageIndex = pageIndex;
		this.search_type = search_type;
	}

}
