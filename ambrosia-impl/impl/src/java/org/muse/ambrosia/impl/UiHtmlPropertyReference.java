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

import org.muse.ambrosia.api.Context;
import org.muse.ambrosia.api.HtmlPropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiHtmlPropertyReference implements HtmlPropertyReference
 */
public class UiHtmlPropertyReference extends UiPropertyReference implements HtmlPropertyReference
{
	protected int maxChars = -1;

	/**
	 * No-arg constructor.
	 */
	public UiHtmlPropertyReference()
	{
	}

	/**
	 * Construct from a dom element.
	 * 
	 * @param service
	 *        the UiService.
	 * @param xml
	 *        The dom element.
	 */
	protected UiHtmlPropertyReference(UiServiceImpl service, Element xml)
	{
		// do the property reference stuff
		super(service, xml);

		// max length
		String max = StringUtil.trimToNull(xml.getAttribute("max"));
		if (max != null)
		{
			try
			{
				setMaxLength(Integer.parseInt(max));
			}
			catch (NumberFormatException e)
			{
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "html";
	}

	/**
	 * {@inheritDoc}
	 */
	public String read(Context context, Object focus)
	{
		String value = super.read(context, focus);
		if (value == null) return null;

		// if missing, don't do special treatment
		if (value.equals(missingValue(context))) return value;

		// truncate if desired and needed
		if (this.maxChars > -1)
		{
			if (value.length() > this.maxChars)
			{
				value = value.substring(0, this.maxChars) + "...";
			}
		}

		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	public HtmlPropertyReference setMaxLength(int maxChars)
	{
		this.maxChars = maxChars;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	protected String unFormat(String value)
	{
		// remove surrounding <p> </p>
		if (value.startsWith("<p>")) value = value.substring(3);
		if (value.endsWith("</p>")) value = value.substring(0, value.length()-4);
		
		// get rid of an html blank
		if (value.equals("&nbsp;")) value = null;

		return value;
	}
}
