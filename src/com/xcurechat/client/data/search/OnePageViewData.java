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
 * The user data objects package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.data.search;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author zapreevis
 * This object is supposed to carry data for the server
 * to be shown at just one page view. Using this object
 * we can make a page by page browsing of the server data.
 */
public class OnePageViewData<Type> implements IsSerializable {
	
	//The maximum number of entries we will be displaying, ever, for any kind of data
	public static final int MAX_NUMBER_OF_ENTRIES_PER_PAGE = 40;
	
	//Room entries, to be displayed at one page
	public List<Type> entries = null;
	
	//The total number of objects in the database. This data field
	//is not necessarily set up on the server, thus one has to be careful!
	public int total_size = 0;
	
	//The offset for the selected data, this object contains
	//data starting from the entry "offset + 1"
	public int offset = 0;
}
