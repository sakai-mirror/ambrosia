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
import org.muse.ambrosia.api.Decision;
import org.muse.ambrosia.api.Message;
import org.muse.ambrosia.api.PropertyReference;
import org.muse.ambrosia.api.Selection;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiSelection presents a selection for the user to choose or not.<br />
 * The text can be either a property reference or a message.
 */
public class UiSelection extends UiComponent implements Selection
{
	/** The value we use if the user does not selecet the selection. */
	protected String notSelectedValue = "false";

	/**
	 * The PropertyReference for encoding and decoding this selection - this is what will be updated with the end-user's selection choice, and what
	 * value seeds the display.
	 */
	protected PropertyReference propertyReference = null;

	/** The read only decision. */
	protected Decision readOnly = null;

	/** The value we find if the user selects the selection. */
	protected String selectedValue = "true";

	/** The message that will provide title text. */
	protected Message titleMessage = null;

	/**
	 * No-arg constructor.
	 */
	public UiSelection()
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
	protected UiSelection(UiServiceImpl service, Element xml)
	{
		// component stuff
		super(service, xml);

		// short form for title - attribute "title" as the selector
		String title = StringUtil.trimToNull(xml.getAttribute("title"));
		if (title != null)
		{
			setTitle(title);
		}

		// short for model
		String model = StringUtil.trimToNull(xml.getAttribute("model"));
		if (model != null)
		{
			PropertyReference pRef = service.newPropertyReference().setReference(model);
			setProperty(pRef);
		}

		// selected value
		String value = StringUtil.trimToNull(xml.getAttribute("value"));
		if (value != null) this.selectedValue = value;

		// title
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "title");
		if (settingsXml != null)
		{
			// let Message parse this
			this.titleMessage = new UiMessage(service, settingsXml);
		}

		// model
		settingsXml = XmlHelper.getChildElementNamed(xml, "model");
		if (settingsXml != null)
		{
			PropertyReference pRef = service.parsePropertyReference(settingsXml);
			if (pRef != null) setProperty(pRef);
		}

		// read only
		settingsXml = XmlHelper.getChildElementNamed(xml, "readOnly");
		if (settingsXml != null)
		{
			this.readOnly = service.parseDecisions(settingsXml);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void render(Context context, Object focus)
	{
		// included?
		if (!isIncluded(context, focus)) return;

		// read only?
		boolean readOnly = false;
		if (this.readOnly != null)
		{
			readOnly = this.readOnly.decide(context, focus);
		}

		// generate some ids
		int idRoot = context.getUniqueId();
		String id = this.getClass().getSimpleName() + "_" + idRoot;
		String decodeId = "decode_" + idRoot;

		PrintWriter response = context.getResponseWriter();

		// read the current value
		String value = "false";
		if (this.propertyReference != null)
		{
			value = this.propertyReference.read(context, focus);
		}

		// convert to boolean
		boolean checked = Boolean.parseBoolean(value);

		response.println("<div class=\"ambrosiaSelection\">");

		// the check box
		response.println("<input type=\"checkbox\" name=\"" + id + "\" id=\"" + id + "\" value=\"" + this.selectedValue + "\" "
				+ (checked ? "CHECKED" : "") + (readOnly ? " disabled=\"disabled\"" : "") + " />");

		// the decode directive
		if ((this.propertyReference != null) && (!readOnly))
		{
			response.println("<input type=\"hidden\" name=\"" + decodeId + "\" value =\"" + id + "\" />" + "<input type=\"hidden\" name=\"" + "prop_"
					+ decodeId + "\" value=\"" + this.propertyReference.getFullReference(context) + "\" />" + "<input type=\"hidden\" name=\""
					+ "null_" + decodeId + "\" value=\"" + this.notSelectedValue + "\" />");
		}

		// title after
		if (this.titleMessage != null)
		{
			response.println("<label for=\"" + id + "\">");
			response.println(this.titleMessage.getMessage(context, focus));
			response.println("</label>");
		}

		response.println("</div>");
	}

	/**
	 * {@inheritDoc}
	 */
	public Selection setProperty(PropertyReference propertyReference)
	{
		this.propertyReference = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Selection setReadOnly(Decision decision)
	{
		this.readOnly = decision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Selection setSelectedValue(String value)
	{
		this.selectedValue = value;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Selection setTitle(String selector, PropertyReference... references)
	{
		this.titleMessage = new UiMessage().setMessage(selector, references);
		return this;
	}
}
