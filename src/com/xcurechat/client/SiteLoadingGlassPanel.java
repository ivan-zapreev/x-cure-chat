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
 * The user interface package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;

import com.google.gwt.event.shared.HandlerRegistration;

import com.google.gwt.user.client.Window;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;

import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This is a glass panel that is shown on site component loading and
 * prevents the user from making all sort of actions on site.

 */
public class SiteLoadingGlassPanel extends Composite implements KeyUpHandler, KeyDownHandler, KeyPressHandler {
	//The site component loading glass panel
	private final FlexTable loadingGlassPanel = new FlexTable();
	
	//The main site's focus panel that contains the site components
	private final FocusPanel theMainFocusPanel;
	
	//The blocking key handler registrations
	private HandlerRegistration keyUpHandlerRegistration    = null;
	private HandlerRegistration keyDownHandlerRegistration  = null;
	private HandlerRegistration keyPressHandlerRegistration = null;
	
	/**
	 * The basic constructor
	 */
	public SiteLoadingGlassPanel( final FocusPanel theMainFocusPanel ) {
		//Store the main focus panel reference
		this.theMainFocusPanel = theMainFocusPanel;
		
		//Get the title manager
		final UITitlesI18N titlesI18N = I18NManager.getTitles();
		
		//Set up the component's content
		final DecoratorPanel decoratedPanel = new DecoratorPanel();
		decoratedPanel.setStyleName( CommonResourcesContainer.INTERNAL_ROUNDED_CORNER_PANEL_STYLE );
		//Add the content to the decorated panel
		final HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		horizontalPanel.setStyleName( CommonResourcesContainer.INTERNAL_ROUNDED_CORNER_PANEL_CONTENT_STYLE );
		//Add the loading image
		horizontalPanel.add( new Image( ServerSideAccessManager.getActivityImageURL( ) ) );
		//Add the spacing
		horizontalPanel.add( new HTML("&nbsp;") );
		//Add the communicating label
		final Label loadingLabel = new Label( titlesI18N.communicatingText() );
		loadingLabel.setStyleName( CommonResourcesContainer.LOADING_LABEL_STYLE );
		horizontalPanel.add( loadingLabel );
		//Store the content in the decorated panel
		decoratedPanel.add( horizontalPanel );
		//Put the stuff into the glass panel, make sure it is centered
		loadingGlassPanel.insertRow( 0 );
		loadingGlassPanel.insertCell( 0, 0 );
		loadingGlassPanel.getCellFormatter().setAlignment( 0, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE );
		loadingGlassPanel.setWidget( 0, 0, decoratedPanel );
		loadingGlassPanel.setVisible( false );
		loadingGlassPanel.setTitle( titlesI18N.communicatingToolTipText() );
		loadingGlassPanel.setStyleName( CommonResourcesContainer.SITE_LOADING_COMPONENT_GLASS_PANEL_STYLE );
		
		//Initialize the widget
		initWidget( loadingGlassPanel );
	}
	
	/**
	 * Allows to enable/disable blocking of the main key events on the site 
	 * @param block
	 */
	private void blockKeyEvents( final boolean block ) {
		if( block ) {
			//Remove the old key handler registrations if any, just in case
			blockKeyEvents( false );
			//Add the new key handler registrations
			keyUpHandlerRegistration    = theMainFocusPanel.addKeyUpHandler( this );
			keyDownHandlerRegistration  = theMainFocusPanel.addKeyDownHandler( this );
			keyPressHandlerRegistration = theMainFocusPanel.addKeyPressHandler( this );
		} else {
			//Remove key Up handler if any
			if( keyUpHandlerRegistration != null ) {
				keyUpHandlerRegistration.removeHandler();
				keyUpHandlerRegistration = null;
			}
			//Remove key Down handler if any
			if( keyDownHandlerRegistration != null ) {
				keyDownHandlerRegistration.removeHandler();
				keyDownHandlerRegistration = null;
			}
			//Remove key Press handler if any
			if( keyPressHandlerRegistration != null ) {
				keyPressHandlerRegistration.removeHandler();
				keyPressHandlerRegistration = null;
			}
		}
	}
	
	/**
	 * Allows to update the size of the glass panel when it is visible
	 */
	public void updateUIElements() {
		//Update the size of the glass panel if it is shown
		if( loadingGlassPanel.isVisible() ) {
			loadingGlassPanel.getCellFormatter().setWidth(  0, 0, Window.getClientWidth() + "px" );
			loadingGlassPanel.getCellFormatter().setHeight( 0, 0, Window.getClientHeight() + "px" );
		}
	}

	@Override
	public void setVisible( final boolean visible ) {
		//Show/Hide the panel
		loadingGlassPanel.setVisible( visible );
		//Block/unblock the key events
		blockKeyEvents( visible );
		//Update the size
		updateUIElements();
	}

	@Override
	public void onKeyUp(KeyUpEvent event) {
		event.preventDefault();
		event.stopPropagation();
	}

	@Override
	public void onKeyDown(KeyDownEvent event) {
		event.preventDefault();
		event.stopPropagation();
	}

	@Override
	public void onKeyPress(KeyPressEvent event) {
		event.preventDefault();
		event.stopPropagation();
	}
}
