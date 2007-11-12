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

import org.muse.ambrosia.api.Context;
import org.muse.ambrosia.api.Evaluation;
import org.muse.ambrosia.api.Message;
import org.muse.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Element;

/**
 * UiEvaluation implements Evaluation.
 */
public class UiEvaluation extends UiText implements Evaluation
{
	/** an icon for the display. */
	protected String icon = null;

	/** The alt text for the icon. */
	protected Message iconAlt = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiEvaluation()
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
	protected UiEvaluation(UiServiceImpl service, Element xml)
	{
		// do the text stuff
		super(service, xml);

		// icon
		String icon = StringUtil.trimToNull(xml.getAttribute("icon"));
		if (icon != null) this.icon = icon;

		// icon
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "icon");
		if (settingsXml != null)
		{
			icon = StringUtil.trimToNull(settingsXml.getAttribute("icon"));
			if (icon != null) this.icon = icon;

			String selector = StringUtil.trimToNull(settingsXml.getAttribute("message"));
			if (selector != null)
			{
				this.iconAlt = new UiMessage().setMessage(selector);
			}

			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "message");
			if (innerXml != null)
			{
				this.iconAlt = new UiMessage(service, innerXml);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		// included?
		if (!isIncluded(context, focus)) return false;

		String msg = null;
		String titleMsg = null;
		if (this.message != null)
		{
			msg = this.message.getMessage(context, focus);
		}
		if (this.titleMessage != null)
		{
			titleMsg = this.titleMessage.getMessage(context, focus);
		}

		if ((msg == null) && (titleMsg == null)) return false;

		PrintWriter response = context.getResponseWriter();

		// title
		if (titleMsg != null)
		{
			response.print("<div class=\"ambrosiaComponentTitle\">");
			response.print(titleMsg);
			response.println("</div>");
		}

		String alt = "";
		if (this.iconAlt != null)
		{
			alt = this.iconAlt.getMessage(context, focus);
		}

		if (msg != null)
		{
			response.println("<div class =\"ambrosiaEvaluation\">"
					+ ((this.icon != null) ? "<img src=\"" + context.getUrl(this.icon) + "\" alt=\"" + alt + "\" title=\"" + alt + "\" />" : "")
					+ msg + "</div>");
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Evaluation setIcon(String icon, String selector, PropertyReference... references)
	{
		this.icon = icon;
		this.iconAlt = new UiMessage().setMessage(selector, references);

		return this;
	}
}
