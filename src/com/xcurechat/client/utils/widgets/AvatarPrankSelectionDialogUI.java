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
package com.xcurechat.client.utils.widgets;

import java.util.Date;
import java.util.HashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.xcurechat.client.SiteManager;
import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.UserManagerAsync;
import com.xcurechat.client.utils.AvatarSpoilersHelper;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;

import com.xcurechat.client.dialogs.ActionGridDialog;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

/**
 * @author zapreevis
 * This dialog allows to select an avatar prank 
 */
public class AvatarPrankSelectionDialogUI extends ActionGridDialog {
	
	//Maximum number of avatar spoilers per page
	private final static int MAX_SPOILERS_PER_TAB = 29; //25 minus the avatar preview
	
	//We have one extra row for holding the status progress bar
	private static final int NUMBER_OF_ROWS = 1;
	
	//Stores the user data
	private final int userID;
	private final boolean isMale;
	private final UserAvatarImageWidget callerUserAvararWidget;
	
	//The user avatar prank preview widget
	private final UserAvatarImageWidget prankPreview = new UserAvatarImageWidget();
	
	//The panel with the avatar sections
	private final DecoratedTabPanel avatarSpoilersTabPanel = new DecoratedTabPanel();
	
	/**
	 * The basic constructor
	 * @param callerUserAvararWidget the user avatar widget from which this dialog was invoked
	 * @param userID the id of the user for which the prank is going to be selected
	 * @param isMale true if the user for which the prank is going to be selected is male
	 */
	public AvatarPrankSelectionDialogUI( final UserAvatarImageWidget callerUserAvararWidget, final int userID, final boolean isMale ){
		super( false ,true, true, null );
		
		//Store the user data
		this.userID = userID;
		this.isMale = isMale;
		this.callerUserAvararWidget = callerUserAvararWidget;
		
		//Set the dialog's caption.
		setText( titlesI18N.chooseAvatarAvatarDialogTitle( ) );
		
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
	 * This method initiates setting the user's avatar to the chosen one
	 * @param prankID the chosen avatar spoiler id
	 */
	private void doChooseAvatarPrank(final int prankID) {
		//Disable all buttons except the cancel one
		setEnabledButtons(false);
		//Ensure lazy loading
		(new SplitLoad(){
			@Override
			public void execute() {
				//Update the prank data on the server 
				AsyncCallback<Date> getProfileCallBack = new AsyncCallback<Date>() {
					public void onSuccess( Date prankExpDate ) {
						//Set the new prank status everywhere in the client
						UserAvatarImageWidget.notifyAvatarChangeListeners( userID, prankID, prankExpDate );
						//Update the caller avatar as well, just in case
						callerUserAvararWidget.updateThisAvatarSpoiler(prankID, prankExpDate );
						//Close the dialog
						hide();
						//Enable all buttons except the cancel one
						setEnabledButtons(true);
					}
					public void onFailure(final Throwable caught) {
						//Report the error
						(new SplitLoad( true ) {
							@Override
							public void execute() {
								ErrorMessagesDialogUI.openErrorDialog( caught );
							}
						}).loadAndExecute();
						//Do the recovery
						recover();
					}
				};
				UserManagerAsync userManagerObject = RPCAccessManager.getUserManagerAsync();
				userManagerObject.setAvatarPrank( SiteManager.getUserID(), SiteManager.getUserSessionId(),
												  userID, prankID, false, getProfileCallBack );
			}
			@Override
			public void recover() {
				//Enable all buttons except the cancel one
				setEnabledButtons(true);
			}
		}).loadAndExecute();
	}
	
	private Widget initAvatarSpoilerPanel( final AvatarSpoilersHelper.AvatarSpoilerDescriptor descriptor ){
		//Initialize the avatar image
		Image image = new Image( GWT.getModuleBaseURL() + descriptor.relativeURL );
		image.setStyleName( CommonResourcesContainer.AVATAR_IMAGE_CHOICE_STYLE );
		image.setTitle( titlesI18N.clickToChooseToolTip() );
		
		//If there is a price tag then the avatar is a special object
		final FocusPanel focusPanel = new FocusPanel();
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		verticalPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		verticalPanel.add( image );
		verticalPanel.add( new PriceTagWidget( null, descriptor.price, false, true ));
		focusPanel.add( verticalPanel );
		
		//Add the floating style and the click handler
		focusPanel.addStyleName( CommonResourcesContainer.AVATAR_WIDGET_IN_LIST_STYLE );
		focusPanel.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				if( isChooseEnabled ) {
					//Initiate the avatar selection, do the RPC call
					doChooseAvatarPrank( descriptor.index );
				}
				//Just in case stop the event here
				e.preventDefault(); e.stopPropagation();
			}
		});
		
		//Add the move in and move out handlers
		focusPanel.addMouseOverHandler( new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				prankPreview.updateThisAvatarSpoiler( descriptor.index, AvatarSpoilersHelper.getNewSpoilerExpirationDate() );
			}
		} );
		focusPanel.addMouseOutHandler( new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				prankPreview.updateThisAvatarSpoiler( AvatarSpoilersHelper.UNDEFILED_AVATAR_SPOILER_ID, null);
			}
		} );
		
		return focusPanel;
	}

	@Override
	protected void actionLeftButton() {
		hide();
	}

	@Override
	protected void actionRightButton() {
		hide();
	}
	
	@Override
	protected void populateDialog() {
		//Make a grid and place all images into it
		addNewGrid( NUMBER_OF_ROWS, false, "", false);
		
		//Add the tabs to the tab panel
		int number_of_tabs = AvatarSpoilersHelper.spoilerIdToDescriptor.size() / MAX_SPOILERS_PER_TAB ;
		if( AvatarSpoilersHelper.spoilerIdToDescriptor.size() % MAX_SPOILERS_PER_TAB > 0 ) {
			number_of_tabs++;
		}
		for( int tab_num = 0; tab_num < number_of_tabs; tab_num++ ) {
			final ScrollPanel scrollPanel = new ScrollPanel();
			scrollPanel.setStyleName( CommonResourcesContainer.CHOOSE_AVATARS_PANEL_STYLE );
			avatarSpoilersTabPanel.add( scrollPanel, "#"+ ( tab_num + 1 ) );
		}
		
		//Add tab selection handlers for the price category titles
		final InterfaceUtils.TabContentManager manager = new InterfaceUtils.TabContentManager() {
			@Override
			public void tabFillOut(int index, DecoratedTabPanel tabPanel) {
				final ScrollPanel scrollPanel = (ScrollPanel) tabPanel.getWidget( index );
				//Add the preview avatar Panel
				final SimplePanel previewPanel = new SimplePanel();
				previewPanel.addStyleName( CommonResourcesContainer.AVATAR_WIDGET_IN_LIST_STYLE );
				prankPreview.updateAvatarImage(userID, isMale, true); //Set the current user avatar
				prankPreview.enablePrankControls( false ); //Do not activate controls
				previewPanel.add( prankPreview );
				
				//Add the flow panel with avatar spoilers
				final FlowPanel avatarsPanel = new FlowPanel();
				avatarsPanel.setWidth("100%");
				avatarsPanel.add( previewPanel );
				//Add the pranks
				final int first_idx =  index       * MAX_SPOILERS_PER_TAB;
				final int last_idx = ( index + 1 ) * MAX_SPOILERS_PER_TAB;
				final int offset = AvatarSpoilersHelper.MIN_AVATAR_SPOILER_INDEX;
				for( int idx = offset + first_idx; idx < offset + last_idx; idx++ ) {
					AvatarSpoilersHelper.AvatarSpoilerDescriptor spoiler = AvatarSpoilersHelper.spoilerIdToDescriptor.get( idx );
					if( spoiler != null ) {
						//We do not actually follows the correct indexes at the last
						//tab, plus what if some indexes are wrong? I.e. not mapped?
						avatarsPanel.add( initAvatarSpoilerPanel( spoiler ) );
					}
				}
				scrollPanel.add( avatarsPanel );
			}
			@Override
			public void tabCleanUp(int index, DecoratedTabPanel tabPanel) {
				((ScrollPanel) tabPanel.getWidget( index )).clear();
			}
		};
		InterfaceUtils.addTabSectionsListener( avatarSpoilersTabPanel, new HashMap<Integer, PriceTagWidget>(), manager );
		addToGrid( FIRST_COLUMN_INDEX, avatarSpoilersTabPanel, false, false );
		
		//Add the progress bar on a new row
		final HorizontalPanel progressBarPanel = new HorizontalPanel();
		progressBarPanel.setWidth("100%");
		progressBarPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		progressBarPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		progressBarPanel.add( progressBarUI );
		addToGrid( FIRST_COLUMN_INDEX, progressBarPanel, true , false );
	}

	@Override
	public void show() {
		super.show();
		avatarSpoilersTabPanel.selectTab(0);
	}

}
