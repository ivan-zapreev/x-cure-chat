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
 * The news section interface package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.news;

import com.google.gwt.user.client.ui.SimplePanel;

import com.xcurechat.client.SiteNavigator;
import com.xcurechat.client.SiteNavigatorElement;

import com.xcurechat.client.userstatus.UserStatusType;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This class extends the site navigator element by implementing the method that returns the body component's instance. 
 */
public class NewNavigatorElement extends SiteNavigatorElement<LatestNewsBodyUIProxy> {
	
	//The only instance of the new-section panel class
	public LatestNewsBodyUIProxy instance = null;

	public NewNavigatorElement( boolean isOnLogOutDisabled, SimplePanel siteBodyPanel,
								SiteNavigator siteNavigator, UserStatusType forceUserStatus ) {
		super( "button_main", isOnLogOutDisabled, siteBodyPanel, siteNavigator, forceUserStatus );
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteNavigatorElement#getBodyContentWidget()
	 */
	@Override
	public LatestNewsBodyUIProxy getBodyContentWidget() {
		if( instance == null ) {
			instance = new LatestNewsBodyUIProxy( getSiteBodyComponentHistoryPrefix() );
		}
		return instance;
	}

	@Override
	public boolean isWithBackground() {
		return true;
	}
	
	@Override
	public String getSiteBodyComponentHistoryPrefix() {
		return getSiteSectionIdentifier() + CommonResourcesContainer.SITE_SECTION_HISTORY_DELIMITER;
	}

	@Override
	public String getSiteSectionIdentifier() {
		return CommonResourcesContainer.NEWS_SECTION_IDENTIFIER_STRING;
	}
}
