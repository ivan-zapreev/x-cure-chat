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
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.forum.messages;

import com.xcurechat.client.data.ForumMessageData;

/**
 * @author zapreevis
 * is responsible for providing a correct forum message widget for each type of the the forum message
 */
public final class ForumMessageWidgetFactory {
	
	public static ForumMessageWidget getWidgetInstance( final ForumMessageData messageData, final boolean isLoggedIn, final boolean oddOrNot ) {
		return getWidgetInstance( messageData, isLoggedIn, oddOrNot, true );
	}
	
	public static ForumMessageWidget getWidgetInstance( final ForumMessageData messageData, final boolean isLoggedIn,
			   											final boolean oddOrNot, final boolean showActionPanel ) {
		return getWidgetInstance( messageData, isLoggedIn, oddOrNot, showActionPanel, false );
	}
	
	public static ForumMessageWidget getWidgetInstance( final ForumMessageData messageData, final boolean isLoggedIn,
													   final boolean oddOrNot, final boolean showActionPanel,
													   final boolean forseContentOpen ) {
		return getWidgetInstance( messageData, isLoggedIn, oddOrNot, showActionPanel, forseContentOpen, true );
	}
	
	public static ForumMessageWidget getWidgetInstance( final ForumMessageData messageData, final boolean isLoggedIn,
													   final boolean oddOrNot, final boolean showActionPanel,
													   final boolean forseContentOpen, final boolean isMsgTitleClickable ) {
		if( messageData.isForumSectionMessage() ) {
			return new ForumSectionMessageUI( messageData, isLoggedIn, oddOrNot, showActionPanel, forseContentOpen, isMsgTitleClickable ); 
		} else {
			if( messageData.isForumTopicMessage() ) {
				return new ForumTopicMessageUI( messageData, isLoggedIn, oddOrNot, showActionPanel, forseContentOpen, isMsgTitleClickable );
			} else {
				return new ForumSimpleMessageUI( messageData, isLoggedIn, oddOrNot, showActionPanel, forseContentOpen, isMsgTitleClickable );
			}
		}
	}
}
