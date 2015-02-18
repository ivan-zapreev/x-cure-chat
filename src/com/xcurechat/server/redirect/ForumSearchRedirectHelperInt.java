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
 * The server-side RPC package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.server.redirect;

import javax.servlet.http.HttpServletRequest;

import com.xcurechat.client.data.search.ForumSearchData;

/**
 * @author zapreevis
 * Contains several method declarations specific for the Forum and the News site sections
 */
public abstract class ForumSearchRedirectHelperInt extends SectionRedirectHelperInt {
	/**
	 * Gets the list of parameters and reconstructs the parameter string using them
	 * @param req the http request to get the site url base from
	 * @return the request parameters string
	 */
	public abstract String getParametersString( final HttpServletRequest req );
	
	/**
	 * Gets the search data object for the forum section based on the request data 
	 * @param req the http request to get the site url base from
	 * @return the forum search data object constructed from the request
	 */
	public abstract ForumSearchData getSearchDataObject( final HttpServletRequest req );
}
