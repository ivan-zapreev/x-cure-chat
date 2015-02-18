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
 * The user data objects package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.data;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author zapreevis
 * This class is used for storing the site-related (statistical) data
 */
public class SiteInfoData implements IsSerializable {
	//The interval of time with which we update the users statistics from the server
	public final static int SERVER_UPDATES_PERIODICITY_MILLISEC = 20000;
	//The site version is stored here
	public static final String SITE_VERSION_ID = "1.2.5"; 
	
	//The total number of registered users
	public int totalRegisteredUsers = 0;
	//The number of logged-in people 
	public int registeredUsersOnline = 0;
	//The number of people browsing the site
	public int visitorsOnline = 0;
	//The site version data that will be sent to the client for 
	//being compared with the SITE_VERSION_ID value on the client.
	public String serverSiteVersionId = SITE_VERSION_ID;
	
	/**
	 * Allows to clone this object
	 */
	public SiteInfoData clone() {
		final SiteInfoData cloneData = new SiteInfoData();
		cloneData.totalRegisteredUsers = this.totalRegisteredUsers;
		cloneData.registeredUsersOnline = this.registeredUsersOnline;
		cloneData.visitorsOnline = this.visitorsOnline;
		cloneData.serverSiteVersionId = this.serverSiteVersionId;
		return cloneData;
	}
}
