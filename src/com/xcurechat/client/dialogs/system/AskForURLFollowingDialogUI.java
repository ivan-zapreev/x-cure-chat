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

import com.google.gwt.user.client.Window;

import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.dialogs.system.messages.QuestionMessageDialogUI;

/** 
 * @author zapreevis
 * Allows to ask if the user really want to follow the url
 */
public class AskForURLFollowingDialogUI extends QuestionMessageDialogUI {
	//The maximum visible length of the URL
	private static final int MAXIMUM_VISIBLE_URL_LENGTH = 45;
	//The url truncation symbols
	private static final String URL_TRUNCATED_SYMBOL_SUFFIX = "...";

	//The chat room data
	private final String url;
	
	public AskForURLFollowingDialogUI( final String url ){
		super();
		
		this.url = url;
		
		//Fill dialog with data
		populateDialog();
	}
	
	@Override
	protected String getDialogQuestion() {
		final String shortURL;
		if( url.length() > MAXIMUM_VISIBLE_URL_LENGTH ) {
			shortURL = url.substring(0, MAXIMUM_VISIBLE_URL_LENGTH - URL_TRUNCATED_SYMBOL_SUFFIX.length() ) + URL_TRUNCATED_SYMBOL_SUFFIX;
		} else {
			shortURL = url;
		}
		return I18NManager.getInfoMessages().doYouWantToFollowTheURL( shortURL );
	}

	@Override
	protected String getDialogTitle() {
		return titlesI18N.urlOpenRequestDialogTitle( );
	}

	@Override
	protected void negativeAnswerAction() {
		//Close the dialog window
		hide();
	}

	@Override
	protected void positiveAnswerAction() {
		//Open the URL
		Window.open(url, "_blank", "");
		//Close the dialog window
		hide();
	}
}
