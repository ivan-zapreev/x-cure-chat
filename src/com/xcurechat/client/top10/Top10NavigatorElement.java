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
 * The top10 site section user interface package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.top10;

import com.google.gwt.user.client.ui.SimplePanel;

import com.xcurechat.client.SiteNavigator;
import com.xcurechat.client.SiteNavigatorElement;

import com.xcurechat.client.userstatus.UserStatusType;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This class extends the site navigator element by implementing the method that returns the body component's instance. 
 */
public class Top10NavigatorElement extends SiteNavigatorElement<Top10BodyWidgetProxy> {
	
	//The unique instance of the top10 body widget proxy
	private Top10BodyWidgetProxy instance = null;

	/**
	 * The basic constructor
	 * @param isOnLogOutDisabled true if this site section should be disabled on log-out
	 * @param siteBodyPanel the site body panel to add the site section widget
	 * @param siteNavigator the instance of the site navigator
	 * @param forceUserStatus the user status that should be forced on entering this site section
	 */
	public Top10NavigatorElement( final boolean isOnLogOutDisabled, final SimplePanel siteBodyPanel,
								  final SiteNavigator siteNavigator, final UserStatusType forceUserStatus) {
		super( "button_rates", isOnLogOutDisabled, siteBodyPanel, siteNavigator, forceUserStatus );
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteNavigatorElement#getBodyContentWidget()
	 */
	@Override
	public Top10BodyWidgetProxy getBodyContentWidget() {
		if( instance == null ) {
			instance = new Top10BodyWidgetProxy( getSiteBodyComponentHistoryPrefix() );
		}
		return instance;
	}
	
	@Override
	public boolean isWithBackground() {
		//In this case this site section manages the background itself in case of small view area
		return true;
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#getSiteBodyComponentHistoryPrefix()
	 */
	@Override
	public String getSiteBodyComponentHistoryPrefix() {
		return getSiteSectionIdentifier() + CommonResourcesContainer.SITE_SECTION_HISTORY_DELIMITER;
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.SiteBodyComponent#getSiteSectionIdentifier()
	 */
	@Override
	public String getSiteSectionIdentifier() {
		return CommonResourcesContainer.TOP10_SECTION_IDENTIFIER_STRING;
	}
}
