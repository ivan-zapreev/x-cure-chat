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
 * The search related package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.data.search;

import com.google.gwt.user.client.rpc.IsSerializable;

import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UIErrorMessages;

/**
 * @author zapreevis
 * This class is supposed to store the main search parameters for the user search.
 */
public class UserSearchData implements IsSerializable {
	
	//The maximum allowed number of users per page for the search results
	public static final int MAX_NUMBER_OF_USERS_PER_PAGE = 9;
	//The maximum allowed search string length
	public static final int MAX_SEARCH_STRING_LENGTH = 50;
	
 	//The user gender IDs they are supposed to be used in the UserSearchData
	public static final Integer USER_GENDER_UNKNOWN = new Integer(-1);
	public static final Integer USER_GENDER_MALE = new Integer(0);
	public static final Integer USER_GENDER_FEMALE = new Integer(1);

	//The string we will search for, can be empty or null
	public String searchString = "";
	//The modifiers for where to look for the string
	public boolean isLogin = false;
	public boolean isFirstName = false;
	public boolean isLastName = false;
	public boolean isCity = false;
	public boolean isCountry = false;
	public boolean isAboutMyself = false;
	//Does the user age matter?
	public int userAgeIntervalID = UserData.AGE_UNKNOWN;
	//Does the user gender matter?
	public int userGender = USER_GENDER_UNKNOWN;
	//Does the user have to be online? False for all, True for online only.
	public boolean isOnline = false;
	//Does the user have to be a friend? False for all, True for friends only.
	public boolean isFriend = false;
	//Does the user have to have pictures? False for all, True for with pictures only.
	public boolean hasPictures = false;
	
	//This field stores true is the search is done by the admin, then
	//he can see some extra information, like system room access entries
	//NOTE: this field is only set on the server side, so it is safe!!!
	public transient boolean isAdmin = false;
	
	/**
	 * Validates the search query data throws an exception if something is inconsistent
	 * @throws SiteException
	 */
	public void validate() throws InternalSiteException {
		UIErrorMessages errorsI18N = I18NManager.getErrors();
		if( ( searchString != null ) && ( searchString.length() > MAX_SEARCH_STRING_LENGTH ) ) {
			throw new InternalSiteException( errorsI18N.searchQueryIsTooLong( MAX_SEARCH_STRING_LENGTH ) );
		} 
	}
}
