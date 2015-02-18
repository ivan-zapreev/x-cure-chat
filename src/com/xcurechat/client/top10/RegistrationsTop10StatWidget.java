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

import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.search.Top10SearchData;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.UserAvatarWidget;

/**
 * @author zapreevis
 * This widget gets the last user registrations top10 section
 */
public class RegistrationsTop10StatWidget extends PagedTop10BodyComponent<ShortUserData> {
	/*The data to be displayed for the user-avatar widget*/
	private final static int DATA_TO_DISPLAY = UserAvatarWidget.DISPLAY_AVATAR_IMAGE_DATA |
											   UserAvatarWidget.DISPLAY_REGISTRATION_DATA    |
											   UserAvatarWidget.DISPLAY_USER_NAME_DATA;

	/**
	 * The basic constructor 
	 */
	public RegistrationsTop10StatWidget() {
		super( titlesI18N.top10RegistrationsTitle(), titlesI18N.top10RegistrationsDesc() );
		addResultsPanelStyle( CommonResourcesContainer.WIDE_STATISTICS_SCROLL_PANEL_STYLE );
	}

	@Override
	protected Top10SearchData.SearchTypes getStatisticsSearchType() {
		return Top10SearchData.SearchTypes.TOP_REGISTRATIONS_SEARH_TYPE;
	}

	@Override
	protected Widget constructResultWidget( final List<ShortUserData> results, int index) {
		return new UserAvatarWidget( results.get( index ), DATA_TO_DISPLAY );
	}
	
	/**
	 * We override the parent's method for this widget in order to facilitate showing the new online users right away
	 */
	@Override
	public void setUserLoggedIn() {
		//Call the method we override to provide event propogation
		super.setUserLoggedIn();
		//If the site section is selected update the current page search results
		if( isSiteSectionSelected ) {
			updateCurrentPage();
		}
	}

}
