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
 * The forum interface package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.forum.sharing;

import com.google.gwt.http.client.URL;

/**
 * @author zapreevis
 * Allows to share a forum message in vkontakte.ru
 */
public class ShareMessageVKontakteURL extends ShareMessageLinkBase {

	/**
	 * The basic constructor
	 * @param messageURL
	 * @param messageTitle
	 * @param linkTitle
	 */
	public ShareMessageVKontakteURL(String messageURL, String messageTitle, String linkTitle) {
		super(messageURL, messageTitle, linkTitle);
		
		//Initialize the widget
		initialize();
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.forum.sharing.ShareMessageLinkBase#getLinkImageFileName()
	 */
	@Override
	public String getLinkImageFileName() {
		return "vkontakte";
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.forum.sharing.ShareMessageLinkBase#getLinkURL(java.lang.String, java.lang.String)
	 */
	@Override
	public String getLinkURL(String messageURL, String messageTitle) {
		return "http://vkontakte.ru/share.php?url="+URL.encodeQueryString( messageURL );
	}

}
