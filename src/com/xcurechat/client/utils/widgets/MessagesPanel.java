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
package com.xcurechat.client.utils.widgets;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;

import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.search.ForumSearchData;
import com.xcurechat.client.data.search.OnePageViewData;
import com.xcurechat.client.forum.ForumBodyComponent;

import com.xcurechat.client.forum.messages.ForumMessageWidget;
import com.xcurechat.client.forum.messages.ForumMessageWidgetFactory;

import com.xcurechat.client.utils.BrowserDetect;
import com.xcurechat.client.utils.InterfaceUtils;

/**
 * @author zapreevis
 * This widget stores the forum messages of the current page and page indexes 
 */
public class MessagesPanel extends ForumBodyComponent {
	private static final int MINIMUM_CLIENT_HEIGHT = 800;
	
	//The scroll panel storing the messages 
	private final ScrollPanel scrollPanel = new ScrollPanel();
	
	//The vertical panel with page indexes and messages panel
	private final VerticalPanel mainPanel = new VerticalPanel();
	
	//The vertical panel with messages
	private final VerticalPanel messagesPanel = new VerticalPanel();
	
	//True if the user is logged in
	private boolean isLoggedIn = false;
	
	//The list of currently shown messages
	private List<ForumMessageData> messagesList = null;
	
	//True if the messages contained in this panel should have action panels
	private final boolean showActionPanel;
	
	//True if the message titles should be allowed to be clickable
	private final boolean clickableMessageTitles;
	
	//The page-indexes list widgets, the top and the bottom ones
	private PageListWidget topPageIndexesWidget;
	private PageListWidget bottomPageIndexesWidget;

	//The site section prefix
	private final String siteSectionPrefix;
	
	/**
	 * This constructor has to be used for all the body components,
	 * it is provided with the forum manager
	 * @param addDecorations if true then we use the decorated panel with the rounded corners around this widget
	 * @param showActionPanel true if the action panel should be shown, otherwise false
	 * @param clickableMessageTitles if the message titles should be allowed to be clickable
	 * @param siteSectionPrefix the site section prefix for the section where this widget is placed
	 */
	public MessagesPanel( final boolean addDecorations, final boolean showActionPanel,
						  final boolean clickableMessageTitles, final String siteSectionPrefix ) {
		//Initialize the super class
		super( addDecorations );
		
		this.siteSectionPrefix = siteSectionPrefix;
		this.showActionPanel = showActionPanel;
		this.clickableMessageTitles = clickableMessageTitles;
	}
	
	/**
	 * Must be called in the constructor of the dub-class to complete the object creation
	 */
	protected void populate() {
		//Initialize the main panel
		mainPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		mainPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		mainPanel.setWidth("100%");
		mainPanel.setHeight("100%");
		
		//Add top page indexes panel
		topPageIndexesWidget = new PageListWidget( siteSectionPrefix );
		topPageIndexesWidget.addStyleName( CommonResourcesContainer.TOP_INDEX_WIDGETS_LIST_STYLE );
		mainPanel.add( topPageIndexesWidget );
		
		//Put the list of panels of messages into the scroll panel
		messagesPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_TOP );
		messagesPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		messagesPanel.setWidth("100%");
		scrollPanel.setStyleName( CommonResourcesContainer.SCROLLABLE_SIMPLE_PANEL );
		scrollPanel.setHeight( "100%" );
		scrollPanel.setWidth( "100%" );
		scrollPanel.add( messagesPanel );
		
		//Initialize the sizes and set the component elements
		mainPanel.add( scrollPanel );
		
		//Add bottom page indexes panel
		bottomPageIndexesWidget = new PageListWidget( siteSectionPrefix );
		bottomPageIndexesWidget.addStyleName( CommonResourcesContainer.BOTTOM_INDEX_WIDGETS_LIST_STYLE );
		mainPanel.add( bottomPageIndexesWidget );
		
		setComponentWidget( mainPanel );
		addDecPanelStyle( CommonResourcesContainer.FORUM_MESSAGES_PANEL_STYLE );
	}
	
	/**
	 * Allows to clear messages from the panel and add a progress bar if needed
	 * @param progressBarUI a progress bar for the update progress or null if it is not needed
	 */
	public void prepareForAnUpdate( ServerCommStatusPanel progressBarUI ) {
		messagesPanel.clear();
		if( progressBarUI != null ) {
			messagesPanel.add( progressBarUI );
		}
	}
	
	/**
	 * Allows to set new messages
	 * @param searchData the search data for which the messages were gathered
	 * @param messages the list of new messages from the server
	 */
	public void setNewMessages( final ForumSearchData searchData, OnePageViewData<ForumMessageData> messages ) {
		messagesPanel.clear();				//Clear messages before setting in the new ones
		messagesList = messages.entries;	//Update the list of currently shown messages
		boolean oddOrNot = true;
		for( ForumMessageData message : messages.entries ) {
			ForumMessageWidget messageWidget = ForumMessageWidgetFactory.getWidgetInstance( message, isLoggedIn, oddOrNot, showActionPanel, false, clickableMessageTitles );
			messagesPanel.add( messageWidget );
			oddOrNot = ! oddOrNot;
			messageWidget.adjustHeight();
		}
		//Scroll the messages to the top
		if( !messages.entries.isEmpty() ) {
			scrollPanel.ensureVisible( messagesPanel.getWidget(0) );
		}
		//Update the page indexes
		topPageIndexesWidget.updatePageIndexes(searchData, messages);
		bottomPageIndexesWidget.updatePageIndexes(searchData, messages);
	}
	
	@Override
	public void setEnabled( boolean enabled ) {
		for( int index = 0; index < messagesPanel.getWidgetCount(); index++ ) {
			Widget msg = messagesPanel.getWidget( index );
			if( msg instanceof ForumMessageWidget ) {
				((ForumMessageWidget)  messagesPanel.getWidget( index )).setEnabled( enabled );
			}
		}
		//Enable/Disable the page indexes panels
		topPageIndexesWidget.setEnabled(enabled);
		bottomPageIndexesWidget.setEnabled(enabled);
	}
	
	@Override
	public void onBeforeComponentIsRemoved() {
		//Close all non-auto-close dialogs opened from by component
		for( int index = 0; index < messagesPanel.getWidgetCount(); index++ ) {
			Widget msg = messagesPanel.getWidget( index );
			if( msg instanceof ForumMessageWidget ) {
				((ForumMessageWidget) msg ).closeDialogs();
			}
		}
	}
	
	@Override
	public void updateUIElements( final boolean forceScrolling, final boolean adjustWidth, final int percentWidth ) {
		//Adjust height
		if( forceScrolling || ( Window.getClientHeight() >= MINIMUM_CLIENT_HEIGHT ) ||
			BrowserDetect.getBrowserDetect().isSafari() ||			//WARNING: Chrome and Safari scroll up the page because of the global FocusPanel
			BrowserDetect.getBrowserDetect().isChrome() ) {			//to prevent this we do not have vertical scrolling of the page
			//Set the scroll panel height only if the client area is big enough
			scrollPanel.setHeight( InterfaceUtils.suggestMainViewHeight( scrollPanel, 80) + "px");
			//In this case the bottom page-indexes widget should be invisible 
			bottomPageIndexesWidget.setVisible(false);
		} else {
			//If not set the scroll height to 100%
			scrollPanel.setHeight( "100%" );
			//In this case the bottom page-indexes widget should be visible 
			bottomPageIndexesWidget.setVisible(true);
		}
		
		//Adjust width
		if(  adjustWidth ) {
			//NOTE: Setting the width of the decorated panel alone does not work!!!
			//Thus we also set the width of the scroll panel
			final int decPanelWidth = (int) ( Window.getClientWidth() / 100.0 * percentWidth ); 
			setDecPanelWidth( decPanelWidth );
			//Here DECORATIONS_WIDTH is the width of the decorations we have around the decorated panel
			final int scrollPanelWidth = decPanelWidth - CommonResourcesContainer.DECORATIONS_WIDTH;
			mainPanel.setWidth( scrollPanelWidth + "px" );
		}
	}
	
	/**
	 * Allows to find a message in the current messages panel view by the message id
	 * @param messageID the id of the message that we want to find
	 * @return the found message data or null if nothing was found
	 */
	public ForumMessageData getCurrentViewMessage( final int messageID ) {
		if( messagesList != null ) {
			for( ForumMessageData messageData : messagesList ) {
				if( messageData.messageID == messageID ) {
					return messageData;
				}
			}
		}
		return null;
	}
	
	private void setUserLoggedIn( final boolean isLoggedIn ) {
		this.isLoggedIn = isLoggedIn;
		for( int index = 0; index < messagesPanel.getWidgetCount(); index++ ) {
			Widget msg = messagesPanel.getWidget( index );
			if( msg instanceof ForumMessageWidget ) {
				((ForumMessageWidget)  messagesPanel.getWidget( index )).setUserLoggedIn( isLoggedIn );
			}
		}
	}
	
	/**
	 * Allows to update the avatar of the listed forum messages for the given user with the new spoiler data
	 * @param userID the id of the user for which the spoiler should be updated
	 * @param spoilerID the new spoiler id
	 * @param spoilerExpDate the new expiration date for the spoiler 
	 */
	public void updateUserAvatarSpoiler( final int userID, final int spoilerID, final Date spoilerExpDate) {
		for( int index = 0; index < messagesPanel.getWidgetCount(); index++ ) {
			Widget msg = messagesPanel.getWidget( index );
			if( msg instanceof ForumMessageWidget ) {
				((ForumMessageWidget)  messagesPanel.getWidget( index )).updateUserAvatarSpoiler( userID, spoilerID, spoilerExpDate );
			}
		}
	}
	
	@Override
	public void setUserLoggedIn() {
		//The user can reply to the forum message and delete it if he is its owner
		setUserLoggedIn( true );
	}
	
	@Override
	public void setUserLoggedOut() {
		//The user can not reply to the forum message and delete it
		setUserLoggedIn( false );
	}

	@Override
	public void onBeforeComponentIsAdded() {
		//Does nothing
	}
}
