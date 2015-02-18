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
package com.xcurechat.client.dialogs.system;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextArea;
import com.xcurechat.client.dialogs.ActionGridDialog;
import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.utils.StringUtils;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This is a user agreement dialog
 */
public class UserAgreementDialogUI extends ActionGridDialog {
	private static final int MAX_TEXT_WIDTH = 60;
	private static final int MAX_VISIBLE_LINES = 20;
	
	//The body of the message
	private TextArea userAgreementTextArea = new TextArea();
	
	//The check box that we have to check if the user agreement is accepted
	private final CheckBox userAgreement;
	
	public UserAgreementDialogUI( DialogBox parentDialog, CheckBox userAgreement) {
		super( false, true, true, parentDialog );
		
		//Set the dialog's title
		this.setText( titlesI18N.userAdreementDialogTitle() );
		
		//Store the reference to the check box
		this.userAgreement = userAgreement;
		
		//Populate the dialog
		populateDialog();
	}

	@Override
	protected void actionLeftButton() {
		//The user agreement is NOT accepted, set the
		//check box as un checked and close the dialog
		userAgreement.setValue( false );
		hide();
	}

	@Override
	protected void actionRightButton() {
		//The user agreement is accepted, set the
		//check box as checked and close the dialog
		userAgreement.setValue( true );
		hide();
	}

	@Override
	protected void populateDialog() {
		//ADD THE MAIN FIELDS
		addNewGrid( 2, false, "", false);
		
		userAgreementTextArea.setReadOnly(true);
		userAgreementTextArea.addStyleName( CommonResourcesContainer.USER_AGREEMENT_TEXT_AREA_STYLE );
		userAgreementTextArea.setCharacterWidth( MAX_TEXT_WIDTH );
		userAgreementTextArea.setVisibleLines( MAX_VISIBLE_LINES );
		userAgreementTextArea.setText( StringUtils.formatTextWidth( MAX_TEXT_WIDTH, I18NManager.getInfoMessages().userAgreementText(), false) );
		addToGrid( getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, userAgreementTextArea, false, false );
		
		addGridActionElements( true, true );
	}
	
	@Override
	protected String getLeftButtonText(){
		return titlesI18N.rejectUserAgreement();
	}
	
	@Override
	protected String getRightButtonText(){
		return titlesI18N.acceptUserAgreement();
	}

}
