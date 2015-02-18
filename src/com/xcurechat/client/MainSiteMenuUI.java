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

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.xcurechat.client.chat.ChatNavigatorElement;

import com.xcurechat.client.data.UserData;

import com.xcurechat.client.dialogs.profile.UserProfileDialogUI;
import com.xcurechat.client.dialogs.profile.UsersBrowsingDialogUI;

import com.xcurechat.client.dialogs.system.UserLoginDialogUI;
import com.xcurechat.client.dialogs.system.UserRegistrationDialogUI;
import com.xcurechat.client.dialogs.system.UserStatsViewerDialogUI;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.UserManagerAsync;
import com.xcurechat.client.rpc.exceptions.UserStateException;

import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.RadioPlayerComposite;

/**
 * @author zapreevis
 * This class represents the UI of the main site menu 
 */
public class MainSiteMenuUI extends Composite {
	
	//The timer that repeatedly checks for the new private messages on the server
	private NewMessagesUpdateTimer newMsgsUpdateTimer = null;
	
	/**
	 * Allows to speed up the new message updates
	 */
	public void speedUpMessageUpdates() {
		if( newMsgsUpdateTimer != null ) {
			newMsgsUpdateTimer.speedUpUpdates();
		}
	}
	
	/**
	 * Starts the checks for new messages, done by timer.
	 * Should be called when the user is logged in to the chat system.
	 */
	private void startNewMsgChecks() {
		final SplitLoad executor = new SplitLoad() {
			@Override
			public void execute() {
				//NOTE: We create a new timer object because otherwise its
				//the isRepeated stays true after we login and logout, and
				//thus there are no repeated message updates
				newMsgsUpdateTimer = new NewMessagesUpdateTimer( MainSiteMenuUI.this );
				newMsgsUpdateTimer.startUpdates();
			}
		};
		executor.loadAndExecute();
	}
	
	/**
	 * Stops the checks for new messages, done by timer.
	 * Should be called when the user is logged out from the chat system.
	 */
	void stopNewMsgChecks() {
		if( newMsgsUpdateTimer != null ) {
			//Stop the updates
			newMsgsUpdateTimer.stopUpdates();
			//NOTE: DIscard the object, because we
			//will create a new one if needed
			newMsgsUpdateTimer = null;
		}
	}
	
	/**
	 * Allows to set the number of unread messages for the site's menu
	 * @param unreadMsgNumber the number of unread messages
	 */
	void setNewNumberOfMessages( final int unreadMsgNumber ) {
		if( unreadMsgNumber == 0 ){
			//Set the simple "messages" title for the menu elements
			userMessagesMenuItem.setText( I18NManager.getTitles().messagesMenuItem() );
			userMessagesMenuItem.removeStyleName( CommonResourcesContainer.NEW_OFFLINE_MESSAGES_MENU_ELEMENT_STYLE );
		} else {
			//Set the "messages({0})" title for the menu elements 
			userMessagesMenuItem.setText( I18NManager.getTitles().messagesNewMenuItem( unreadMsgNumber ) );
			userMessagesMenuItem.addStyleName( CommonResourcesContainer.NEW_OFFLINE_MESSAGES_MENU_ELEMENT_STYLE );
		}
	}

	/**
	 * @author zapreevis
	 * Initiates the user-statistics procedure
	 */
	private class UserStatisticsCommand implements Command {
		public void execute() {
			final SplitLoad executor = new SplitLoad( true ) {
				@Override
				public void execute() {
					UserStatsViewerDialogUI statisticsDialog = new UserStatsViewerDialogUI(SiteManager.getUserID(), SiteManager.getUserLoginName(), null);
					statisticsDialog.show();
					statisticsDialog.center();
				}
			};
			executor.loadAndExecute();
		}
	};
    
	/**
	 * @author zapreevis
	 * Initiates the radios dialog
	 */
    private class RadiosCommand implements Command {
		public void execute() {
			final SplitLoad executor = new SplitLoad( true ) {
				@Override
				public void execute() {
					RadioPlayerComposite radiosDialog = new RadioPlayerComposite();
					radiosDialog.show();
					radiosDialog.center();
				}
			};
			executor.loadAndExecute();
		}
    };
	
	/**
	 * @author zapreevis
	 * Initiates the user-registration procedure
	 */
	private class UserRegisterCommand implements Command {
		public void execute() {
			final SplitLoad executor = new SplitLoad( true ) {
				@Override
				public void execute() {
					UserRegistrationDialogUI registrationDialog = new UserRegistrationDialogUI();
					registrationDialog.show();
					registrationDialog.center();
				}
			};
			executor.loadAndExecute();
		}
    };
    
	/**
	 * @author zapreevis
	 * Initiates the user-login procedure
	 */
	private class UserLoginCommand implements Command {
		public void execute() {
			final SplitLoad executor = new SplitLoad( true ) {
				@Override
				public void execute() {
					UserLoginDialogUI.openLoginDialog();
				}
			};
			executor.loadAndExecute();
		}
	};
  
	/**
	 * @author zapreevis
	 * Initiates the user-logout procedure
	 */
	private class UserLogoutCommand implements Command {
		public void execute() {
			final int userID = SiteManager.getUserID();
			final String userSessionId = SiteManager.getUserSessionId();
			if( ( userID != UserData.UNKNOWN_UID ) && ( userSessionId != null ) ){
				(new SplitLoad( true ) {
					@Override
					public void execute() {
						AsyncCallback<Void> callback = new AsyncCallback<Void>() {
							public void onSuccess(Void result) {
								//The user is successfully logged out change the interface
								SiteManagerUI.getInstance().removeLoggedInUser();
							}
							public void onFailure(final Throwable caught) {
								//If this was not the exception about the fact that the user was not logged in
								if( ! ( caught instanceof UserStateException ) ) {
									(new SplitLoad( true ) {
										@Override
										public void execute() {
											//Report the error
											ErrorMessagesDialogUI.openErrorDialog( caught );
										}
									}).loadAndExecute();
									//Do the recovery
									recover();
								}
							};
						};
						
						//Send the log-out request
						final UserManagerAsync userMNGService = RPCAccessManager.getUserManagerAsync();
						userMNGService.logout( userID, userSessionId, callback);
					}
					@Override
					public void recover() {
						//We will log out the user in any case
						SiteManagerUI.getInstance().removeLoggedInUser();						
					}
				}).loadAndExecute();
			} else {
				//This should not be happening, since this action must not
				//be available for the user, when it is not logged in.
				Window.alert("Logout fauled: The user is not logged in!");
			}
		}
	};

	/**
	 * @author zapreevis
	 * Initiates the procedure for managing user settings
	 */
	private class UserSettingsCommand implements Command {
		public void execute() {
			final SplitLoad executor = new SplitLoad( true ) {
				@Override
				public void execute() {
					UserProfileDialogUI profileDialog = new UserProfileDialogUI(null);
					profileDialog.show();
					profileDialog.center();
				}
			};
			executor.loadAndExecute();
		}
	};
	
	/**
	 * @author zapreevis
	 * Initiates the user manager dialog
	 */
	private class UserSearchCommand implements Command {
		public void execute() {
			final SplitLoad executor = new SplitLoad( true ) {
				@Override
				public void execute() {
					UsersBrowsingDialogUI userSearchDialog = new UsersBrowsingDialogUI(null);
					userSearchDialog.show();
					userSearchDialog.center();
				}
			};
			executor.loadAndExecute();
		}
	}
	
	/**
	 * @author zapreevis
	 * Initiates the user manager dialog
	 */
	private class RoomManagerCommand implements Command {
		public void execute() {
			ChatNavigatorElement.openRoomsManagerDialog();
		}
	}
	
	/**
	 * @author zapreevis
	 * Initiates the user private messages management dialog
	 */
	private class PrivateMessagesCommand implements Command {
		public void execute() {
			ChatNavigatorElement.openMessagesManagerDialog();
		}
	}
	
	//Allocate menu items with no commands
	private final MenuItem radioMenuItem = new MenuItem( I18NManager.getTitles().radiosMenuItem(), new RadiosCommand() );
	private final MenuItem registerMenuItem = new MenuItem( I18NManager.getTitles().registerMenuItem(), new UserRegisterCommand() );
	private final MenuItem loginMenuItem = new MenuItem( I18NManager.getTitles().loginMenuItem(), new UserLoginCommand() );
	private final MenuItem logoutMenuItem = new MenuItem( I18NManager.getTitles().logoutMenuItem(), new UserLogoutCommand() ); 
	private final MenuItem prefsMenuItem = new MenuItem( I18NManager.getTitles().prefsMenuItem(), new UserSettingsCommand() );
	private final MenuItem statisticsMenuItem = new MenuItem( I18NManager.getTitles().statisticsMenuItem(), new UserStatisticsCommand() );
	private final MenuItem roomManagerMenuItem = new MenuItem( I18NManager.getTitles().roomsMenuItem(), new RoomManagerCommand() );
	private final MenuItem userSearchMenuItem = new MenuItem( I18NManager.getTitles().usersMenuItem(), new UserSearchCommand() );
	private final MenuItem userMessagesMenuItem = new MenuItem( I18NManager.getTitles().messagesMenuItem(), new PrivateMessagesCommand() );

	//The top menu with login/logout/profile and etc entries
	private MenuBar theMainMenuBar = new MenuBar();

	private MainSiteMenuUI(){
		//Allocate all menu items
		
		//Set the menu bar size
		theMainMenuBar.setSize("100%", "90%");
		theMainMenuBar.setTitle( I18NManager.getTitles().mainMenuTitle() );
		
		//Add specific style for the radio menu item
		radioMenuItem.addStyleName( CommonResourcesContainer.RADIO_MENU_ITEM_SYTLE );
		
		theMainMenuBar.setAnimationEnabled(true);
		
		initWidget(theMainMenuBar);
	}
	
	//The single instance of the main menu
	private static final MainSiteMenuUI theMainMenuUI = new MainSiteMenuUI();
	
	/**
	 * @return the uniques instance of the side menu UI
	 */
	public static MainSiteMenuUI getMainSiteMenuUI() {
		return theMainMenuUI;
	}
	
	/**
	 * Set the logged in menu, based on user type
	 * @param userLoginName
	 * @param accountType
	 */
	public void setLoggedInMenu( final String userLoginName, final int accountType ){
		//Completely clear the menu object
		theMainMenuBar.clearItems();
		
		//Add functionality DEPENDING ON THE USER TYPE
		theMainMenuBar.addItem(userSearchMenuItem);
		theMainMenuBar.addItem(userMessagesMenuItem);
		theMainMenuBar.addItem(prefsMenuItem);
		theMainMenuBar.addItem(roomManagerMenuItem);
		if( SiteManager.isAdministrator() ) {
			//Only show the statistics menu for the administrator.
			theMainMenuBar.addItem(statisticsMenuItem);
		}
		theMainMenuBar.addItem(radioMenuItem);
		theMainMenuBar.addItem(logoutMenuItem);
		
		//Start the checks for new private messages
		startNewMsgChecks();
	}

	/**
	 * Set the default menu, for non-logged-in users
	 */
	public void setLoggedOutMenu(){
		//Stop the checks for new private messages
		stopNewMsgChecks();
		
		theMainMenuBar.clearItems();
		
		theMainMenuBar.addItem( registerMenuItem );
		theMainMenuBar.addItem( loginMenuItem );
	}

}
