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
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.chat;

import com.allen_sauer.gwt.log.client.Log;
import com.xcurechat.client.SiteBodySectionContentProxy;

import com.xcurechat.client.utils.widgets.UserAvatarImageWidget;

/**
 * @author zapreevis
 * This is a proxy object for the chat site section body component
 */
public class RoomsManagerUIProxy extends SiteBodySectionContentProxy<RoomsManagerUI> {
	
	//The additional known method ids for the specific methods located in RoomsManagerUI
	protected final static int openRoomsManagerDialog_METHOD_ID 	= getNewMethodId();
	protected final static int openMessagesManagerDialog_METHOD_ID 	= getNewMethodId();

	/**
	 * The inherited constructor
	 */
	public RoomsManagerUIProxy(String siteSectionPrefix) {
		super(siteSectionPrefix);
	}
	
	/**
	 * Should be called when the user is logged out from the site in order to remove the rooms manager
	 */
	void clearRoomsManagerUIInstance() {
		final RoomsManagerUI instance = getSiteSectionBodyComponent();
		if( instance != null ) {
			//Remove the old listener interface
			UserAvatarImageWidget.removeAvatarSpoilerChangeListener( instance );
			//Remove the instance of the site body component
			removeSiteSectionBodyComponent();
		}
	}
	
	/**
	 * Allows to open the rooms manager dialog. The reason we do it here is
	 * because of the instance of the rooms manager that has to be used.
	 */
	public final void openRoomsManagerDialog() {
		QueueMethodCall<Void> methodCall = new QueueMethodCall<Void>();
		methodCall.methodId = openRoomsManagerDialog_METHOD_ID;
		queueMethodCall( methodCall );
		Log.debug("Invoking method openRoomsManagerDialog from " + siteSectionPrefix );
	}
	
	/**
	 * Allows to open the messages manager. The reason we do it here is
	 * because of the instance of the rooms manager that has to be used.
	 */
	public final void openMessagesManagerDialog() {
		QueueMethodCall<Void> methodCall = new QueueMethodCall<Void>();
		methodCall.methodId = openMessagesManagerDialog_METHOD_ID;
		queueMethodCall( methodCall );
		Log.debug("Invoking method openMessagesManagerDialog from " + siteSectionPrefix );
	}
	
	@Override
	protected final boolean processQueuedMethodCall( final RoomsManagerUI sectionComponent, final QueueMethodCall<?> queuedMethodCall ) {
		//Process the method call by the super method
		boolean isOK = super.processQueuedMethodCall( sectionComponent, queuedMethodCall );
		//If the processing was not successful, try the additional methods defined here
		if( ! isOK ) {
			if( queuedMethodCall.methodId == openRoomsManagerDialog_METHOD_ID ) {
				isOK = true;	//The method was found and processed
				sectionComponent.openRoomsManagerDialog( );
			} else {
				if( queuedMethodCall.methodId == openMessagesManagerDialog_METHOD_ID ) {
					isOK = true;	//The method was found and processed
					sectionComponent.openMessagesManagerDialog( );
				}
			}
		}
		
		return isOK;
	}
	
	@Override
	public final RoomsManagerUI loadSiteSectionBodyComponentInstance( String siteSectionPrefix ) {
		//Instantiate the manager
		final RoomsManagerUI instance = new RoomsManagerUI( siteSectionPrefix );
		//Register the new listener instance
		UserAvatarImageWidget.addAvatarSpoilerChangeListener( instance );
		//Return the newly created instance
		return instance;
	}
}
