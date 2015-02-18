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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.xcurechat.client.data.search.ForumSearchData;
import com.xcurechat.client.utils.ClientEncoder;
import com.xcurechat.client.utils.EncoderInt;

/**
 * @author zapreevis
 * This class represents a page index widget based on an Anchor object
 */
public class PageIndexWidget extends Composite {
	//The encoder interface
	private static final EncoderInt encoder = new ClientEncoder();
	
	//The corresponding forum search data
	private final boolean isCurrentPage;
	private final Anchor hyperLink;
	
	//The click handler that allows to disable the widget
	private static ClickHandler disableClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			event.stopPropagation();
			event.preventDefault();
		}
	};
	
	//The click handler registration for disabled index widget
	private HandlerRegistration disableHandlerRegistration = null;
	
	/**
	 * THe basic constructor
	 * @param searchData the original search object with the pageIndex field indicating he currently selected page
	 * @param pageIndex the page index is the index for which this page-index widget is created
	 * @param isEnabled true to create the widget in the enabled mode, false in the disabled
	 * @param siteSectionPrefix the history token site section prefix
	 */
	public PageIndexWidget( final ForumSearchData searchData, final int pageIndex,
							final boolean isEnabled, final String siteSectionPrefix ) {
		this.isCurrentPage = (searchData.pageIndex == pageIndex );
		//Create a copy in order to be able to set the proper search object index and not spoil the original search object
		final ForumSearchData localSearchData = searchData.clone();
		localSearchData.pageIndex = pageIndex;
		hyperLink = new Anchor("" + pageIndex, CommonResourcesContainer.URI_HASH_SYMBOL + siteSectionPrefix + localSearchData.serialize(encoder));
		if( isCurrentPage ) {
			hyperLink.addStyleName( CommonResourcesContainer.CURRENT_PAGE_INDEX_STYLE );
			//the current page index widget is always disabled
			setEnabled( false );
		} else {
			//Set the link as enabled/disabled
			setEnabled( isEnabled );
		}
		
		initWidget(hyperLink);
	}
	
	/**
	 * Allows to enable/disable the page index hyperlink. Note that the current page-index widget is always disabled.
	 * @param enabled true to enable, false to disabled
	 */
	public void setEnabled( final boolean enabled ) {
		//First remove the click handler registration if any
		if( disableHandlerRegistration != null ) {
			disableHandlerRegistration.removeHandler();
			disableHandlerRegistration = null;
		}
		
		//Now set the element to enabled/disabled mode
		if( enabled && ! isCurrentPage ) {
			hyperLink.removeStyleName( CommonResourcesContainer.DISABLED_PAGE_INDEX_STYLE );
			hyperLink.addStyleName( CommonResourcesContainer.ENABLED_PAGE_INDEX_STYLE );
			hyperLink.setEnabled(true);
		} else {
			hyperLink.removeStyleName( CommonResourcesContainer.ENABLED_PAGE_INDEX_STYLE );
			if( ! isCurrentPage ) {
				hyperLink.addStyleName( CommonResourcesContainer.DISABLED_PAGE_INDEX_STYLE );
			}
			disableHandlerRegistration = hyperLink.addClickHandler( disableClickHandler );
			hyperLink.setEnabled(false);
		}
	}
}
