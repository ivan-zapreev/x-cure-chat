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
package com.xcurechat.client.decorations;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

import com.xcurechat.client.rpc.ServerSideAccessManager;

/**
 * @author zapreevis
 * This class is responsible for dynamically updating the site decorations.
 * I.e. background images, and title color, and may be smth else
 */
public class SiteDynamicDecorations extends Timer {
	//The maximum space (height and width) the background image can
	//consume, relative to the client area of the browser window.
	//There are different sizes for the main front page of the web
	//site and the page the user gets after being logged on.
	private static final int MAX_BG_IMG_WIDTH_PERCENT = 40;
	private static final int MAX_BG_IMG_HEIGHT_PERCENT = 80;
	//The maximum space (height and width) the logo image can
	//consume, relative to the client area of the browser window
	private static final int MAX_LOGO_WIDTH_PERCENT = 33;
	private static final int MAX_LOGO_HEIGHT_PERCENT = 9;
	
	/**
	 * @author zapreevis
	 * This class stores information about the background images
	 */
	private class BackgroundImageHolder {
		public final String url;
		//The width and height of the original image
		private final int width;
		private final int height;
		//The recommended image sizes for visualization
		private int recommendedWidth;
		private int recommendedHeight;
		
		/**
		 * Allows to set the recommended background image width and height
		 * based on the current client area of the browser window.
		 * @param percentWidth the % of client area width that the image is allowed to take 
		 * @param percentHeight the % of client area height that the image is allowed to take 
		 * This method should be called each time before calling the 
		 * getRecommendedWidth and getRecommendedHeight methods
		 */
		public void recommendImageSize(final int percentWidth, final int percentHeight) {
			final int max_width = (int) ( Window.getClientWidth() * ( (double) percentWidth / 100.0 ) );
			final int max_height = (int) ( Window.getClientHeight() * ( (double) percentHeight / 100.0 ) );
			recommendedWidth = width; recommendedHeight = height;
			final double w_h_ratio = (double) recommendedWidth / (double) recommendedHeight;  
			final double h_w_ratio = (double) recommendedHeight / (double) recommendedWidth;  
			if( recommendedWidth > max_width ) {
				recommendedWidth = max_width;
				recommendedHeight = (int) ( h_w_ratio * recommendedWidth ); 
			}
			if( recommendedHeight > max_height ) {
				recommendedHeight = max_height;
				recommendedWidth = (int) ( w_h_ratio * recommendedHeight );
			}
		}
		
		public int getRecommendedWidth() {
			return recommendedWidth;
		}
		
		public int getRecommendedHeight() {
			return recommendedHeight;
		}
		
		public BackgroundImageHolder(final String fileName, final int width, final int height) {
			this.width = width;
			this.height = height;
			this.url = ServerSideAccessManager.getBackgroundImagesLocation() + fileName;
		}
	}
	
	public static final String SITE_TITLE_LABEL_DEFAULT_STYLE = "xcure-Chat-Site-Title";
	public static final String SITE_BG_IMAGE_PANEL_VISIBLE_STYLE = "xcure-Chat-Site-Background-Image-Panel-Visible";
	public static final String SITE_BG_IMAGE_PANEL_INVISIBLE_STYLE = "xcure-Chat-Site-Background-Image-Panel-InVisible";
	public static final String SITE_BACKGROUND_IMAGE_STYLE = "xcure-Chat-Site-Background-Image";
	//Note that the actual colors are placed in the color.html located in the same folder with this source file
	public static final String SITE_TITLE_TIME_COLOR_STYLE_NAME_PREFIX = "xcure-Chat-Site-Title-Time";
	//The location of the site background images
	public static final String BACKGROUND_IMAGES_RELATIVE_LOCATION = "site/";
	//The location of the logo images relative to ServerSideAccessManager.getBackgroundImagesLocation()
	public static final String LOGO_IMAGES_RELATIVE_LOCATION = "logo/";
	
	//The list of background images
	private static final List<BackgroundImageHolder> mainBackgroundImageURLs = new ArrayList<BackgroundImageHolder>();
	
	//The list of time of the day background images
	private static final List<BackgroundImageHolder> logoTimeImageURLs = new ArrayList<BackgroundImageHolder>();
	
	//The instance of this class
	private static final SiteDynamicDecorations instance = new SiteDynamicDecorations();
	
	/**
	 * Allows to add a new logo image with the given index
	 * @param index the index of the image
	 * @param width the width of the image
	 * @param height the height of the image
	 */
	private static void addLogoImage( final int index, final int width, final int height ) {
		logoTimeImageURLs.add( instance.new BackgroundImageHolder( LOGO_IMAGES_RELATIVE_LOCATION + "logo_" + index + ".png", width, height ) );
	}
	
	/**
	 * Allows to add a new background image with the given index and sizes
	 * @param index the index of the image
	 * @param width the width of the image
	 * @param height the height of the image
	 */
	private static void addBackgroundImage( final int index, final int width, final int height ) {
		mainBackgroundImageURLs.add( instance.new BackgroundImageHolder( BACKGROUND_IMAGES_RELATIVE_LOCATION + "background_"+index+".png", width, height ) );
	}
	
	//The background "time of the day" image
	private static final Image logoTimeImage = new Image();
	//The background image
	private static final Image backgroundImage = new Image();
	//The background image panel
	private static SimplePanel backgroundImagePanel = new SimplePanel();
	//Current background image index, set to minus one, to avoid
	//loading two images one afther another on the first site load
	private static int backgroundImageIndex = -1;
	//Current background time image index, set to minus one, to avoid
	//loading two images one afther another on the first site load
	private static int logoTimeImageIndex = -1;
	//Contains true if we need to show the image background, otherwise false
	private static boolean isShowBackground = false;
	
	static {
		//Initialize the background image panel
		backgroundImagePanel.setStyleName( SITE_BG_IMAGE_PANEL_VISIBLE_STYLE );
		backgroundImage.addStyleName( SITE_BACKGROUND_IMAGE_STYLE );
		backgroundImagePanel.add( backgroundImage );
		
		//Initialize the main background images
		final int MINIMUM_IMAGE_INDEX = 1;
		int counter = MINIMUM_IMAGE_INDEX;
		addBackgroundImage( counter++, 715, 930 );		/*1*/
		addBackgroundImage( counter++, 724, 938 );		/*2*/
		addBackgroundImage( counter++, 764, 1047 );		/*3*/
		addBackgroundImage( counter++, 803, 1054 );		/*4*/
		addBackgroundImage( counter++, 786, 1051 );		/*5*/
		addBackgroundImage( counter++, 730, 927 );		/*6*/
		addBackgroundImage( counter++, 759, 1051 );		/*7*/
		addBackgroundImage( counter++, 793, 1051 );		/*8*/
		addBackgroundImage( counter++, 790, 1051 );		/*9*/
		addBackgroundImage( counter++, 765, 1054 );		/*10*/
		addBackgroundImage( counter++, 729, 927 );		/*11*/
		addBackgroundImage( counter++, 798, 1056 );		/*12*/
		addBackgroundImage( counter++, 609, 775 );		/*13*/
		addBackgroundImage( counter++, 878, 1228 );		/*14*/
		addBackgroundImage( counter++, 757, 1051 );		/*15*/
		addBackgroundImage( counter++, 709, 1013 );		/*16*/
		addBackgroundImage( counter++, 730, 929 );		/*17*/
		addBackgroundImage( counter++, 1040, 1230 );	/*18*/
		addBackgroundImage( counter++, 643, 894 );		/*19*/
		addBackgroundImage( counter++, 727, 925 );		/*20*/
		addBackgroundImage( counter++, 652, 1010 );		/*21*/
		addBackgroundImage( counter++, 568, 797 );		/*22*/
		addBackgroundImage( counter++, 570, 780 );		/*23*/
		addBackgroundImage( counter++, 655, 797 );		/*24*/
		addBackgroundImage( counter++, 728, 923 );		/*25*/
		addBackgroundImage( counter++, 606, 796 );		/*26*/
		addBackgroundImage( counter++, 551, 792 );		/*27*/
		addBackgroundImage( counter++, 721, 1056 );		/*28*/
		addBackgroundImage( counter++, 852, 1080 );		/*29*/
		addBackgroundImage( counter++, 772, 1230 );		/*30*/
		addBackgroundImage( counter++, 733, 930 );		/*31*/
		addBackgroundImage( counter++, 544, 785 );		/*32*/
		addBackgroundImage( counter++, 852, 1232 );		/*33*/
		addBackgroundImage( counter++, 896, 1232 );		/*34*/
		addBackgroundImage( counter++, 727, 931 );		/*35*/
		addBackgroundImage( counter++, 824, 1230 );		/*36*/
		addBackgroundImage( counter++, 694, 1123 );		/*37*/
		addBackgroundImage( counter++, 980, 1230 );		/*38*/
		addBackgroundImage( counter++, 840, 1228 );		/*39*/
		addBackgroundImage( counter++, 814, 1230 );		/*40*/
		addBackgroundImage( counter++, 730, 929 );		/*41*/
		
		//Initialize the time of the day background images, ordered by time, starting at 00:00
		for( int index = MINIMUM_IMAGE_INDEX; index <= 24; index ++ ) {
			addLogoImage( index, 431, 80 );
		}
	}
	
	private SiteDynamicDecorations() {}

	/**
	 * Allows to retrieve the instance of the site dynamic decoration update class
	 */
	public static SiteDynamicDecorations getInstance() {
		return instance;
	}
	
	/**
	 * Returns the panel of the background image for the site
	 * @return the site's background-image panel
	 */
	public static SimplePanel getBackgroundImagePanel() {
		return backgroundImagePanel;
	}
	
	public static void showSiteBackground( final boolean isShowBackgroundArd ) {
		isShowBackground = isShowBackgroundArd;
		if( isShowBackground ) {
			backgroundImagePanel.setStyleName( SITE_BG_IMAGE_PANEL_VISIBLE_STYLE );
		} else {
			backgroundImagePanel.setStyleName( SITE_BG_IMAGE_PANEL_INVISIBLE_STYLE );
		}
		//Update the background image sizes
		updateMainImagesAndSizes();
	}
	
	/**
	 * Returns the background image representing the time of the day
	 * @return the site's time-of-the-day background image
	 */
	public static Image getBackgroundTimeImage() {
		return instance.logoTimeImage;
	}
	
	/**
	 * Updates the provided image with the data stored at
	 * the specified index of the data holders array
	 * @param dataHolders the list of data holders
	 * @param index the index of the required data holder
	 * @param image the image to update
	 * @param percentWidth the % of client area width that the image is allowed to take 
	 * @param percentHeight the % of client area height that the image is allowed to take 
	 */
	private static void updateImage( final List<BackgroundImageHolder> dataHolders,
									 final int index, final Image image,
								     final int percentWidth, final int percentHeight ){
		//Check for the proper bounds to avoid index out of bounds exception!!!
		if( dataHolders.size() > 0 ) {
			final int actual_index = index % dataHolders.size();
			if( actual_index >= 0 ) {
				final BackgroundImageHolder dataHolder = dataHolders.get( actual_index );
				dataHolder.recommendImageSize( percentWidth, percentHeight  );
				image.setVisible(false);
				image.setUrl( dataHolder.url );
				image.setSize( dataHolder.getRecommendedWidth()+"px",
							   dataHolder.getRecommendedHeight()+"px" );
				image.setVisible(true);
			}
		}
	}
	
	/**
	 * Allows to resize the background image. Does the background image update only if
	 * the class instance is properly set up and initialized.
	 */
	public static void updateMainImagesAndSizes() {
		updateImage( logoTimeImageURLs, logoTimeImageIndex, logoTimeImage,
				     MAX_LOGO_WIDTH_PERCENT, MAX_LOGO_HEIGHT_PERCENT );
		if( isShowBackground ) {
			updateImage( mainBackgroundImageURLs, backgroundImageIndex, backgroundImage,
				     	 MAX_BG_IMG_WIDTH_PERCENT, MAX_BG_IMG_HEIGHT_PERCENT );
		}
	}
	
	@Override
	public void run() {
		//Choose the first background image randomly
		backgroundImageIndex = Random.nextInt( mainBackgroundImageURLs.size() ) ;
		//Choose the logo image randomly
		logoTimeImageIndex = Random.nextInt( logoTimeImageURLs.size() ) ;
		
		//Set the background image and its size
		updateMainImagesAndSizes();
		
		//Reschedule to run in 30 minutes
		this.schedule( 1800000 );
	}
}
