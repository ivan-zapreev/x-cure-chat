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
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.data;

import java.util.Date;

/**
 * @author zapreevis
 * Represents short file descriptor of a file owned by some user. This user has some user id and a user name
 */
public class ShortUserFileDescriptor extends ShortFileDescriptor {
	
	public int ownerID = ShortUserData.UNKNOWN_UID;
	public String ownerLoginName = null;
	public Date uploadDate = null;

	/**
	 * The basic constructor
	 */
	public ShortUserFileDescriptor() {
		super();
	}
	
	/**
	 * Allows to clone the given instance of the file descriptor
	 */
	public ShortUserFileDescriptor clone() {
		//First get the data from the super class
		ShortUserFileDescriptor copy = (ShortUserFileDescriptor) super.copyTo( new ShortUserFileDescriptor() );
		//Next copy the local data
		copy.ownerID = ownerID;
		copy.ownerLoginName = ownerLoginName;
		copy.uploadDate = new Date( uploadDate.getTime() );
		//Return the clone object
		return copy;
	}
}
