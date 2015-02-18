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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.HTML;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

import com.xcurechat.client.data.SiteInfoData;
import com.xcurechat.client.dialogs.system.messages.InfoMessageDialogUI;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.UserManagerAsync;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This class represents a site-related (statistics) monitor showing e.g.
 * the number of registered users, users online and guests.
 */
public class SiteInfoWidget extends Composite {
	
	//Indicates the fact that the user has been notified about that the site version has changed
	private boolean isSiteVerAlerted = false;
	//The last known site version as came from the server
	private String lastKnownServerVersion = SiteInfoData.SITE_VERSION_ID;
	
	/**
	 * @author zapreevis
	 * This is the timer extension that probes the server for the statistics update every now and again
	 */
	private class ServerStatsUpdateTimer extends Timer {
		private boolean isFirstTime = true;
		
		private void scheduleRepeatingIfFirstTime() {
			if( isFirstTime ) {
				this.scheduleRepeating( SiteInfoData.SERVER_UPDATES_PERIODICITY_MILLISEC );
				isFirstTime = false;
			}
		}
		
		@Override
		public void run() {
			//Ensure lazy loading
			(new SplitLoad(){
				@Override
				public void execute() {
					AsyncCallback<SiteInfoData> callback = new AsyncCallback<SiteInfoData>() {
						@Override
						public void onSuccess( SiteInfoData result ) {
							//Process the results of the query
							
							//Manage the site versions check
							checkOnSiteVersions( result.serverSiteVersionId );
							
							//Set the field-label values
							siteVersionId.setText( SiteInfoData.SITE_VERSION_ID );   //Note that here we always display the client site version
							usersInTotal.setText( result.totalRegisteredUsers + "" );
							onlineUsers.setText( result.registeredUsersOnline + "" );
							onlineGuests.setText( ( result.visitorsOnline - result.registeredUsersOnline ) + "" );
							//Schedule another update
							scheduleRepeatingIfFirstTime();
						}

						@Override
						public void onFailure(Throwable caught) {
							//Do the recovery
							recover();
						}
					};
					
					//Perform the server call
					UserManagerAsync userManagerObject = RPCAccessManager.getUserManagerAsync();
					userManagerObject.getSiteUsersStatistics( SiteManager.getUserID(), SiteManager.getUserSessionId(), callback );
				}
				@Override
				public void recover() {
					//Do nothing, just schedule another update
					scheduleRepeatingIfFirstTime();
				}
			}).loadAndExecute();
		}
	}
	
	//Stores the site version id
	private final Label siteVersionId = new Label();
	//Stores the number of registered users
	private final Label usersInTotal = new Label();
	//Stores the number of online registered users
	private final Label onlineUsers = new Label();
	//Stores the number of online guests (non-registered users)
	private final Label onlineGuests = new Label();
	
	//The localization object
	private final UITitlesI18N i18nTitles = I18NManager.getTitles();
	
	//The timer that will do the updates
	private final ServerStatsUpdateTimer updateTimer = new ServerStatsUpdateTimer();
	
	/**
	 * The basic constructor
	 */
	public SiteInfoWidget() {
		HorizontalPanel mainPanel = new HorizontalPanel();
		mainPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT );
		mainPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		
		addNewFieldValueWidgetsPair( mainPanel, i18nTitles.siteVersionFieldLabel(), siteVersionId, true);
		addNewFieldValueWidgetsPair( mainPanel, i18nTitles.totalUsersFieldLabel(), usersInTotal, true);
		addNewFieldValueWidgetsPair( mainPanel, i18nTitles.onlineUsersFieldLabel(), onlineUsers, true);
		addNewFieldValueWidgetsPair( mainPanel, i18nTitles.onlineGuestsFieldLabel(), onlineGuests, false);
		
		//Initialize the Widget
		initWidget( mainPanel );
	}
	
	/**
	 * Allows to check that the server site version is the same as the client site version
	 * if not then it notifies the user about that the user interface has to be reloaded.
	 * Note that the notification is done once per UI instance and new site version.
	 * @param serverSiteVersionId the site version on the server
	 */
	private void checkOnSiteVersions( final String serverSiteVersionId ) {
		if( serverSiteVersionId != null ) {
			//If the received server version is valid
			if( ! serverSiteVersionId.equals( SiteInfoData.SITE_VERSION_ID ) ) {
				//If the server version is different from the client version
				if( ! isSiteVerAlerted || ! lastKnownServerVersion.equals( serverSiteVersionId ) ) {
					//If we did not alert the user about the site version change yet or
					//we did but the server site version has changed again then we alert
					
					//Alert the user about the site version change
					(new SplitLoad( true ) {
						@Override
						public void execute() {
							InfoMessageDialogUI.openInfoDialog( I18NManager.getInfoMessages().alertNewSiteVersionInfo(serverSiteVersionId), true);
						}
					}).loadAndExecute();
					
					//Mark the alert as performed and update the last known server version
					isSiteVerAlerted = true;
					lastKnownServerVersion =  serverSiteVersionId;
				}
			}
		} else {
			//This should not be happening
			Window.alert("The received site version is null!");
		}
	}
	
	/**
	 * Allows to add the field to the users statistics panel
	 */
	private void addNewFieldValueWidgetsPair( final HorizontalPanel panel, final String fieldName,
											  final Label valueLabel, final boolean addComma ) {
		final Label fieldLabel = new Label( fieldName );
		fieldLabel.setWordWrap( false );
		fieldLabel.addStyleName( CommonResourcesContainer.STATISTICS_PANEL_FIELD_STYLE );
		panel.add( fieldLabel );
		
		panel.add( new HTML("&nbsp;") );

		valueLabel.setWordWrap( false );
		valueLabel.setText("0"); //Set the initial value to be zero
		valueLabel.addStyleName( CommonResourcesContainer.STATISTICS_PANEL_VALUE_STYLE );
		panel.add( valueLabel );
		
		if( addComma ) {
			Label commaLabel = new Label(",");
			commaLabel.addStyleName( CommonResourcesContainer.STATISTICS_PANEL_FIELD_STYLE );
			panel.add( commaLabel );
		}
		panel.add( new HTML("&nbsp;") );
	}
	
	/**
	 * Allows to start the periodic updates of the statistics from the server
	 */
	public void startUpdates() {
		//Schedule the updates
		updateTimer.schedule(100);
	}
}
