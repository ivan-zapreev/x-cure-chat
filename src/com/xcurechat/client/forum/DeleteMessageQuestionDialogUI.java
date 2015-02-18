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
package com.xcurechat.client.forum;

import com.xcurechat.client.dialogs.system.messages.QuestionMessageDialogUI;
import com.xcurechat.client.forum.messages.ForumMessageWidget;

/**
 * @author zapreevis
 * Allows to ask the user if he really wants to delete a non-empty topic, or a message with replies
 */
public class DeleteMessageQuestionDialogUI extends QuestionMessageDialogUI {
	
	private final ForumMessageWidget messageW;
	
	/**
	 * The basic constructor
	 */
	public DeleteMessageQuestionDialogUI( final ForumMessageWidget messageW) {
		super();
		
		this.messageW = messageW;
		
		//Fill dialog with data
		populateDialog();
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.system.messages.QuestionMessageDialogUI#getDialogQuestion()
	 */
	@Override
	protected String getDialogQuestion() {
		return titlesI18N.doYouWantToDeleteForumTopicMessageWithReplies();
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.system.messages.QuestionMessageDialogUI#getDialogTitle()
	 */
	@Override
	protected String getDialogTitle() {
		return titlesI18N.deleteForumTopicMessageWithRepliesDialogTitle();
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.system.messages.QuestionMessageDialogUI#negativeAnswerAction()
	 */
	@Override
	protected void negativeAnswerAction() {
		hide();
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.system.messages.QuestionMessageDialogUI#positiveAnswerAction()
	 */
	@Override
	protected void positiveAnswerAction() {
		//Call the server to delete the message
		messageW.deleteMessage();
		//Hide this dialog
		hide();
	}
}
