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


/**
 * @author zapreevis
 * This represents a simple horizontal progress bar for the date, the older the
 * date is the less green we have, there are 5 gradations of oldness
 */
public class DateOldProgressBarUI extends SimpleHorizontalProgressBarUI {
	//The maximum progress bar capacity
	//public static final int MAXIMUM_DATE_PROGRESS_BAR_CAPACITY = 11;
	private static final int MAXIMUM_DATE_PROGRESS_BAR_CAPACITY = 10;
	private static final int PROGRESS_BAR_WIDHIN_TIME_STAGE_ZERO = MAXIMUM_DATE_PROGRESS_BAR_CAPACITY;
	private static final int PROGRESS_BAR_WIDHIN_TIME_STAGE_ONE = PROGRESS_BAR_WIDHIN_TIME_STAGE_ZERO-1;
	private static final int PROGRESS_BAR_WIDHIN_TIME_STAGE_TWO = PROGRESS_BAR_WIDHIN_TIME_STAGE_ONE-1;
	private static final int PROGRESS_BAR_WIDHIN_TIME_STAGE_THREE = PROGRESS_BAR_WIDHIN_TIME_STAGE_TWO-1;
	private static final int PROGRESS_BAR_WIDHIN_TIME_STAGE_FOUR = PROGRESS_BAR_WIDHIN_TIME_STAGE_THREE - 1;
	private static final int PROGRESS_BAR_WIDHIN_TIME_STAGE_FIVE = PROGRESS_BAR_WIDHIN_TIME_STAGE_FOUR - 1;
	private static final int PROGRESS_BAR_WIDHIN_TIME_STAGE_SIX = PROGRESS_BAR_WIDHIN_TIME_STAGE_FIVE - 1;
	private static final int PROGRESS_BAR_WIDHIN_TIME_STAGE_SEVEN = PROGRESS_BAR_WIDHIN_TIME_STAGE_SIX - 1;
	private static final int PROGRESS_BAR_WIDHIN_TIME_STAGE_EIGHT = PROGRESS_BAR_WIDHIN_TIME_STAGE_SEVEN - 1;
	private static final int PROGRESS_BAR_WIDHIN_TIME_STAGE_NINE = PROGRESS_BAR_WIDHIN_TIME_STAGE_EIGHT - 1;
	private static final int PROGRESS_BAR_MORE_THAN_TIME_STAGE_NINE = PROGRESS_BAR_WIDHIN_TIME_STAGE_NINE - 1;
	
	//The number of milliseconds in four one hours (4 * 60 * 60 * 1000 milliseconds)
	private static final long TIME_STAZE_ZERO = 4 * 60 * 60 * 1000;
	//The number of milliseconds in twelve hours
	private static final long TIME_STAGE_ONE =  3 * TIME_STAZE_ZERO;
	//The number of milliseconds in one day
	private static final long TIME_STAGE_TWO = 2 * TIME_STAGE_ONE;
	//The number of milliseconds in two days
	private static final long TIME_STAGE_THREE = 2 * TIME_STAGE_TWO;
	//The number of milliseconds in four days
	private static final long TIME_STAGE_FOUR = 2 * TIME_STAGE_THREE;
	//The number of milliseconds in eight days
	private static final long TIME_STAGE_FIVE = 2 * TIME_STAGE_FOUR;
	//The number of milliseconds in 16 days
	private static final long TIME_STAGE_SIX = 2 * TIME_STAGE_FIVE;
	//The number of milliseconds in 32 days
	private static final long TIME_STAGE_SEVEN = 2 * TIME_STAGE_SIX;
	//The number of milliseconds in 62
	private static final long TIME_STAGE_EIGHT = 2 * TIME_STAGE_SEVEN;
	//The number of milliseconds in 124 days
	private static final long TIME_STAGE_NINE = 2 * TIME_STAGE_EIGHT;
	
	/**
	 * Allows to construct the progress bar
	 * @param progressBarWidthPixels the width of the progress bar
	 * @param progressBarHeightPixels the height of the progress bar
	 * @param dateValue the date to create the progress bar for
	 */
	public DateOldProgressBarUI( final int progressBarWidthPixels, final int progressBarHeightPixels, final Date dateValue ) {
		super(progressBarWidthPixels, progressBarHeightPixels, MAXIMUM_DATE_PROGRESS_BAR_CAPACITY, getDateProgressBarValue( dateValue ) );
	}
	
	/**
	 * Maps the provided date in to the progress bar value constant, e.g. PROGRESS_BAR_WIDHIN_ONE_WEEK
	 * @param dateValue the date to map to the progress bar value constant
	 * @return the resulting constant
	 */
	public static final int getDateProgressBarValue( final Date dateValue ) {
		final long currentTime = (new Date()).getTime();
		final long dateValueTime = dateValue.getTime();
		final int result;
		
		if( ( currentTime - TIME_STAGE_NINE) <  dateValueTime ) {
			if( ( currentTime - TIME_STAGE_EIGHT ) <  dateValueTime ) {
				if( ( currentTime - TIME_STAGE_SEVEN ) <  dateValueTime ) {
					if( ( currentTime - TIME_STAGE_SIX ) <  dateValueTime ) {
						if( ( currentTime - TIME_STAGE_FIVE ) <  dateValueTime ) {
							if( ( currentTime - TIME_STAGE_FOUR ) <  dateValueTime ) {
								if( ( currentTime - TIME_STAGE_THREE ) <  dateValueTime ) {
									if( ( currentTime - TIME_STAGE_TWO ) <  dateValueTime ) {
										if( ( currentTime - TIME_STAGE_ONE ) <  dateValueTime ) {
											if( ( currentTime - TIME_STAZE_ZERO ) <  dateValueTime ) {
												result = PROGRESS_BAR_WIDHIN_TIME_STAGE_ZERO;
											} else {
												//result = PROGRESS_BAR_WIDHIN_TWO_HOURS;
												result = PROGRESS_BAR_WIDHIN_TIME_STAGE_ONE;
											}
										} else {
											result = PROGRESS_BAR_WIDHIN_TIME_STAGE_TWO;
										}				
									} else {
										result = PROGRESS_BAR_WIDHIN_TIME_STAGE_THREE;
									}				
								} else {
									result = PROGRESS_BAR_WIDHIN_TIME_STAGE_FOUR;
								}				
							} else {
								result = PROGRESS_BAR_WIDHIN_TIME_STAGE_FIVE;
							}				
						} else {
							result = PROGRESS_BAR_WIDHIN_TIME_STAGE_SIX;
						}				
					} else {
						result = PROGRESS_BAR_WIDHIN_TIME_STAGE_SEVEN;
					}				
				} else {
					result = PROGRESS_BAR_WIDHIN_TIME_STAGE_EIGHT;
				}				
			} else {
				result = PROGRESS_BAR_WIDHIN_TIME_STAGE_NINE;
			}
		} else {
			result = PROGRESS_BAR_MORE_THAN_TIME_STAGE_NINE;
		}
		return result;
	}
}
