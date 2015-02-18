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
package com.xcurechat.client.dialogs.profile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasClickHandlers;

import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.PresetAvatarImages;
import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.PriceTagWidget;

import com.xcurechat.client.dialogs.ActionGridDialog;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.rpc.UserManagerAsync;

/**
 * @author zapreevis
 * This dialog allows to view the predefined avatars and to choose one for the profile 
 */
public class ChooseAvatarDialogUI extends ActionGridDialog {
	
	//The number of columns in the avatar's table
	private static final int NUMBER_OF_COLUMNS = 1;
	//We have one extra row for holding the status progress bar
	private static final int NUMBER_OF_ROWS = 2;
	
	//The panel with the avatar sections
	private final DecoratedTabPanel avatarSectionsTabPanel = new DecoratedTabPanel();
	
	//Maps tab indexes to the price tag widgets, for the priced categories only
	private Map<Integer, PriceTagWidget> pricedSectionTitles = new HashMap<Integer, PriceTagWidget>();
	//Maps the tab indexes to the avatar sections
	private Map<Integer, PresetAvatarImages.AvatarSectionDescriptor> tabsToSections = new HashMap<Integer,  PresetAvatarImages.AvatarSectionDescriptor>();

	public ChooseAvatarDialogUI( DialogBox parentDialog ){
		super( false, true, true, parentDialog );
		
		//Set the dialog's caption.
		setText( titlesI18N.chooseUserAvatarDialogTitle( ) );
		
		//Enable the action buttons and hot key, even though we will not be adding these buttons
		//to the grid, we will use the hot keys associated with them to close this dialog
		setLeftEnabled( true );
		setRightEnabled( true );
		
		//Fill dialog with data
		populateDialog();
	}
	
	//Allows to manage enable/disable choosing of the avatars
	private boolean isChooseEnabled = true;
	private void setEnabledButtons(final boolean enableChoose) {
		isChooseEnabled = enableChoose;
	}

	/**
	 * This method initiates setting the user's avatar to the choosen one
	 * @param index the choosen avatar's index
	 */
	private void doChooseAvatarServerCall(final int index) {
		//Disable all buttons except the cancel one
		setEnabledButtons(false);
		
		//Ensure lazy loading
		(new SplitLoad( true ){
			@Override
			public void execute() {
				//Retrieve the user data from the server 
				CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>(progressBarUI) {
					public void onSuccessAct(Void result) {
						if( parentDialog instanceof UserProfileDialogUI ) {
							( (UserProfileDialogUI) parentDialog).updateAvatarImage();
						}
						//Enable the buttons
						setEnabledButtons(true);
						//Close the dialog
						hide();
					}
					public void onFailureAct(final Throwable caught) {
						(new SplitLoad( true ) {
							@Override
							public void execute() {
								//Show the error message
								ErrorMessagesDialogUI.openErrorDialog(caught);
							}
						}).loadAndExecute();
						//Use the recovery method
						recover();
					}
				};
				UserManagerAsync userManagerObject = RPCAccessManager.getUserManagerAsync();
				userManagerObject.chooseAvatar(SiteManager.getUserID(), SiteManager.getUserSessionId(), index, callback );
			}
			@Override
			public void recover() {
				//Enable the buttons
				setEnabledButtons(true);
			}
		}).loadAndExecute();
	}
	
	private Widget initAvatarPanel( final int index, final PresetAvatarImages.AvatarDescriptor descriptor ){
		Widget avatarWidget;
		
		//Initialize the avatar image
		final String avatarURLBase = ServerSideAccessManager.getPresetAvatarImagesBase();
		Image image = new Image( avatarURLBase + descriptor.relativeURL );
		image.setStyleName( CommonResourcesContainer.AVATAR_IMAGE_CHOICE_DEFAULT_STYLE );
		image.setTitle( titlesI18N.clickToChooseToolTip() );
		
		//Sort out what the avatar widget is.
		if( descriptor.price > 0 ) {
			//If there is a price tag then the avatar is a special object
			FocusPanel focusPanel = new FocusPanel();
			VerticalPanel verticalPanel = new VerticalPanel();
			verticalPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
			verticalPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
			verticalPanel.add( image );
			verticalPanel.add( new PriceTagWidget( null, descriptor.price, false, true ));
			focusPanel.add( verticalPanel );
			avatarWidget = focusPanel;
		} else {
			//If there is no price then the avatar is the image widget itself
			avatarWidget = image;
		}
		
		//Add the floading style and the click handler
		avatarWidget.addStyleName( CommonResourcesContainer.AVATAR_IMAGE_IN_LIST_STYLE );
		((HasClickHandlers) avatarWidget).addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				if( isChooseEnabled ) {
					//Initiate the avatar selection, do the RPC call
					doChooseAvatarServerCall( index );
				}
				//Just in case stop the event here
				e.preventDefault(); e.stopPropagation();
			}
		});
		
		return (Widget) avatarWidget;
	}

	@Override
	protected void actionLeftButton() {
		hide();
	}

	@Override
	protected void actionRightButton() {
		hide();
	}
	
	/**
	 * Allows to add a new avatar's section into the avatar's section dialog
	 * @param avatarSectionsTabPanel the decorated panel storing the smile section
	 * @param avatarSection the avatar section descriptor
	 */
	private void addAvatarSectionBody( final DecoratedTabPanel avatarSectionsTabPanel,
									   final PresetAvatarImages.AvatarSectionDescriptor avatarSection ) {
		//Initialize the scroll panel
		final ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.setStyleName( CommonResourcesContainer.CHOOSE_AVATAR_PANEL_STYLE );
		
		//Add to the tab panel
		final PriceTagWidget minMoneyTitle = new PriceTagWidget( null, avatarSection.price, true, false );
		avatarSectionsTabPanel.add( scrollPanel, minMoneyTitle );
		
		//Store the tab to section mapping
		tabsToSections.put( avatarSectionsTabPanel.getWidgetIndex( scrollPanel ) , avatarSection );
		//Store the tab to price tab mapping
		pricedSectionTitles.put( avatarSectionsTabPanel.getWidgetIndex( scrollPanel ), minMoneyTitle );
	}

	@Override
	protected void populateDialog() {
		//Make a grid and place all images into it
		addNewGrid( NUMBER_OF_ROWS, false, "", false);
		
		//Add the avatar section here
		for( PresetAvatarImages.AvatarSectionDescriptor avatarSection : PresetAvatarImages.avatarSections ) {
			addAvatarSectionBody( avatarSectionsTabPanel, avatarSection );
		}
		addToGrid( FIRST_COLUMN_INDEX, avatarSectionsTabPanel, false, false );
		
		//Add tab selection handlers for the price category titles
		final InterfaceUtils.TabContentManager manager = new InterfaceUtils.TabContentManager() {
			@Override
			public void tabFillOut(int index, DecoratedTabPanel tabPanel) {
				final ScrollPanel scrollPanel = (ScrollPanel) tabPanel.getWidget( index );
				final PresetAvatarImages.AvatarSectionDescriptor avatarSection = tabsToSections.get( index );
				
				final FlowPanel avatarsPanel = new FlowPanel(); avatarsPanel.setWidth("100%");
				Iterator<Entry<Integer, PresetAvatarImages.AvatarDescriptor>> iterator = avatarSection.avatars.entrySet().iterator();
				while( iterator.hasNext() ) {
					Entry<Integer, PresetAvatarImages.AvatarDescriptor> entry = iterator.next();
					avatarsPanel.add( initAvatarPanel( entry.getKey(), entry.getValue() ) );
				}
				scrollPanel.add( avatarsPanel );
			}
			@Override
			public void tabCleanUp(int index, DecoratedTabPanel tabPanel) {
				((ScrollPanel) tabPanel.getWidget( index )).clear();
			}
		};
		InterfaceUtils.addTabSectionsListener( avatarSectionsTabPanel, pricedSectionTitles, manager );
		
		//Add the progress bar on a new row
		HorizontalPanel progressBarPanel = new HorizontalPanel();
		progressBarPanel.setWidth("100%");
		progressBarPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		progressBarPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		progressBarPanel.add( progressBarUI );
		addToGrid( this.getCurrentGridIndex(), 0, NUMBER_OF_COLUMNS, progressBarPanel, true , false );
	}

	@Override
	public void show() {
		super.show();
		avatarSectionsTabPanel.selectTab(0);
	}

}
