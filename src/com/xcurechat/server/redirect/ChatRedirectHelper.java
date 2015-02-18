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

import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This helper allows to form the proper urls for the site info section 
 */
public class ChatRedirectHelper extends SectionRedirectHelperInt {
	/* (non-Javadoc)
	 * @see com.xcurechat.server.redirect.SectionRedirectHelperInt#getHelperSectionName()
	 */
	@Override
	public String getSectionName() {
		//Is set to the same name as for the GWT version of the chat site section
		return CommonResourcesContainer.CHAT_SECTION_IDENTIFIER_STRING;
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.server.redirect.SectionRedirectHelperInt#getSiteGWTURL(java.lang.String, java.util.Map)
	 */
	@Override
	public String getSiteGWTURL(final HttpServletRequest req) {
		return completeGWTUrl( req, null );
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.server.redirect.SectionRedirectHelperInt#getSiteJSPURL(java.lang.String, java.util.Map)
	 */
	@Override
	public String getSiteServletURL(final HttpServletRequest req) {
		return completeServletUrl( req, null );
	}

}
