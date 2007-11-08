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
import org.muse.ambrosia.api.HtmlEdit;
import org.muse.ambrosia.api.Message;
import org.muse.ambrosia.api.PropertyReference;
import org.muse.ambrosia.api.TextEdit;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Element;

/**
 * UiHtmlEdit implements HtmlEdit.
 */
public class UiHtmlEdit extends UiComponent implements HtmlEdit
{
	/** The decision that controls if the field should get on-load focus. */
	protected Decision focusDecision = null;

	/** The number of columns per row for the box. */
	protected int numCols = 50;

	/** The number of rows for the text box. */
	protected int numRows = 4;

	/** The decision to control the onEmptyAlert. */
	protected Decision onEmptyAlertDecision = null;

	/** The message for the onEmptyAlert. */
	protected Message onEmptyAlertMsg = null;

	/**
	 * The PropertyReference for encoding and decoding this selection - this is what will be updated with the end-user's text edit, and what value
	 * seeds the display.
	 */
	protected PropertyReference propertyReference = null;

	/** The read-only decision. */
	protected Decision readOnly = null;

	/** The message that will provide title text. */
	protected Message titleMessage = null;

	/**
	 * No-arg constructor.
	 */
	public UiHtmlEdit()
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
	protected UiHtmlEdit(UiServiceImpl service, Element xml)
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

		// size
		try
		{
			int rows = Integer.parseInt(xml.getAttribute("rows"));
			int cols = Integer.parseInt(xml.getAttribute("cols"));
			setSize(rows, cols);
		}
		catch (Throwable ignore)
		{
		}

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

		// onEmptyAlert
		settingsXml = XmlHelper.getChildElementNamed(xml, "onEmptyAlert");
		if (settingsXml != null)
		{
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "message");
			if (innerXml != null)
			{
				this.onEmptyAlertMsg = new UiMessage(service, innerXml);
			}

			this.onEmptyAlertDecision = service.parseDecisions(settingsXml);
		}

		// read only shortcut
		String readOnly = StringUtil.trimToNull(xml.getAttribute("readOnly"));
		if ((readOnly != null) && ("TRUE".equals(readOnly)))
		{
			this.readOnly = new UiDecision().setProperty(new UiConstantPropertyReference().setValue("true"));
		}

		// read only
		settingsXml = XmlHelper.getChildElementNamed(xml, "readOnly");
		if (settingsXml != null)
		{
			this.readOnly = service.parseDecisions(settingsXml);
		}

		// focus
		settingsXml = XmlHelper.getChildElementNamed(xml, "focus");
		if (settingsXml != null)
		{
			this.focusDecision = service.parseDecisions(settingsXml);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		// included?
		if (!isIncluded(context, focus)) return false;

		// read only?
		boolean readOnly = false;
		if (this.readOnly != null)
		{
			readOnly = this.readOnly.decide(context, focus);
		}

		// alert if empty at submit?
		boolean onEmptyAlert = false;
		if (this.onEmptyAlertMsg != null)
		{
			onEmptyAlert = true;
			if (this.onEmptyAlertDecision != null)
			{
				onEmptyAlert = this.onEmptyAlertDecision.decide(context, focus);
			}
		}

		PrintWriter response = context.getResponseWriter();

		// set some ids
		int idRoot = context.getUniqueId();
		String id = this.getClass().getSimpleName() + "_" + idRoot;
		String decodeId = "decode_" + idRoot;

		// read the current value
		String value = "";
		if (this.propertyReference != null)
		{
			value = this.propertyReference.read(context, focus);
		}

		// single line
		if (numRows == 1)
		{
			if (onEmptyAlert)
			{
				// this will become visible if a submit happens and the validation fails
				response.println("<div class=\"ambrosiaAlert\" style=\"display:none\" id=\"alert_" + id + "\">"
						+ Validator.escapeHtml(this.onEmptyAlertMsg.getMessage(context, focus)) + "</div>");

				// this marks the field as required
				// response.println("<span class=\"reqStarInline\">*</span>");
			}

			// title
			if (this.titleMessage != null)
			{
				response.println("<label class=\"ambrosiaComponentTitle\" for=\"" + id + "\">");
				response.println(this.titleMessage.getMessage(context, focus));
				response.println("</label>");
			}

			response.println("<input type=\"text\" id=\"" + id + "\" name=\"" + id + "\" size=\"" + Integer.toString(numCols) + "\" value=\""
					+ Validator.escapeHtml(value) + "\"" + (readOnly ? " disabled=\"disabled\"" : "") + " />");

			context.editComponentRendered(id);
		}

		// or multi line
		else
		{
			if (onEmptyAlert)
			{
				// this will become visible if a submit happens and the validation fails
				response.println("<div class=\"ambrosiaAlert\" style=\"display:none\" id=\"alert_" + id + "\">"
						+ Validator.escapeHtml(this.onEmptyAlertMsg.getMessage(context, focus)) + "</div>");

				// this marks the field as required
				// response.println("<span class=\"reqStarInline\">*</span>");
			}

			if (this.titleMessage != null)
			{
				response.println("<label class=\"ambrosiaComponentTitle\" for=\"" + id + "\">");
				response.println(this.titleMessage.getMessage(context, focus));
				response.println("</label>");
			}

			response.println("<textarea " + (readOnly ? " class=\"ambrosiaTextEditDisabled\"" : "") + "id=\"" + id + "\" name=\"" + id + "\" cols="
					+ Integer.toString(numCols) + " rows=" + Integer.toString(numRows) + (readOnly ? " disabled=\"disabled\"" : "") + ">");
			response.print(Validator.escapeHtmlTextarea(value));
			response.println("</textarea>");
		}

		// the decode directive
		if ((this.propertyReference != null) && (!readOnly))
		{
			response.println("<input type=\"hidden\" name=\"" + decodeId + "\" value =\"" + id + "\" />" + "<input type=\"hidden\" name=\"" + "prop_"
					+ decodeId + "\" value=\"" + this.propertyReference.getFullReference(context) + "\" />" + "<input type=\"hidden\" name=\""
					+ "type_" + decodeId + "\" value=\"" + this.propertyReference.getType() + "\" />");
		}

		// for onEmptyAlert, add some client-side validation
		if ((onEmptyAlert) && (!readOnly))
		{
			context.addValidation("	if (trim(document.getElementById('" + id + "').value) == \"\")\n" + "	{\n"
					+ "		if (document.getElementById('alert_" + id + "').style.display == \"none\")\n" + "		{\n"
					+ "			document.getElementById('alert_" + id + "').style.display = \"\";\n" + "			rv=false;\n" + "		}\n" + "	}\n");
		}

		// for on-load focus
		if ((!readOnly) && (this.focusDecision != null) && (this.focusDecision.decide(context, focus)))
		{
			// add the field name / id to the focus path
			context.addFocusId(id);
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public HtmlEdit setFocus(Decision decision)
	{
		this.focusDecision = decision;

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public HtmlEdit setOnEmptyAlert(Decision decision, String selector, PropertyReference... references)
	{
		this.onEmptyAlertDecision = decision;
		this.onEmptyAlertMsg = new UiMessage().setMessage(selector, references);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public HtmlEdit setProperty(PropertyReference propertyReference)
	{
		this.propertyReference = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public HtmlEdit setReadOnly(Decision decision)
	{
		this.readOnly = decision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public HtmlEdit setSize(int rows, int cols)
	{
		this.numRows = rows;
		this.numCols = cols;

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public HtmlEdit setTitle(String selector, PropertyReference... references)
	{
		this.titleMessage = new UiMessage().setMessage(selector, references);
		return this;
	}
}
