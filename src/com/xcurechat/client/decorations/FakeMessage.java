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
package com.xcurechat.client.decorations;

import com.xcurechat.client.data.ChatMessage;

/**
 * @author zapreevis
 * This is a fake message class for the site's introduction panel
 */
public class FakeMessage extends ChatMessage {
	
	//The urls for the fake chat-message image
	public transient String thumbnailURL = null;
	public transient String originalURL = null;
	
	//The fake chat message avatar UI
	public FakeChatMessageAvatarUI avatar = null;
	
	//True if this is meft message, otherwise false
	public boolean isLeftMessage = false;
}
