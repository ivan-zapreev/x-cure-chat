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
package com.xcurechat.client.utils;

import com.google.gwt.http.client.URL;

/**
 * @author zapreevis
 * Contains the encoding and decoding functions that have to be used in the client
 */
public class ClientEncoder implements EncoderInt {

	/**
	 * @see com.xcurechat.client.utils.EncoderInt#decodeURLComponent(java.lang.String)
	 */
	@Override
	public String decodeURLComponent(final String value) {
		return URL.decodeQueryString( value );
	}

	/**
	 * @see com.xcurechat.client.utils.EncoderInt#encodeURLComponent(java.lang.String)
	 */
	@Override
	public String encodeURLComponent(String value) {
		return URL.encodeQueryString( value );
	}
}
