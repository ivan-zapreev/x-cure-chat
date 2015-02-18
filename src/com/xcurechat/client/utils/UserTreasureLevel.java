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

import com.google.gwt.user.client.rpc.IsSerializable;
import com.xcurechat.client.i18n.UITitlesI18N;
import com.xcurechat.client.rpc.ServerSideAccessManager;

public enum UserTreasureLevel implements IsSerializable {
	LEVEL_0(0, SiteChatActivityHelper.TREASURE_BOUND_LEVEL_0 ),
	LEVEL_1(1, SiteChatActivityHelper.TREASURE_BOUND_LEVEL_1 ),
	LEVEL_2(2, SiteChatActivityHelper.TREASURE_BOUND_LEVEL_2 ),
	LEVEL_3(3, SiteChatActivityHelper.TREASURE_BOUND_LEVEL_3 ),
	LEVEL_4(4, SiteChatActivityHelper.TREASURE_BOUND_LEVEL_4 ),
	LEVEL_5(5, SiteChatActivityHelper.TREASURE_BOUND_LEVEL_5 ),
	LEVEL_6(6, SiteChatActivityHelper.TREASURE_BOUND_LEVEL_6 ),
	LEVEL_7(7, SiteChatActivityHelper.TREASURE_BOUND_LEVEL_7 ),
	LEVEL_8(8, SiteChatActivityHelper.TREASURE_BOUND_LEVEL_8 ),
	LEVEL_9(9, SiteChatActivityHelper.TREASURE_BOUND_LEVEL_9 );
	
	public static final String USER_TREASURE_LEVEL_IMAGES_LOCATION_FOLDER = "chat/";
	private final String treasureLevelImageURL;
	private final int rangeEnd;
	
	private UserTreasureLevel( final int treasureLevelImageURL, final int rangeEnd ){
		this.treasureLevelImageURL = ServerSideAccessManager.USER_STATUS_IMAGES_BASE_URL + USER_TREASURE_LEVEL_IMAGES_LOCATION_FOLDER + treasureLevelImageURL + ".png";
		this.rangeEnd = rangeEnd; 
	}
	
	public String getUserTreasureLevelImageURL() {
		return  treasureLevelImageURL;
	}
	
	public int getRangeEnd() {
		return rangeEnd;
	}
	
	public String getUserTreasureImageTooltip( final UITitlesI18N i18nTitles, final int goldPiecesInTheWallet ) {
		return i18nTitles.goldPiecesInTheWallet( goldPiecesInTheWallet );
	}
	
	/**
	 * @author zapreevis
	 * This is a helper class for the user chat activity 
	 */
	public static class SiteChatActivityHelper {
		//The upper bound for the number of gold pieces in each treasure level
		public static final int TREASURE_BOUND_LEVEL_0 = 5;
		public static final int TREASURE_BOUND_LEVEL_1 = 10;
		public static final int TREASURE_BOUND_LEVEL_2 = 20;
		public static final int TREASURE_BOUND_LEVEL_3 = 40;
		public static final int TREASURE_BOUND_LEVEL_4 = 80;
		public static final int TREASURE_BOUND_LEVEL_5 = 160;
		public static final int TREASURE_BOUND_LEVEL_6 = 320;
		public static final int TREASURE_BOUND_LEVEL_7 = 640;
		public static final int TREASURE_BOUND_LEVEL_8 = 1280;
		public static final int TREASURE_BOUND_LEVEL_9 = -1;
		
		public static UserTreasureLevel getUserTreasureLevel( final int goldPiecesInTheWallet ) {
			if( goldPiecesInTheWallet < TREASURE_BOUND_LEVEL_0 ) {
				return LEVEL_0;
			} else {
				if( goldPiecesInTheWallet < TREASURE_BOUND_LEVEL_1 ) {
					return LEVEL_1;
				} else {
					if( goldPiecesInTheWallet < TREASURE_BOUND_LEVEL_2 ) {
						return LEVEL_2;
					} else {
						if( goldPiecesInTheWallet < TREASURE_BOUND_LEVEL_3 ) {
							return LEVEL_3;
						} else {
							if( goldPiecesInTheWallet < TREASURE_BOUND_LEVEL_4 ) {
								return LEVEL_4;
							} else {
								if( goldPiecesInTheWallet < TREASURE_BOUND_LEVEL_5 ) {
									return LEVEL_5;
								} else {
									if( goldPiecesInTheWallet < TREASURE_BOUND_LEVEL_6 ) {
										return LEVEL_6;
									} else {
										if( goldPiecesInTheWallet < TREASURE_BOUND_LEVEL_7 ) {
											return LEVEL_7;
										} else {
											if( goldPiecesInTheWallet < TREASURE_BOUND_LEVEL_8 ) {
												return LEVEL_8;
											} else {
												return LEVEL_9;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
