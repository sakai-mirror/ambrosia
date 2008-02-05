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

package org.muse.ambrosia.impl;

import org.muse.ambrosia.api.Values;

/**
 * UiValues implements Values
 */
public class UiValues implements Values
{
	protected String[] values = new String[0];

	/**
	 * {@inheritDoc}
	 */
	public String[] getValues()
	{
		return values;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValues(String[] values)
	{
		if (values == null)
		{
			this.values = new String[0];
		}
		else
		{
			this.values = values;
		}
	}
}
