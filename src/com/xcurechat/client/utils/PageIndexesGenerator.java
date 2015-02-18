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
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zapreevis
 * This class is responsible for generating sequence of page indexes for page browsing
 */
public class PageIndexesGenerator {
	//The maximum number of pages accessible from the right or from the left of the current page
	private static final int MAXIMUM_VISIBLE_PAGES_SEQ_RAD = 5;
	
	//The dummy page index that is used to indicate that the dotted separator should be inserted
	public static final int DUMMY_PAGE_INDEX = -1;

	/**
	 * This constructor is private because this class is not supposed to be instantiated
	 */
	private PageIndexesGenerator( ) {}
	
	/**
	 * Allows to get the number of pages for the given number of elements and number of elements per page
	 * @param number_of_elements the number of elements
	 * @param max_num_elements_per_page the number of elements per page
	 * @return the number of pages needed, >= 1
	 */
	public static final int getNumberOfPages(final int number_of_elements, final int max_num_elements_per_page ) {
		//Compute the maximum page index
		int tmpMaximumPageIndex =  ( number_of_elements / max_num_elements_per_page ) +
								   ( (number_of_elements % max_num_elements_per_page) > 0 ? 1 : 0);
		return (tmpMaximumPageIndex == 0) ? 1 : tmpMaximumPageIndex;
	}
	
	/**
	 * Allows to get the array of page indexes, with the dummy page
	 * indexes inserted to show that there is a gap between page indexes.
	 * @param minPageIdx the minimum page index
	 * @param currentPageIdx the current page index
	 * @param maxPageIdx the maximum page index
	 * @return the array with the generated page indexes
	 */
	public static final List<Integer> getPageIndexes( final int minPageIdx, final int currentPageIdx, final int maxPageIdx  ) {
		List<Integer> indexes = new ArrayList<Integer>();
		
		int startIdx = Math.max( currentPageIdx - MAXIMUM_VISIBLE_PAGES_SEQ_RAD, minPageIdx );
		int endIdx = Math.min( startIdx + 2 * MAXIMUM_VISIBLE_PAGES_SEQ_RAD, maxPageIdx );
		startIdx = Math.max( endIdx - 2 * MAXIMUM_VISIBLE_PAGES_SEQ_RAD, minPageIdx );
		
		//Handle the first page indexes and everything that precedes startIdx 
		if( startIdx != minPageIdx ) {
			//Add the first page index
			indexes.add( minPageIdx );
			//Add dummies and extra navigation if needed
			addIntermediateIndexes( indexes, minPageIdx, startIdx );
		}
		
		//Add the indexes within the view range
		for( int index = startIdx; index <= endIdx; index++ ) {
			indexes.add( index );
		}
		
		//Handle the last page indexes and everything that follows endIdx 
		if( endIdx != maxPageIdx ) {
			//Add dummies and extra navigation if needed
			addIntermediateIndexes( indexes, endIdx, maxPageIdx );
			//Add the last page index
			indexes.add( maxPageIdx );
		}
		
		return indexes;
	}
	
	/**
	 * Allows to take two indexes firstIndex and secondIndex, which should be
	 * non-negative and decide if we need to add something between them, i.e.
	 * either a dummy or two dummies with an intermediate page index. These
	 * indexes are appended to the provided list of indexes. Note that, this
	 * method automatically determines which is the first and which is the second.
	 * I.e. whether the interval is [firstIndex, secondIndex] or [secondIndex, firstIndex].   
	 * @param indexes the list of currently constructed indexes
	 * @param firstIndex the first index
	 * @param secondIndex the second index
	 */
	private static final void addIntermediateIndexes( List<Integer> indexes, final int firstIndex, final int secondIndex ) {
		//Determine the begin and end indexes
		final int beginIndex = Math.min( firstIndex, secondIndex );
		final int endIndex = Math.max( firstIndex, secondIndex );
		
		//Compute the number of pages between the pages referenced by the begin and end index
		final int num_idx_between = endIndex - beginIndex - 1;
		
		//Decide on what to do with the in-between page indexes
		if( num_idx_between > 0 ) {
			if( (num_idx_between == 1 ) || (num_idx_between == 2 ) ) {
				//If there are 1 or 2 pages between the begin
				//and end index then we only add one dummy  
				indexes.add( DUMMY_PAGE_INDEX );
			} else {
				//In this case there are more than two pages between
				//the begin page and the end page, then  we want to
				//be able to navigate into the middle of the interval
				//[beginIndex, endIndex]. I.e. add more stuff :-) 
				indexes.add( DUMMY_PAGE_INDEX );
				indexes.add( (int) Math.ceil(((float)( num_idx_between )) / 2) + beginIndex );
				indexes.add( DUMMY_PAGE_INDEX );
			}
		}
	}
}
