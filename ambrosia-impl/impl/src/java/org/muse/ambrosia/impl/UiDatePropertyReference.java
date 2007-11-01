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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.muse.ambrosia.api.Context;
import org.muse.ambrosia.api.DatePropertyReference;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Element;

/**
 * UiDatePropertyReference implements DatePropertyReference.
 */
public class UiDatePropertyReference extends UiPropertyReference implements DatePropertyReference
{
	/** If set, split the date to date on top, time below. */
	protected boolean multiLine = false;

	/** If set, use the SHORT instad of MEDIUM format. */
	protected boolean shortFormat = false;

	/**
	 * No-arg constructor.
	 */
	public UiDatePropertyReference()
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
	protected UiDatePropertyReference(UiServiceImpl service, Element xml)
	{
		// do the property reference stuff
		super(service, xml);

		// two line
		String twoLine = StringUtil.trimToNull(xml.getAttribute("twoLine"));
		if ((twoLine != null) && (twoLine.equals("TRUE"))) setTwoLine();

		String fmt = StringUtil.trimToNull(xml.getAttribute("format"));
		if ((fmt != null) && (fmt.equals("SHORT"))) setShort();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "date";
	}

	/**
	 * {@inheritDoc}
	 */
	public DatePropertyReference setShort()
	{
		this.shortFormat = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public DatePropertyReference setTwoLine()
	{
		this.multiLine = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	protected String format(Context context, Object value)
	{
		if (value instanceof Date)
		{
			Date date = (Date) value;
			// TODO: use the end-user's locale and time zone prefs

			// pick format
			int fmt = DateFormat.MEDIUM;
			if (shortFormat) fmt = DateFormat.SHORT;

			if (multiLine)
			{
				DateFormat dateFormat = DateFormat.getDateInstance(fmt);
				DateFormat timeFormat = DateFormat.getTimeInstance(fmt);

				return "<span style=\"white-space: nowrap;\">" + Validator.escapeHtml(dateFormat.format(date))
						+ "</span><br /><span style=\"white-space: nowrap;\">" + Validator.escapeHtml(timeFormat.format(date)) + "</span>";
			}
			else
			{
				DateFormat format = DateFormat.getDateTimeInstance(fmt, fmt);

				return Validator.escapeHtml(format.format(date));
			}
		}

		return super.format(context, value);
	}

	/**
	 * {@inheritDoc}
	 */
	protected String unFormat(String value)
	{
		if (value == null) return null;

		// assume single line, both date and time

		// pick format
		int fmt = DateFormat.MEDIUM;
		if (shortFormat) fmt = DateFormat.SHORT;

		// TODO: use the end-user's locale and time zone prefs
		DateFormat format = DateFormat.getDateTimeInstance(fmt, fmt);
		try
		{
			Date date = format.parse(value);
			return Long.toString(date.getTime());
		}
		catch (ParseException e)
		{
			return null;
		}
	}
}
