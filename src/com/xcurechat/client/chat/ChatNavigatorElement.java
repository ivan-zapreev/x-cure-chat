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
 * The chat site section interface package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.chat;

import com.google.gwt.user.client.ui.SimplePanel;

import com.xcurechat.client.SiteNavigator;
import com.xcurechat.client.SiteNavigatorElement;


import com.xcurechat.client.userstatus.UserStatusManager;
import com.xcurechat.client.userstatus.UserStatusQueue;
import com.xcurechat.client.userstatus.UserStatusType;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This modification of the site navigator element allows to set the old
 * user status when one returns to the chat from the other site sections.
 */
public class ChatNavigatorElement extends SiteNavigatorElement<RoomsManagerUIProxy> {

	//The only available room's manager proxy instance
	private static RoomsManagerUIProxy instance = new RoomsManagerUIProxy( CommonResourcesContainer.CHAT_SECTION_IDENTIFIER_STRING +
																		   CommonResourcesContainer.SITE_SECTION_HISTORY_DELIMITER );
	
	/**
	 * Allows to open the rooms manager dialog. The reason we do it here is
	 * because of the instance of the rooms manager that has to be used.
	 */
	public static void openRoomsManagerDialog() {
		instance.openRoomsManagerDialog();
	}
	
	/**
	 * Allows to open the messages manager. The reason we do it here is
	 * because of the instance of the rooms manager that has to be used.
	 */
	public static void openMessagesManagerDialog() {
		instance.openMessagesManagerDialog();
	}
	
	/**
	 * The basic constructor
	 */
	public ChatNavigatorElement( boolean isOnLogOutDisabled, SimplePanel siteBodyPanel,
								 SiteNavigator siteNavigator, UserStatusType forceUserStatus ) {
		super( "button_chat", isOnLogOutDisabled, siteBodyPanel, siteNavigator, forceUserStatus );
	}
	
	@Override
	protected UserStatusType getUserStatusToForce() {
		//Get the previous user status
		final UserStatusQueue instance = UserStatusManager.getUserStatusQueue();
		final UserStatusType currentStatus = instance.getCurrentUserStatus();
		//Update the user status
		UserStatusType nextStatus;
		if( currentStatus == UserStatusType.ANOTHER_SITE_SECTION ) {
			//If the current user status is ANOTHER_SITE_SECTION then
			final UserStatusType previousStatus = instance.getPreviousUserStatus();
			if( previousStatus == UserStatusType.ANOTHER_SITE_SECTION ||
				previousStatus == UserStatusType.AWAY	) {
				//If the previous user status was ANOTHER_SITE_SECTION or AWAY then we switch to FREE_FOR_CHAT
				nextStatus = UserStatusType.FREE_FOR_CHAT;
			} else {
				//Otherwise we force the old user status
				nextStatus = previousStatus;
			}
		} else {
			//If the current user status is different from ANOTHER_SITE_SECTION then we keep the current status
			nextStatus = currentStatus;
		}
		
		return nextStatus;
	}

	@Override
	public RoomsManagerUIProxy getBodyContentWidget() {
		return instance;
	}
	
	@Override
	public boolean isWithBackground() {
		return false;
	}

	@Override
	public String getSiteSectionIdentifier() {
		return CommonResourcesContainer.CHAT_SECTION_IDENTIFIER_STRING;
	}
	
	@Override
	public String getSiteBodyComponentHistoryPrefix() {
		return getSiteSectionIdentifier() + CommonResourcesContainer.SITE_SECTION_HISTORY_DELIMITER;
	}
	
	@Override
	public void setLoggedIn( final boolean isLoggedIn ) {
		super.setLoggedIn( isLoggedIn );
		//In case the user is logging out then remove the existing instance of the RoomsManagerUI
		if( isLoggedIn == false ) {
			instance.clearRoomsManagerUIInstance();
		}
	}
}
