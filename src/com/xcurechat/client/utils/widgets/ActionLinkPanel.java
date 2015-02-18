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
 * The forum interface utils package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.utils.widgets;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * @author zapreevis
 * This class represents the panel with the image and the link, some sort of a button
 */
public class ActionLinkPanel extends Composite {
	//The main focus panel
	private final FocusPanel resultFocusPanel = new FocusPanel();
	//The horizontal panel storing the action link widgets
	private final HorizontalPanel actionPanel = new HorizontalPanel();
	//The action image, might be null
	private final Image actionImage = new Image( );
	//The action label
	private final Label actionLabel;
	
	//The action click handler if any
	private final ClickHandler action;
	//The handler's registration or null if the handler is not registered
	private HandlerRegistration registration = null;
	
	private final boolean withActionImage;
	private String actionImageUrlEnbl;
	private String actionImageUrlDisbl;
	
	private final String actionNameStr;
	private final String actionToolTipEnbl;
	private final String actionToolTipDisbl;
	
	//Stores the current enabledness status, i.e. true if the panel was attempted to be enabled otherwise false
	private boolean isEnabled;
	/**
	 * Constructs the action panel
	 * @param actionImageUrlEnbl the url of the enabled action image, for withActionImage == true
	 * @param actionToolTipEnbl the tool tip for the enabled action panel
	 * @param actionImageUrlDisbl the url of the disabled action image, for withActionImage == true
	 * @param actionToolTipDisbl the tool tip for the disabled action panel
	 * @param actionNameStr the text for the action panel link
	 * @param action the action triggered when the action panel is clicked, when enabled, if null then the action panel will always be disabled
	 * @param enable if true then the action panel will be initialized in the enabled mode, if action != null
	 * @param withActionImage if true then we construct tha action panel with the action image, otherwise we only have a text link
	 */	
	public ActionLinkPanel( final String actionImageUrlEnbl, final String actionToolTipEnbl,
							final String actionImageUrlDisbl, final String actionToolTipDisbl,
							final String actionNameStr, final ClickHandler action,
							final boolean enable, final boolean withActionImage ) {
		this( actionImageUrlEnbl, actionToolTipEnbl, actionImageUrlDisbl, actionToolTipDisbl,
			  actionNameStr, action, enable, withActionImage, false );
	}
	
	/**
	 * Constructs the action panel
	 * @param actionImageUrlEnbl the url of the enabled action image, for withActionImage == true
	 * @param actionToolTipEnbl the tool tip for the enabled action panel
	 * @param actionImageUrlDisbl the url of the disabled action image, for withActionImage == true
	 * @param actionToolTipDisbl the tool tip for the disabled action panel
	 * @param actionNameStr the text for the action panel link
	 * @param action the action triggered when the action panel is clicked, when enabled, if null then the action panel will always be disabled
	 * @param enable if true then the action panel will be initialized in the enabled mode, if action != null
	 * @param withActionImage if true then we construct tha action panel with the action image, otherwise we only have a text link
	 * @param hideActionNameStr if withActionImage == true and hideActionNameStr == true then we hide
	 * 							the action name string until the tile the user puts a mouse over the action link panel
	 */	
	public ActionLinkPanel( final String actionImageUrlEnbl, final String actionToolTipEnbl,
							final String actionImageUrlDisbl, final String actionToolTipDisbl,
							final String actionNameStr, final ClickHandler action,
							final boolean enable, final boolean withActionImage,
							final boolean hideActionNameStr ) {		
		//Store the provided data
		this.action = action;
		this.withActionImage = withActionImage;
		this.actionImageUrlEnbl = actionImageUrlEnbl;
		this.actionImageUrlDisbl = actionImageUrlDisbl;
		this.actionNameStr = ( ( actionNameStr != null ) && ( actionNameStr.trim().isEmpty() ) ) ? null : actionNameStr ;
		this.actionToolTipEnbl = ( actionToolTipEnbl == null || actionToolTipEnbl.trim().isEmpty() ) ? actionNameStr : actionToolTipEnbl ;
		this.actionToolTipDisbl = ( actionToolTipDisbl == null || actionToolTipDisbl.trim().isEmpty() ) ? "" : actionToolTipDisbl;

		//Create the action panel
		actionPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT );
		actionPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		actionPanel.addStyleName( CommonResourcesContainer.FORCE_INLINE_DISPLAY_STYLE );
		actionPanel.addStyleName( CommonResourcesContainer.NO_TABLE_SPACING_STYLE );
		if( withActionImage ) {
			actionImage.addStyleName( CommonResourcesContainer.FORCE_INLINE_DISPLAY_STYLE );
			actionPanel.add( actionImage );
			//Add the delimiter only if there is an image and there is an action name
			if( this.actionNameStr != null ) {
				HTML delimiter = new HTML("&nbsp;");
				delimiter.addStyleName( CommonResourcesContainer.FORCE_INLINE_DISPLAY_STYLE );
				actionPanel.add( delimiter );
			}
		}
		
		if( this.actionNameStr != null ) {
			//There is an action name
			actionLabel = new Label( this.actionNameStr );
			actionLabel.setWordWrap( false );
			actionLabel.addStyleName( CommonResourcesContainer.FORCE_INLINE_DISPLAY_STYLE );
			if( withActionImage && hideActionNameStr ) {
				//In case we want to have a short action panel, then if we have the
				//image and we are asked to hide the link name, do the following 
				resultFocusPanel.addMouseOverHandler( new MouseOverHandler(){
					@Override
					public void onMouseOver(MouseOverEvent event) {
						if( actionPanel.getWidgetIndex( actionLabel ) == -1 ) {
							//If the action name is not in the panel, then add it
							actionPanel.add( actionLabel );
						}
					}
				});
				resultFocusPanel.addMouseOutHandler( new MouseOutHandler(){
					@Override
					public void onMouseOut(MouseOutEvent event) {
						if( actionPanel.getWidgetIndex( actionLabel ) != -1 ) {
							//If the action name is in the panel, then remove it
							actionPanel.remove( actionLabel );
						}
					}
				});
			} else {
				//If we do not have an action image or we did not ask for hiding
				//the link name, then just add the link name to the panel
				actionPanel.add( actionLabel );
			}
		} else {
			//There is no action name at all
			actionLabel = null;
		}
		resultFocusPanel.add( actionPanel );
		
		//Enable the action panel if needed
		setEnabled( enable );
		
		//Initialize the composite
		initWidget( resultFocusPanel );
	}
	
	/**
	 * Add an "important-link" style to the action label
	 */
	public void setImportant() {
		if( actionLabel != null ) {
			actionLabel.addStyleName( CommonResourcesContainer.ACTION_LINK_IMP_STYLE );
		}
	}
	
	/**
	 * Allows to set the action link panel as enabled or disabled
	 * @param enbl true for enable, otherwise fase
	 */
	public void setEnabled( final boolean enbl ) {
		//Store the current enabledness status
		this.isEnabled = enbl;
		//If the handler is not set then
		final boolean enable = enbl && ( action != null );
		if( registration != null ) {
			registration.removeHandler();
			registration = null;
		}
		if( enable ) {
			if( withActionImage ) {
				actionImage.setUrl( actionImageUrlEnbl );
				actionImage.removeStyleName( CommonResourcesContainer.ACTION_IMAGE_DISABLED_LINK_STYLE );
				actionImage.addStyleName( CommonResourcesContainer.ACTION_IMAGE_LINK_STYLE );
			}
			if( actionLabel != null ) {
				actionLabel.removeStyleName( CommonResourcesContainer.DIALOG_LINK_DISABLED_STYLE );
				actionLabel.addStyleName( CommonResourcesContainer.DIALOG_LINK_BLUE_STYLE );
			}
			registration = resultFocusPanel.addClickHandler( action );
			resultFocusPanel.setTitle( actionToolTipEnbl );
		} else {
			if( withActionImage ) {
				actionImage.setUrl( actionImageUrlDisbl );
				actionImage.removeStyleName( CommonResourcesContainer.ACTION_IMAGE_LINK_STYLE );
				actionImage.addStyleName( CommonResourcesContainer.ACTION_IMAGE_DISABLED_LINK_STYLE );
			}
			if( actionLabel != null ) {
				actionLabel.removeStyleName( CommonResourcesContainer.DIALOG_LINK_BLUE_STYLE );
				actionLabel.addStyleName( CommonResourcesContainer.DIALOG_LINK_DISABLED_STYLE );
			}
			resultFocusPanel.setTitle( actionToolTipDisbl );
		}
	}
	
	/**
	 * Allows to re-set the enabled action image
	 * @param actionImageUrlEnbl the new enabled action image url
	 */
	public void setActionImageUrlEnbl( final String actionImageUrlEnbl ) {
		this.actionImageUrlEnbl = actionImageUrlEnbl;
		//Update the image
		setEnabled( isEnabled );
	}
	
	/**
	 * Allows to re-set the disabled action image
	 * @param actionImageUrlEnbl the new disabled action image url
	 */
	public void setActionImageUrlDisbl( final String actionImageUrlDisbl ) {
		this.actionImageUrlDisbl = actionImageUrlDisbl;
		//Update the image
		setEnabled( isEnabled );
	}
}
