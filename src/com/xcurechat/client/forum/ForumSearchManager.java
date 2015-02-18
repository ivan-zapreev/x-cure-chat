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
 * The forum interface package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.forum;

import com.google.gwt.user.client.Window;
import com.xcurechat.client.SiteManager;

import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.search.ForumSearchData;
import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;
import com.xcurechat.client.dialogs.system.messages.InfoMessageDialogUI;

import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.rpc.ForumManagerAsync;
import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.MessagesPanel;
import com.xcurechat.client.utils.widgets.PageListWidget;

/**
 * @author zapreevis
 * This class represents the forum manager that interconnects all forum UI components,
 * i.e. contains logics relating the forum body elements
 */
public final class ForumSearchManager {
	
	//The forum's search panel
	private static ForumSearchPanel searchPanel = null;
	//The forum's message stack navigator
	private static MessagesStackNavigator stackNavigator = null;
	//The forum's messages panel
	private static MessagesPanel messagesPanel = null;
	//The forum's body widget
	private static ForumBodyWidget forumBodyWidget = null;
	
	//The maximum page index for the current search result
	private static int maximumPageIndex = ForumSearchData.MINIMUM_PAGE_INDEX;
	
	/**
	 * The only constructor of the Forum manager
	 */
	private ForumSearchManager() {
	}
	
	/**
	 * Must be called once in order to set up the UI components to enable search
	 */
	public static void setForumUIComponents( final ForumSearchPanel searchPanelArg, 
											 final MessagesStackNavigator stackNavigatorArg,
											 final MessagesPanel messagesPanelArg, 
											 final ForumBodyWidget forumBodyWidgetArg ) {
		searchPanel = searchPanelArg;
		stackNavigator = stackNavigatorArg;
		messagesPanel = messagesPanelArg;
		forumBodyWidget = forumBodyWidgetArg;
	}
	
	/**
	 * Does the same search as was done the last time. Should be used for
	 * the refresh purposes. Makes sure that the proper history token is set.
	 */
	public static void doSearch( ) {
		doSearch( stackNavigator.getCurrentMessageStackElement() );
	}
	
	/**
	 * Does the same search as was done the last time but goes to a
	 * the first page. Makes sure that the proper history token is set.
	 * @param newPageIndex the page we navigate to
	 */
	public static void doFirstPageSearch( ) {
		doSearch( ForumSearchData.MINIMUM_PAGE_INDEX );
	}
	
	/**
	 * Initiates the search and then updates the forum components
	 * The forum components should be set disabled before this
	 * method is called. After the search is done this method will enable them
	 * NOTE: the search data should be validated beforehand.
	 * Makes sure that the proper history token is set.
	 * THis method forces the search no matter what.
	 * @param element the message stack element that contains all
	 * of the needed search data.
	 */
	public static void doSearch( final MessageStackElement element ) {
		doSearch( element, true );
	}
	
	/**
	 * Does the same search as was done the last time but goes to a
	 * different page. Makes sure that the proper history token is set.
	 * @param newPageIndex the page we navigate to
	 */
	private static void doSearch( final int newPageIndex ) {
		//Get the current message stack element
		final MessageStackElement element = stackNavigator.getCurrentMessageStackElement();
		if( element != null ) {
			//WARNING: Clone the data here, to prevent spoiling the original object! 
			final ForumSearchData searchData = element.getSearchData().clone();
			
			if( ( newPageIndex >= ForumSearchData.MINIMUM_PAGE_INDEX ) && ( newPageIndex <= maximumPageIndex ) ) {
				searchData.pageIndex = newPageIndex;
			}
			
			//NOTE: In case the page index is out of bounds then we just perform the same search
			doSearch( new MessageStackElement( searchData, element.getMessageData() ) );
		}
	}
	
	/**
	 * Initiates the search and then updates the forum components
	 * The forum components should be set disabled before this
	 * method is called. After the search is done this method will
	 * enable them
	 * NOTE: the search data should be validated beforehand.
	 * Makes sure that the proper history token is set.
	 * @param element the message stack element that contains all
	 * 					of the needed search data.
	 * @param forceRefresh if true then even if we are already
	 * 					have the search results set, we update them
	 */
	public static void doSearch( final MessageStackElement element, final boolean forceRefresh ) {
		setEnabled( false );
		
		try {
			//Just in case, we first validate the search parameters
			final ForumSearchData searchData = element.getSearchData();
			searchData.validate();
			
			//Just and extra check, this check should never be triggered in the production code
			if( ( stackNavigator == null ) || ( searchPanel == null ) || ( messagesPanel == null ) ) {
				Window.alert( "The forum manager is not set up, either the stack navigator or the search panel or the messages panel is null" );
				return;
			}
			
			if( forceRefresh || stackNavigator.isNewSearchNeeded( element ) ) {
				//Force the search parameters into the interface of the SearchPanel
				//For this the search data should contain transient userLoginName.
				//NOTE: Do not update the search setting when we browse the replies
				if( ! element.isMessageReply() ) {
					searchPanel.enforceSearchParams( searchData ); 
				}
				
				messagesPanel.prepareForAnUpdate( null );	//Clear the old messages
				
				//Update the message stack panel with the new search result settings
				stackNavigator.addStackMessageElement( element );
				
				//Ensure lazy loading
				(new SplitLoad( true ){
					@Override
					public void execute() {
						//Perform the server call in order to search for forum messages
						CommStatusAsyncCallback<OnePageViewData<ForumMessageData>> callback = new CommStatusAsyncCallback<OnePageViewData<ForumMessageData>>( searchPanel.getProgressBarUI() ) {
							public void onSuccessAct(final OnePageViewData<ForumMessageData> result) {
			
								//Update the list of messages in the messages Panel
								messagesPanel.setNewMessages( searchData, result );
								maximumPageIndex = PageListWidget.computeMaximumPageIndex(result);
								
								//Enable the controls
								setEnabled( true );
								
								(new SplitLoad( true ) {
									@Override
									public void execute() {
										//Let the user know that there are no search results found
										if( result.total_size == 0 ) {
											if( element.isMessageReply() ) {
												if( element.isSectionTopicsView() ) {
													InfoMessageDialogUI.openInfoDialog( I18NManager.getInfoMessages().noTopicsInThisForumSection() );
												} else {
													if( element.isTopicMessagesView() ) {
														InfoMessageDialogUI.openInfoDialog( I18NManager.getInfoMessages().noPostsInThisForumTopic() );
													} else {
														InfoMessageDialogUI.openInfoDialog( I18NManager.getInfoMessages().noRepliesToThisForumMessage() );
													}
												}
											} else {
												InfoMessageDialogUI.openInfoDialog( I18NManager.getInfoMessages().noSearchResultsForTheQuery() );
											}
										}
									}
								}).loadAndExecute();
							}
							public void onFailureAct(final Throwable caught) {
								(new SplitLoad( true ) {
									@Override
									public void execute() {
										//Report the error
										ErrorMessagesDialogUI.openErrorDialog( caught );
									}
								}).loadAndExecute();
								//Use the recovery method
								recover();
							}
						};
						ForumManagerAsync forumManagerObj = RPCAccessManager.getForumManagerAsync();
						forumManagerObj.searchMessages(SiteManager.getUserID(), SiteManager.getUserSessionId(), searchData, callback );			
					}
					@Override
					public void recover() {
						//Enable control elements
						setEnabled( true );
					}
				}).loadAndExecute();
			} else {
				//In case the update is not needed, re-enable the controls
				setEnabled( true );				
			}
		} catch( final SiteException e) {
			//Re-enable the controls
			setEnabled( true );				
			//Should not be happening unless we are doing a search from the history
			//token and some one has made it contain illegal search parameters.
			(new SplitLoad( true ) {
				@Override
				public void execute() {
					//Report the error
					ErrorMessagesDialogUI.openErrorDialog( e );
				}
			}).loadAndExecute();
		}
	}
	
	/**
	 * Allows to set all of the forum UI components into the enabled/disabled mode
	 * @param enabled true to enable, to disable false
	 */
	private static void setEnabled( final boolean enabled ) {
		if( forumBodyWidget != null ) {
			forumBodyWidget.setEnabled( enabled );
		} else {
			//This should not be happening
			Window.alert( "The forum search manager does not reference the forum body widget!" );
		}
	}

}