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
 * The client-side utilities package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.utils.widgets;

import com.google.gwt.event.shared.HandlerRegistration;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.dom.client.Element;

/**
 * @author zapreevis
 * Representa the Opera safe button that does not trigger the onClick event of the handlers unless the button is enabled
 */
public class Button extends com.google.gwt.user.client.ui.Button {
	
	/**
	 * @author zapreevis
	 * This class wraps around the provided click handler ans ensures
	 * that the click action is not triggered unless the button is enabled
	 */
	private class ClickHandlerWrapper implements ClickHandler {
		private final ClickHandler handler; 
		public ClickHandlerWrapper( final ClickHandler handler ) {
			this.handler = handler;
		}
		
		public void onClick( ClickEvent event ) {
			if( Button.this.isEnabled() && ( handler != null ) ) {
				handler.onClick( event );
			}
		}
	}
	
	/**
	 * The basic constructor
	 */
	public Button() {
		super();
	}

	/**
	 * Create the button with the given HTML caption
	 * @param html the HTML caption
	 */
	public Button(String html) {
		super(html);
	}

	/**
	 * This constructor may be used by subclasses to explicitly use an existing
	 * element. This element must be a &lt;button&gt; element.
	 * @param element the element to be used
	 */
	public Button(Element element) {
		super(element);
	}

	/**
	 * Creates a button with the given HTML caption and click listener.
	 * @param html the HTML caption
	 * @param handler the click handler
	 */
	public Button(String html, ClickHandler handler) {
		super( html );
		//Add the click handler separately
		addClickHandler( handler );
	}
	
	@Override
	public HandlerRegistration addClickHandler( ClickHandler handler ) {
		return super.addClickHandler( new ClickHandlerWrapper( handler ) );
	}
}
