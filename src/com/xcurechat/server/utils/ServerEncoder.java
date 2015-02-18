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
 */
package com.xcurechat.server.utils;

import com.xcurechat.client.utils.EncoderInt;

/**
 * @author zapreevis
 * The encoder for the server side
 */
public class ServerEncoder implements EncoderInt {

	@Override
	public String encodeURLComponent(final String value) {
		return WebUtilities.encodeGWTURLComponent( value );
	}
	
	@Override
	public String decodeURLComponent(final String value) {
		return WebUtilities.decodeGWTURLComponent( value );
	}		

}
