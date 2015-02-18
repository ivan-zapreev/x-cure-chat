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
 * The client-side utilities package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.utils.widgets;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.search.ForumSearchData;

import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.utils.PageIndexesGenerator;

/**
 * @author zapreevis
 * Represents a class that should be used for listing pages
 */
public class PageListWidget extends Composite {
	//The panel storing the page indexes
	private final HorizontalPanel indexesPanel;
	//Stores the maximum page index for the current search data
	private int maximumPageIndex = 1;
	//The list of currently displayed page index widgets
	private List<PageIndexWidget> indexWidgets = new ArrayList<PageIndexWidget>();
	//The site section prefix
	private final String siteSectionPrefix;
	
	/**
	 * The basic constructor 
	 * @param siteSectionPrefix the history token site section prefix
	 */
	public PageListWidget( final String siteSectionPrefix ) {
		this.siteSectionPrefix = siteSectionPrefix;
		final HorizontalPanel mainPanel = new HorizontalPanel();
		mainPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER);
		mainPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM);
		indexesPanel = new HorizontalPanel();
		indexesPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER);
		indexesPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM);
		Label pageLabel = new Label( I18NManager.getTitles().pageString() );
		pageLabel.setStyleName( CommonResourcesContainer.REGULAR_FIELD_STYLE );
		mainPanel.add( pageLabel );
		mainPanel.add( new HTML(":&nbsp;") );
		mainPanel.add( indexesPanel );
		//The wrapper panel
		HorizontalPanel wrapPanel = new HorizontalPanel();
		wrapPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		wrapPanel.add( mainPanel );
		initWidget( wrapPanel );
	}
	
	/**
	 * Allows to set the current page-indexes's state, i.e. the current page index and the maximum page index
	 * @param currentPageIndex the current page index
	 * @param intMaximumPageIndex the maximum age index
	 */
	public void updatePageIndexes(final ForumSearchData searchData, final OnePageViewData<ForumMessageData> searchResults) {
		//Clear the current indexes
		indexesPanel.clear(); indexWidgets.clear();
		//Compute the maximum page index
		maximumPageIndex = computeMaximumPageIndex( searchResults );
		//Set the new list of indexes
		List<Integer> indexes = PageIndexesGenerator.getPageIndexes( 1, searchData.pageIndex, maximumPageIndex  );
		for(Integer index : indexes ) {
			if( index != PageIndexesGenerator.DUMMY_PAGE_INDEX ) {
				//Add dummy pages delimiter
				PageIndexWidget indexWidget = new PageIndexWidget( searchData, index, false, siteSectionPrefix );
				indexWidgets.add( indexWidget );
				indexesPanel.add( indexWidget );
			} else {
				//Add dummy pages delimiter
				indexesPanel.add(new Label("..."));
			}
			//Add delimiter
			indexesPanel.add(new HTML("&nbsp;"));
		}
	}
	
	/**
	 * Allows to enable/disable the page index hyperlinks. Note that the current page-index widget is always disabled.
	 * @param enabled true to enable, false to disabled
	 */
	public void setEnabled( final boolean enabled ) {
		for(PageIndexWidget indexWidget : indexWidgets) {
			indexWidget.setEnabled( enabled );
		}
	}
	
	/**
	 * Allows to get the maximum page index for the currently dislayed list of page indexes
	 * @return the maximum page index >= 1
	 */
	public int getMaximumPageIndex() {
		return maximumPageIndex;
	}
	
	/**
	 * Allows to compute the maximum page index from the search results
	 * @param searchResults the search results
	 * @return the maximum page index >=1;
	 */
	public static int computeMaximumPageIndex(final OnePageViewData<ForumMessageData> searchResults) {
		//Compute the maximum page index, indexes start from one
		return PageIndexesGenerator.getNumberOfPages( searchResults.total_size, ForumSearchData.MAX_NUMBER_OF_MESSAGES_PER_PAGE);
	}
}
