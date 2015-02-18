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
 * The user interface internationalization package.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.i18n;

import com.google.gwt.core.client.GWT;

public class I18NManager {
	//The internationalized sitles
	private static UITitlesI18N uiTitles = null;
	//The internationalized errors
	private static UIErrorMessages uiErrors = null;
	//The internationalized messages
	private static UIInfoMessages uiMessages = null;
	
	public static UITitlesI18N getTitles(){
		if( uiTitles == null ) {
			uiTitles = (UITitlesI18N)GWT.create(UITitlesI18N.class);
		}
		return uiTitles;
	}

	public static UIErrorMessages getErrors(){
		if( uiErrors == null ) {
			uiErrors = (UIErrorMessages)GWT.create(UIErrorMessages.class);
		}
		return uiErrors;
	}
	
	public static UIInfoMessages getInfoMessages(){
		if( uiMessages == null ) {
			uiMessages = (UIInfoMessages)GWT.create(UIInfoMessages.class);
		}
		return uiMessages;
	}
}
