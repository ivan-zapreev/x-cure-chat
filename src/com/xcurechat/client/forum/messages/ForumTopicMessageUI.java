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
 * Represents the forum topic message
 */
public class ForumTopicMessageUI extends ForumMessageWidget {

	public ForumTopicMessageUI( ForumMessageData messageData, boolean isLoggedIn, boolean oddOrNot,
								  boolean showActionPanel, boolean forseContentOpen, final boolean isMsgTitleClickable ) {
		super(messageData, isLoggedIn, oddOrNot, showActionPanel, forseContentOpen, isMsgTitleClickable );
		
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
	public UserAvatarWidget populateAvatarTitlePanel(VerticalPanel avatarVerticalPanel, final ShortUserData senderData) {
		//Add the title label
		final Label createdByLabel = new Label( i18nTitles.topicCreatedByFieldTitle() );
		createdByLabel.setStylePrimaryName( CommonResourcesContainer.USER_DIALOG_COMPULSORY_FIELD_STYLE );
		createdByLabel.setWordWrap( false );
		avatarVerticalPanel.add( createdByLabel );
		avatarVerticalPanel.addStyleName( CommonResourcesContainer.FORUM_TITLE_EXTRA_STYLE );
		
		//Add the avatar in a mode without the image
		final UserAvatarWidget avatarWidget = new UserAvatarWidget( senderData, UserAvatarWidget.DISPLAY_USER_NAME_DATA );
		avatarVerticalPanel.add( avatarWidget );
		
		//Return the avatar object
		return avatarWidget;
	}
	
	@Override
	public String getMessageSubjectFieldName() {
		return i18nTitles.forumTopicFieldTitle();
	}

	@Override
	public void setMessageSubjectFieldValueStyles(Label messageSubjectField, FlowPanel messageSubjectContent) {
		messageSubjectField.addStyleName( CommonResourcesContainer.USER_DIALOG_COMPULSORY_FIELD_STYLE );
		messageSubjectField.addStyleName( CommonResourcesContainer.FORUM_TITLE_EXTRA_STYLE );
		messageSubjectContent.addStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_IMP_STYLE_NAME );
		messageSubjectContent.addStyleName( CommonResourcesContainer.FORUM_TITLE_EXTRA_STYLE );
	}

	@Override
	public void populateMessageTitleInfoPanel(FlowPanel titleInfoPanel) {
		//Keep this panel empty
	}

	@Override
	public Label populateMessageTitleLastReplyInfoPanel( FlowPanel titleInfoLastRepPanel ) {
		//If the topic has no replies then add the date of when it was created
		if( messageData.numberOfReplies == 0 ) {
			//Then add the creation date
			addNewFieldValuePair( titleInfoLastRepPanel, i18nTitles.forumMessageCreatedOn(), messageData.sentDate, true );
			//If there are no replies and the topic was updated more than a minute ago after its creation
			if( ( ( messageData.sentDate.getTime() + 60000 ) < messageData.updateDate.getTime() ) ) {
				//Then add the update date
				addNewFieldValuePair( titleInfoLastRepPanel, i18nTitles.forumMessageUpdatedOn(), messageData.updateDate, true );
			}
		}
		addNewFieldValuePair( titleInfoLastRepPanel, i18nTitles.forumTopicNumberOfPosts(), messageData.numberOfReplies + "", (messageData.numberOfReplies > 0) );
		//Add the last reply by message
		return addLastReplyInfoToPanel( titleInfoLastRepPanel, i18nTitles.forumTopicLastPostOn() );
	}

	@Override
	protected boolean hasMessageBodyContent() {
		return true;
	}

	@Override
	protected String getMessageBodyContentTitle() {
		return i18nTitles.forumMessageTopicDescription();
	}

	@Override
	protected boolean isMessageBodyContentOpen() {
		return false;
	}

	@Override
	protected boolean isAddViewMessageTopicActionLink() {
		//This is a topic message so no view topic action links here
		return false;
	}

	@Override
	protected String getViewForumMessageRepliesURL(boolean isEnabled) {
		return ServerSideAccessManager.getEnterForumTopicURL( isEnabled );
	}

	@Override
	protected String getViewForumMessageRepliesLinkText() {
		 return i18nTitles.forumEnterTopicButton();
	}

	@Override
	protected boolean isReplyToMessageActionLink() {
		return false;
	}

	@Override
	public boolean allowForViewingRepliesWhenNote() {
		return true;
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
		return false;
	}
}
