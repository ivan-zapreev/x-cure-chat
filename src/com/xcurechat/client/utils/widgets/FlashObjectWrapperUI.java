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
 * The user interface utils package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.utils.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.event.shared.HandlerRegistration;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.utils.BrowserDetect;
import com.xcurechat.client.utils.InterfaceUtils;

/**
 * @author zapreevis
 * This class wraps around the flash object and provides
 * a possibility of extending it with a download link,
 * if this is a site-local file, and a click to load
 * flash "glass" panel.
 */
public class FlashObjectWrapperUI extends Composite {
	
	//The player and file download link panel panel
	private final FlowPanel mainWidgetPanel = new FlowPanel();
	
	//The main panel of the composite
	private final SimplePanel flashContainerPanel = new SimplePanel();
	
	//The player blocking panel
	private final FocusPanel blockedPlayerPanelPanel = new FocusPanel();
	
	//The click handler registration for the glass cover panel
	private HandlerRegistration registration = null;
	
	/**
	 * The main constructor
	 * @param embeddedString the string storing the embedded flash object tags
	 * @param isInitiallyBlocked if true then we only add the flash object once this widget was clicked
	 * @param downloadFileURL the url for the file played in the flash or null if no download link should be allowed
	 */
	public FlashObjectWrapperUI( final String embeddedString, final boolean isInitiallyBlocked, final String downloadFileURL ) {
		//Set up the widget elements first, add the flash panel and the download link if needed
		flashContainerPanel.setStyleName( CommonResourcesContainer.FLASH_PLAYER_PANEL_STYLE );
		mainWidgetPanel.add( flashContainerPanel );
		if( downloadFileURL != null ) {
			Widget downloadLinkWidget = InterfaceUtils.getDownloadLinkWidget( downloadFileURL, true );
			if( BrowserDetect.getBrowserDetect().isChrome() ) {
				//Add an additional centering style for chrome
				downloadLinkWidget.addStyleName( CommonResourcesContainer.DOWNLOAD_LINK_CHROME_CENTERING_STYLE );
			}
			mainWidgetPanel.add( downloadLinkWidget );
		}
		
		//Check if the need the delayed flash loading panel or we can add the flash directly
		if( isInitiallyBlocked ) {
			//We want a glass panel
			blockedPlayerPanelPanel.setTitle( I18NManager.getTitles().clickToViewToolTip() );
			blockedPlayerPanelPanel.setStyleName( CommonResourcesContainer.FLASH_PLAYER_BLOCKED_PANEL_STYLE );
			registration = blockedPlayerPanelPanel.addClickHandler( new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					//Remove the click handler
					if( registration != null ) {
						registration.removeHandler();
						registration = null;
					}
					
					//Add the flash object to the main panel
					setFlashObjectToPanel( embeddedString );
					
					//Cancel the event propagation
					event.stopPropagation();
					event.preventDefault();
				}
			} );
			flashContainerPanel.add( blockedPlayerPanelPanel );
		} else {
			//We do not want a glass panel, just add the flash object to the main panel
			setFlashObjectToPanel( embeddedString );
		}

		//Set the widget style
		mainWidgetPanel.setStyleName( CommonResourcesContainer.FLASH_PLAYER_WIDGET_STYLE );
		
		//Initialize the composite
		initWidget( mainWidgetPanel );
	}
	
	/**
	 * Allows to set the width and height attributes of the widget
	 * @param width  the width attribute value
	 * @param height the height attribute value
	 */
	@Override
	public void setSize(final String width, final String height) {
		//Set the flash container panel size
		flashContainerPanel.setSize(width, height);
		//Set the delayed-flash-load panel size
		blockedPlayerPanelPanel.setSize(width, height);
		//We only set the width for the player and download link 
		//table because the height will be set automatically
		mainWidgetPanel.setWidth( width );
	}
	
	private void setFlashObjectToPanel( final String embeddedString ) {
		//Clear the panel
		flashContainerPanel.clear();
		//Add the flash object to the panel
		flashContainerPanel.add( new HTML( embeddedString ) );
	}
}
