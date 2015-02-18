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

import com.google.gwt.user.client.ui.Anchor;

/**
 * @author zapreevis
 * This abstract class should be implemented by every site body content widget.
 * I.e. the widget that takes the site section's content
 */
public interface SiteBodySectionContent extends SiteBodyComponent {
	/**
	 * Is called on window resize and allows to update all of the UI component sizes
	 */
	public abstract void updateUIElements();
	
	/**
	 * Allows to process the provided history token. The provided token has no site component prefix
	 * @param historyToken the history token to be processed by the body component
	 */
	public abstract void processHistoryToken( final String historyToken );
	
	/**
	 * Allows to update the target history token in the specified anchor object with
	 * the current history token corresponding to the current state of the site section
	 * @param anchorLink the anchor object to update
	 */
	public abstract void updateTargetHistoryToken( final Anchor anchorLink );
	
	/**
	 * Allows to set all of the site section UI components into the enabled/disabled 
	 * mode. Normally the entire site sections do not need to be enabled/disabled thus
	 * this default implementation does nothing.
	 * @param enabled true to enable, to disable false
	 */
	@Override
	public void setEnabled( boolean enabled );
}
