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
package com.xcurechat.client.popup;

import com.google.gwt.user.client.Window;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import com.xcurechat.client.SiteManagerUI;
import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * The base class for all our popup panels
 */
public class InfoPopupPanel extends PopupPanel {
	
	//The decorator panel that makes the rounded corners happen
	private DecoratorPanel decoratedPanel = new DecoratorPanel();

	//The internationalization data object
	protected final UITitlesI18N titlesI18N = I18NManager.getTitles();
	
	/**
	 * @author zapreevis
	 * This call back listener class makes sure that the info popup is positioned
	 * in such a way that it is close to the item we clicked on and that it 
	 * does not go outside the page area (may be it is not really needed).
	 */
	public class InfoPopUpPositionCallback implements PopupPanel.PositionCallback {
		private final Widget sender;
		
		public InfoPopUpPositionCallback(final Widget sender){
			this.sender = sender;
		}
		
		/**
		 * We start from the left top corner of the item on which the click was done.
		 * This position is taken as the left top corner for the pop-up. If the popup size is
		 * too large then we can go outside of the visible area, we avoid this by calculating
		 * the position of the right bottom corner of the popup. If it is outside of the visible
		 * area then shift the popup by the extra lenghts, and just in case subtract 20 pixels 
		 */
		public void setPosition(int offsetWidth, int offsetHeight){
			int left = sender.getAbsoluteLeft();
			int right = offsetWidth + left;
			int right_margin = Window.getClientWidth() - right; 
			if( right_margin < 0  ) {
				left += right_margin - 20;
			}
			int top = sender.getAbsoluteTop();
			int bottom = offsetHeight + top;
			int bottom_margin = Window.getClientHeight() - bottom;
			if( bottom_margin < 0  ) {
				top += bottom_margin - 20;
			}
			setPopupPosition( left, top);
		}
	}
	
	protected InfoPopupPanel(boolean autoHide, boolean modal){
		super(autoHide, modal);
		
		//Register the popup in the list of popups eligible for closing on user-logout
		SiteManagerUI.getInstance().registerPopup( this );
		
		//Set Animation ON
		this.setAnimationEnabled( true );
		
		//Add the close by escape handler
		this.addDomHandler( new KeyPressHandler(){
			public void onKeyPress( KeyPressEvent event ){
				NativeEvent nativeEvent = event.getNativeEvent();
				if( ( nativeEvent.getKeyCode() == KeyCodes.KEY_ESCAPE ) ) {
					hide();
				}
			}
		}, KeyPressEvent.getType() );
		
		//Set the width of the panel
		//this.setWidth("25%");
		
		//Adding decorator panel inside the pop-up because 
		//we want the panel to have rounded borders.
		decoratedPanel.setSize( "100%", "100%");
		decoratedPanel.setStyleName( CommonResourcesContainer.INFO_POPUP_STYLE_NAME );
		this.add( decoratedPanel );
	}
	
	/**
	 * Allows to add the content widget to the panel
	 * @param widget the widget to add
	 */
	protected void addContentWidget( final Widget widget) {
		widget.addStyleName( CommonResourcesContainer.INFO_POPUP_PANEL_CONTENT_STYLE );
		decoratedPanel.add( widget );
	}
	
	/**
	 * Adds the label and returns the pointer to it
	 */
	protected Label addLabel(HorizontalPanel horizontalPanel, final String name, final String style){
		Label lab  = new Label( name );
		lab.setWordWrap(false);
		lab.setStyleName( style );
		horizontalPanel.add( lab );
		return lab;
	}
	
	/**
	 * Tries to cure the problem with having a non-selected pop-up, when we attempt to hide it!
	 * TODO: This still does not really do the trick. The problem is that when the focus is set
	 * so something else, like a tab bar of the tabbed panel, then the popup does not disappear
	 * on hide. I believe this is because the focus is elsewhere so events are not dispatched.
	 */
	@Override
	public void hide(){
		//Unregister this dialog's popup window
		SiteManagerUI.getInstance().unregisterPopup( this );
		//Hide the popup window
		super.hide();
		show();
		super.hide();
		RootPanel.get().remove(this);
		setVisible(false);
	}
}
