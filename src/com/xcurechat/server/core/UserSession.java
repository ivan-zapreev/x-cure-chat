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
 * The server-side RPC package.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.server.core;

import com.xcurechat.client.data.MainUserData;

/**
 * @author zapreevis
 * The object that manages user session
 */
public class UserSession{

	//The user Data object
	private MainUserData userData;
	
	/**
	 * Creates the user session support object
	 * @param userData the user data object
	 */
	public UserSession( MainUserData userData ){
		this.userData = userData;
	}
	
	/**
	 * Updates the user data
	 * @param userData the new user data
	 */
	public void setUserData( MainUserData userData ){
		this.userData = userData;
	}
	
	/**
	 * Get the user data
	 * @return the user data
	 */
	public MainUserData getUserData( ){
		return userData;
	}
}
