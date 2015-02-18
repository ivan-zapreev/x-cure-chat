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
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.dialogs.system.messages;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

import com.xcurechat.client.dialogs.ActionGridDialog;

/**
 * @author zapreevis
 * Displays the question that needs either positive or negative answer
 */
public abstract class QuestionMessageDialogUI extends ActionGridDialog {
	//The no/yes buttons for the question 
	private final Button noButton = new Button( titlesI18N.noButtonTitle() );
	private final Button yesButton = new Button(titlesI18N.yesButtonTitle());

	public QuestionMessageDialogUI( ) {
		//Autohide and modaldialog
		super( false, true, true, null );
		
		//Do basic initialization
		setLeftEnabled(true);
		setRightEnabled(true);
		
		this.setStyleName( CommonResourcesContainer.INFO_MESSAGE_DIALOG_STYLE );
	}
	
	/**
	 * Allows to enable-disable the buttons and action related to the positive and negative ansvers
	 * @param enabledNo if true enable buttons and actions for the negative answer  
	 * @param enabledYes if true enable buttons and actions for the positive answer
	 */
	protected void setControlsEnabled(final boolean enabledNo, final boolean enabledYes){
		setLeftEnabled( enabledNo );
		noButton.setEnabled( enabledNo );
		setRightEnabled( enabledYes );
		yesButton.setEnabled( enabledYes );
	}
	
	@Override
	protected void populateDialog(){
		this.setText( getDialogTitle() );
		
		addNewGrid( 2, false, "", false);
		
		//Add the info message
		Widget messageLabel;
		if( isHtmlQuestionText() ) {
			messageLabel = new HTML( getDialogQuestion() );
		} else {
			messageLabel = new Label( getDialogQuestion() );
		}
		addToGrid( this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, messageLabel, false, false );
		
		//Add the close button
		noButton.setStyleName(CommonResourcesContainer.INFO_MESSAGE_DIALOG_BUTTON_STYLE);
		noButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent e){
				actionLeftButton();
			}
		});
		
		//Add the close button
		yesButton.setStyleName(CommonResourcesContainer.INFO_MESSAGE_DIALOG_BUTTON_STYLE);
		yesButton.setFocus( true );
		yesButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent e){
				actionRightButton();
			}
		});
		
		HorizontalPanel panelOne = new HorizontalPanel();
		panelOne.setWidth("100%");
		panelOne.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		HorizontalPanel panelTwo = new HorizontalPanel();
		panelTwo.setWidth("20%");
		panelTwo.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		panelOne.add( panelTwo );
		panelTwo.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		panelTwo.add( noButton );
		panelTwo.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT );
		panelTwo.add( yesButton );
		
		addToGrid( this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, panelOne, true, false );
	}
	
	@Override
	protected final void actionLeftButton() {
		negativeAnswerAction();
	}
	
	@Override
	protected final void actionRightButton() {
		positiveAnswerAction();
	}
	
	/**
	 * Allows to determine if the dialog's question should be wrapped
	 * into HTML object, if not then it will be a Label.
	 * Override to allow for HTML, returns false by default.  
	 * @return false by default
	 */
	public boolean isHtmlQuestionText() {
		return false;
	}
	
	/**
	 * Is called is the question is answered negatively
	 */
	protected abstract void negativeAnswerAction();
	
	/**
	 * Is called is the question is answered positively
	 */
	protected abstract void positiveAnswerAction();
	
	/**
	 * @return the title of this dialog
	 */
	protected abstract String getDialogTitle();
	
	/**
	 * @return the question of this dialog
	 */
	protected abstract String getDialogQuestion();

}
