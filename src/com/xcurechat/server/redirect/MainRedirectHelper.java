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

import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

import com.xcurechat.server.utils.ServerEncoder;

/**
 * @author zapreevis
 * This helper allows to form the proper urls for the site main section 
 */
public class MainRedirectHelper extends ForumSearchRedirectHelperInt {
	//The encoder that has to be used in the server side
	private static final ServerEncoder encoder = new ServerEncoder(); 

	@Override
	@SuppressWarnings("unchecked")
	public ForumSearchData getSearchDataObject( HttpServletRequest req) {
		//Get the search data from the request
		final ForumSearchData searchData = new ForumSearchData();
		searchData.setParametersFromMap( encoder, req.getParameterMap() );
		
		//Create a new clean object and fill it out with the needed data
		//This is done for the sake of avoiding other possible arguments
		final ForumSearchData searchObject = new ForumSearchData();
		searchData.baseMessageID = ForumSearchData.UNKNOWN_BASE_MESSAGE_ID_VAL;
		searchObject.pageIndex = searchData.pageIndex;
		searchObject.isApproved = true;
		return searchObject;
	}
	
	@Override
	public String getParametersString( final HttpServletRequest req ) {
		return getSearchDataObject(req).serialize( encoder );
	}
	
	/* (non-Javadoc)
	 * @see com.xcurechat.server.redirect.SectionRedirectHelperInt#getHelperSectionName()
	 */
	@Override
	public String getSectionName() {
		//Is set to the same name as for the GWT version of the main site section
		return CommonResourcesContainer.NEWS_SECTION_IDENTIFIER_STRING;
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.server.redirect.SectionRedirectHelperInt#getSiteGWTURL(java.lang.String, java.util.Map)
	 */
	@Override
	public String getSiteGWTURL(final HttpServletRequest req) {
		return completeGWTUrl( req, getParametersString(req) );
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.server.redirect.SectionRedirectHelperInt#getSiteJSPURL(java.lang.String, java.util.Map)
	 */
	@Override
	public String getSiteServletURL(final HttpServletRequest req) {
		return completeServletUrl( req, getParametersString(req) );
	}
}
