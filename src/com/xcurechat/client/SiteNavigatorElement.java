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
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Image;

import com.xcurechat.client.rpc.exceptions.UserStateException;

import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.decorations.SiteDynamicDecorations;

import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

import com.xcurechat.client.userstatus.UserStatusType;
import com.xcurechat.client.userstatus.UserStatusManager;

/**
 * @author zapreevis
 * This class represents the site navigator element entry
 */
public abstract class SiteNavigatorElement<BodyContentWidgetType extends SiteBodySectionContent> extends Composite {
	//Is set to two minutes
	private static final int ALERT_REMOVAL_TIME_OUT_MILLISEC = 120000;
	
	//The string storing the base of the navigation element URL
	private static final String NAVIGATION_IMAGE_URL_BASE = ServerSideAccessManager.SITE_IMAGES_LOCATION + "site_manager/";
	//The extension of the navigation image file
	private static final String NAVIGATION_IMAGE_FILE_EXT = ".png";
	//The disabled menu image suffix
	private static final String NAVIGATION_IMAGE_DISABLED_SUFFIX = "_dis";
	//The alert image suffix
	private static final String ALERT_IMAGE_SUFFIX = "_alert.gif";
	
	//Is the reference to the site's body panel that stores the body components
	private final SimplePanel siteBodyPanel;
	//Other element properties
	private final boolean isOnLogOutDisabled;
	//The internal element status true if this site section is selected, otherwise false
	private boolean isSelected = false;
	//Stores true if the item is disabled, otherwise false
	private boolean isDisabled = false;
	//The image that represents this navigation element
	private final Image elementImage = new Image();
	//The image that represents the section alert
	private final Image alertImage = new Image();
	//The Anchor object that is the base of the composite widget
	private final Anchor anchorLink = new Anchor();
	//The enabled navigation element image url
	private final String selectedImageURL;
	private final String unselectedImageURL;
	//Stores the reference to the site navigator
	private final SiteNavigator siteNavigator;
	//The status to be forced when the user selects this site section
	private final UserStatusType forceUserStatus;
	//Contains true if the user is indicated as being logged in
	protected boolean isLoggedIn;
	//Contains true if the site section alert is on, otherwise false
	private boolean isSectionAlertOn = false;
	//The alert removal timer
	private AlertRemovalTimer alertRemovalTimer = null;
	
	/**
	 * @author zapreevis
	 * This class is used for canceling the site section notification after a certain time out.
	 */
	private class AlertRemovalTimer extends Timer {
		@Override
		public void run() {
			//Remove the alert
			alertSiteSection( false );
		}
	}
	
	/**
	 * The site-navigator element is constructed with the following parameters
	 * @param imageBaseName the name of the base name of the image that is related to this element
	 * @param isOnLogOutDisabled if true then in case the user is logged out this section is disabled
	 * @param siteBodyPanel the site's body panel that should store the widget related to this site section
	 * @param siteNavigator the instance of the site navigator itself
	 * @param forceUserStatus the status to be forced when the user selects this site section
	 * decorations into the logged out mode
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	protected SiteNavigatorElement( final String imageBaseName, final boolean isOnLogOutDisabled,
								    final SimplePanel siteBodyPanel, final SiteNavigator siteNavigator,
								    final UserStatusType forceUserStatus ) {
		//Store the reference to the site's main vertical panel
		this.siteBodyPanel = siteBodyPanel;
		//Store the reference to the site navigator
		this.siteNavigator = siteNavigator;
		//Store the other properties
		this.isOnLogOutDisabled = isOnLogOutDisabled;
		this.forceUserStatus = forceUserStatus;
		
		//Set the image urls 
		final String locale = InterfaceUtils.getCurrentLocale();
		selectedImageURL = NAVIGATION_IMAGE_URL_BASE + imageBaseName + "_" + locale + NAVIGATION_IMAGE_FILE_EXT;
		unselectedImageURL = NAVIGATION_IMAGE_URL_BASE + imageBaseName + "_" + locale + NAVIGATION_IMAGE_DISABLED_SUFFIX + NAVIGATION_IMAGE_FILE_EXT;
		
		//Set the initial image URL, style, and set the click handler
		setNavigatorImage( unselectedImageURL, CommonResourcesContainer.NAVIGATION_IMAGE_ENTRY_STYLE, isSectionAlertOn );
		anchorLink.addStyleName( CommonResourcesContainer.SITE_SECTION_LINK_STYLE );
		
		//In case of clicking on the anchor we want to set the proper
		//anchor link url because it might not have been set initially
		//due to the  site section not being selected before
		sinkEvents( Event.ONMOUSEUP | Event.ONDBLCLICK | Event.ONCONTEXTMENU );
		
		//Initialize the composite widget
		initWidget( anchorLink );
	}
	
	/**
	 * Allows to update the navigator element UI 
	 * @param imageURL the url for the navigaro element image, to be set
	 * @param imageStyle the style for the image to be used
	 * @param doAlert if true then we also set the alert image
	 */
	private void setNavigatorImage( final String imageURL, final String imageStyle, final boolean doAlert ) {
		elementImage.setUrl( imageURL );
		elementImage.setStyleName( imageStyle );
		String imageString = elementImage.getElement().getString();
		if( doAlert ) {
			alertImage.setStyleName( CommonResourcesContainer.ALERT_IMAGE_STYLE );
			alertImage.setUrl( NAVIGATION_IMAGE_URL_BASE + getSiteSectionIdentifier() + ALERT_IMAGE_SUFFIX );
			imageString += alertImage.getElement().getString();
			//If we set the alert on then we should either re-set the
			//existing timer or create a new one if no timer is present
			if( alertRemovalTimer != null ) {
				alertRemovalTimer.cancel();
			} else {
				alertRemovalTimer = new AlertRemovalTimer();
			}
			alertRemovalTimer.schedule( ALERT_REMOVAL_TIME_OUT_MILLISEC );
		} else {
			//If we set the site image without the alert
			//and there is an alert timer then cancel it.
			if( alertRemovalTimer != null ) {
				alertRemovalTimer.cancel();
				alertRemovalTimer = null;
			}
		}
		anchorLink.setHTML( imageString );
		//Remember the new alert state
		isSectionAlertOn = doAlert;
	}
	
	/**
	 * Allows to process the history token, and to select the given site section
	 * if the history token corresponds to it. 
	 * @param historyToken the history token
	 * @param reportDisabled if true then we notify the user that the site section could not be selected as it is disabled
	 * @return true if the history token corresponds to the given site section and the section was selected
	 * if the site section is disabled, then this method returns false and warns the user with the error dialog
	 */
	public boolean processHystoryToken( final String historyToken, final boolean reportDisabled ) {
		final String siteSectionHistoryPrefix = getSiteBodyComponentHistoryPrefix();
		if( ( historyToken != null) && historyToken.startsWith( siteSectionHistoryPrefix ) ) {
			//If the entry is not disabled and is not yet selected then select it
			if( ! isDisabled ){
				//Set this site section as selected
				siteNavigator.setSelectedSiteSection( this );
				//Process the history token by the body component
				getBodyContentWidget().processHistoryToken( historyToken.substring( historyToken.indexOf( CommonResourcesContainer.SITE_SECTION_HISTORY_DELIMITER ) + 1 ) );
				//The site section did get to be selected
				return true;
			} else {
				if( reportDisabled ) {
					(new SplitLoad( true ) {
						@Override
						public void execute() {
							//Report the user that this section is only available after the login
							ErrorMessagesDialogUI.openErrorDialog( new UserStateException( UserStateException.SITE_SECTION_NO_ACCESS_USER_IS_NOT_LOGGED_IN_ERR ) );
						}
					}).loadAndExecute();
				}
				//The site section did not get to be selected
				return false;
			}
		} else {
			//The site section did not get to be selected
			return false;
		}
	}
	
	@Override
	public void onBrowserEvent(Event event) {
		switch ( DOM.eventGetType( event ) ) {
			case Event.ONMOUSEUP:
			case Event.ONDBLCLICK:
			case Event.ONCONTEXTMENU:
				//Update the Anchor link URL
				getBodyContentWidget().updateTargetHistoryToken( anchorLink );
			default:
				//Do nothing
		}
	}
	
	/**
	 * Allows to get the user status that should be forced when this site section is selected
	 * @return the user status that should be forced when this site section is selected
	 */
	protected UserStatusType getUserStatusToForce() {
		return forceUserStatus;
	}
	
	/**
	 * This method allows to set this element as selected or not selected
	 * @param select if true then the element is set to be selected
	 */
	public void setSelected( final boolean select ) {
		//NOTE: The best way is to clean the site body panel as soon as the newly selected Navigation Element is found
		if( select ) {
			//If the component is not currently selected
			if( ! this.isSelected ) {
				//Remove the old site body component if any
				Widget currentBodyComponent = siteBodyPanel.getWidget();
				if( ( currentBodyComponent != null ) && ( currentBodyComponent instanceof SiteBodySectionContent ) ) {
					//Notify the site body component that it is going to be removed
					( ( SiteBodySectionContent ) currentBodyComponent ).onBeforeComponentIsRemoved();
				}
				siteBodyPanel.clear();
				
				//Notify the body component that it is going to be selected.
				getBodyContentWidget().onBeforeComponentIsAdded();
				
				//Add the component to the site's body panel
				siteBodyPanel.add( (Widget) getBodyContentWidget() );
				
				//Notify the body component that it has just been selected.
				getBodyContentWidget().onAfterComponentIsAdded();
				
				//Set the site navigator's item as selected, removes the alert, if any
				setNavigatorImage( selectedImageURL, CommonResourcesContainer.NAVIGATION_IMAGE_ENTRY_SELECTED_STYLE, false );
				
				//Change the site background for this section
				SiteDynamicDecorations.showSiteBackground( isWithBackground() );
				
				//Ensure delayed loading of the java script code
				final SplitLoad loader = new SplitLoad(){
					@Override
					public void execute() {
						//Force the user status
						UserStatusManager.getUserStatusQueue().setCurrentUserStatus( getUserStatusToForce( ) );
					}
				};
				loader.loadAndExecute();
			} else {
				//NOTE: We want to select a component that is already selected, do nothing
			}
		} else {
			//Set the image to be in the unselected mode, removes the alert, if any
			setNavigatorImage( unselectedImageURL, CommonResourcesContainer.NAVIGATION_IMAGE_ENTRY_STYLE, false );
		}
		this.isSelected = select;
	}
	
	/**
	 * Allows to set the logged in status for the component
	 * @param isLoggedIn true if this is the user is logged in, false if he is logged out
	 */
	public void setLoggedIn( final boolean isLoggedIn ) {
		this.isLoggedIn = isLoggedIn;
		if( isLoggedIn ) {
			//After logging in all of the site sections are available
			this.isDisabled = false;
			//Notify the body component that the user is logged in
			getBodyContentWidget().setUserLoggedIn();
		} else {
			//After logging out only some of the site sections are available
			this.isDisabled = isOnLogOutDisabled;
			//Notify the body component that the user is logged out
			getBodyContentWidget().setUserLoggedOut();
		}
	}
	
	/**
	 * Allows to update the navigator's image with adding/removing the alert for the section.
	 * @param isAlertOn true to set the alert on, false to set it off
	 */
	public void alertSiteSection( boolean isAlertOn ) {
		if( isSectionAlertOn != isAlertOn ) {
			//Set the alert on or off
			if( isSelected ) {
				setNavigatorImage( selectedImageURL, CommonResourcesContainer.NAVIGATION_IMAGE_ENTRY_SELECTED_STYLE, isAlertOn );
			} else {
				setNavigatorImage( unselectedImageURL, CommonResourcesContainer.NAVIGATION_IMAGE_ENTRY_STYLE, isAlertOn );
			}
		}
	}
	
	/**
	 * Allows to test is this site's section is selected
	 * @return true if the site's section is selected, otherwise false
	 */
	public boolean isSelected() {
		return isSelected;
	}
	
	/**
	 * Allows to test is this site's section is disabled
	 * @return true if the site's section is disabled, otherwise false
	 */
	public boolean isDisabled() {
		return isDisabled;
	}
	
	/**
	 * The unique site section identifier String
	 * @return the unique site section identifier String  
	 */
	public abstract String getSiteSectionIdentifier();
	
	/**
	 * This string should end with SITE_SECTION_HISTORY_DELIMITER.
	 * @return the history prefix for the site body component. 
	 */
	public abstract String getSiteBodyComponentHistoryPrefix();
	
	/**
	 * Allows to get the site body widget related to this navigator element
	 * @param the site body widget
	 */
	public abstract BodyContentWidgetType getBodyContentWidget();
	
	/**
	 * This method allows to detect if the given site section should be shown
	 * with the background or not.
	 * 
	 * WARNING: The result of this function must be constant all the time!
	 * 			I.e. it must not change from call to call.
	 * 
	 * @return if true then this site section is shown with a background
	 */
	public abstract boolean isWithBackground();
}
