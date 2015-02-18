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
 * The server-side utilities package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.server.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

import com.xcurechat.client.rpc.exceptions.InternalSiteException;

/**
 * @author zapreevis
 * This class allows to compute the MD5 hash of a binary array of data
 */
public class MD5 {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( MD5.class );
	
	public static String getMD5( byte[] data ) throws InternalSiteException {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(data,0,data.length);
			BigInteger bigInt = new BigInteger(1,m.digest());
			return String.format("%1$032X", bigInt);
		} catch ( NoSuchAlgorithmException e ) {
			logger.error("Unable to find the MD5 algorithm for the digest", e);
			throw new InternalSiteException( InternalSiteException.UNKNOWN_INTERNAL_SITE_EXCEPTION_ERR );
		}
	}
}
