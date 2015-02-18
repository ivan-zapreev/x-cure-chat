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

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;

/**
 * @author zapreevis
 * This represents a simple horizontal progress bar
 */
public class SimpleHorizontalProgressBarUI extends Composite {
	//The maximum capacity of the progress bar in abstract units
	private final int MAX_PROGRESSBAR_CAPACITY;
	
	//The height of the progress bar in pixels
	private final int HEIGHT_INNER_IN_PIXELS;
	//The progress bar length in pixels is set relative to the textObject offset width
	private final int PROGRESS_BAR_WIDTH_IN_PIXELS;
	
	//The old value of the progress bar
	private int oldProgressBarValue = -1;
	
	//The progress bar is made of a panel
	private HorizontalPanel progressBarPanel = new HorizontalPanel(); 
	private HTML leftBar = new HTML();
	private HTML rightBar = new HTML();
	
	/**
	 * This constructor should be only visible inside of the package.
	 * Note that to initialize the progress bar one has to bind it to the text base object.
	 * This should be done using the method bindProgressBar(...).
	 */
	public SimpleHorizontalProgressBarUI( final int progressBarWidthPixels,  final int progressBarHeightPixels,
								   final int maximumProgressBarCapacity, final int currentProgressBarValue ) {
		//Initialize the progress bar content first
		progressBarPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		progressBarPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		progressBarPanel.add( leftBar );
		progressBarPanel.add( rightBar );
		
		PROGRESS_BAR_WIDTH_IN_PIXELS = progressBarWidthPixels;
		progressBarPanel.setWidth( PROGRESS_BAR_WIDTH_IN_PIXELS + "px");
		MAX_PROGRESSBAR_CAPACITY = maximumProgressBarCapacity;
		HEIGHT_INNER_IN_PIXELS = progressBarHeightPixels;
		
		//Update the progress bar
		forceProgressUpdate( currentProgressBarValue );
		
		//Initialize the composite
		initWidget( progressBarPanel );
	}
	
	private String getBar(double percent, int red, int green, int blue, boolean isTopInt) {
		double width_double = ( ( (double) (percent * PROGRESS_BAR_WIDTH_IN_PIXELS) ) / 100.0 );
		int width;
		if( isTopInt ) {
			width = (int) Math.ceil( width_double );
		} else {
			width = (int) Math.floor( width_double );
		}
		return "<div style=\"width:" + width + "px; height: " + HEIGHT_INNER_IN_PIXELS +
				"px; background-color: RGB(" + red + ","+ green + "," + blue + ");\"></div>";
	}
	
	private String getLeftBar(double percent){
		return getBar(percent, 122, 0, 255, true);
	}
	
	private String getRightBar(double percent){
		return getBar(percent, 122, 0, (int) ( 128.0 * ( (double) (100 - percent) ) / 100.0 ), false);
	}

	/**
	 * When called forced the progress bar update
	 * @param providedProgressBarValue the new progress bar value to be set
	 */
	public void forceProgressUpdate( final int providedProgressBarValue ) {
		int currentProgressBarValue = providedProgressBarValue;
		if( oldProgressBarValue != currentProgressBarValue ) {
			if( currentProgressBarValue > MAX_PROGRESSBAR_CAPACITY ) {
				currentProgressBarValue = MAX_PROGRESSBAR_CAPACITY;
			} else {
				if( currentProgressBarValue < 0 ) {
					currentProgressBarValue = 0;
				}
			}
			final double percent = ( (double) 100.0 * currentProgressBarValue ) / MAX_PROGRESSBAR_CAPACITY ; 
			leftBar.setHTML( getLeftBar( percent ) );
			rightBar.setHTML( getRightBar( 100.0 - percent ) );
			oldProgressBarValue = currentProgressBarValue;
		}
	}
}
