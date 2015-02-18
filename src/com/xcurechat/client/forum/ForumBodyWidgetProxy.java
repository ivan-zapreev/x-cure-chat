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
 * The forum site section interface package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.forum;

import com.xcurechat.client.SiteBodySectionContentProxy;

/**
 * @author zapreevis
 * This is a proxy object for the forum site section body component
 */
public class ForumBodyWidgetProxy extends SiteBodySectionContentProxy<ForumBodyWidget> {

	/**
	 * The inherited constructor
	 */
	public ForumBodyWidgetProxy(String siteSectionPrefix) {
		super(siteSectionPrefix);
	}

	@Override
	public final ForumBodyWidget loadSiteSectionBodyComponentInstance( String siteSectionPrefix ) {
		return new ForumBodyWidget( siteSectionPrefix );
	}

}
