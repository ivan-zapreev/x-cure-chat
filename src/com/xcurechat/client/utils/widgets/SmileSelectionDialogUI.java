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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.user.client.Window;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SmileyHandler;
import com.xcurechat.client.utils.SmileyHandlerUI;
import com.xcurechat.client.utils.SplitLoad;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import com.xcurechat.client.dialogs.ActionGridDialog;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;
import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UIErrorMessages;

/**
 * @author zapreevis
 * This is the smile selection dialog
 */
public class SmileSelectionDialogUI extends ActionGridDialog {
	private static final int DEFAULT_TAB_INDEX = 0;
	
	//The internationalized error messages 
	private static final UIErrorMessages errorMsgs = I18NManager.getErrors();
	
	/**
	 * @author zapreevis
	 * This interface should be implemented by the class to which the selected smiley code will be set
	 */
	public interface SmileySelectionTarget {
		/**
		 * Sets the smiley's internal code string
		 * @param smileyInternalCodeString the code string to be set
		 */
		public void addSmileStringToMessage( final String smileyInternalCodeString ) ;
	}
	
	/**
	 * @author zapreevis
	 * This class represents a single smile entry widget
	 */
	private class SmilePanelEntryWidget extends Composite {
		public static final String SMILE_PANEL_IMAGE_WIDGET_STYLE = "xcure-Chat-Smile-Selection-Dialog-Image";
		public static final String SMILE_CODES_LABEL_STYLE = "xcure-Chat-Smile-Selection-Dialog-Label";
		
		public SmilePanelEntryWidget( final SmileyHandler.SmileyCategoryInfo categoryInfo, final SmileyHandler.SmileyInfo smileInfo  ) {
			//Add the wrapping Focus panel for the click events
			FocusPanel clickablePanel = new FocusPanel();
			clickablePanel.addClickHandler( new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					//Check that the user has enough money
					final int usersGold = UserTreasureWidget.getGoldPieceCunt();
					if( ( categoryInfo.minGold != 0 ) &&  ( usersGold < categoryInfo.minGold ) ) {
						(new SplitLoad( true ) {
							@Override
							public void execute() {
								//Report the error
								ErrorMessagesDialogUI.openErrorDialog( errorMsgs.insufficientGold( usersGold, categoryInfo.minGold ) );
							}
						}).loadAndExecute();
					} else {
						if( usersGold < smileInfo.price ) {
							(new SplitLoad( true ) {
								@Override
								public void execute() {
									//Report the error
									ErrorMessagesDialogUI.openErrorDialog( errorMsgs.insufficientGold( usersGold, smileInfo.price ) );
								}
							}).loadAndExecute();
						} else {
							//Add the smile to the message text.
							if( smileySelectionTarget != null ) {
								smileySelectionTarget.addSmileStringToMessage( SmileyHandler.getSmileCodeString( smileInfo.smileyCodeID ) );
							} else {
								(new SplitLoad( true ) {
									@Override
									public void execute() {
										//Report the error
										ErrorMessagesDialogUI.openErrorDialog( errorMsgs.smileySelectionDialogFocusLost() );
									}
								}).loadAndExecute();
							}
						}
					}
					
					//Prevent the default action and stop propagation
					event.preventDefault();
					event.stopPropagation();
				}
			});
			
			//Add content
			VerticalPanel mainSmileEntryPanel = new VerticalPanel();
			mainSmileEntryPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
			mainSmileEntryPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
			
			//First add the smile image
			Image smileImage = SmileyHandlerUI.getSmileIcon( smileInfo.smileyCodeID );
			if( smileImage != null ) {
				smileImage.addStyleName( SMILE_PANEL_IMAGE_WIDGET_STYLE );
				mainSmileEntryPanel.add( smileImage );
			}
			
			//Then add the smile codes
			String smileCodeStrings = "";
			List<String> smileStringCodes = SmileyHandler.getSmileCodeStrings( smileInfo.smileyCodeID );
			if( smileStringCodes != null ) {
				//Add the codes one by one
				Iterator<String> iter = smileStringCodes.iterator();
				while( iter.hasNext() ) {
					smileCodeStrings += iter.next() + ", ";
				}
			}
			final String internalCodeStringRepresentation = SmileyHandler.getSmileCodeString( smileInfo.smileyCodeID );
			smileCodeStrings += internalCodeStringRepresentation;
			
			//Add the information under the smiley image
			HorizontalPanel infoPanel = new HorizontalPanel();
			infoPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
			infoPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
			Label label = new Label( smileCodeStrings );
			label.setStyleName( SMILE_CODES_LABEL_STYLE );
			label.setWordWrap( false );
			infoPanel.add( label );
			
			//Add the smile price if it is not zero
			if( smileInfo.price > 0 ) {
				infoPanel.add( new HTML(",&nbsp") );
				infoPanel.add( new PriceTagWidget(null, smileInfo.price, false, true) );
			}
			mainSmileEntryPanel.add( infoPanel );
			
			//Add the tool tip and the click handler, use internalCodeStringRepresentation
			mainSmileEntryPanel.setTitle( titlesI18N.clickToPutTheSmileCodeIntoTheChatMessage() );
			
			//Initialize the composite
			clickablePanel.add( mainSmileEntryPanel );
			initWidget( clickablePanel );
		}
	}
	
	//The instance of the smiley selection dialog
	private static SmileSelectionDialogUI instance = null;
	//True if the given instance of the smiley selection dialog is shown for the first time
	private static boolean isFirstTimeShow = true;
	//True if on the widget new show we should force the dialog's maximization
	private static boolean isMaximized = true;
	//Contains the tab index that should be set as selected on the next dialog show
	private static int selectTabIndex = DEFAULT_TAB_INDEX;
	//True if on the widget new show we should force the dialog's position adjustment
	private static boolean isForceReAlign = true;
	//The old left and top corner position of the dialog
	private static int oldLeftPos = 0;
	private static int oldTopPos = 0;
	
	//Allows to get the instance of the smiley selection dialog. This is
	//the only existing instance of the dialog, i.e. it is a singleton.
	private static final SmileSelectionDialogUI getInstance() {
		if( instance == null ) {
			instance = new SmileSelectionDialogUI( false, false, true );
		}
		return instance;
	}
	
	/**
	 * Allows to bind the smiley selection dialog to a new target and open the dialog
	 * If the target is null then the dialog gets unbound and hidden
	 * @param new_target the target to bind the dialog to
	 */
	public static final void bindAndShow( SmileySelectionTarget new_target ) {
		bind( new_target, true );
	}
	
	/**
	 * Allows to bind the smiley selection dialog
	 * If the target is null then the dialog gets unbound and hidden
	 * @param new_target the target to bind the dialog to
	 */
	public static final void bind( final SmileySelectionTarget new_target ) {
		bind( new_target, false );
	}
	
	/**
	 * Allows to bind the smiley selection dialog
	 */
	public static final void open() {
		//Get a new or the old instance
		final SmileSelectionDialogUI instance = getInstance();
		if( isFirstTimeShow == true ) {
			//The dialog is not yet shown, this we make a new instance and show it
			instance.show();
		}
		//The dialog is being already showing, maximize it for usability
		instance.maximize(true);
	}
	
	/**
	 * Allows to bind the smiley selection dialog
	 * If the target is null then the dialog gets unbound and hidden
	 * @param new_target the target to bind the dialog to
	 * @param forseShow if true then if the dialog is not shown then we force it to show up
	 */
	private static final void bind( final SmileySelectionTarget new_target, final boolean forseShow ) {
		if( new_target != null ) {
			//The new target is not null
			if( ( instance != null ) && ( isFirstTimeShow == false ) ) {
				//If the instance is not null and it is showing
				final SmileySelectionTarget current_target = instance.getSmileySelectionTarget();
				if( current_target != new_target ) {
					//If this is a new target for us then we unbind and hide the dialog
					//Preserve the maximization, the selected tab index and, etc
					unbindAndHide();
					//Then get, bind and show a new one, to make sure it is always on top
					final SmileSelectionDialogUI new_instance = getInstance();
					new_instance.setSmileySelectionTarget( new_target );
					new_instance.show();
				} else {
					//If this is the same old target then do nothing. The
					//dialog is already shown and the target is binded.
				}
			} else {
				//The instance is not there or the instance is present but was not shown yet then
				if( instance == null ) {
					//Then get and bind and a new instance
					getInstance();
				}
				//Set the new target
				instance.setSmileySelectionTarget( new_target );
				//Check if we need to force the dialog to be shown
				if( forseShow ) {
					//Show the dialog since it definitely was not shown yet
					instance.show();
				}
			}
		} else {
			//The new target is null, thus unbind and hide
			unbindAndHide();
		}
	}
	
	/**
	 * Allows to un-bind the smiley selection dialog from the current target and hide it
	 */
	public static final void unbindAndHide( ) {
		unbindAndHide( true );
	}
	
	/**
	 * Allows to un-bind the smiley selection dialog from the current target
	 */
	public static final void unbind( ) {
		if( instance != null ) {
			instance.setSmileySelectionTarget( null );
		}
	}
	
	/**
	 * Allows to un-bind the smiley selection dialog from the current target and hide it
	 */
	private static final void unbindAndHide( final boolean preserveDialogSettings ) {
		//We do not create a new instance if it is not already there, thus
		//we use the given static variable and not the getInstance method
		if( instance != null ) {
			if( preserveDialogSettings ) {
				//Store the selected tab index
				selectTabIndex = instance.smileCategoriesTabPanel.getTabBar().getSelectedTab();
				//Store the maximization status, if needed
				isMaximized    = instance.disclosirePanel.isOpen();
				//Store the dialog positions
				oldLeftPos     = instance.getPopupLeft();
				oldTopPos      = instance.getPopupTop(); 
				isForceReAlign = false;
			} else {
				//Reset the selected tab index
				selectTabIndex = DEFAULT_TAB_INDEX;
				//Reset the maximization status
				isMaximized    = true;
				//Reset the dialog positions
				oldLeftPos 	   = 0;
				oldTopPos  	   = 0; 
				isForceReAlign = true;
			}
			//Unbind the target
			instance.setSmileySelectionTarget( null );
			//Close the dialog and remove its instance
			instance.hide();
			instance = null;
			isFirstTimeShow = true;
		}
	} 
	
	//This is a reference to the object that will be used for tupping selected smiley codes into
	private SmileySelectionTarget smileySelectionTarget = null;
	
	//The disclosure panel that allows to reduce the size of the smiles dialog
	private DisclosurePanel disclosirePanel = null;
	
	//Maps tab indexes to the price tag widgets, for the priced categories only
	private Map<Integer, PriceTagWidget> pricedCategoryTitles = new HashMap<Integer, PriceTagWidget>();
	//Maps the tab indexes to the smile categories
	private Map<Integer, SmileyHandler.SmileyCategoryInfo> tabsToCategories = new HashMap<Integer, SmileyHandler.SmileyCategoryInfo>();
	
	//The panel with the smile categories
	private final DecoratedTabPanel smileCategoriesTabPanel = new DecoratedTabPanel();
	
	//If true then we show the smiley categories that one has to pay for, otherwise not
	private final boolean showPayedCategories;
	
	private SmileSelectionDialogUI( final boolean autoHide, final boolean modal,
								    final boolean showPayedCategories ) {
		//We do not pass this dialog as a constructor parameter because we do not want it do be made hidden
		super(false, autoHide, modal, null);
		
		//Store the other fields
		this.showPayedCategories = showPayedCategories;
		
		//Set the dialog title
		this.setText( titlesI18N.smilesSelectionDialogTitle() );
		
		//Populate the dialog
		populateDialog();
	}
	
	/**
	 * Allows to set the new smiley selection target
	 * @param smileySelectionTarget a new smiley selection taget or null to unbind the smiley selection dialog from the current target
	 */
	private void setSmileySelectionTarget(final SmileySelectionTarget smileySelectionTarget) {
		//Store the reference to the parent dialog locally 
		this.smileySelectionTarget = smileySelectionTarget;		
	}
	
	/**
	 * Allows to get the current smiley selection target
	 * @return the smiley selection target
	 */
	private SmileySelectionTarget getSmileySelectionTarget() {
		return smileySelectionTarget;		
	}
	
	/**
	 * Allows to maximize and minimize the dialog view
	 * @param maximize true for maximizing and false for minimizing
	 */
	public void maximize(final boolean maximize ){
		//If the disclosure panel is present
		if( disclosirePanel != null ) {
			//If the panel is not already minimized/maximized
			if( disclosirePanel.isOpen() != maximize ) {
				//With the enabled animation the disclosure panel
				//does not show all it's content after being open
				//It is a sort of problem in GWT 1.6
				disclosirePanel.setAnimationEnabled(false);
				disclosirePanel.setOpen( maximize );
				disclosirePanel.setAnimationEnabled(true);
			}
		}
	}

	@Override
	protected void actionLeftButton() {
		unbindAndHide();
	}

	@Override
	protected void actionRightButton() {
		unbindAndHide();
	}
	
	/**
	 * Adds the given smiles category to the tab panel
	 * @param smileCategoriesTabPanel the tab panel to add the category to
	 * @param categoryInfo the smiley category info
	 */
	private void addCategoryToSmileyCategoryPanel( final DecoratedTabPanel smileCategoriesTabPanel,
												   final SmileyHandler.SmileyCategoryInfo categoryInfo ) {
		//Initialize the scroll panel
		final SimplePanel scrollPanel = new SimplePanel();
		scrollPanel.setStyleName( CommonResourcesContainer.SMILEY_LIST_SCROLL_PANEL_STYLE );
		
		//Add proper tab header
		if( categoryInfo.minGold > 0 ) {
			final PriceTagWidget minMoneyTitle = new PriceTagWidget( SmileyHandlerUI.getCategoryTitle( categoryInfo.categoryID ),
																     categoryInfo.minGold, true, false );
			smileCategoriesTabPanel.add( scrollPanel, minMoneyTitle );
			final int index = smileCategoriesTabPanel.getWidgetIndex( scrollPanel );
			pricedCategoryTitles.put( index, minMoneyTitle);
		} else {
			smileCategoriesTabPanel.add( scrollPanel, SmileyHandlerUI.getCategoryTitle( categoryInfo.categoryID ) );
		}
		
		//Remember the category
		tabsToCategories.put( smileCategoriesTabPanel.getWidgetIndex( scrollPanel ), categoryInfo );
	}
	
	/**
	 * Populates the provided tab panel with the smile categories and smiles
	 * @param smileCategoriesTabPanel
	 */
	private void populateSmileCategoriesPanel( final DecoratedTabPanel smileCategoriesTabPanel ) {
		Map<SmileyHandler.SmileyCategoryInfo,List<SmileyHandler.SmileyInfo>> categoryToSmileys = SmileyHandler.getCategoryToSmileInternalCodesMapping();
		//First add the favorites category and then all others
		addCategoryToSmileyCategoryPanel( smileCategoriesTabPanel, SmileyHandler.SMILE_FAVORITES_CATEGORY_INFO );
		
		//Add the remaining categories
		Iterator<SmileyHandler.SmileyCategoryInfo> categoryIter = categoryToSmileys.keySet().iterator();
		while( categoryIter.hasNext() ) {
			SmileyHandler.SmileyCategoryInfo categoryInfo = categoryIter.next();
			//Do not show payed categories if requested
			if( categoryInfo != SmileyHandler.SMILE_FAVORITES_CATEGORY_INFO &&
				( showPayedCategories || ( categoryInfo.minGold == 0 ) ) ) {
				addCategoryToSmileyCategoryPanel( smileCategoriesTabPanel, categoryInfo );
			}
		}
	}
	
	/**
	 * Allows to open and position the smiley dialog
	 * @param isFirstTimeShow if true then this is the first time thid dialog is shown
	 * @return always false should be used to set isFirstTimeShow in the calling class
	 */
	@Override
	public void show( ) {
		//Show the dialog and reposition it manually, unfortunately there seems 
		//to be a bug in GWT (with computing the offset width and height) and
		//thus we can not use setPopupPositionAndShow method
		super.show();
		//Maximize the dialog if it is needed
		this.maximize( isMaximized );
		//If the first time then position the dialog nicely
		final int leftPos, topPos;
		if( isForceReAlign ) {
			//The minimal left and top margin for displaying the smile selection dialog
			final int MINIMAL_TOP_LEFT_MARGIN = 10;
			//Put the dialog centered horizontally but close to the page's top
			int leftPosition = ( Window.getClientWidth() - this.getOffsetWidth() ) / 2;
			if( leftPosition < MINIMAL_TOP_LEFT_MARGIN ) {
				leftPosition = MINIMAL_TOP_LEFT_MARGIN;
			}
			//NOTE: The dialog might be opened in the scrolled down page, thus add the scroll top value
			leftPos = leftPosition;
			topPos  = Window.getScrollTop() + MINIMAL_TOP_LEFT_MARGIN;
			//Mark the dialog as aligned
			isForceReAlign = false;
		} else {
			leftPos = oldLeftPos;
			topPos  = oldTopPos;
		}
		this.setPopupPosition( leftPos, topPos );
		
		//Refresh the smileys in the selected tab, this is needed for the case when
		//the client view has changed and thus the smiley sizes should be changed
		smileCategoriesTabPanel.selectTab( smileCategoriesTabPanel.getTabBar().getSelectedTab() );
		
		//Re-set the first-time-show to false
		isFirstTimeShow = false;
	}
	
	@Override
	protected void populateDialog() {
		disclosirePanel = addNewGrid( 2, 1, true, titlesI18N.disclosurePanelSmilesSelectionDialogTitle(), true);
		
		//Add the smiles tab panel to the dialog
		this.addToGrid( FIRST_COLUMN_INDEX, smileCategoriesTabPanel, false, false);

		//Set the current image to the image panel 
		populateSmileCategoriesPanel( smileCategoriesTabPanel );
		
		//Add the handler for the price category titles
		final InterfaceUtils.TabContentManager manager = new InterfaceUtils.TabContentManager() {
			@Override
			public void tabFillOut(int index, DecoratedTabPanel tabPanel) {
				final SimplePanel scrollPanel = (SimplePanel) tabPanel.getWidget( index );
				final SmileyHandler.SmileyCategoryInfo categoryInfo = tabsToCategories.get( index );
				if( categoryInfo != null ) {
					final List<SmileyHandler.SmileyInfo> categorySmiles = SmileyHandler.getSmileInfoByCategory( categoryInfo );
					if( categorySmiles != null ) {
						FlowPanel flowPanel = new FlowPanel();
						Iterator<SmileyHandler.SmileyInfo> smileInfoIter = categorySmiles.iterator();
						while( smileInfoIter.hasNext() ) {
							SmilePanelEntryWidget smileEntryWidget = new SmilePanelEntryWidget( categoryInfo, smileInfoIter.next() );
							smileEntryWidget.addStyleName( CommonResourcesContainer.SMILE_PANEL_ENTRY_WIDGET_STYLE );
							flowPanel.add( smileEntryWidget );
						}
						scrollPanel.add( flowPanel );
					}
				}
			}
			@Override
			public void tabCleanUp(int index, DecoratedTabPanel tabPanel) {
				((SimplePanel) tabPanel.getWidget( index )).clear();
			}
		};
		InterfaceUtils.addTabSectionsListener( smileCategoriesTabPanel, pricedCategoryTitles, manager );
		
		//Select the tab with the favorite icons
		smileCategoriesTabPanel.selectTab( selectTabIndex );

		//Add the close button
		Button closeButton = new Button();
		closeButton.setText( titlesI18N.closeButtonTitle() );
		closeButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		closeButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				//Close the dialog
				unbindAndHide();
			}
		} );
		this.addToGrid( FIRST_COLUMN_INDEX, closeButton, true, true);
	}
	
}
