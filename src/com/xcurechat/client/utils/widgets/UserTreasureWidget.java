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
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.utils.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.xcurechat.client.dialogs.system.messages.InfoMessageDialogUI;
import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.utils.SplitLoad;

/**
 * @author zapreevis
 * This widget is used to visualize the user treasure, it is a singleton class
 */
public class UserTreasureWidget extends Composite implements ClickHandler {
	
	//The instance of the widget
	private static UserTreasureWidget instance = null;
	
	//THe main panel of the widget will have to be clickable, for invoking the widget's help info
	private final FocusPanel mainPanel = new FocusPanel();
	//The main data pabel that will store the widget's data
	private final HorizontalPanel dataPanel = new HorizontalPanel();
	//The gold image
	private final Image goldPiecesImage = new Image( ServerSideAccessManager.SITE_IMAGES_LOCATION + "coins.png");
	private final Label gouldPiecesCount = new Label();
	//Stores the gold piece count value
	private int goldPieceCount = 0;
	
	private UserTreasureWidget() {
		dataPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		dataPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		
		dataPanel.add( goldPiecesImage );
		dataPanel.add( new HTML("&nbsp;") );
		dataPanel.add( gouldPiecesCount );
		
		mainPanel.setStyleName( CommonResourcesContainer.USER_TREASURE_WIDGET_STYLE );
		mainPanel.add( dataPanel );
		
		//Initialize the composite
		initWidget( mainPanel );
	}
	
	/**
	 * Allows to access the instance of the widget 
	 * @return the instance of the widget
	 */
	public static UserTreasureWidget getInstance() {
		if( instance == null ) {
			instance = new UserTreasureWidget();
			instance.mainPanel.addClickHandler( instance );
		}
		return instance;
	}
	
	/**
	 * Allows to set the gold-piece count into the widget 
	 * @param goldPieceCount the gold-piece count
	 */
	public static void setGoldPieceCount( final int goldPieceCount ) {
		final UserTreasureWidget instance = getInstance();
		instance.goldPieceCount = goldPieceCount;
		instance.gouldPiecesCount.setText( "" + instance.goldPieceCount );
	}
	
	/**
	 * Allows to get the current gold piece count
	 * @return the current cold piece count
	 */
	public static int getGoldPieceCunt() {
		return getInstance().goldPieceCount;
	}

	@Override
	public void onClick(ClickEvent event) {
		(new SplitLoad( true ) {
			@Override
			public void execute() {
				InfoMessageDialogUI.openInfoDialog( I18NManager.getInfoMessages().userTreasureWalletHelp(), true);
			}
		}).loadAndExecute();
	}
	
}
