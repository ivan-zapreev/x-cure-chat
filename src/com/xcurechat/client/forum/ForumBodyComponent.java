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
package com.xcurechat.client.forum;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.xcurechat.client.SiteBodyComponent;
import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This class is the base class for all of the forum's body components
 */
public abstract class ForumBodyComponent extends Composite implements SiteBodyComponent {
	
	//The localization manager
	protected final UITitlesI18N i18NTitles = I18NManager.getTitles();
	//The decorated panel storing its elements.
	private final SimplePanel mainPanel;
	//The main widget that is the actual widget wrapped with this panel widget
	private Widget mainWidget = null;
	//Is true if we habe a decorated panel as the main panel, otherwise false
	protected final boolean addDecorations;
	
	/**
	 * This constructor has to be used for all the body components,
	 * it is provided with the forum manager
	 * @param addDecorations if true then we use the decorated panel with the rounded corners around this widget
	 */
	public ForumBodyComponent( final boolean addDecorations ) {
		this.addDecorations = addDecorations;
		if( addDecorations ) {
			mainPanel = new DecoratorPanel();
			mainPanel.setStyleName( CommonResourcesContainer.GRAY_ROUNDED_CORNER_PANEL_STYLE );
		} else {
			mainPanel = new SimplePanel();
		}
		initWidget(mainPanel);
	}
	
	/**
	 * Allows to set the main widget of this forum body component.
	 * First removes the previously set widget if any.
	 * @param widget the widget to set
	 */
	public void setComponentWidget( final Widget widget ){
		mainPanel.clear();
		mainWidget = widget;
		mainWidget.addStyleName( CommonResourcesContainer.FORUM_TITLE_COMPONENT_STYLE );
		mainPanel.add( mainWidget );
	}
	
	/**
	 * Allows to add style to the panel that is the root of this composite.
	 * @param style the style name to add
	 */
	public void addDecPanelStyle( final String styleName ){
		mainPanel.addStyleName( styleName );
	}
	
	/**
	 * Allows to set the decorated panel's width in pixels.
	 * In case the decoration panel is not set to be the main
	 * one then the width of the decorations is subtracted
	 * from the provided value
	 * @param width the desired width in pixels, including the decorations width
	 */
	public void setDecPanelWidth( final int width ) {
		final int mainWidgetWidth;
		final int mainPanelWidth;
		//Compute the width of the decorated panel and the main widget
		if( addDecorations ) {
			mainPanelWidth  = width;
			mainWidgetWidth = width - CommonResourcesContainer.DECORATIONS_WIDTH;
		} else {
			mainPanelWidth  = width - CommonResourcesContainer.DECORATIONS_WIDTH;
			mainWidgetWidth = width - CommonResourcesContainer.DECORATIONS_WIDTH;
		}
		//Set the width of the main pane;
		mainPanel.setWidth( mainPanelWidth + "px" );
		//Set the width of the main widget
		if( mainWidget != null ) {
			mainWidget.setWidth( mainWidgetWidth + "px" );
		}
	}
	
	/**
	 * Currently is not needed, thus has an empty default implementation
	 */
	@Override
	public void onAfterComponentIsAdded() {
	}
	
	/**
	 * Allows to update the panel's height on window resize
	 * @param forceScrolling if true then the component is asked to force vertical scrolling
	 * @param adjustWidth if we want to adjust width
	 * @param percentWidth if adjustWidth == true then we use this percent value to set the width relative to the view area 
	 */
	public abstract void updateUIElements( final boolean forceScrolling, final boolean adjustWidth, final int percentWidth );

}
