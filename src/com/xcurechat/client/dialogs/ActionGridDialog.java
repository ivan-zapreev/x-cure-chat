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
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.dialogs;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;

import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

import com.google.gwt.user.client.Window;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.xcurechat.client.SiteManager;
import com.xcurechat.client.SiteManagerUI;

import com.xcurechat.client.data.MainUserData;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;

import com.xcurechat.client.rpc.ServerSideAccessManager;

import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.ServerCommStatusPanel;
import com.xcurechat.client.utils.widgets.SmileSelectionDialogUI;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.TextBoxWithSuggText;

/**
 * @author zapreevis
 * This abstract dialog is supposed to unite some common dialog features
 */
public abstract class ActionGridDialog extends DialogBox {
	
	//The default number of columns in the dialog-form grid
	protected static final int DEFAULT_NUMBER_OF_COLUMNS = 2; 
	
	//Indexes for the first three Grid columns
	protected final static int FIRST_COLUMN_INDEX = 0;
	protected final static int SECOND_COLUMN_INDEX = 1;
	protected final static int THIRD_COLUMN_INDEX = 2;
	protected final static int FOURTH_COLUMN_INDEX = 3;
	protected final static int FIFTH_COLUMN_INDEX = 4;
	protected final static int SIXTH_COLUMN_INDEX = 5;
	protected final static int SEVENTH_COLUMN_INDEX = 6;
	
	//The localization for the text
	protected static final UITitlesI18N titlesI18N = I18NManager.getTitles();
	
	//Create the vertical panel for both grids
	private final VerticalPanel formVerticalPanel = new VerticalPanel();

	//Left and right grid buttons
	private final Button leftButton = new Button(); 
	private final Button rightButton = new Button();
	
	//The loading progress bar
	protected final ServerCommStatusPanel progressBarUI = new ServerCommStatusPanel();
	
	//This is the CAPTCHA test image, in certain cases it is within this dialog
	private final Image captchaImage = new Image( );
	private final TextBoxWithSuggText captchaAnswerTextBox = new TextBoxWithSuggText( titlesI18N.captchaImageHelpMsg() ); 
	
	// Create index to Grid mapping to contain the dialog fields
	private HashMap<Integer, FlexTable> indexToGrid = new HashMap<Integer, FlexTable>(); 
	// Create index to the current row index mapping
	private HashMap<Integer, Integer> indexToCurrentRow = new HashMap<Integer, Integer>(); 
	// Create index to the Grid size mapping
	private HashMap<Integer, Integer> indexToGridSize = new HashMap<Integer, Integer>();
	//The list of all disclosure panels, we need it to work aroud the bug with hiding the panels
	private List<Widget> panelWidgetList = new ArrayList<Widget>(); 
	
	//Indicates if the left/right button and the corresponding action is enabled
	private boolean isLeftEnabled = false;
	private boolean isRightEnabled = false;
	
	//This variable stores the index of the current Grid
	private int currentGridIndex = -1;
	
	//The reference to this dialog, is neede for the sub-dialogs
	protected final DialogBox thisDialog; 

	/**
	 * @return the index of the grid we are working with now
	 */
	public int getCurrentGridIndex() {
		return currentGridIndex;
	}
	
    //Contains true if the user using the interface is an admin 
	protected final boolean isAdmin = ( SiteManager.getUserProfileType() == MainUserData.ADMIN_USER_TYPE );

	//The parent dialog, sometimes it needs to be updated or hidden/shown
	protected final DialogBox parentDialog;
	
	/**
	 * The main constructor
	 * @param hasSmileyTarget must be true if the dialog contains smiley selection targets, instances. 
	 * @param autoHide true for auto hide
	 * @param modal true for a modal dialog
	 * @param parentDialog the parent dialog, i.e. the one we open this dialog from
	 * @param registerWindow if true then the window is registered in the list of
	 * all popups and is closed automatially on the user log out
	 */
	public ActionGridDialog( final boolean hasSmileyTarget, final boolean autoHide,
							 final boolean modal, final DialogBox parentDialog, final boolean registerWindow ) {
		super(autoHide, modal);
		this.setAnimationEnabled(true);
		
		//Store the reference to this dialog
		thisDialog = this;
		this.parentDialog = parentDialog;

		//Make sure that if this dialog is opened then the parent dialog is made invisible.
		//Moreover, when this dialog is closed then we should show the hidden dialog back. 
		if( parentDialog != null ) {
			parentDialog.setVisible(false);
			this.addCloseHandler( new CloseHandler<PopupPanel>(){
				public void onClose(CloseEvent<PopupPanel> e) {
					if( e.getTarget() == thisDialog ) {
						//Unregister this dialog's popup window
						SiteManagerUI.getInstance().unregisterPopup( thisDialog );
						//Show the parent dialog
						parentDialog.setVisible(true);
						if( parentDialog instanceof ActionGridDialog ) {
							if( ( (ActionGridDialog) parentDialog).centerOnReShow() ) {
								parentDialog.center();
							}
						}
					}
				}
			} );
		}
		
		//If there are smiley selection targets
		if( hasSmileyTarget ) {
			//We unbind the smiley selection dialog from the
			//current target to prevent using the old target
			SmileSelectionDialogUI.unbind();
			//Also, if this dialog gets closed then we unbind
			//the target in order to prevent using the old target
			this.addCloseHandler( new CloseHandler<PopupPanel>(){
				public void onClose(CloseEvent<PopupPanel> e) {
					if( e.getTarget() == thisDialog ) {
						SmileSelectionDialogUI.unbind();
					}
				}
			} );
		}
		
		//Register this window in the list of windows that have to be closed on log-out
		//WARNING: Because of the following we can not instantiate action-grid-based 
		//dialogs in the at the time when the SiteManagerUI is not yet instantiated
		if( registerWindow ) {
			SiteManagerUI.getInstance().registerPopup( this );
		}
		
		//Add the action buttons' handler
		this.addDomHandler( new KeyDownHandler(){
			public void onKeyDown( KeyDownEvent event ){
				if( isLeftEnabled && ( event.getNativeKeyCode() == getLeftButtonHotKey() ) ) {
					actionLeftButton();
				} else {
					if( isRightEnabled && isRightButtonModKeyDown( event ) &&
						( event.getNativeKeyCode() == getRightButtonHotKey() ) ) {
						actionRightButton();
					}
				}
			}
		}, KeyDownEvent.getType() );
		
		//Set the style
		this.setStyleName( CommonResourcesContainer.USER_DIALOG_STYLE_NAME );
		
		//Add the tool tip saying how to close the dialog window
		if( autoHide ) {
			this.setTitle( titlesI18N.closeDialogTextTip() );
		}
		
		//Add the main vertical panel that will store
		//all disclosure panels and other elements
		formVerticalPanel.setSize( "100%", "100%");
	    this.setWidget( formVerticalPanel );		
	}
	
	/**
	 * The main constructor. Created with this constructor the window is entitled for
	 * and automated closing on site-manager-ui log-out
	 * @param hasSmileyTarget must be true if the dialog contains smiley selection targets, instances. 
	 * @param autoHide true for auto hide
	 * @param modal true for a modal dialog
	 * @param parentDialog the parent dialog, i.e. the one we open this dialog from
	 */
	public ActionGridDialog( final boolean hasSmileyTarget, final boolean autoHide,
							 final boolean modal, final DialogBox parentDialog ) {
		this( hasSmileyTarget, autoHide, modal, parentDialog, true );
	}
	
	@Override
	public void setAutoHideEnabled( boolean autoHide ) {
		//Allows to handle the proper dialog tool tip, if the dialog is set auto hide or not
		super.setAutoHideEnabled( autoHide );
		if( autoHide ) {
			this.setTitle( titlesI18N.closeDialogTextTip() );
		} else {
			this.setTitle( "" );			
		}
	}
	
	private void setFocus( final Widget w ){
		if( w instanceof Focusable ) {
			Scheduler.get().scheduleDeferred( new ScheduledCommand(){
				public void execute(){
					((Focusable) w).setFocus(true);
				}
			});
		} else {
			Window.alert("Trying to set focus to an unfocusable Widget!");
		}
	}
	
	/**
	 * Add a new widget to the compulsory dialog-form grid 
	 * @param gridIndex the index of the Grid which we will add elements to
	 * @param column the column to add to
	 * @param colspan the colump span, for the widget taking more than one column 
	 * @param w the widget to add
	 * @param isNewRow if true then we will add it to the new row
	 * @param setFocus true to set focus on the given Widget
	 */
	protected void addToGrid( int gridIndex, int column, int colspan, Widget w, boolean isNewRow, boolean hasFocus ) {
		FlexTable currentGrid = indexToGrid.get( gridIndex );
		int currentRowIndex = indexToCurrentRow.get( gridIndex );
		int gridSize = indexToGridSize.get( gridIndex );
		if( currentRowIndex < gridSize ) {
			if( isNewRow ){
				currentRowIndex += 1;
			}
			currentGrid.getFlexCellFormatter().setColSpan( currentRowIndex, column, colspan );
			//In case we are adding a button, then we center it inside the cell
			if( canCenterInCell( colspan, w ) ) {
				currentGrid.getFlexCellFormatter().setHorizontalAlignment( currentRowIndex, column, HasHorizontalAlignment.ALIGN_CENTER );
			}
			currentGrid.setWidget(currentRowIndex, column, w );
			if( hasFocus ) {
				setFocus( w );
			}
		} else {
			Window.alert("An internal error, trying to add an element to a non-existing row.");
		}
		indexToCurrentRow.put( gridIndex, currentRowIndex );
	}
	
	/**
	 * Allows to detect if we are allowed to center the given widget in the cell in case the cell span is > 1
	 * @param colspan the column span of the sell to which the widget will be added
	 * @param w the widget that will be placed in the cell
	 * @return true if the centering is allowed, by default we only allow to center buttons
	 */
	protected boolean canCenterInCell( final int colspan, final Widget w ) {
		return w instanceof Button;
	}
	
	/**
	 * Add a new widget to the compulsory dialog-form grid 
	 * @param gridIndex the index of the Grid which we will add elements to
	 * @param column the column to add to
	 * @param w the widget to add
	 * @param isNewRow if true then we will add it to the new row
	 * @param setFocus true to set focus on the given Widget
	 */
	protected void addToGrid( int gridIndex, int column, Widget w, boolean isNewRow, boolean hasFocus ) {
		addToGrid( gridIndex, column, 1, w, isNewRow, hasFocus );
	}
	
	/**
	 * Add a new widget to the compulsory dialog-form grid 
	 * @param column the column to add to
	 * @param w the widget to add
	 * @param isNewRow if true then we will add it to the new row
	 * @param setFocus true to set focus on the given Widget
	 */
	protected void addToGrid( int column, Widget w, boolean isNewRow, boolean hasFocus ) {
		addToGrid( currentGridIndex, column, w, isNewRow, hasFocus ); 
	}
	
	/**
	 * Adds a new Grid to the Vertical panel and wraps it around
	 * with a Disclosure panel, the latter can be open or closed
	 * @param numberRows the number of rows in the Grid 
	 * @param numberColumns the number of columns in the Grid 
	 * @param withDisclosure true if we want to have the
	 * disclosure panel around the grid.
	 * @param title the title of the disclosure panel
	 * @param isOpen true for an open disclosure panel
	 * @return returns the the added disclosure panel or null if
	 *         the disclodure panel did not have to be added, i.e.
	 *         withDisclosure == false;
	 */
	protected DisclosurePanel addNewGrid( int numberRows, int numberColumns, boolean withDisclosure, String title, boolean isOpen ){
		DisclosurePanel disclosurePanel = null;
		//Update the current Grid index
		currentGridIndex ++;
		
		//Greate a new Grid and add the mappings
		FlexTable newGrid = new FlexTable( );
		for(int i = 0; i < numberRows; i++) {
			newGrid.insertRow( i );
			for(int j=0; j < numberColumns; j++) {
				newGrid.insertCell( i, j );
			}
		}
		newGrid.setSize("100%", "100%");
		indexToGrid.put( currentGridIndex, newGrid );
		indexToGridSize.put( currentGridIndex,  numberRows );
		indexToCurrentRow.put( currentGridIndex,  0 );
		
		if( withDisclosure ) {
			//Create a DisclosurePanel, wrap it around the Grid and add to the main VerticalPanel
			disclosurePanel = new DisclosurePanel( title + CommonResourcesContainer.FIELD_LABEL_SUFFIX );
			disclosurePanel.setAnimationEnabled( true );
			disclosurePanel.setOpen( isOpen );
			disclosurePanel.add( newGrid );
			
			//disclosurePanel.setSize("100%", "100%");
			//Add the panel with the Grid to the form
			formVerticalPanel.add( disclosurePanel );
			panelWidgetList.add( disclosurePanel );
		} else {
			//Add the Grid to the form
			formVerticalPanel.add( newGrid );
			panelWidgetList.add( newGrid );
		}
		
		return disclosurePanel;
	}
	
	/**
	 * Adds a new Grid to the Vertical panel and wraps it around
	 * with a Disclosure panel, the latter can be open or closed
	 * @param numberRows the number of rows in the Grid 
	 * @param withDisclosure true if we want to have the
	 * disclosure panel around the grid.
	 * @param title the title of the disclosure panel
	 * @param isOpen true for an open disclosure panel
	 * @return returns the the added disclosure panel or null if
	 *         the disclodure panel did not have to be added, i.e.
	 *         withDisclosure == false;
	 */
	protected DisclosurePanel addNewGrid( int numberRows, boolean withDisclosure, String title, boolean isOpen ){
		return addNewGrid( numberRows, DEFAULT_NUMBER_OF_COLUMNS, withDisclosure, title, isOpen );
	}
	
	/**
	 * This method sets the label value, if the value is undefined
	 * then it makes the field name and value labels invisible.
	 * If the field name label is null then we set the field value to "undefined"
	 * @param fieldNameLabel the label of the field name
	 * @param fieldValueLabel the label to set value to
	 * @param value the value to set
	 */
	protected void setLabelValue( final Label fieldNameLabel, final Label fieldValueLabel, final String value ) {
		if( ( value == null ) || ( value.trim().isEmpty() ) ) {
			if( fieldNameLabel != null ) {
				fieldNameLabel.setVisible(false);
				fieldValueLabel.setVisible(false);
			} else {
				fieldValueLabel.setText( titlesI18N.undefinedTextValue() );
			}
		} else {
			fieldValueLabel.setText( value );
		}
	}
	
	/**
	 * This method sets the Date value label. Calls the setLabelValue method.
	 * @param label the label to set value to
	 * @param date the date value to set
	 */
	protected void setDateLabelValue( Label label, Date date ) {
		String dateStr = null;
		if( date != null ) {
			final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat( PredefinedFormat.DATE_TIME_SHORT );
			dateStr = dateTimeFormat.format( date );
		}
		setLabelValue( null, label, dateStr );
	}
	
	/**
	 * Allows to add the captcha problem.
	 * @param isNewGrid if true then the problem is added to a new grid 
	 *                  with two or three rows, otherwise to a current one
	 * @param isOneRow if true then the captcha problem uses (first) three
	 *                 rows of the current grid or all three rows of the
	 *                 newly created grid if isNewGrid == true. If false
	 *                 then the problem uses two rows and two columns
	 */
	protected void addCaptchaTestGrid(final boolean isNewGrid, final boolean isOneRow) {
		final int labelColumnIdx, imageColumnIdx, textBoxColumnIdx;
		final boolean isNewRowForLabel;
		final boolean isNewRowForImageAndTextBox;
		if( isNewGrid ) {
			if( isOneRow ) {
				addNewGrid( 1, false, "", false);
			} else {
				addNewGrid( 2, false, "", false);
			}
		}
		labelColumnIdx = FIRST_COLUMN_INDEX;
		imageColumnIdx = ( isOneRow ? SECOND_COLUMN_INDEX : FIRST_COLUMN_INDEX );
		textBoxColumnIdx = ( isOneRow ? THIRD_COLUMN_INDEX : SECOND_COLUMN_INDEX );
		isNewRowForLabel = ! isNewGrid;
		isNewRowForImageAndTextBox = ! isOneRow;
		
		//Add the elements to the grid
		Label captchaImageField = InterfaceUtils.getNewFieldLabel(titlesI18N.captchaImage(), true );
		addToGrid( labelColumnIdx, captchaImageField, isNewRowForLabel, false );
		
		captchaImage.setUrl( ServerSideAccessManager.getCaptchaProblemServletURL() );
		captchaImage.setStyleName( CommonResourcesContainer.CAPTCHA_IMAGE_STYLE );
		captchaImage.setTitle(titlesI18N.captchaImageHelpMsg());
		addToGrid( imageColumnIdx, captchaImage, isNewRowForImageAndTextBox, false );
		addToGrid( textBoxColumnIdx, captchaAnswerTextBox, false, false );
	}
	
	/**
	 * Adds the CAPTCHA test wors in the main grid of the dialog.
	 * Uses two rows in the current grid.
	 */
	protected void addCaptchaTestRows(){
		addCaptchaTestGrid( false, false );
	}

	/**
	 * @return the captcha-answer field value
	 */
	protected String getCaptchaAnswerText() {
		return captchaAnswerTextBox.getText();
	}
	
	/**
	 * Updates the Captcha image
	 */
	protected void updateCaptchaImage(){
		//If the Captcha test failed then update the URL in the dialog
		captchaImage.setUrl(ServerSideAccessManager.getCaptchaProblemServletURL());
		//Clean the TextBox of the Captcha test
		cleanCaptchaTextBox();
	}
	
	/**
	 * Remove the old text from the response text box, if any
	 */
	protected void cleanCaptchaTextBox() {
		captchaAnswerTextBox.setText( "" );
	}
	
	/**
	 * Adds left, right action buttons and also the progress bar to the new sub grid.
	 * The number of columns in the Grid will be the extra columns plus two columns
	 * for the left and right action buttons plus the progress bar if any.
	 * @param numberOfExtraColumns the number of extra Grid columns in the new grid
	 * @param isLeftEnabled if the left button should be enabled
	 * @param leftButtonColumn the column to add the left button to
	 * @param isRightEnabled if the right button should be enabled
	 * @param rightButtonColumn the column to add the right button to
	 * @param isProgressBar true if we want to add the progress bar
	 * @param progressBarColumn the column index to which the progress bar will be added
	 * @param isNewRow true to put buttons on the new row, otherwise false
	 * @param the flex table to which the required elements were added, the returned table contains just one row
	 */
	protected FlexTable addGridActionElements( final int numberOfExtraColumns, final boolean isLeftEnabled,
											   final int leftButtonColumn, final boolean isRightEnabled,
											   final int rightButtonColumn, final boolean isProgressBar,
											   final int progressBarColumn, final boolean isNewRow ) {
		//Greate a new Grid
		FlexTable newGrid = new FlexTable( );
		//There will be just one row in the table
		newGrid.insertRow( 0 );
		//The number of columns in the Grid will be the extra columns plus two columns
		//for the left and right action buttons plus the progress bar if any
		final int numberColumns = numberOfExtraColumns + 2 + ( isProgressBar ? 1 : 0);
		for(int i=0; i < numberColumns; i++) {
			newGrid.insertCell( 0, i );
		}
		newGrid.setSize("100%", "100%");
		//Add the subgrid to the current grid, covering all the cells of the current grid
		addToGrid( getCurrentGridIndex(), FIRST_COLUMN_INDEX, indexToGrid.get( getCurrentGridIndex() ).getCellCount(0), newGrid, isNewRow, false );
		
		this.isLeftEnabled = isLeftEnabled;
		this.isRightEnabled = isRightEnabled;
		
		//Create the 'Cancel' button, along with a listener that hides the dialog
		//when the button is clicked.
		leftButton.setText( getLeftButtonText() );
		leftButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		leftButton.setEnabled( isLeftEnabled );
		leftButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				actionLeftButton();
			} } );
		setLeftActionButtonTitle( titlesI18N.defaultLeftActionButtonTitle() );
		//NOTE: If this is a new grid then we do not put things on a new row
		//Set to be horizontally aligned
		newGrid.getCellFormatter().setHorizontalAlignment(0, leftButtonColumn, HasHorizontalAlignment.ALIGN_CENTER);
		newGrid.setWidget(0, leftButtonColumn, leftButton);
		
		//Create the 'Register' button, along with a listener that hides the dialog
		//when the button is clicked.
		rightButton.setText( getRightButtonText() );
		rightButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		rightButton.setEnabled( isRightEnabled );
		rightButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				actionRightButton();
			}
		} );
		setRightActionButtonTitle( titlesI18N.defaultRightActionButtonTitle() );
		//Set to be horizontally aligned
		newGrid.getCellFormatter().setHorizontalAlignment(0, rightButtonColumn, HasHorizontalAlignment.ALIGN_CENTER);
		newGrid.setWidget(0, rightButtonColumn, rightButton);
		
		//Add the progress bar if needed
		if( isProgressBar ) {
			//Set to be horizontally aligned
			newGrid.getCellFormatter().setHorizontalAlignment(0, progressBarColumn, HasHorizontalAlignment.ALIGN_CENTER);
			newGrid.setWidget(0, progressBarColumn, progressBarUI);
		}
		
		return newGrid;
	}

	/**
	 * Adds left, right action buttons and also the progress bar to the new sub grid.
	 * @param isLeftEnabled if the left button should be enabled
	 * @param isRightEnabled if the right button should be enabled
	 * @param isProgressBar if the progress bar should be added
	 * @param isNewRow true to put buttons on the new row, otherwise false
	 */
	protected void addGridActionElements( final boolean isLeftEnabled, final boolean isRightEnabled,
										  final boolean isProgressBar, final boolean isNewRow ){
		if( isProgressBar ) {
			addGridActionElements( 0, isLeftEnabled, FIRST_COLUMN_INDEX, isRightEnabled, THIRD_COLUMN_INDEX, true, SECOND_COLUMN_INDEX, isNewRow );
		} else {
			addGridActionElements( 0, isLeftEnabled, FIRST_COLUMN_INDEX, isRightEnabled, SECOND_COLUMN_INDEX, false, 0, isNewRow );
		}
	}
	
	/**
	 * Adds left, right action buttons to the new sub grid.
	 * @param isLeftEnabled if the left button should be enabled
	 * @param isRightEnabled if the right button should be enabled
	 * @param isNewRow true to put buttons on the new row, otherwise false
	 */
	protected void addGridActionElements( final boolean isLeftEnabled, final boolean isRightEnabled, final boolean isNewRow ){
		addGridActionElements( isLeftEnabled, isRightEnabled, false, isNewRow );
	}

	/**
	 * Adds left, right action buttons to the new sub grid.
	 * @param isLeftEnabled if the left button should be enabled
	 * @param isRightEnabled if the right button should be enabled
	 */
	protected void addGridActionElements( boolean isLeftEnabled, boolean isRightEnabled ) {
		addGridActionElements( isLeftEnabled, isRightEnabled, true );
	}

	/**
	 * Sets the left button/action enabled, disabled;
	 * @param isLeftEnabled true for enabling, otherwise false
	 */
	public void setLeftEnabled(boolean isLeftEnabled){
		this.isLeftEnabled = isLeftEnabled;
		leftButton.setEnabled( isLeftEnabled );
	}

	/**
	 * Sets the right button/action enabled, disabled;
	 * @param isRightEnabled true for enabling, otherwise false
	 */
	public void setRightEnabled(boolean isRightEnabled){
		this.isRightEnabled = isRightEnabled;
		rightButton.setEnabled( isRightEnabled );
	}
	
	@Override
	public void setVisible(final boolean visible) {
		if( visible ){
			for( int i = 0; i < panelWidgetList.size(); i++){
				formVerticalPanel.add( panelWidgetList.get(i) );
			}
		} else {
			//Remove all the widgets from the panel
			formVerticalPanel.clear();
		}
		super.setVisible(visible);
	}
	
	/**
	 * @return The left button's hot keys 
	 */
	protected char getLeftButtonHotKey() {
		return KeyCodes.KEY_ESCAPE;
	}
	
	/**
	 * Sets the left action button text
	 */
	protected void setLeftActionButtonText( String text ) {
		leftButton.setText( text );
	}

	/**
	 * Sets the right action button text
	 */
	protected void setRightActionButtonText( String text ) {
		rightButton.setText( text );
	}
	
	/**
	 * Sets the left action button title
	 */
	protected void setLeftActionButtonTitle( String title ) {
		leftButton.setTitle( title );
	}

	/**
	 * Sets the right action button title
	 */
	protected void setRightActionButtonTitle( String title ) {
		rightButton.setTitle( title );
	}
	
	/**
	 * Allows to detect if the modification key related to the right-button action key is down 
	 * @param event the key down event
	 * @return true if the modification key is down, otherwise false
	 */
	protected boolean isRightButtonModKeyDown(KeyDownEvent event) {
		return event.isControlKeyDown();
	}
	
	/**
	 * @return The right button's hot keys 
	 */
	protected char getRightButtonHotKey() {
		return KeyCodes.KEY_ENTER;
	}
	
	/**
	 * This indicates if the dialog has to be re-centered on showing
	 * after been hidden.
	 * @return if true then the dialog is centered
	 */
	protected boolean centerOnReShow() {
		return true;
	}
	
	/**
	 * @return the left-button caption
	 */
	protected String getLeftButtonText(){
		return "Left";
	}

	/**
	 * @return the right-button caption
	 */
	protected String getRightButtonText(){
		return "Right";
	}
	
	/**
	 * Fills the dialog with data
	 */
	protected abstract void populateDialog(); 

	/**
	 * The left button's action
	 */
	protected abstract void actionLeftButton();

	/**
	 * The right button's action
	 */
	protected abstract void actionRightButton();
}
