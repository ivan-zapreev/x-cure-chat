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
package com.xcurechat.client.news;

import java.util.Date;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

import com.xcurechat.client.SiteBodySectionContent;
import com.xcurechat.client.SiteManager;

import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.search.ForumSearchData;
import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.rpc.ForumManagerAsync;
import com.xcurechat.client.rpc.RPCAccessManager;

import com.xcurechat.client.utils.ClientEncoder;
import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.EncoderInt;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.MessagesPanel;
import com.xcurechat.client.utils.widgets.ServerCommStatusPanel;
import com.xcurechat.client.utils.widgets.UserAvatarImageWidget;

/**
 * @author zapreevis
 * This class is responsible for showing the site latest news from the forum.
 * This class is a singleton
 */
public class LatestNewsBodyUI extends Composite implements SiteBodySectionContent, UserAvatarImageWidget.AvatarSpoilerChangeListener {
	
	//The main vertical panel storing the News-Section body components
    private final VerticalPanel mainVerticalPanel = new VerticalPanel();
	
	//The instance of the forum messages panel
	private final MessagesPanel messagesPanel;
	
	//The progress bar widget
	private final ServerCommStatusPanel progressBarUI = new ServerCommStatusPanel();
	
	//Stores the latest successful news search data
	private ForumSearchData latestSearchData = new ForumSearchData();
	
	//The encoder interface
	private static final EncoderInt encoder = new ClientEncoder();
	
	//Contains true if the news site section is currently selected (i.e. displayed)
	private boolean isNewsSectionSelected = false;
	
	//The counter for the number of search we are currently performing
	private int activeSearchCounter = 0;
	
	//The site section history prefix
	private final String siteSectionPrefix; 
	
	/**
	 * The basic constructor provided with the site section url prefix
	 * @param siteSectionPrefix the history token site section prefix
	 */
	LatestNewsBodyUI( final String siteSectionPrefix ) {
		super();
		
		//Store the data
		this.siteSectionPrefix = siteSectionPrefix;
		
		//Register as an avatar spoiler change listener
		UserAvatarImageWidget.addAvatarSpoilerChangeListener( this );
		
		//Instantiate the messages panel
		messagesPanel = new NewsMessagesPanel( true, false, false, siteSectionPrefix );
		
		//Construct the latest news default search data
		latestSearchData.baseMessageID = ForumSearchData.UNKNOWN_BASE_MESSAGE_ID_VAL;
		latestSearchData.isApproved = true; //Look for the last approved messages
		latestSearchData.pageIndex = 1;
		
		//Set the styles of the panel
		mainVerticalPanel.addStyleName( CommonResourcesContainer.NEWS_PANEL_STYLE );
		mainVerticalPanel.setHeight("100%");
		mainVerticalPanel.add( messagesPanel );
		
		//Initialize the composite
		initWidget( mainVerticalPanel );
	}
	
	/**
	 * Allows to request the server for the new data for the panel, using the current search data settings.
	 * Allows for concurrent searches, but tries to keep up with the most relevant
	 * @param newSearchData the search data for the search, should be a different object than latestSearchData!
	 * @param updateHistory if true then the history token is updated without an event, otherwise it is not updated at all
	 */
	private void updateLatestNews( final ForumSearchData newSearchData, final boolean updateHistory ) {
		//If there are no active searches at the moment or there is an active
		//search but for another search data then we perform the search 
		if( ( activeSearchCounter == 0 ) ||
			( ( activeSearchCounter > 0 ) &&
			  ( !latestSearchData.serialize(encoder).equals( newSearchData.serialize(encoder) ) ) ) ) {
			//Increment the active searches counter 
			activeSearchCounter++;
			
			//Disable the control elements
			setEnabled( false );
			
			//Store the latest search data, so that if a refresh happens afterwards, then we search data is up to date
			latestSearchData = newSearchData;
			
			//Clear the old messages, start the progress
			messagesPanel.prepareForAnUpdate( progressBarUI );
			
			//Ensure lazy loading
			(new SplitLoad( true ){
				@Override
				public void execute() {
					//Perform the server call in order to search for the recent approved forum messages
					CommStatusAsyncCallback<OnePageViewData<ForumMessageData>> callback = new CommStatusAsyncCallback<OnePageViewData<ForumMessageData>>(progressBarUI) {
						public void onSuccessAct(OnePageViewData<ForumMessageData> result) {
							//Check that the latest search token is still the one we have searched for
							if( latestSearchData == newSearchData ) {
								//Update the history token if the site section is selected
								if( updateHistory && isNewsSectionSelected ) {
									History.newItem( getTargetHistoryToken( newSearchData ), false );
								}
								//Update the list of messages in the messages Panel
								messagesPanel.setNewMessages( newSearchData, result );
								//Update the UI elements to restore sizes, just in case because In IE8 we get a zero width panel!
								updateUIElements();
								//Enable control elements
								setEnabled( true );
							}
							//Decrement the active searches counter 
							activeSearchCounter--;
						}
						public void onFailureAct( final Throwable caught ) {
							//Check that the latest search token is still the one we have searched for
							if( latestSearchData == newSearchData ) {
								(new SplitLoad( true ) {
									@Override
									public void execute() {
										//Report the error
										ErrorMessagesDialogUI.openErrorDialog( caught );
									}
								}).loadAndExecute();
							}
							//Use the recovery method
							recover();
						}
					};
					ForumManagerAsync forumManagerObj = RPCAccessManager.getForumManagerAsync();
					forumManagerObj.searchMessages(SiteManager.getUserID(), SiteManager.getUserSessionId(),  newSearchData, callback );
				}
				@Override
				public void recover() {
					//Check that the latest search token is still the one we have searched for
					if( latestSearchData == newSearchData ) {
						//Update the history token if the site section is selected
						if( updateHistory && isNewsSectionSelected ) {
							History.newItem( getTargetHistoryToken( newSearchData ), false );
						}
						//Enable control elements
						setEnabled( true );
					}
					//Decrement the active searches counter 
					activeSearchCounter--;
				}
			}).loadAndExecute();
		}
	}
	
	/**
	 * Allows to construct the history token from the search data
	 * @param searchData the search data
	 * @return the resulting history token
	 */
	private String getTargetHistoryToken(final ForumSearchData searchData) {
		//Construct the history token from the current search data
		return siteSectionPrefix + searchData.serialize( encoder );
	}
	
	/**
	 * Allows to update the panel's height on window resize
	 */
	@Override
	public void updateUIElements() {
		//We do the update in the deferred command because only this way 
		//the browser does it after the rendering of the view is complete
		Scheduler.get().scheduleDeferred( new ScheduledCommand(){
			@Override
			public void execute() {
				//MSIE and Opera somehow do not want to put the proper width for the panel,
				//it shows it with a zero width!!! So now we always adjust the width
				messagesPanel.updateUIElements( true, true, 60 );
			}
		} );
	}
	
	@Override
	public void setUserLoggedIn() {
		messagesPanel.setUserLoggedIn();
		
		//Get the update from the server, but do not update the history token
		//because we might be in another site section or if we are at this
		//site section then the history token must have been set before
		if( isNewsSectionSelected ) {
			updateLatestNews( latestSearchData.clone(), false );
		}
	}
	
	@Override
	public void setUserLoggedOut() {
		messagesPanel.setUserLoggedOut();
	}
	
	@Override
	public void onAfterComponentIsAdded() {
		//Re-set the panel size
		updateUIElements();
		
		//The news section is selected, i.e. displayed
		isNewsSectionSelected = true;
		
		//Get the update from the server, update the history
		//token because we changed the site section
		updateLatestNews( latestSearchData.clone(), true );
	}
	
	@Override
	public void setEnabled( final boolean enabled ) {
		messagesPanel.setEnabled( enabled );
	}

	@Override
	public void onBeforeComponentIsAdded() {
		//NOTE: Nothing to be done here
	}
	
	@Override
	public void onBeforeComponentIsRemoved() {
		messagesPanel.onBeforeComponentIsRemoved();
		
		//The news section is unselected, i.e. not displayed
		isNewsSectionSelected = false;
	}
	
	@Override
	public void processHistoryToken(String historyToken) {
		//Unpack the token into the search object
		final ForumSearchData newSearchData = ForumSearchData.deserialize(encoder, historyToken);
		newSearchData.baseMessageID = ForumSearchData.UNKNOWN_BASE_MESSAGE_ID_VAL;
		newSearchData.isApproved = true; //Force the approved messages just in case
		//Get the update from the server,do not update
		//the history token because it must be current
		updateLatestNews( newSearchData, false );
	}

	@Override
	public void updateTargetHistoryToken(Anchor anchorLink) {
		//Construct the history token from the current search data
		anchorLink.setHref( CommonResourcesContainer.URI_HASH_SYMBOL + getTargetHistoryToken( latestSearchData ) );
	}

	@Override
	public void avatarSpoilerChanged(int userID, int spoilerID, Date spoilerExpDate) {
		messagesPanel.updateUserAvatarSpoiler(userID, spoilerID, spoilerExpDate);
	}
}
