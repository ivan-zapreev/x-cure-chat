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

import com.xcurechat.client.data.ShortUserFileDescriptor;
import com.xcurechat.client.data.search.Top10SearchData;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This widget gets the last-profile-file results for the top10 section
 */
public class LastProfileFileTop10StatWidget extends PagedTop10BodyComponent<ShortUserFileDescriptor> {

	/**
	 * The basic constructor 
	 */
	public LastProfileFileTop10StatWidget() {
		super( titlesI18N.top10LastProfileFileTitle(), titlesI18N.top10LastProfileFileDesc() );
		addResultsPanelStyle( CommonResourcesContainer.WIDE_STATISTICS_SCROLL_PANEL_STYLE );
	}

	@Override
	protected Top10SearchData.SearchTypes getStatisticsSearchType() {
		return Top10SearchData.SearchTypes.TOP_USER_FILES_SEARH_TYPE;
	}

	@Override
	protected Widget constructResultWidget( final List<ShortUserFileDescriptor> results, int index) {
		return new Top10UserFileWidget( null, results, index );
	}

}
