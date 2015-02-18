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
 * The util interface widgets package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.utils.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.xcurechat.client.rpc.ServerSideAccessManager;

/**
 * @author zapreevis
 * This represents the navigation button panel for the paged views
 */
public abstract class NavigationButtonPanel extends Composite {
	//Defines if this is the "next" button or the "previous" button
	private final boolean isNext;
	private final int buttom_type;
	private boolean isEnabled = false;
	private boolean isActive = false;
	private final boolean isHideOnDisabled;
	private final String extraLeftButtonStyle;
	private final String extraRightButtonStyle;
	private final String extraTopButtonStyle;
	private final String extraBottomButtonStyle;
	//This is the focus panel wrapping around the butto's content panel
	private final FocusPanel buttonPanel;
	//The additional alignment panel
	private HorizontalPanel alignmentPanel;
	//The Panel storing the button's content widgets 
	private Panel contentPanel;
	//The main focus panel with the image in it
	private Image buttonImage;
	//The label storing the next page index
	private Label pageLabel;
	
	/**
	 * The basic constructor 
	 * @param buttom_type defines the type of the button NAV_LEFT_IMG_BUTTON, ...
	 * @param isEnabled if true then the button is enabled, if false then it is disabled. That is based on the current page index
	 * @param isActive if true then the button is active, if it is enabled, otherwise not
	 * @param isHideOnDisabled if true then we hide the button if it is disabled
	 * @param extraLeftButtonStyle the additional left-button style or null
	 * @param extraRightButtonStyle the additional right-button style or null
	 * @param extraTopButtonStyle the additional top-button style or null
	 * @param extraBottomButtonStyle the additional bottom-button style or null
	 */
	public NavigationButtonPanel( final int buttom_type, final boolean isEnabled, final boolean isActive,
								  final boolean isHideOnDisabled, final String extraLeftButtonStyle,
								  final String extraRightButtonStyle, final String extraTopButtonStyle,
								  final String extraBottomButtonStyle ) {
		//Set/Store the button type;
		this.buttom_type = buttom_type;
		this.isNext = ( buttom_type == CommonResourcesContainer.NAV_RIGHT_IMG_BUTTON ) || ( buttom_type == CommonResourcesContainer.NAV_BOTTOM_IMG_BUTTON );
		this.isHideOnDisabled = isHideOnDisabled;
		this.extraLeftButtonStyle	 = extraLeftButtonStyle;
		this.extraRightButtonStyle	 = extraRightButtonStyle;
		this.extraTopButtonStyle	 = extraTopButtonStyle;
		this.extraBottomButtonStyle	 = extraBottomButtonStyle;
		
		//Add the click listener
		buttonPanel = new FocusPanel();
		buttonPanel.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if( NavigationButtonPanel.this.isEnabled &&
					NavigationButtonPanel.this.isActive  ) {
					//Disable the event, just in case
					event.stopPropagation();
					event.preventDefault();
					//Navigate to the page
					moveToPage( isNext );
				}
			}
		} );
		
		//Complete the widget creation
		try{
			initializeNavigationButtonContent( );
		} catch (Exception e) {
			//There is nothing we can really do about it
		}
		
		//Set the enabled and active statuses
		setAllowed( isEnabled );
		setEnabled( isActive );
		
		//Initialize the composite
		initWidget( buttonPanel );
	}
	
	/**
	 * Allows to create and set up the button panel with the image and the label
	 * @throws Exception if the button type is unknown
	 */
	public void initializeNavigationButtonContent( ) throws Exception {
		//Create and the content widgets
		pageLabel = new Label();
		pageLabel.setStyleName( CommonResourcesContainer.FIELD_VALUE_DEFAULT_IMP_STYLE_NAME );
		buttonImage = new Image();
		buttonImage.setStyleName( CommonResourcesContainer.NAVIGATION_BUTTON_IMAGE_STYLE );
		
		//Initialize the content panel and add the widgets in the right order
		if( ( buttom_type == CommonResourcesContainer.NAV_TOP_IMG_BUTTON ) || ( buttom_type == CommonResourcesContainer.NAV_BOTTOM_IMG_BUTTON )  ) {
			contentPanel = new HorizontalPanel(); 
			((HasAlignment)contentPanel).setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
			((HasAlignment)contentPanel).setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
			contentPanel.add( buttonImage );
			contentPanel.add( pageLabel );
		} else {
			contentPanel = new VerticalPanel(); 
			((HasAlignment)contentPanel).setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
			((HasAlignment)contentPanel).setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
			contentPanel.add( pageLabel );
			contentPanel.add( buttonImage );
		}
		
		final String direction;
		final String buttonStyle;
		final String extraButtonStyle;
		switch(buttom_type) {
			case CommonResourcesContainer.NAV_LEFT_IMG_BUTTON:
				direction = "left";
				buttonStyle = CommonResourcesContainer.NAVIGATION_BUTTON_LEFT_STYLE;
				extraButtonStyle = extraLeftButtonStyle;
				break;
			case CommonResourcesContainer.NAV_RIGHT_IMG_BUTTON:
				direction = "right";
				buttonStyle = CommonResourcesContainer.NAVIGATION_BUTTON_RIGHT_STYLE;
				extraButtonStyle = extraRightButtonStyle;
				break;
			case CommonResourcesContainer.NAV_TOP_IMG_BUTTON:
				direction = "top";
				buttonStyle = CommonResourcesContainer.NAVIGATION_BUTTON_TOP_STYLE;
				extraButtonStyle = extraTopButtonStyle;
				break;
			case CommonResourcesContainer.NAV_BOTTOM_IMG_BUTTON:
				direction = "bottom";
				buttonStyle = CommonResourcesContainer.NAVIGATION_BUTTON_BOTTOM_STYLE;
				extraButtonStyle = extraBottomButtonStyle;
				break;
			default:
				throw new Exception( "Unknown navigation button type: " + buttom_type );
		}
		
		//Set the styles
		buttonPanel.setStyleName( buttonStyle );
		if( extraButtonStyle != null ) {
			buttonPanel.addStyleName( extraButtonStyle );
		}
		
		//Set up the button's image
		buttonImage.setUrl( GWT.getModuleBaseURL() + ServerSideAccessManager.SITE_IMAGES_LOCATION + "navigation_" + direction + ".png" );
		
		//Construct the alignment panel, that is used to store the content panel
		alignmentPanel = new HorizontalPanel();
		alignmentPanel.setWidth("100%");
		alignmentPanel.setHeight("100%");
		alignmentPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		alignmentPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		
		//Add the content panel to the alignment panel
		alignmentPanel.add( contentPanel );
		//Add the alignment panel to the button panel
		buttonPanel.add( alignmentPanel ) ;
	}
	
	public void setRemainingPageCount( final int minimum_page_index, final int current_page_index, final int maximum_page_index ) {
		if( pageLabel != null ) {
			if( isNext ) {
				pageLabel.setText( (maximum_page_index - current_page_index ) + "");
			} else {
				pageLabel.setText( (current_page_index - minimum_page_index ) + "");
			}
		}
	}
	
	public void setAllowed( final boolean isEnabled) {
		this.isEnabled = isEnabled;
		updateButtonUI();
	}
	
	public void setEnabled( final boolean isActive ) {
		this.isActive = isActive;
		updateButtonUI();
	}
	
	private void updateButtonUI() {
		final boolean isOn = isEnabled && isActive;
		if( buttonPanel != null ) {
			if( isOn ) {
				buttonPanel.removeStyleName( CommonResourcesContainer.NAVIGATION_BUTTON_OFF_STYLE );
				buttonPanel.addStyleName( CommonResourcesContainer.NAVIGATION_BUTTON_ON_STYLE );
			} else {
				buttonPanel.removeStyleName( CommonResourcesContainer.NAVIGATION_BUTTON_ON_STYLE );
				buttonPanel.addStyleName( CommonResourcesContainer.NAVIGATION_BUTTON_OFF_STYLE );
			}
			//Hide the button's content if it is not active
			if( ( alignmentPanel != null ) && ( contentPanel != null ) && isHideOnDisabled ) {
				alignmentPanel.clear();
				if( isEnabled ) {
					alignmentPanel.add( contentPanel );
				}
			}
		}
	}
	
	/**
	 * Allows to more to some page either the next one or the previous one
	 * @param isNext if true then we should move to the next page, if false, then to the previous
	 */
	protected abstract void moveToPage( final boolean isNext );
}
