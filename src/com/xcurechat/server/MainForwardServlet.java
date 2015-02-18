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
package com.xcurechat.server;

import com.xcurechat.server.redirect.ForwardServletBase;
import com.xcurechat.server.redirect.MainRedirectHelper;
import com.xcurechat.server.redirect.SectionRedirectHelperInt;

/**
 * @author zapreevis
 * Forwards the servlet requests to main JSP
 */
public class MainForwardServlet extends ForwardServletBase {

	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see com.xcurechat.server.utils.ForwardServletBase#getRedirectHelper()
	 */
	@Override
	protected SectionRedirectHelperInt getRedirectHelper() {
		return new MainRedirectHelper();
	}

}
