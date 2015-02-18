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
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;

import com.google.gwt.user.client.Window;

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.PopupPanel;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.UserManagerAsync;
import com.xcurechat.client.rpc.exceptions.UserStateException;

import com.xcurechat.client.userstatus.UserStatusManager;
import com.xcurechat.client.utils.SmileyHandlerUI;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.UserTreasureWidget;

import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.decorations.SiteDynamicDecorations;

import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;
import com.xcurechat.client.dialogs.system.messages.InfoMessageDialogUI;

/**
 * @author zapreevis
 * This is the main class of the UI interface, it contains all other UI elements
 * This is a singleton class, because there should not be more than one instances
 */
final public class SiteManagerUI extends Composite {
	//This is the main site focus panel that is needed for the auto-away user 
	//status component. Defined as static because we need it in lazy loading.
	private static final FocusPanel theMainFocusPanel = new FocusPanel();
	//The main site glass panel that is shown on site component loading
	//Defined as static because we need it in lazy loading.
	private static final SiteLoadingGlassPanel loadingGlassPanel = new SiteLoadingGlassPanel( theMainFocusPanel );
	//The number of active loadings that are currently in progress, this
	//is needed for properly showing and hiding the glass panel
	private static int activeInstancesCounter = 0;
	
	/**
	 * Allows to show/hide the site component loading glass panel
	 * Takes care about the number of currently loading instances
	 * @param visible true to show, false to hide
	 */
	public static void setGlassPanelVisible( final boolean visible ) {
		if( visible ) {
			//If the glass panel should be shown
			
			//If it is not currently shown, then show it
			if( activeInstancesCounter == 0 ) {
				loadingGlassPanel.setVisible( true );
			}
			//Increment the active instances counter
			activeInstancesCounter++;
		} else {
			//If the glass panel should be hidden

			//Decrement the active instances counter
			activeInstancesCounter--;
			
			//If there are no more active instances then hide the panel
			if( activeInstancesCounter == 0 ) {
				loadingGlassPanel.setVisible( false );
			}
		}
	}
	
	//The only instance of this class
	private static final SiteManagerUI siteManagerUIObj = new SiteManagerUI(); 
	
	//The horizontal panel that will contain the site title and the main menu.
	private HorizontalPanel siteTitlePanel = new HorizontalPanel(); 
	//The vertical panel that contains the title, the menu and the room manager
	private VerticalPanel siteVerticalPanel = new VerticalPanel();
	//The site body panel
	private final SimplePanel bodyPanel = new SimplePanel();
	//The site navigator
	private final SiteNavigator siteNavigator = new SiteNavigator( bodyPanel );
	//The list that will store the site's title components
	//WARNING: These widgets have to implement SiteTitleComponent interface
	private final List<Widget> siteTitleComponents = new ArrayList<Widget>();
	//The list of all popup panels that are currently open
	private final List<PopupPanel> openedPopUps = new ArrayList<PopupPanel>();
	//This marker stores true if the closing all procedure is active for the pop-up windows
	private boolean isCloseAllPopupInProgress = false;
	
	/**
	 * The simple constructor.
	 */
	private SiteManagerUI(){
		//Call the super constructor
		super();
		
		//This listener is needed to keep the element's size in synch when the window is resized
		Window.addResizeHandler( new ResizeHandler(){
			public void onResize(ResizeEvent e){
				updateMainElementsSize();
			}
		});
		
		//Set the sizes
		siteVerticalPanel.setSize("100%", Window.getClientHeight()+"px");
		//Set the width of the title panel and its style
		siteTitlePanel.setWidth("100%");
		siteTitlePanel.addStyleName( CommonResourcesContainer.MAIN_SITE_TITLE_MENU_PANEL_STYLE );
		siteVerticalPanel.setSize("100%", "100%");
		siteVerticalPanel.add(siteTitlePanel);
		//Set up the main background image
		siteVerticalPanel.add( SiteDynamicDecorations.getBackgroundImagePanel() );
		//Add the site body panel
		siteVerticalPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		bodyPanel.setWidth("100%");
		siteVerticalPanel.add( bodyPanel );
		//Add the glass panel
		siteVerticalPanel.add( loadingGlassPanel );
		
		//Add the main focus panel for making the auto user status possible
		//The main panel then will contain all of the UI elements
		theMainFocusPanel.add( siteVerticalPanel );
		
		//Create and start the dynamic update timer
		SiteDynamicDecorations.getInstance().schedule(10);
		
		//Add the site title components
		//addSiteTitleComponent( new RadioPlayerComposite() );
		addSiteTitleComponent( new SiteTitle() );
		addSiteTitleComponent( siteNavigator );
		addSiteTitleComponent( new ThreeLevelSiteMenu( ) );
		
		//Check if the user is already logged in, if yes then set it
		//Try to set the user from cookie, this completes the interface
		//initialization and then we can take care of the old history token
		setLoggedInUserFromCookie();
		
		//Initialize the composite widget
		initWidget( theMainFocusPanel );
	}
	
	/**
	 * Allows to get the main focus panel of the website, this is useful for the user status and alert widgets
	 * @return the main focus panel of the site, not null
	 */
	public FocusPanel getMainFocusPanel() {
		return theMainFocusPanel;
	}
	
	private void setLoggedInUserFromCookie(){
		if( SiteManager.isUserLoggedIn() ){
			final String userLoginName = SiteManager.getUserLoginName();
			final int userID = SiteManager.getUserID();
			final String userSessionId = SiteManager.getUserSessionId();
			final int userProfileType = SiteManager.getUserProfileType();

			//Ensure lazy loading
			(new SplitLoad(){
				@Override
				public void execute() {
					AsyncCallback<Void> callback = new AsyncCallback<Void>() {
						public void onSuccess(Void result) {
							//If the user sessionId for the userLoginName is still valid
							//Then set this user up as if it was logged in automatically
							MainUserData userData = new MainUserData();
							userData.setUserLoginName( userLoginName );
							userData.setUID( userID );
							userData.setUserSessionId( userSessionId );
							userData.setUserProfileType( userProfileType );
							//Set the logged-in user
							setLoggedInUser( userData );
						}
						public void onFailure(final Throwable caught) {
							//If there were some errors, we will still show them,
							//Unless of course this is not a user state exception
							//then we just log the user out.
							if( ! ( caught instanceof UserStateException ) ) {
								(new SplitLoad( true ) {
									@Override
									public void execute() {
										ErrorMessagesDialogUI.openErrorDialog( caught );
									}
								}).loadAndExecute();
							}
							//Do the recovery
							recover();
						};
					};
					
					//Validate the user login/sessionId pare on the server
					UserManagerAsync userMNGService = RPCAccessManager.getUserManagerAsync();
					userMNGService.validate( userID, userSessionId, callback );
				}
				
				@Override
				public void recover() {
					//The user session was invalidated remove the user
					removeLoggedInUser();
				}
			}).loadAndExecute();
		} else {
			removeLoggedInUser();
		}
	}
	
	/**
	 * Allows to set the main sizes for the panels and other elements
	 */
	private void updateMainElementsSize() {
		//Set the main panel size
		siteVerticalPanel.setSize("100%", "100%");
		//Set the width of the title panel
		siteTitlePanel.setWidth("100%");
		//Update the size of the background image
		SiteDynamicDecorations.updateMainImagesAndSizes();
		//Update the site title components
		Iterator<Widget> iter = siteTitleComponents.iterator();
		while( iter.hasNext() ) {
			( (SiteTitleComponent) iter.next() ).onWindowResize();
		}
		//Update the smiley sizes in the messages
		SmileyHandlerUI.adjustSmileySizes();
		//Update the size of the glass panel if it is shown
		loadingGlassPanel.updateUIElements();
	}
	
	/**
	 * Allows to register a new pop-up window
	 * @param newWindow the new popup window to register
	 */
	public void registerPopup( PopupPanel newWindow ) {
		if( ! isCloseAllPopupInProgress ) {
			openedPopUps.add( newWindow );
		}
	}
	
	/**
	 * Allows to unregister a new pop-up window
	 * @param newWindow the new popup window to unregister
	 */
	public void unregisterPopup( PopupPanel newWindow ) {
		if( ! isCloseAllPopupInProgress ) {
			openedPopUps.remove( newWindow );
		}
	}
	
	public void closeAllRegisteredPopups() {
		//Set the closing all marker
		isCloseAllPopupInProgress = true;
		//Iterate through all the popups and close them, Iterate the list backwards
		//because we typically have a stack of dialog windows that are open
		ListIterator<PopupPanel> iterWindows = openedPopUps.listIterator( openedPopUps.size() );
		while( iterWindows.hasPrevious() ) {
			try {
				iterWindows.previous().hide();
			} catch( Throwable e) {
				//DO nothing if the exceptions happened then we window is probably already closed or smth.
			}
		}
		//Clear the list of popups
		openedPopUps.clear();
		//Remove the closing all marker
		isCloseAllPopupInProgress = false;
	}
	
	/**
	 * After a successful login or registration this method should be
	 * called to change the interface to the registered-user area.
	 * @param mainUserData the user object with the login name,
	 * user id, session id and profile type
	 */
	public void setLoggedInUser( final MainUserData mainUserData ) {
		//Update the site manager, set the logged in user
		SiteManager.setLoggedInUser( mainUserData );
		
		//Ensure delayed loading the this java script code
		final SplitLoad loader = new SplitLoad(){
			@Override
			public void execute() {
				//Set the user treasure 
				UserTreasureWidget.getInstance().setGoldPieceCount( mainUserData.getGoldPiecesCount() );
				//Hook up the alert widget and the status widget to the main site panel by instantiating them
				NewMessageAlertWidget.getInstance();
				UserStatusManager.getInstance();
			}
		};
		loader.loadAndExecute();
		
		//Update the site title UI components
		Iterator<Widget> iter = siteTitleComponents.iterator();
		while( iter.hasNext() ) {
			Widget element = iter.next();
			SiteTitleComponent elementCast = ( SiteTitleComponent ) element;
			elementCast.setLoggedIn(mainUserData);
			if( elementCast.getTitlePanelWidthInPercent() > 0.0 ) {
				siteTitlePanel.setCellWidth( element, elementCast.getTitlePanelWidthInPercent()+"%" );
			}
			siteTitlePanel.setCellHorizontalAlignment( element, elementCast.getHorizontalAlignment() );
		}
		//Check of the first time help was shown to the user
		if(  !SiteManager.isFirstTimeHelpShown() ) {
			//Show the help message
			(new SplitLoad( true ) {
				@Override
				public void execute() {
					InfoMessageDialogUI.openInfoDialog( I18NManager.getInfoMessages().helpMessageHTML(), true);
				}
			}).loadAndExecute();
			//Set the cookie saying that the help was shown
			SiteManager.setFirstTimeHelpShown();
		}
	}

	/**
	 * Remove user data and change interface after the user has
	 * logged out from the server
	 */
	public void removeLoggedInUser() {
		//Update the site manager, remove the logged in user
		SiteManager.removeLoggedInUser();
		//Close all of the registered popup windows
		closeAllRegisteredPopups();
		//Update the site title UI components
		Iterator<Widget> iter = siteTitleComponents.iterator();
		while( iter.hasNext() ) {
			Widget element = iter.next();
			SiteTitleComponent elementCast = ( SiteTitleComponent ) element;
			elementCast.setLoggedOut();
			if( elementCast.getTitlePanelWidthInPercent() > 0.0 ) {
				siteTitlePanel.setCellWidth( element, elementCast.getTitlePanelWidthInPercent()+"%" );
			}
			siteTitlePanel.setCellHorizontalAlignment( element, elementCast.getHorizontalAlignment() );
		}
	}
	
	/**
	 * Allows to add a site title component to the interface
	 * @param titleComponent the title component to be added
	 */
	public void addSiteTitleComponent( final Widget titleComponent ) {
		//Add to the list of title components
		siteTitleComponents.add( titleComponent );
		//Add to the title panel
		siteTitlePanel.add( titleComponent );
		//Call the on resize here, just in case
		((SiteTitleComponent) titleComponent).onWindowResize();
	}
	
	/**
	 * Allows to get the only instance of the site navigator object
	 * @return the site navigator object
	 */
	public SiteNavigator getSiteNavigator() {
		return siteNavigator;
	}
	
	/**
	 * Alert the specified site section
	 * @param siteSectionIdentifier the identifier of the site section that should be alerted
	 */
	public void alertNonSelectedSiteSection(String siteSectionIdentifier) {
		siteNavigator.alertNonSelectedSiteSection( siteSectionIdentifier );
	}
	
	//The chat room client area bottom margin in pixels,
	//from the bottom of the browser's client area
	public static final int CLIENT_AREA_HEIGHT_MARGIN_BOTTOM = 20;
	
	/**
	 * Returns the instance of the Site Manager 
	 */
	public static SiteManagerUI getInstance(){
		return siteManagerUIObj;
	}
}
