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

import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.search.ForumSearchData;

import com.xcurechat.client.dialogs.ActionGridDialog;
import com.xcurechat.client.forum.messages.ForumMessageWidget;
import com.xcurechat.client.forum.messages.ForumMessageWidgetFactory;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This dialog allows to view a forum message
 */
public class ViewMessageDialogUI extends ActionGridDialog {

	//The ui object of the forum message
	private final ForumMessageWidget forumMessageUI;
	
	//If true then we are viewing a forum topic message for which we want to have "Find on the forum" feature
	private final boolean isTopicView;
	
	//The stored message data
	private final ForumMessageData messageData;

	private static final int HALF_MAXIMUM_FORUM_MESSAGEBODY_HEIGHT = CommonResourcesContainer.MAXIMUM_FORUM_MESSAGEBODY_HEIGHT / 2;
	
	/**
	 * Constructs the forum message data view dialog
	 * @param messageData the forum message data to display
	 */
	public ViewMessageDialogUI( final ForumMessageData messageData ) {
		this( messageData, false );
	}
	
	/**
	 * Constructs the forum message data view dialog
	 * @param messageData the forum message data to display
	 * @param isTopicView true for viewing a forum topic message for which we want to have "Find on the forum" feature
	 */
	public ViewMessageDialogUI( final ForumMessageData messageData, final boolean isTopicView ) {
		super( false, true, true, null);
		
		this.messageData = messageData;
		this.isTopicView = isTopicView;
		this.forumMessageUI = ForumMessageWidgetFactory.getWidgetInstance( messageData, SiteManager.isUserLoggedIn(), true, false, true, false );
		this.forumMessageUI.adjustWidth( CommonResourcesContainer.MAXIMUM_WIDTH_MESSAGE_TITLE_STYLE, CommonResourcesContainer.MAXIMUM_WIDTH_MESSAGE_BODY_SCROLL_STYLE );
		forumMessageUI.adjustHeight( ViewMessageDialogUI.HALF_MAXIMUM_FORUM_MESSAGEBODY_HEIGHT, true );
		this.forumMessageUI.setEnabled( true );
		
		//Set the dialog's caption.
		if( messageData.isForumSectionMessage() ) {
			setText( titlesI18N.viewForumSectionDialogTitle() );
		} else {
			if( messageData.isForumTopicMessage() ) {
				setText( titlesI18N.viewForumTopicDialogTitle() );
			} else {
				setText( titlesI18N.viewForumMessageDialogTitle() );
			}
		}
		
		//Enable the action buttons and hot key, even though we will not be adding these buttons
		//to the grid, we will use the hot keys associated with them to close this dialog
		setLeftEnabled( true );
		setRightEnabled( true );
		
		//Fill dialog with data
		populateDialog();
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.ActionGridDialog#actionLeftButton()
	 */
	@Override
	protected void actionLeftButton() {
		hide();
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.ActionGridDialog#actionRightButton()
	 */
	@Override
	protected void actionRightButton() {
		if( isTopicView ) {
			//Construct the query for searching the topic
			ForumSearchData forumSearchData = new ForumSearchData();
			forumSearchData.isOnlyMessage = true;
			forumSearchData.baseMessageID = messageData.messageID;
			//Do not set the forum message data into the message stack
			//element, because we are not browsing the message replies
			ForumSearchManager.doSearch( new MessageStackElement( forumSearchData, null ) );
		}
		hide();
	}
	
	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.ActionGridDialog#populateDialog()
	 */
	@Override
	protected void populateDialog() {
		//ADD THE MAIN DATA FIELDS
		if( isTopicView ) {
			addNewGrid( 2, 2, false, "", true);
		} else {
			addNewGrid( 1, 1, false, "", true);
		}
		
		//Make a wrapper scroll panel around the message because it can be too long
		VerticalPanel centeringPanel = new VerticalPanel();
		centeringPanel.addStyleName( CommonResourcesContainer.MAXIMUM_WIDTH_FOR_MESSAGE_VIEW_STYLE );
		centeringPanel.setHeight("100%");
		centeringPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		centeringPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		centeringPanel.add( forumMessageUI );
		
		ScrollPanel wrapperPanel = new ScrollPanel();
		wrapperPanel.setStyleName( CommonResourcesContainer.SCROLLABLE_SIMPLE_PANEL );
		//Force the maximum viewable area for the displayed forum message
		wrapperPanel.addStyleName( CommonResourcesContainer.MAXIMUM_SIZE_FOR_MESSAGE_VIEW_STYLE );
		wrapperPanel.add( centeringPanel );
		
		if( isTopicView ) {
			addToGrid( this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, wrapperPanel, false, false );
			
			//Add the grid action buttons
			this.addGridActionElements(true, true);
		} else {
			addToGrid( FIRST_COLUMN_INDEX, wrapperPanel, false, false );
		}
	}

	@Override
	protected String getLeftButtonText(){
		return titlesI18N.closeButtonTitle();
	}

	@Override
	protected String getRightButtonText(){
		return titlesI18N.locateButtonTitle();
	}

}
