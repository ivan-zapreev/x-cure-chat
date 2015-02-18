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

import java.util.Comparator;
import java.util.List;

/**
 * @author zapreevis
 * Implement the quick sorting algorithms, I took this from some online
 * resource and changed it to work with the lists and Comparator interface.
 */
public class QuickSort<Type> {
	
	private final Comparator<Type> comparator;
	
	public QuickSort( final Comparator<Type> comparator) {
		this.comparator = comparator;
	}
	
    /**
     * Quicksort algorithm.
     * @param data an array of Comparable items.
     */
    public void quicksort( List<Type> data ) {
        quicksort( data, 0, data.size() - 1 );
    }
    
    private static final int CUTOFF = 10;
    
    /**
     * Internal quicksort method that makes recursive calls.
     * Uses median-of-three partitioning and a cutoff of 10.
     * @param data an array of Comparable items.
     * @param low the left-most index of the subarray.
     * @param high the right-most index of the subarray.
     */
    private void quicksort( List<Type> data, int low, int high ) {
        if( low + CUTOFF > high )
            insertionSort( data, low, high );
        else {
            // Sort low, middle, high
            int middle = ( low + high ) / 2;
            if( comparator.compare( data.get( middle ), data.get( low ) ) < 0 ) {
                swapReferences( data, low, middle );
            }
            if( comparator.compare( data.get( high ),data.get( low ) ) < 0 ) {
                swapReferences( data, low, high );
            }
            if( comparator.compare( data.get( high ), data.get( middle ) ) < 0 ) {
                swapReferences( data, middle, high );
            }
            
            // Place pivot at position high - 1
            swapReferences( data, middle, high - 1 );
            Type pivot = data.get( high - 1 );
            
            // Begin partitioning
            int i, j;
            for( i = low, j = high - 1; ; ) {
                while( comparator.compare( data.get( ++i ),  pivot ) < 0 ) {
                    ;
                }
                while( comparator.compare( pivot, data.get( --j ) ) < 0 ) {
                    ;
                }
                if( i >= j ) {
                    break;
                }
                swapReferences( data, i, j );
            }
            
            // Restore pivot
            swapReferences( data, i, high - 1 );
            
            quicksort( data, low, i - 1 );    // Sort small elements
            quicksort( data, i + 1, high );   // Sort large elements
        }
    }
    
    /**
     * Method to swap to elements in an array.
     * @param data an array of objects.
     * @param index1 the index of the first object.
     * @param index2 the index of the second object.
     */
    public final void swapReferences( List<Type> data, int index1, int index2 ) {
    	Type tmp = data.get( index1 );
        data.set( index1, data.get( index2 ) );
        data.set( index2, tmp );
    }
    
    
    /**
     * Internal insertion sort routine for subarrays
     * that is used by quicksort.
     * @param data an array of Comparable items.
     * @param low the left-most index of the subarray.
     * @param n the number of items to sort.
     */
    private void insertionSort( List<Type> data, int low, int high ) {
        for( int p = low + 1; p <= high; p++ ) {
            Type tmp = data.get( p );
            int j;
            
            for( j = p; j > low && comparator.compare( tmp, data.get( j - 1 ) ) < 0; j-- ) {
                data.set( j, data.get( j - 1 ) );
            }
            data.set( j, tmp );
        }
    }
}
