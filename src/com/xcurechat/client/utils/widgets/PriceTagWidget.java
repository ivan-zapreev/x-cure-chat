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


import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
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

/**
 * @author zapreevis
 * This widget is used to visualize the price tag
 */
public class PriceTagWidget extends Composite {
	
	private static UITitlesI18N titles = I18NManager.getTitles();
	
	//The main focus panel, clickale
	private final FocusPanel focusPanel;
	//The main data panel that will store the widget's data
	private final HorizontalPanel dataPanel = new HorizontalPanel();
	//The gold image
	private final Image goldPieceImage = new Image();
	private final Label gouldPiecesPrice = new Label();
	//Stores the price in Gold pieces
	private int priceInGoldPieces = 0;
	//Stores the indicator of whether the object is enabled,
	private boolean isEnbled;
	//Allows to detect if this is the object's initialization phase
	private boolean isInitialized = false; 
	
	/**
	 * THe basic constructor
	 * @param priceStrPrefix the prefix that will be placed before the price tag
	 * @param priceInGoldPieces the price or the minimum price in gold pieces
	 * @param isMinimumPrice if true then this is the "minimum money in the wallet", otherwise it is just a price tag
	 * @param isEnabled indicates which mode we initialize the object in
	 */
	public PriceTagWidget( final String priceStrPrefix, final int priceInGoldPieces,
						   final boolean isMinimumPrice, final boolean isEnabled ) {
		//Store the prise
		this.priceInGoldPieces = priceInGoldPieces;
		
		dataPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		dataPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		
		if( priceStrPrefix != null ) {
			dataPanel.add( new Label( priceStrPrefix ) );
			dataPanel.add( new HTML("&#58;&nbsp;") );
		}
		
		if( isMinimumPrice ) {
			dataPanel.setTitle( titles.accessStartsFromNumGoldPieces( priceInGoldPieces ) );
		} else {
			dataPanel.setTitle( titles.priceIsNumGoldPieces( priceInGoldPieces ) );
		}
		
		dataPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		goldPieceImage.setUrl( ServerSideAccessManager.SITE_IMAGES_LOCATION + "coin.png" );
		dataPanel.add( goldPieceImage );
		
		dataPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		dataPanel.add( new HTML("&nbsp;") );
		gouldPiecesPrice.setText( priceInGoldPieces+"" );
		dataPanel.add( gouldPiecesPrice );
		
		setEnabled( isEnabled );
		
		focusPanel = new FocusPanel();
		focusPanel.add( dataPanel );
		
		isInitialized = true;
		
		initWidget( focusPanel );
	}
	
	/**
	 * Allows to add a click handler
	 * @param clickHandler the click handler to add
	 */
	public void addClickHandler( final ClickHandler clickHandler ) {
		if( clickHandler != null ) {
			focusPanel.addClickHandler( clickHandler );
			focusPanel.addStyleName( CommonResourcesContainer.PRICE_TAG_WIDGET_CLICKABLE_STYLE );
		}
	}
	
	/**
	 * Allows to set the widget into the enabled/disabled mode, but this does not disable the click listener, if any
	 * @param isEnabled true to enables, false to disable
	 */
	public void setEnabled( final boolean isEnabled ) {
		if( this.isEnbled != isEnabled || ! isInitialized ) {
			this.isEnbled = isEnabled;
			if( isEnabled ) {
				gouldPiecesPrice.setStyleName( CommonResourcesContainer.PRICE_TAG_WIDGET_ENABLED_STYLE );
			} else {
				gouldPiecesPrice.setStyleName( CommonResourcesContainer.PRICE_TAG_WIDGET_DISABLED_STYLE );
			}
		}
	}
	
	public int getPriceInGoldPieces() {
		return priceInGoldPieces;
	}
	
}
