/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Regents of the University of Michigan & Foothill College, ETUDES Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.muse.ambrosia.api;

/**
 * PagePosition models paging position.
 */
public interface Paging
{
	/**
	 * Access the item number (1 based) of the first item of the current page.
	 * 
	 * @return The item number (1 based) of the first item of the current page.
	 */
	Integer getCurFirstItem();

	/**
	 * Access the item number (1 based) of the last item of the current page.
	 * 
	 * @return The item number (1 based) of the last item of the current page.
	 */
	Integer getCurLastItem();

	/**
	 * Access the current page.
	 * 
	 * @return The current page.
	 */
	Integer getCurrent();

	/**
	 * Get a new Paging object representing the first page.
	 * 
	 * @return A new Paging object representing the first page.
	 */
	Paging getFirst();

	/**
	 * Check if the current page is set to the first page.
	 * 
	 * @return TRUE if the current page is set to the first page, FALSE if not.
	 */
	Boolean getIsFirst();

	/**
	 * Check if the current page is set to the last page.
	 * 
	 * @return TRUE if the current page is set to the last page, FALSE if not.
	 */
	Boolean getIsLast();

	/**
	 * Get a new Paging object representing the last page.
	 * 
	 * @return A new Paging object representing the last page.
	 */
	Paging getLast();

	/**
	 * Access the max number of pages.
	 * 
	 * @return The max number of pages.
	 */
	Integer getMax();

	/**
	 * Access the max number of items.
	 * 
	 * @return The max number of items.
	 */
	Integer getMaxItems();

	/**
	 * Get a new Paging object representing the next page.
	 * 
	 * @return A new Paging object representing the next page.
	 */
	Paging getNext();

	/**
	 * Get a new Paging object representing the prev page.
	 * 
	 * @return A new Paging object representing the prev page.
	 */
	Paging getPrev();

	/**
	 * Access the items-per-page size.
	 * 
	 * @return The items-per-page size.
	 */
	Integer getSize();

	/**
	 * Set the current page.
	 * 
	 * @param current
	 *        The current page.
	 */
	void setCurrent(Integer current);

	/**
	 * Set the max number of items.
	 * 
	 * @param max
	 *        The max number of items.
	 */
	void setMaxItems(Integer max);

	/**
	 * Set the items-per-page size.
	 * 
	 * @param size
	 *        The items-per-page size.
	 */
	void setPageSize(Integer size);
}
