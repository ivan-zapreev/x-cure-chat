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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.dialogs.system.messages.InfoMessageDialogUI;

import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.userstatus.UserStatusManager;
import com.xcurechat.client.userstatus.UserStatusWidget;

import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.UserTreasureWidget;

/**
 * @author zapreevis
 * This class represents the site's three level menu
 */
public class ThreeLevelSiteMenu extends Composite implements SiteTitleComponent {
	
	//The panel storing the logged in user status panel
	private final SimplePanel userStatusPanel = new SimplePanel();
	
	//The user treasure wallet panel
	private final SimplePanel userTreasurePanel = new SimplePanel();
	
	//The user status manager
	private UserStatusManager userStatusManager = null;
	
	//The main menu of the site
	private final MainSiteMenuUI theMainMenuUI = MainSiteMenuUI.getMainSiteMenuUI();
	
	//The panel storing the 'help' item
	private final SimplePanel helpItemPanel = new SimplePanel();
	
	//The panel storing the 'new message alert' item
	private final SimplePanel alertWidgetPanel = new SimplePanel();
	
	//Stores the internal "is user logged in status", initially the user is not logged in
	private boolean isUserLoggedIn = false;
	
	/**
	 * The leveled site menu. It has the user status manager
	 * that has to be instantiated prior to instantiating this class
	 */
	public ThreeLevelSiteMenu( ) {
		//Position the choice of locale and the menu one above another
		final VerticalPanel menuLevelsPanel = new VerticalPanel();
		menuLevelsPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT );
		
		//Add the user statistics menu level
		final HorizontalPanel topLevel = new HorizontalPanel();
		topLevel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		topLevel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		SiteInfoWidget statisticsWidget = new SiteInfoWidget();
		statisticsWidget.startUpdates(); //Start the updates
		topLevel.add( statisticsWidget ); //Add the user statistics widget
		topLevel.add( InterfaceUtils.getLocaleSelectionPanel() ); //Add the locale selector
		menuLevelsPanel.add( topLevel );
		
		//Add the user info menu level
		final HorizontalPanel userAndLocalePanel = new HorizontalPanel();
		userAndLocalePanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		userAndLocalePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		userAndLocalePanel.add( userStatusPanel );							//Add the user status panel
		userAndLocalePanel.add( userTreasurePanel );						//The user treasure wallet panel
		userAndLocalePanel.add( alertWidgetPanel );      					//Add the sound notifier button panel
		userAndLocalePanel.add( helpItemPanel );							//Add the help link panel
		menuLevelsPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT );
		menuLevelsPanel.add( userAndLocalePanel );
		
		//Add the main menu level
		theMainMenuUI.setLoggedOutMenu();
		menuLevelsPanel.add( theMainMenuUI );
		menuLevelsPanel.setWidth("100%");
		
		//Initialize the composite
		initWidget( menuLevelsPanel );
	}

	public HorizontalAlignmentConstant getHorizontalAlignment() {
		return HasHorizontalAlignment.ALIGN_RIGHT;
	}

	public double getTitlePanelWidthInPercent() {
		//No specific width value here
		return -1.0;
	}

	public void onWindowResize() {
		//NOTE: Nothing to be done here
	}

	public void setLoggedIn( final MainUserData mainUserData ) {
		if( ! isUserLoggedIn ) {
			//Ensure delayed loading the this java script code
			final SplitLoad loader = new SplitLoad(){
				@Override
				public void execute() {
					//Initialize the status manager
					userStatusPanel.clear();
					//Get the instance of the user status manager
					userStatusManager = UserStatusManager.getInstance( );
					if( userStatusManager != null ) {
						UserStatusWidget userStatusWidget = userStatusManager.getUserStatusWidget( mainUserData.getShortLoginName() );
						userStatusWidget.setStyleName( CommonResourcesContainer.USER_STATUS_WIDGET_EXTRA_STYLE );
						userStatusPanel.add( userStatusWidget );
						userStatusManager.start(); //Start the user status manager
					}
					
					//Add the user treasure widget
					userTreasurePanel.clear();
					userTreasurePanel.add( UserTreasureWidget.getInstance() );
					
					//Add the new message alert notifier and start it
					alertWidgetPanel.clear();
					final NewMessageAlertWidget alertNotifierWidget = NewMessageAlertWidget.getInstance();
					alertWidgetPanel.add( alertNotifierWidget );
					alertNotifierWidget.start();
					
					//Add the help item to the panel
					final Label helpItemLabel = new Label( I18NManager.getTitles().helpMenuItem() );
					helpItemLabel.addClickHandler( new ClickHandler() {
			    		public void onClick( ClickEvent e) {
			    			//Show the help message
			    			(new SplitLoad( true ) {
			    				@Override
			    				public void execute() {
					    			InfoMessageDialogUI.openInfoDialog( I18NManager.getInfoMessages().helpMessageHTML(), true);
			    				}
			    			}).loadAndExecute();
			    		}
			    	});
					//Add the help label specific alternations to the style
					helpItemLabel.setStyleName( CommonResourcesContainer.HELP_ITEM_STYLE_STYLE );
					helpItemPanel.add( helpItemLabel );
					
					//Set the main site menu into the logged-in mode
					theMainMenuUI.setLoggedInMenu( mainUserData.getUserLoginName(), mainUserData.getUserProfileType() );
				}
			};
			loader.execute();
		}
		
		//Set up the user login status
		isUserLoggedIn = true;
	}

	public void setLoggedOut() {
		if( isUserLoggedIn ) {
			//Ensure delayed loading the this java script code
			final SplitLoad loader = new SplitLoad(){
				@Override
				public void execute() {
					//Stop the status manager and remove it
					if( userStatusManager != null ) {
						userStatusManager.stop();
					}
					userStatusPanel.clear();
					
					//Remove the user treasure widget
					userTreasurePanel.clear();
					
					//Remove the new message alert notifier and stop it
					alertWidgetPanel.clear();
					NewMessageAlertWidget.getInstance().stop();
					
					//Remove the help item from the panel
					helpItemPanel.clear();
					
					//Set the main site menu into the logged-out mode
					theMainMenuUI.setLoggedOutMenu();
				}
			};
			loader.execute();
		}
		
		//Set up the user login status
		isUserLoggedIn = false;
	}

}
