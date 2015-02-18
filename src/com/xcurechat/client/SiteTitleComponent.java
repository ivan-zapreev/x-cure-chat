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

import com.xcurechat.client.data.MainUserData;

import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

/**
 * @author zapreevis
 * This interface sould be implemented by the site title components
 */
public interface SiteTitleComponent {
	
	/**
	 * Is called by the SiteManagerUI when the user is logged out
	 */
	public abstract void setLoggedOut();
	
	/**
	 * Is called by the SiteManagerUI when the user is logged in
	 * @param mainUserData the object storing the logged in user data
	 */
	public abstract void setLoggedIn( final MainUserData mainUserData );
	
	/**
	 * This method will be called when the client window is resized.
	 * This can be used to resize the component's internal elements.
	 */
	public abstract void onWindowResize();
	
	/**
	 * Returns the horizontal alignment of the component inside the title cell.
	 * Might depend on the current status of the component, i.e. logged in/out user
	 * @return the horizontal alignment of the component
	 */
	public abstract HorizontalAlignmentConstant getHorizontalAlignment();
	
	/**
	 * Returns the width in % relative to the client area width for the cell in
	 * the site title, that will store this component. Might depend on the current
	 * status of the component, i.e. logged in/out user
	 * @return the width in % for the cell, in the site-title panel, that will
	 * store this component, if the returned value is <0.0 the there is no width specified
	 */
	public abstract double getTitlePanelWidthInPercent();
}
