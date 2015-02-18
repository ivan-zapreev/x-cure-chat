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
 */
package com.xcurechat.client.forum;

import java.util.List;

import com.google.gwt.user.client.Window;
import com.xcurechat.client.data.search.UserSearchData;

import com.xcurechat.client.dialogs.UserSearchDialog;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;
import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.utils.SplitLoad;

/**
 * @author zapreevis
 * Simply allows to search for users to select the forum messages author
 */
public class SelectAuthorDialogUI extends UserSearchDialog {
	//The number of result rows per page
	private static final int NUMBER_OF_ROWS_PER_PAGE = UserSearchData.MAX_NUMBER_OF_USERS_PER_PAGE;
	//The title component which should hold the selected user
	private final ForumSearchPanel titlePanel;
	
	public SelectAuthorDialogUI( final ForumSearchPanel titlePanel ){
		super( NUMBER_OF_ROWS_PER_PAGE, null, true);
		
		this.titlePanel = titlePanel;
		
		//Set the dialog's caption.
		updateDialogTitle();
		
		//Populate the dialog with elements
		populateDialog();
	}
	
	@Override
	protected void actionButtonAction(final List<Integer> userIDS, final List<String> userLoginNames) {
		if( userIDS.isEmpty() ) {
			//Give an error message that there are no selected users
			(new SplitLoad( true ) {
				@Override
				public void execute() {
					//Report the error
					ErrorMessagesDialogUI.openErrorDialog( I18NManager.getErrors().thereIsNoSelectedUsers() );
				}
			}).loadAndExecute();
		} else {
			if( titlePanel != null ) {
				//There should not be more than one user selected, since we only allow for a single selection
				( titlePanel ).setForumAuthor( userIDS.get(0), userLoginNames.get(0));
				//Close this dialog, since we've selected a recepient
				hide();
			} else {
				//This is an error case, this should not be happening
				Window.alert("The author is selected but we do not know what to do with him!");
			}
		}
	}
	
	@Override
	protected String actionButtonText() {
		//We return null because we do not need any action buttons yet
		return titlesI18N.selectButtonTitle();
	}

	@Override
	protected UserSearchData getExtraUserSearchData() {
		return new UserSearchData();
	}

	@Override
	protected void updateDialogTitle( final int numberOfEntries, final int numberOfPages, final int currentPageNumber ) {
		this.setText( titlesI18N.selectAuthorDialogTitle( numberOfEntries, currentPageNumber, numberOfPages ) );
	}
}
