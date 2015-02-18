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
 * Represents the forum section message
 */
public class ForumSectionMessageUI extends ForumMessageWidget {

	public ForumSectionMessageUI( ForumMessageData messageData, boolean isLoggedIn, boolean oddOrNot,
								  boolean showActionPanel, boolean forseContentOpen, final boolean isMsgTitleClickable ) {
		super( messageData, isLoggedIn, oddOrNot, showActionPanel, forseContentOpen, isMsgTitleClickable );
		
		//Initialize the widget
		initializeWidget();
	}
	
	/* (non-Javadoc)
	 * @see com.xcurechat.client.forum.messages.ForumMessageWidget#isAvatarVerticalPanelNeeded()
	 */
	@Override
	public boolean isAvatarTitlePanelNeeded() {
		//The Forum section does nto need an avatar panel
		return false;
	}

	@Override
	public UserAvatarWidget populateAvatarTitlePanel(VerticalPanel avatarVerticalPanel, final ShortUserData senderData) {
		//NOTE: Do nothing because this panel does not have to be filled out, also no avatar
		return null;
	}

	@Override
	public String getMessageSubjectFieldName() {
		return i18nTitles.forumSectionNameTitle();
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
	public Label populateMessageTitleLastReplyInfoPanel( FlowPanel titleInfoLastRepPanel) {
		//Only indicate the number of posts
		addNewFieldValuePair( titleInfoLastRepPanel, i18nTitles.forumTopicNumberOfPosts(), messageData.numberOfReplies + "", (messageData.numberOfReplies > 0) );
		//Add the info about who was the last to reply
		return addLastReplyInfoToPanel( titleInfoLastRepPanel, i18nTitles.forumTopicLastPostOn() );
	}

	@Override
	protected boolean hasMessageBodyContent() {
		return false;
	}

	@Override
	protected String getMessageBodyContentTitle() {
		//The section does not have any content
		return null;
	}

	@Override
	protected boolean isMessageBodyContentOpen() {
		//The section does not have any content
		return false;
	}

	@Override
	protected boolean isAddViewMessageTopicActionLink() {
		//This is a section message so no view topic links here
		return false;
	}

	@Override
	protected String getViewForumMessageRepliesURL(boolean isEnabled) {
		return ServerSideAccessManager.getEnterForumTopicURL( isEnabled );
	}

	@Override
	protected String getViewForumMessageRepliesLinkText() {
		 return i18nTitles.forumEnterSectionButton();
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
		return isLoggedInAndAdmin(); //Can only be managed by the admin
	}

	@Override
	protected boolean isEditMessageActionLink() {
		return isLoggedInAndAdmin(); //Can only be managed by the admin
	}

	@Override
	protected boolean isMoveMessageActionLink() {
		return isLoggedInAndAdmin(); //The admin can move the forum section!
	}

	@Override
	protected boolean isApproveMessageActionLink() {
		return false;
	}
}
