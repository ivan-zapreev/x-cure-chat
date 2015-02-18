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
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client;

/**
 * @author zapreevis
 * This is the common interface for the site body part
 */
public interface SiteBodyComponent {
	
	/**
	 * Allows to tell the body component that the user is logged in
	 */
	public abstract void setUserLoggedIn();
	
	/**
	 * Allows to tell the body component that the user is logged out
	 */
	public abstract void setUserLoggedOut();
	
	/**
	 * This method is called before the site body component is added to the site's main panel.
	 * I.e. it is called each time before the given site section is selected
	 */
	public abstract void onBeforeComponentIsAdded();
	
	/**
	 * This method is called right after the site body component is added to the
	 * site's main panel. I.e. it is called each time after the given site section
	 * is displayed
	 */
	public abstract void onAfterComponentIsAdded();
	
	/**
	 * This method is called before the site body component is removed from the site's main panel.
	 * I.e. it is called each time before the given site section is de-selected
	 */
	public abstract void onBeforeComponentIsRemoved();
	
	/**
	 * Allows to set the component into enabled/disabled mode
	 * @param enabled true to disable, false to enable
	 */
	public abstract void setEnabled( boolean enabled );

}
