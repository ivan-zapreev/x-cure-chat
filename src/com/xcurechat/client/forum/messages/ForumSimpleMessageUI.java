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
package com.xcurechat.client.forum.messages;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.ShortUserData;


import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.UserAvatarWidget;

/**
 * @author zapreevis
 * Represents the forum simple-post message
 */
public class ForumSimpleMessageUI extends ForumMessageWidget {
	
	public ForumSimpleMessageUI( ForumMessageData messageData, boolean isLoggedIn, boolean oddOrNot,
								  boolean showActionPanel, boolean forseContentOpen, final boolean isMsgTitleClickable ) {
		super( messageData, isLoggedIn, oddOrNot, showActionPanel, forseContentOpen, isMsgTitleClickable);
		
		//Initialize the widget
		initializeWidget();
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.forum.messages.ForumMessageWidget#isAvatarVerticalPanelNeeded()
	 */
	@Override
	public boolean isAvatarTitlePanelNeeded() {
		return true;
	}

	@Override
	public UserAvatarWidget populateAvatarTitlePanel(VerticalPanel avatarVerticalPanel, final ShortUserData senderData ) {
		//Create the avatar
		final UserAvatarWidget avatar = new UserAvatarWidget( senderData );
		//Add it to the panel
		avatarVerticalPanel.add( avatar );
		//Return the avatar
		return avatar;
	}

	@Override
	public String getMessageSubjectFieldName() {
		return i18nTitles.forumMessageSubjectTitle();
	}
	
	@Override
	public void setMessageSubjectFieldValueStyles(Label messageSubjectField, FlowPanel messageSubjectContent) {
		messageSubjectField.addStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		messageSubjectContent.addStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_STYLE_NAME );
		messageSubjectContent.addStyleName( CommonResourcesContainer.SIMPLE_FORUM_MESSAGE_SUBJECT_STYLE );
	}

	@Override
	public void populateMessageTitleInfoPanel(FlowPanel titleInfoPanel) {
		final boolean wasUpdatedLater = ( messageData.sentDate.getTime() + 60000 ) < messageData.updateDate.getTime();
		addNewFieldValuePair( titleInfoPanel, i18nTitles.forumMessageCreatedOn(), messageData.sentDate, wasUpdatedLater );
		//If the message was updated more that a minute after it was created
		if( wasUpdatedLater ) {
			addNewFieldValuePair( titleInfoPanel, i18nTitles.forumMessageUpdatedOn(), messageData.updateDate, false );
		}
	}

	@Override
	public Label populateMessageTitleLastReplyInfoPanel( FlowPanel titleInfoLastRepPanel) {
		addNewFieldValuePair( titleInfoLastRepPanel, i18nTitles.forumMessageNumberOfReplies(), messageData.numberOfReplies + "", (messageData.numberOfReplies > 0) );
		return addLastReplyInfoToPanel( titleInfoLastRepPanel, i18nTitles.forumMessageLastReplyOn() );
	}

	@Override
	protected boolean hasMessageBodyContent() {
		return true;
	}

	@Override
	protected String getMessageBodyContentTitle() {
		return i18nTitles.forumMessageMessageBody();
	}

	@Override
	protected boolean isMessageBodyContentOpen() {
		return true;
	}

	@Override
	protected boolean isAddViewMessageTopicActionLink() {
		//This message belongs to some topic, allow to view it
		return true;
	}

	@Override
	protected String getViewForumMessageRepliesURL(boolean isEnabled) {
		return ServerSideAccessManager.getViewRepliesMessageURL( isEnabled );
	}

	@Override
	protected String getViewForumMessageRepliesLinkText() {
		 return i18nTitles.forumMessageViewRepliesButton();
	}

	@Override
	protected boolean isReplyToMessageActionLink() {
		return true;
	}

	@Override
	public boolean allowForViewingRepliesWhenNote() {
		return false;
	}

	@Override
	protected boolean isDeleteMessageActionLink() {
		return isLoggedInAndAdminOrMessageOwner();
	}

	@Override
	protected boolean isEditMessageActionLink() {
		return isLoggedInAndAdminOrMessageOwner();
	}

	@Override
	protected boolean isMoveMessageActionLink() {
		return isLoggedInAndAdmin();
	}

	@Override
	protected boolean isApproveMessageActionLink() {
		return  isLoggedInAndAdmin();
	}
}
