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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.event.shared.HandlerRegistration;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.google.gwt.widgetideas.graphics.client.GWTCanvas;
import com.google.gwt.widgetideas.graphics.client.ImageLoader;

import com.xcurechat.client.data.ShortFileDescriptor;

import com.xcurechat.client.utils.FlashObjectWithJWPlayer;
import com.xcurechat.client.utils.SupportedFileMimeTypes;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * Allows to view the media files based on the file descriptors
 */
public abstract class ViewMediaFilesDialogUI<T extends ShortFileDescriptor> extends ViewMediaDialogBase{
	
	/**
	 * @author zapreevis
	 * This is a helper class used to ensure that the returned image widgets implement the RotateableImageWidget interface
	 */
	private class ImageWidgetComposite extends Composite implements RotateableImageWidget {
		public final ScrollPanel scrollPanel = new ScrollPanel();
		public final GWTCanvas canvas = new GWTCanvas();
		public final FocusPanel zoomPanel = new FocusPanel();
		public final T fileDescr;
		public ImageWidgetComposite( final T fileDescr ) {
			//Call the super constructor
			super();
			//Save the data
			this.fileDescr = fileDescr;
			//Set up the widget
			canvas.setStyleName( CommonResourcesContainer.IMAGE_MEDIA_FILE_SHOW_STYLE );
			zoomPanel.add( canvas );
			scrollPanel.add( zoomPanel );
			//Initialize the widget
			initWidget( scrollPanel );
		}
		@Override
		public String getFileURL() {
			return getMediaFileServerURL( fileDescr );
		}
	}
	
	/**
	 * @author zapreevis
	 * This is a helper class used to ensure that the returned playable file widgets implement the DownloadableFileWidget interface
	 */
	private class PlayableFileComposite extends Composite implements DownloadableFileWidget {
		public final T fileDescr;
		public PlayableFileComposite( final T fileDescr ) {
			this.fileDescr = fileDescr;
			FlashObjectWithJWPlayer flashObject = new FlashObjectWithJWPlayer( null, null, GWT.getModuleBaseURL() );
			
			//Construct the embedded flash object
			flashObject.setMediaUrl( getMediaFileServerURL( fileDescr ), fileDescr.mimeType );
			flashObject.completeEmbedFlash();
			
			//Initialize the widget but do not add the download link since this is handled in the parent dialog
			initWidget( flashObject.getEmbeddedObjectWidget( false, false ) );
		}
		@Override
		public String getFileURL() {
			return getMediaFileServerURL( fileDescr );
		}
	}
	
	/**
	 * @author zapreevis
	 * This class is responsible for zoomin the image in and out
	 */
	public class ImageZoomHelper implements ClickHandler {
		public static final String ZOOME_IN_IMAGE_STYLE = "xcure-Chat-Zoom-In-Cursor";
		public static final String ZOOME_OUT_IMAGE_STYLE = "xcure-Chat-Zoom-Out-Cursor";
		
		//Contains true if the image is zoomed in
		private boolean isZoomedIn = false;
		//The file descriptor
		private T fileDescr;
		//The image data object
		private final ImageElement imageData;
		//The canvas reference
		private final GWTCanvas canvas;

		public ImageZoomHelper( final T fileDescr, final GWTCanvas canvas, final ImageElement imageData ) {
			this.fileDescr = fileDescr;
			this.imageData = imageData;
			this.canvas = canvas;
			//Set the zoom in style
			canvas.addStyleName( ZOOME_IN_IMAGE_STYLE );
		}
		
		@Override
		public void onClick(ClickEvent event) {
			//Remember the new zooming status
			isZoomedIn = ! isZoomedIn;
			//Then do the zooming
			if( isZoomedIn ) {
				canvas.removeStyleName( ZOOME_IN_IMAGE_STYLE );
				canvas.addStyleName( ZOOME_OUT_IMAGE_STYLE );
			} else {
				canvas.removeStyleName( ZOOME_OUT_IMAGE_STYLE );
				canvas.addStyleName( ZOOME_IN_IMAGE_STYLE );
			}
			//Restore the image by drawing it again
			drawImageToCanvas( canvas, imageData, fileDescr, isZoomedIn );
		}
		
	}
	
	//The client area width for switching to the large image size in the image file view
	protected static final int GO_MAX_IMAGE_SIZE_CLIENT_WIDTH = 1000;
	//The client area height for switching to the large image size in the image file view
	protected static final int GO_MAX_IMAGE_SIZE_CLIENT_HEIGHT = 800;
	
	//These are the standard minimum image sizes for the image view dialog
	protected static final int MAX_IMAGE_VIEW_WIDTH = 800;
	protected static final int MAX_IMAGE_VIEW_HEIGHT = 600;
	
	//These are the standard minimum image sizes for the image view dialog
	protected static final int MIN_IMAGE_VIEW_WIDTH = 640;
	protected static final int MIN_IMAGE_VIEW_HEIGHT = 480;
	
	//The list of media file descriptors
	private final List<T> fileList;
	
	//These variables store the image-view related data for the image and the scroll  
	//panel. This information always corresponds to the last shown image file
	private Map<Integer, HandlerRegistration> zoomHandlerRegistrations = new HashMap<Integer, HandlerRegistration>();
	private boolean isZoomNeeded = false;
	private int scrollPanelWidth   = 0;
	private int scrollPanelHeight  = 0;
	
	/**
	 * Allows to view one media file in the dialog
	 * @param parentDialog the parent dialog
	 * @param fileList the list of media dile descriptors, not NULL
	 * @param currentIndex the index of the file to show first
	 */
	public ViewMediaFilesDialogUI( final DialogBox parentDialog, final T fileDescr ) {
		super( parentDialog );
		
		//Store the file descriptor
		this.fileList = new ArrayList<T>();
		this.fileList.add( fileDescr );
	}
	
	/**
	 * Allows to view several media files in one dialog
	 * @param parentDialog the parent dialog
	 * @param fileList the list of media dile descriptors, not NULL
	 * @param currentIndex the index of the file to show first
	 */
	public ViewMediaFilesDialogUI( final DialogBox parentDialog, final List<T> fileList, final int currentIndex ) {
		super( parentDialog, fileList.size(), currentIndex );
		
		//Store the file descriptors
		this.fileList = fileList;
	}
	
	/**
	 * Allows to get a media file descriptor by the file index
	 * @param index the file index
	 * @return the corresponding media file descriptor or null if the is not file descripor with such index
	 */
	protected T getMediaFileDescriptor( final int index ) {
		return fileList.get( index );
	}
	
	/**
	 * Allows to retrieve the server URL of the media file that has to be displayed
	 * @param fileDescr the file descriptor
	 * @return the server URL
	 */
	public abstract String getMediaFileServerURL( final T fileDescr );
	
	/**
	 * Allows to compute the image-file related sizes needed for the view
	 * @param fileDescr the file descriptor
	 */
	private void computeImageRelatedSizes( final T fileDescr ) {
		//Compute the maximum allowed view width and height
		final int MAX_ALLOWED_VIEW_WIDTH;
		final int MAX_ALLOWED_VIEW_HEIGHT;
		if( ( Window.getClientHeight() > GO_MAX_IMAGE_SIZE_CLIENT_HEIGHT ) && ( Window.getClientWidth() > GO_MAX_IMAGE_SIZE_CLIENT_WIDTH ) ) {
			MAX_ALLOWED_VIEW_WIDTH = MAX_IMAGE_VIEW_WIDTH; MAX_ALLOWED_VIEW_HEIGHT = MAX_IMAGE_VIEW_HEIGHT;
		} else {
			MAX_ALLOWED_VIEW_WIDTH = MIN_IMAGE_VIEW_WIDTH; MAX_ALLOWED_VIEW_HEIGHT = MIN_IMAGE_VIEW_HEIGHT;
		}
		
		//Compute the width and height with which the image and the scroll panel wrapper should be displayed
		int scaled_img_width    = fileDescr.widthPixels;
		int scaled_img_height   = fileDescr.heightPixels;
		int scroll_panel_width  = fileDescr.widthPixels;
		int scroll_panel_height = fileDescr.heightPixels;
		//Stores true if we need to enable the zoom-in and out capabilities
		final boolean isRurnZoomingOn;
		
		//If the actual image size is larger than the maximum view
		//size then we want to resize the image to make it fit
		if( ( scaled_img_width > MAX_ALLOWED_VIEW_WIDTH ) ||
			( scaled_img_height > MAX_ALLOWED_VIEW_HEIGHT ) ) {
			//If the image size is larger than the default one and the window
			//size is large enough, then we let the image to have its original size
			isRurnZoomingOn = true;
			final double w_h_ratio = (double) scaled_img_width / (double) scaled_img_height;  
			final double h_w_ratio = (double) scaled_img_height / (double) scaled_img_width;  
			if( scaled_img_width > MAX_ALLOWED_VIEW_WIDTH ) {
				scaled_img_width = MAX_ALLOWED_VIEW_WIDTH;
				scaled_img_height = (int) ( h_w_ratio * scaled_img_width ); 
			}
			if( scaled_img_height > MAX_ALLOWED_VIEW_HEIGHT ) {
				scaled_img_height = MAX_ALLOWED_VIEW_HEIGHT;
				scaled_img_width = (int) ( w_h_ratio * scaled_img_height );
			}
			scroll_panel_width = scaled_img_width;
			scroll_panel_height = scaled_img_height;
		} else {
			//If we are here than it means that the image fits into the allowed view or the image sizes are unknown
			isRurnZoomingOn = false;
			if( ( scaled_img_width  > ShortFileDescriptor.ZERO_IMAGE_WIDTH ) ||
				( scaled_img_height > ShortFileDescriptor.ZERO_IMAGE_HEIGHT ) ) {
				//If one of the image sizes is not zero but is smaller than
				//the minimum allowed size then the image gets stretched.
				if( ( scaled_img_width <= ShortFileDescriptor.MINIMUM_VALID_IMAGE_WIDTH ) ||
					( scaled_img_height <= ShortFileDescriptor.MINIMUM_VALID_IMAGE_HEIGHT ) ) {
					scaled_img_width = MAX_ALLOWED_VIEW_WIDTH;
					scaled_img_height = MAX_ALLOWED_VIEW_HEIGHT;
					scroll_panel_width = MAX_ALLOWED_VIEW_WIDTH;
					scroll_panel_height = MAX_ALLOWED_VIEW_HEIGHT;
				} else {
					//DO NOTHING: The image fits perfectly without scaling and is not too small
				}
			} else {
				//If both of the image sizes are unknown, then this is could be a default
				//image file descriptor for a non-existent or newly-uploaded image
				scaled_img_width = MAX_ALLOWED_VIEW_WIDTH;
				scaled_img_height = MAX_ALLOWED_VIEW_HEIGHT;
				scroll_panel_width = MAX_ALLOWED_VIEW_WIDTH;
				scroll_panel_height = MAX_ALLOWED_VIEW_HEIGHT;
			}
		}
		
		//Store the image view related data
		isZoomNeeded	   = isRurnZoomingOn;
		fileDescr.widthScaledPixels = scaled_img_width; 
		fileDescr.heightScaledPixels = scaled_img_height; 
		scrollPanelWidth   = scroll_panel_width;
		scrollPanelHeight  = scroll_panel_height;
	}	
	
	/**
	 * Allows to set up the image and its wrapper-scroll-panel widgets.
	 * @param index the index of the file widget
	 * @param imageViewScrollPanel the image view scroll panel
	 * @param canvas the canvas to put the image into
	 * @param fileDescr the image file descriptor
	 */
	private void setUpImageAndScrollPanel( final int index, final ImageWidgetComposite imageWidget ) {
		//Disable the controls, start the progress bar
		markImageLoading( true );
		setEnabled( false );
		
		//Compute the image related sizes
		computeImageRelatedSizes( imageWidget.fileDescr );
		
		//Set the initial computed values for the scroll panel sizes
		imageWidget.scrollPanel.setWidth( scrollPanelWidth + "px" );
		imageWidget.scrollPanel.setHeight( scrollPanelHeight + "px" );
		
		//Remove the old zoom handler if needed
		HandlerRegistration zoomHandlerReg = zoomHandlerRegistrations.get( index );
		if( zoomHandlerReg != null ) {
			zoomHandlerReg.removeHandler();
		}
		
		//Load the image and then draw it to the canvas and rotate
		String[] urls = new String[1];
		urls[0] = getMediaFileServerURL( imageWidget.fileDescr );
		ImageLoader.loadImages( urls, new ImageLoader.CallBack(){
		    public void onImagesLoaded( ImageElement[] imageElements ) {
		    	ImageElement imageData = imageElements[0];
				//The original image will be showed in zoomed-out mode
				if( drawImageToCanvas( imageWidget.canvas, imageData, imageWidget.fileDescr, false) ) {
					//The image sizes have changed, re-set/update the scroll panel sizes
					imageWidget.scrollPanel.setWidth( scrollPanelWidth + "px" );
					imageWidget.scrollPanel.setHeight( scrollPanelHeight + "px" );
					//Center the dialog once again
					ViewMediaFilesDialogUI.this.center();
				}
				//Enable the new zoom handler if it is needed
				if( isZoomNeeded ) {
					zoomHandlerRegistrations.put( index, imageWidget.zoomPanel.addClickHandler( new ImageZoomHelper( imageWidget.fileDescr, imageWidget.canvas, imageData ) ) );
				}
				//Stop the progress bar, enable the controls
				markImageLoading( false );
				setEnabled( true );
		    }
		} );
	}

	/**
	 * Allows to draw the image to the canvas, taking into account the rotation.
	 * Clears the canvas before drawing the image. Also computes all the needed
	 * image sizes before drawing.
	 * @param canvas the canvas to draw to
	 * @param imageData the loaded image data do draw
	 * @param zoomedOutImageWidth the width of the image or the zoom-out mode
	 * @param zoomedOutImageHeight the height of the image or the zoom-out mode
	 * @param isZoomedIn if true then the image should be zoomed in, otherwise false
	 * @param fileDescr the image file descriptor that contains the rotation parameters
	 * @return true if the image sizes we-reset from the image data object, this
	 * 				happens only if the original image file descriptor did not have
	 * 				the image height and width set. 
	 */
	private boolean drawImageToCanvas( final GWTCanvas canvas, final ImageElement imageData,
									   final T fileDescr, final boolean isZoomedIn) {
		boolean didSizesChanged = false;
		//This check is needed for the initial image setting after the image was
		//just uploaded and does not have image width and height set yet
		if( fileDescr.imageOrient == ShortFileDescriptor.ImageOrientation.DEFAULT ) {
			//If the image orientation is default, then we check if the width and the height are set
			//to something larger than zero, if not so then we are dealing with the file descriptor
			//for a new image, unless indeed this is a zero size image which is unlikely. Thus we
			//set size the values from the loaded image data 
			if( ( fileDescr.widthPixels  <= ShortFileDescriptor.ZERO_IMAGE_WIDTH ) &&
				( fileDescr.heightPixels <= ShortFileDescriptor.ZERO_IMAGE_HEIGHT ) ) {
				//The sizes are about to be changed
				didSizesChanged = true;
				//Set the new original image sizes
				fileDescr.widthPixels = imageData.getWidth();
				fileDescr.heightPixels = imageData.getHeight();
				//Re-compute the image related sizes
				computeImageRelatedSizes( fileDescr );
			}
		}
		
		//Set the appropriate canvas size
		if( isZoomedIn ) {
			canvas.setSize( fileDescr.widthPixels + "px", fileDescr.heightPixels + "px" ); 
			canvas.setCoordSize( fileDescr.widthPixels, fileDescr.heightPixels );
		} else {
			canvas.setSize( fileDescr.widthScaledPixels + "px", fileDescr.heightScaledPixels + "px" ); 
			canvas.setCoordSize( fileDescr.widthScaledPixels, fileDescr.heightScaledPixels );
		}
		
		//Save context
		canvas.saveContext();
		
		//Set the image scaling
		if( ! isZoomedIn ) {
			//If we are zoomed out then the image needs scaling
			canvas.scale( ( (double) fileDescr.widthScaledPixels )  / (double) fileDescr.widthPixels,
						  ( (double) fileDescr.heightScaledPixels ) / (double) fileDescr.heightPixels );
		}
		
		//Transform the coordinate system of the canvas 
		canvas.rotate( fileDescr.imageOrient.getRadians() );
		
		//Set the initial canvas coordinates so that the image is placed into the view when rotated
		//Note that, since of the scaling above, we use the original image sizes here
		final double initialXCoordinate;
		final double initialYCoordinate;
		switch( fileDescr.imageOrient ) {
			case RIGHT_90:
				initialXCoordinate = 0.0;
				initialYCoordinate = -1 * fileDescr.widthPixels;
				break;
			case RIGHT_180:
				initialXCoordinate = -1 * fileDescr.widthPixels;
				initialYCoordinate = -1 * fileDescr.heightPixels;
				break;
			case RIGHT_270:
				initialXCoordinate = -1 * fileDescr.heightPixels;
				initialYCoordinate =  0.0;
				break;
			case DEFAULT:
			default:
				initialXCoordinate = 0.0;
				initialYCoordinate = 0.0;
				break;
		}
		
		//Draw the image into the canvas, so that it will be rotated
		canvas.drawImage( imageData, initialXCoordinate, initialYCoordinate );
		
		//Restore context
		canvas.restoreContext();
		
		return didSizesChanged;
	}
	
	/**
	 * Allows to initialize an image widget for the given file descriptor that is known to represent an image
	 * @param index the file widget index
	 * @param fileDescr the file descriptor
	 * @return the image widget
	 */
	private Widget getImageWidget( final int index, final T fileDescr ) {
		final ImageWidgetComposite imageWidget = new ImageWidgetComposite( fileDescr );
		//Create the image and the wrapper panel widget and then set them up with the related sizes
		setUpImageAndScrollPanel( index, imageWidget );
		
		return imageWidget;
	}

	@Override
	protected Widget getMediaFileWidget( final int index ) {
		final Widget result;
		final T fileDescr = getMediaFileDescriptor( index );
		if( SupportedFileMimeTypes.isImageMimeType( fileDescr.mimeType ) ) {
			result = getImageWidget( index, fileDescr );
		} else {
			if( SupportedFileMimeTypes.isPlayableMimeType( fileDescr.mimeType ) ) {
				result = new PlayableFileComposite( fileDescr );
			} else {
				Window.alert("An unknown media file");
				result = new SimplePanel();
			}
		}
		return result;
	}
	
	@Override
	protected final void rotateImageRight( int index ) {
		final ImageWidgetComposite imageWidget = (ImageWidgetComposite) getFileWidget( index );
		//Update the file descriptor with the new orientation
		imageWidget.fileDescr.rotateImageDataRight();
		
		//Set up the image related sizes to the corresponding widgets
		setUpImageAndScrollPanel( index, imageWidget );
		
		//Re-center the dialog
		this.center();
	}
}
