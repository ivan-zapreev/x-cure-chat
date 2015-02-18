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
 * The file processing package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.utils;

import java.util.Date;

import com.xcurechat.client.i18n.UITitlesI18N;
import com.xcurechat.client.rpc.ServerSideAccessManager;

public enum SiteUserAge {
	NEW(0),
	BEGINNER(1),
	NORMAL(2),
	EXPERIENCED(3),
	OLD(4),
	PERMANENT(5);
	
	private static final String USER_SITE_AGE_IMAGES_LOCATION_FOLDER = "age/";
	private final String ageImageURL;
	
	private SiteUserAge( final int ageImageIndex ){
		this.ageImageURL = ServerSideAccessManager.USER_STATUS_IMAGES_BASE_URL + USER_SITE_AGE_IMAGES_LOCATION_FOLDER + ageImageIndex + ".png";
	}
	
	public String getAgeImageURL() {
		return  ageImageURL;
	}
	
	public String getAgeImageTooltip( final UITitlesI18N i18nTitles ) {
		return SiteUserAgeHelper.getAgeImageTooltip( i18nTitles, this );
	}
	
	/**
	 * @author zapreevis
	 * This is a helper class for the user age 
	 */
	public static class SiteUserAgeHelper {
		//The user age statuses and periods
		private static final long ONE_WEEK_MILLISEC = 7 * 24 * 60 * 60 * 1000;
		private static final long ONE_MONTH_MILLISEC = 4 * ONE_WEEK_MILLISEC;
		private static final long THREE_MONTH_MILLISEC = 3 * ONE_MONTH_MILLISEC;
		private static final long SIX_MONTH_MILLISEC = 2 * THREE_MONTH_MILLISEC;
		private static final long ONE_YEAR_MILLISEC = 2 * SIX_MONTH_MILLISEC;

		public static String getAgeImageTooltip( final UITitlesI18N i18nTitles, final SiteUserAge siteUserAge ) {
			switch( siteUserAge ) {
				case NEW: return i18nTitles.newSiteUserAge();
				case BEGINNER: return i18nTitles.beginnerSiteUserAge();
				case NORMAL: return i18nTitles.normalSiteUserAge();
				case EXPERIENCED: return i18nTitles.experiencedSiteUserAge();
				case OLD: return i18nTitles.oldSiteUserAge();
				case PERMANENT: return i18nTitles.permanentSiteUserAge();
				default: return "Unknown";
			}
		}
		
		public static SiteUserAge getSiteUserAgeByRegDate( final Date userRegistrationDate ) {
			long registrationTimeMillisec = userRegistrationDate.getTime();
			long currentTimeMillisec = System.currentTimeMillis();
			long timeOnSiteMillisec = currentTimeMillisec - registrationTimeMillisec;
			
			if( timeOnSiteMillisec < ONE_WEEK_MILLISEC ) {
				return NEW;
			} else {
				if( timeOnSiteMillisec < ONE_MONTH_MILLISEC ) {
					return BEGINNER;
				} else {
					if( timeOnSiteMillisec < THREE_MONTH_MILLISEC ) {
						return NORMAL;
					} else {
						if( timeOnSiteMillisec < SIX_MONTH_MILLISEC ) {
							return EXPERIENCED;
						} else {
							if( timeOnSiteMillisec < ONE_YEAR_MILLISEC ) {
								return OLD;
							} else {
								return PERMANENT;
							}
						}
					}
				}
			}
		}
	}
}
