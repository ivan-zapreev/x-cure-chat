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
package com.xcurechat.client.dialogs.profile;

import java.util.List;

import com.google.gwt.user.client.ui.DialogBox;

import com.xcurechat.client.data.search.UserSearchData;

import com.xcurechat.client.dialogs.UserSearchDialog;

/**
 * @author zapreevis
 * Simply allows to search for users
 */
public class UsersBrowsingDialogUI extends UserSearchDialog {
	//The number of result rows per page
	private static final int NUMBER_OF_ROWS_PER_PAGE = UserSearchData.MAX_NUMBER_OF_USERS_PER_PAGE;
	
	public UsersBrowsingDialogUI( final DialogBox parentDialog ){
		super( NUMBER_OF_ROWS_PER_PAGE, parentDialog);
		
		//Set the dialog's caption.
		updateDialogTitle();
		
		//Populate the dialog with elements
		populateDialog();
	}
	
	@Override
	protected void actionButtonAction(final List<Integer> userIDS, final List<String> userLoginNames) {
		//We do not plan on any extra action here yet.
	}

	@Override
	protected String actionButtonText() {
		//We return null because we do not need any action buttons yet
		return null;
	}

	@Override
	protected UserSearchData getExtraUserSearchData() {
		return new UserSearchData();
	}

	@Override
	protected void updateDialogTitle( final int numberOfEntries, final int numberOfPages, final int currentPageNumber ) {
		this.setText( titlesI18N.userSearchDialogTitle( numberOfEntries, currentPageNumber, numberOfPages ) );
	}
}
