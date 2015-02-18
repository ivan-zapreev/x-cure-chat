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
 * The forum interface package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.forum;

import com.xcurechat.client.utils.widgets.MessagesPanel;

/**
 * @author zapreevis
 * The panel for storing the forum messages
 */
public class ForumMessagesPanel extends MessagesPanel {
	
	/**
	 * See the constructor of the super class
	 * @param addDecorations if true then we use the decorated panel with the rounded corners around this widget
	 * @param showActionPanel true if the action panel should be shown, otherwise false
	 * @param clickableMessageTitles if the message titles should be allowed to be clickable
	 * @param siteSectionPrefix the history token site section prefix
	 */
	public ForumMessagesPanel( final boolean addDecorations, final boolean showActionPanel,
							   final boolean clickableMessageTitles, final String siteSectionPrefix ) {
		super( addDecorations, showActionPanel, clickableMessageTitles, siteSectionPrefix );
		
		//Complete creation of the panel
		populate();
	}

}
