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
package com.xcurechat.client.chat;

import java.util.Map;
import java.util.Iterator;
import java.util.Map.Entry;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;


import com.xcurechat.client.data.MessageFontData;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This object allows to select the font, font size and the font color
 */
public class FontSelectorPanelUI extends Composite {
	
	//The main horizontal panel representing this element
	private final HorizontalPanel mainHorizontalPanel = new HorizontalPanel();
	//The font list box
	private final ListBox fontListBox = new ListBox();
	//The font size list box
	private final ListBox fontSizeListBox = new ListBox();
	//The font color list box
	private final ListBox fontColorListBox = new ListBox();
	//The sample Label which shows the impact of the selections
	private final Label sampleLabel = new Label();
	//The titles internationalization object
	private final UITitlesI18N titlesI18N = I18NManager.getTitles();
	
	public FontSelectorPanelUI() {
		//Construct the object
		mainHorizontalPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		mainHorizontalPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		
		mainHorizontalPanel.add( InterfaceUtils.getNewFieldLabel(titlesI18N.fontTitile(), false) );
		initializeListBox( fontListBox, MessageFontData.fontTypeToFontFamilyName,
							MessageFontData.fontTypeToCssClass, MessageFontData.DEFAULT_FONT_FAMILY );
		mainHorizontalPanel.add( fontListBox );
		
		mainHorizontalPanel.add( new HTML("&nbsp;") );
		
		mainHorizontalPanel.add( InterfaceUtils.getNewFieldLabel(titlesI18N.fontSizeTitile(), false) );
		initializeListBox( fontSizeListBox, MessageFontData.fontSizeToSizeName,
							MessageFontData.fontSizeToCssClass, MessageFontData.DEFAULT_FONT_SIZE );
		mainHorizontalPanel.add( fontSizeListBox );
		
		mainHorizontalPanel.add( new HTML("&nbsp;") );
		
		mainHorizontalPanel.add( InterfaceUtils.getNewFieldLabel(titlesI18N.fontColorTitile(), false) );
		initializeListBox( fontColorListBox, MessageFontData.fontColorToColorName,
							MessageFontData.fontColorToCssClass, MessageFontData.DEFAULT_FONT_COLOR );
		mainHorizontalPanel.add( fontColorListBox );
		
		mainHorizontalPanel.add( new HTML("&nbsp;") );
		
		mainHorizontalPanel.add( InterfaceUtils.getNewFieldLabel(titlesI18N.fontSampleField(), false) );
		sampleLabel.setText( titlesI18N.fontSampleText() );
		sampleLabel.setTitle( titlesI18N.fontSampleToolTip() );
		mainHorizontalPanel.add( sampleLabel );
		
		mainHorizontalPanel.addStyleName( CommonResourcesContainer.FONT_SELECTION_PANEL_STYLE );
		//Initialize the widget
		initWidget( mainHorizontalPanel );
	}
	
	/**
	 * Allows to update the sample view based on the currently selected text styles
	 */
	public void updateSampleViewStyles() {
		//Since when the dialog is initialized not all of the list boxes are filled with
		//data, we have to check that they are not empty, otherwise we get a problem
		if( fontListBox.getItemCount() > 0 ) {
			sampleLabel.setStyleName( getListBoxSelectedStyleName( fontListBox ) );
		}
		if( fontSizeListBox.getItemCount() > 0 ) {
			sampleLabel.addStyleName( getListBoxSelectedStyleName( fontSizeListBox ) );
		}
		if( fontColorListBox.getItemCount() > 0 ) {
			sampleLabel.addStyleName( getListBoxSelectedStyleName( fontColorListBox ) );
		}
		//And finally we add the sample's label
		sampleLabel.addStyleName( CommonResourcesContainer.FONT_SAMPLE_LABEL_STYLE );
	}
	
	/**
	 * Allows to set the style name as selected in the list box
	 * @param listBox the list box to set the value in
	 * @param styleName the style which we want to set as selected
	 */
	public void setListBoxStyleName( final ListBox listBox, final String styleName ) {
		//First set the selected element
		for( int index = 0; index < listBox.getItemCount(); index++ ) {
			if( listBox.getValue( index ).equals( styleName ) ) {
				listBox.setSelectedIndex( index );
				break;
			}
		}
		//Second, update the sample view
		updateSampleViewStyles();
	}
	
	/**
	 * Allows to set the selected style value by its ID in the list box
	 * @param listBox the list box to set the value in
	 * @param styleID the style value ID
	 */
	public void setListBoxStyleID( final ListBox listBox, final int styleID,
			  							final Map<Integer, String> dataToStyleName ) {
		setListBoxStyleName( listBox, dataToStyleName.get( styleID ) );
	}
	
	/**
	 * Allows to retrieve the selected style name from the list box
	 * @param listBox the list box to get the selected style from
	 * @return the style selected by the user in this box
	 */
	public String getListBoxSelectedStyleName( final ListBox listBox ) {
		final int selectedIndex = listBox.getSelectedIndex();
		if( selectedIndex == -1 ) {
			//Nothing is selected yet, return the fist element in the list
			return listBox.getValue( 0 );
		} else {
			//An item is selected, get its value
			return listBox.getValue( selectedIndex );
		}
	}
	
	/**
	 * Allows to retrieve the selected style ID from the list box
	 * @param listBox the list box to get the selected style from
	 * @return the Id of the style selected by the user in this box
	 */
	public int getListBoxSelectedStyleID( final ListBox listBox, final Map<Integer, String> dataToStyleName,
										  final int defaultStyleID ) {
		String styleName = getListBoxSelectedStyleName( listBox );
		Iterator< Entry<Integer,String> > entrySetIter = dataToStyleName.entrySet().iterator();
		while( entrySetIter.hasNext() ) {
			Entry<Integer, String> entry = entrySetIter.next();
			if( entry.getValue().equals( styleName ) ) {
				return entry.getKey();
			}
		}
		return defaultStyleID;
	}
	
	/**
	 * @return the currently selected font family
	 */
	public int getSelectedFontType() {
		return getListBoxSelectedStyleID( fontListBox, MessageFontData.fontTypeToCssClass, MessageFontData.DEFAULT_FONT_FAMILY );
	}
	
	/**
	 * Allows to set the font type
	 * @param styleID the id of the font type
	 */
	public void setSelectedFontType( final int styleID ) {
		setListBoxStyleID( fontListBox, styleID, MessageFontData.fontTypeToCssClass );
	}
	
	/**
	 * @return the currently selected font size
	 */
	public int getSelectedFontSize() {
		return getListBoxSelectedStyleID( fontSizeListBox, MessageFontData.fontSizeToCssClass, MessageFontData.DEFAULT_FONT_SIZE );
	}
	
	/**
	 * Allows to set the font size
	 * @param styleID the id of the font size
	 */
	public void setSelectedFontSize( final int styleID ) {
		setListBoxStyleID( fontSizeListBox, styleID, MessageFontData.fontSizeToCssClass );
	}
	
	/**
	 * @return the currently selected font color
	 */
	public int getSelectedFontColor() {
		return getListBoxSelectedStyleID( fontColorListBox, MessageFontData.fontColorToCssClass, MessageFontData.DEFAULT_FONT_COLOR );
	}
	
	/**
	 * Allows to set the font color. If the unknown font color style ID is
	 * provided then we set the default font color
	 * @param styleID the id of the font color
	 */
	public void setSelectedFontColor( final int styleID ) {
		if( styleID == MessageFontData.UNKNOWN_FONT_COLOR ) {
			setListBoxStyleID( fontColorListBox, MessageFontData.DEFAULT_FONT_COLOR, MessageFontData.fontColorToCssClass );
		} else {
			setListBoxStyleID( fontColorListBox, styleID, MessageFontData.fontColorToCssClass );
		}
	}
	
	/**
	 * Initializes the list box that allows to select styles
	 * @param listBox the list box to initialize
	 * @param dataToName the mapping from the style ID to the human readable name of the style
	 * @param dataToStyleName the mapping from the style ID to the CSS style name
	 * @param defaultValue the default style ID that should be set as selected
	 */
	public void initializeListBox( final ListBox listBox, final Map<Integer, String> dataToName,
								   final Map<Integer, String> dataToStyleName, final int defaultValue) {
		//Initialize the list box with data
		Iterator< Entry<Integer,String> > entrySetIter = dataToName.entrySet().iterator();
		while( entrySetIter.hasNext() ) {
			Entry<Integer, String> entry = entrySetIter.next();
			listBox.addItem( entry.getValue(), dataToStyleName.get( entry.getKey() ) );
		}
		listBox.setVisibleItemCount( 1 );
		
		//Set the default value as being selected
		setListBoxStyleID( listBox, defaultValue, dataToStyleName );
		
		//Add the change listener
		listBox.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent e) {
				//Update the sample view
				updateSampleViewStyles();
			}
		});
	}
}
