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

import java.io.PrintWriter;

import org.muse.ambrosia.api.Alert;
import org.muse.ambrosia.api.Context;
import org.muse.ambrosia.api.Message;
import org.muse.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiAlert presents implements Alert.
 */
public class UiAlert extends UiComponent implements Alert
{
	/** The message for the instruction text. */
	protected Message text = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiAlert()
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
	protected UiAlert(UiServiceImpl service, Element xml)
	{
		// do the component stuff
		super(service, xml);

		// short for for text - attribute "selector" for the selector, and attribute "model" for a single reference.
		String selector = StringUtil.trimToNull(xml.getAttribute("selector"));
		String ref = StringUtil.trimToNull(xml.getAttribute("model"));
		if ((selector != null) || (ref != null))
		{
			if (ref == null)
			{
				setText(selector);
			}
			else
			{
				setText(selector, service.newPropertyReference().setReference(ref));
			}
		}

		// sub-element configuration
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "message");
		if (settingsXml != null)
		{
			// let Message parse this
			this.text = new UiMessage(service, settingsXml);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void render(Context context, Object focus)
	{
		// included?
		if (!isIncluded(context, focus)) return;

		PrintWriter response = context.getResponseWriter();

		if (this.text != null)
		{
			response.println("<div class =\"ambrosiaAlert\">" + this.text.getMessage(context, focus) + "</div>");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Alert setText(String selector, PropertyReference... references)
	{
		this.text = new UiMessage().setMessage(selector, references);
		return this;
	}
}
