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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.HTML;

import com.xcurechat.client.data.search.ForumSearchData;
import com.xcurechat.client.data.ForumMessageData;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;
import com.xcurechat.client.utils.ClientEncoder;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This class represents message stack element
 */
public class MessageStackElement extends Composite {
	
	private final int MESSAGE_TITLE_PREVIEW_LENGTH = 25;
	private final String MESSAGE_TITLE_PREVIEW_SUFFIX = "...";
	
	//The encoder that we should use in the client
	private static final ClientEncoder encoder = new ClientEncoder();
	
	//THe main horizontal panel that stores the message stack element visual data
	private final HorizontalPanel mainPanel = new HorizontalPanel();
	
	//THe main focus panel used to handle click events
	private final FocusPanel focusPanel = new FocusPanel();
	
	//Stores the search data corresponding to this message stack entry
	private final ForumSearchData searchData;
	
	//Stores the message for the case of browsing message replies or null
	private final ForumMessageData messageData;
	
	//The internationalization class
	private static final UITitlesI18N i18nTitles = I18NManager.getTitles();
	
	//Contains true if the component is enabled otherwise false
	private boolean isEnabled = false;
	
	//Contains true if this is the last stack message component
	private boolean isLastStackMessageElement = false;
	
	//The click handler for initiating the search action
	//This is for the case when we are not browsing the
	//reply messages or this is not the last message in
	//the stack (i.e. the latest)
	private HandlerRegistration searchClickHandler = null;
	//For viewing the message in case of viewing message replies
	//and in particular if this message is the latest in the stack
	private HandlerRegistration viewMessageClickHandler = null;
	
	//Stores the opened view message dialog or null
	private ViewMessageDialogUI viewMessageDialog = null;
	
	/**
	 * The basic constructor
	 */
	public MessageStackElement( ForumSearchData searchData, ForumMessageData messageData ) {
		this.searchData = searchData;
		this.messageData = messageData;
		
		//Populate the panel
		populate();
		
		//Init the widget
		initWidget( focusPanel );
	}
	
	/**
	 * Allows to detect if two stack elements are equal
	 * @param anotherElement another stack element to compare with
	 * @return true if this and another stack elements are equal stack elements
	 */
	public boolean isEqualStackElements( final MessageStackElement anotherElement ) {
		return isEqualStackElements( anotherElement, false );
	}
	
	/**
	 * Allows to detect if two stack elements are equal, possibly just up to the page index
	 * @param anotherElement another stack element to compare with
	 * @param isUpToPageIndex if true then we compare the stack elements up to the page index
	 * @return true if this and another stack elements are equal stack elements
	 */
	public boolean isEqualStackElements( final MessageStackElement anotherElement, final boolean isUpToPageIndex ) {
		boolean isEqual = false;
		if( anotherElement != null ) {
			if( ( !this.isMessageReply() && !anotherElement.isMessageReply() ) ||
				(  this.isMessageReply() &&  anotherElement.isMessageReply() ) ) {
				if( isUpToPageIndex ) {
					//Alter the page indexes and then compare
					final ForumSearchData localData = this.getSearchData().clone();
					final ForumSearchData otherData = anotherElement.getSearchData().clone();
					localData.pageIndex = 0; otherData.pageIndex = 0;
					isEqual = localData.serialize(encoder).equals( otherData.serialize(encoder) );
				} else {
					//Note that, the search data uniquely defines the stack elements
					isEqual = this.serialize().equals( anotherElement.serialize() );
				}
			}
		}
		return isEqual;
	}
	
	private void populate() {
		mainPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT );
		mainPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		
		if( messageData == null ) {
			//This is some SEARCH RESULTS that we are browsing
			if( searchData.isBrowsingSectionsSearch() ) {
				//We are browsing the initial search results, this is a widget for it
				focusPanel.addStyleName( CommonResourcesContainer.ALL_TOPICS_SEARCH_RESULTS_STYLE );
				Label allTopicsLabel = new Label( i18nTitles.forumMsgStackAllTopicsLabel(searchData.pageIndex) );
				allTopicsLabel.setWordWrap(false);
				mainPanel.add( allTopicsLabel );
				focusPanel.setTitle( i18nTitles.forumMsgStackClickToViewForumTopics() );
			} else {
				//We are browsing some custom search results, this is a widget for it
				focusPanel.addStyleName( CommonResourcesContainer.CUSTOM_SEARCH_RESULTS_STYLE );
				Label customSearchLabel = new Label( i18nTitles.forumMsgStackCustomSearchLabel(searchData.pageIndex) );
				customSearchLabel.setWordWrap(false);
				mainPanel.add( customSearchLabel );
				focusPanel.setTitle( i18nTitles.forumMsgStackClickToViewCustomSearchResults() );
			}
		} else {
			//This is some MESSAGE REPLIES that we are browsing
			final String title;
			if( this.isSectionTopicsView() ) {
				title = i18nTitles.forumMsgStackTopicsOfSection(searchData.pageIndex);
			} else {
				if( this.isTopicMessagesView() ) {
					title = i18nTitles.forumMsgStackRepliesToTopic(searchData.pageIndex);
				} else {
					title = i18nTitles.forumMsgStackRepliesToPost(searchData.pageIndex);
				}
			}
			Label repliesToLabel = new Label( title );
			repliesToLabel.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
			repliesToLabel.setWordWrap(false);
			mainPanel.add( repliesToLabel );
			mainPanel.add( new HTML("&nbsp;") );
			String text;
			if( messageData.messageTitle.length() > MESSAGE_TITLE_PREVIEW_LENGTH ) {
				text = messageData.messageTitle.substring( 0 , MESSAGE_TITLE_PREVIEW_LENGTH ) + MESSAGE_TITLE_PREVIEW_SUFFIX;
			} else {
				text = messageData.messageTitle;
			}
			Label titlePreviesLabel = new Label( text );
			titlePreviesLabel.setStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_STYLE_NAME );
			titlePreviesLabel.setWordWrap(false);
			mainPanel.add( titlePreviesLabel );
			//Add the end delimiter
			mainPanel.add( new HTML("&nbsp;") );
			mainPanel.add( new Label(">>") );
			focusPanel.setTitle( i18nTitles.forumMsgStackClickToViewTheMessage() );
		}
		
		//Make clickable if the component is enabled
		if( isEnabled ) {
			this.addStyleName( CommonResourcesContainer.CLICKABLE_STYLE );
		}
		
		focusPanel.add( mainPanel );
	}
	
	/**
	 * Allows to retrieve the set short forum message data or null if this is the search query stack element.
	 * @return the short forum message data or null
	 */
	public ForumMessageData getMessageData() {
		return messageData;
	}
	
	/**
	 * In case of browsing message replies, allows to mark the stack element as the last one in the stack
	 */
	public void setLastStackElement() {
		isLastStackMessageElement = true;
		if( messageData != null ) {
			focusPanel.removeStyleName( CommonResourcesContainer.MSG_REPLIES_STYLE );			
			focusPanel.addStyleName( CommonResourcesContainer.MSG_REPLIES_LAST_STACK_ELEMENT_STYLE );
			focusPanel.setTitle( i18nTitles.forumMsgStackClickToViewTheMessage() );
			//Alter the click listeners, make the click listener to show the message preview
			removeClickHandlers();
			viewMessageClickHandler = focusPanel.addClickHandler( new ClickHandler(){
				public void onClick( ClickEvent e ){
					//Stop the event from being propagated
					e.stopPropagation(); e.preventDefault();
					//Perform the needed action
					openMessageViewDialog();
				}
			});
		}
	}
	
	/**
	 * In case of browsing message replies, allows to mark the stack element as the non-last one in the stack
	 */
	public void setNotLastStackElement() {
		isLastStackMessageElement = false;
		if( messageData != null ) {
			focusPanel.removeStyleName( CommonResourcesContainer.MSG_REPLIES_LAST_STACK_ELEMENT_STYLE );			
			focusPanel.addStyleName( CommonResourcesContainer.MSG_REPLIES_STYLE );
			focusPanel.setTitle( i18nTitles.forumMsgStackClickToViewReplies() );
			//Alter the click listeners, make the click listener to perform the search the message preview
			removeClickHandlers();
			searchClickHandler = focusPanel.addClickHandler( new ClickHandler(){
				public void onClick( ClickEvent e ){
					//Perform the needed action
					ForumSearchManager.doSearch( MessageStackElement.this );
					//Stop the event from being propagated
					e.stopPropagation(); e.preventDefault();
				}
			});
		}
	}
	
	/**
	 * This method is called before the site body component is removed from the site's main panel.
	 * I.e. it is called each time before the given site section is de-selected.
	 * Has to be implemented by the child class for receiving this event.
	 */
	public void onBeforeComponentIsRemoved() {
		//Close all of the dialogs opened from this component
		closeMessageViewDialog();
	}
	
	/**
	 * Allows to set the component into enabled/disabled mode
	 * @param enabled true to disable, false to enable
	 */
	public void setEnabled( boolean enabled ) {
		//remove the clickable style, alter disable/enable the click handlers
		//Make clickable if the component is enabled
		isEnabled = enabled;
		//Remove click handlers, if any
		removeClickHandlers();
		//Do the re-set
		if( isEnabled ) {
			this.addStyleName( CommonResourcesContainer.CLICKABLE_STYLE );
			if( ( messageData != null ) &&  isLastStackMessageElement ) {
				viewMessageClickHandler = focusPanel.addClickHandler( new ClickHandler(){
					public void onClick( ClickEvent e ){
						//Stop the event from being propagated
						e.stopPropagation(); e.preventDefault();
						//Perform the needed action
						openMessageViewDialog();
					}
				});
			} else {
				searchClickHandler = focusPanel.addClickHandler( new ClickHandler(){
					public void onClick( ClickEvent e ){
						//Perform the needed action
						ForumSearchManager.doSearch( MessageStackElement.this );
						//Stop the event from being propagated
						e.stopPropagation(); e.preventDefault();
					}
				});
			}
		} else {
			this.removeStyleName( CommonResourcesContainer.CLICKABLE_STYLE );
		}
	}
	
	/**
	 * Open the message preview dialog
	 */
	private void openMessageViewDialog() {
		//Ensure lazy loading
		( new SplitLoad( true ) {
			@Override
			public void execute() {
				closeMessageViewDialog();
				viewMessageDialog = new ViewMessageDialogUI( messageData );
				viewMessageDialog.show();
				viewMessageDialog.center();
			}
		}).loadAndExecute();
	}
	
	/**
	 * Closes the message view dialog if it is open
	 */
	private void closeMessageViewDialog() {
		if( viewMessageDialog != null ) {
			viewMessageDialog.hide();
			viewMessageDialog = null;
		}
	}
	
	/**
	 * Allows to remove the click listeners
	 */
	private void removeClickHandlers() {
		if( viewMessageClickHandler != null ) {
			viewMessageClickHandler.removeHandler();
			viewMessageClickHandler = null;
		}
		if( searchClickHandler != null ) {
			searchClickHandler.removeHandler();
			searchClickHandler = null;
		}
	}
	
	/**
	 * @param true if we are browsing message replies
	 */
	public boolean isMessageReply() {
		return messageData != null;
	}
	
	/**
	 * @param true if we are browsing topic message replies
	 */
	public boolean isTopicMessagesView() {
		return isMessageReply() && messageData.isForumTopicMessage();
	}
	
	/**
	 * @param true if we are browsing topics of some section
	 */
	public boolean isSectionTopicsView() {
		return isMessageReply() && messageData.isForumSectionMessage();
	}
	
	/**
	 * Allows to retrieve the search setting for the given stack element
	 */
	public ForumSearchData getSearchData() {
		return searchData;
	}
	
	/**
	 * Is redefined to serialize the current stack element search data into a string.
	 * The serialization is URL safe.
	 */
	public String serialize() {
		return searchData.serialize( encoder );
	}
}
