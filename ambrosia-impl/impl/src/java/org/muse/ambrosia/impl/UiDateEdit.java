/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Regents of the University of Michigan & Foothill College, ETUDES Project
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
import org.muse.ambrosia.api.DateEdit;
import org.muse.ambrosia.api.Decision;
import org.muse.ambrosia.api.Message;
import org.muse.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiDateEdit implements DateEdit.
 */
public class UiDateEdit extends UiComponent implements DateEdit
{
	/** The decision that controls if the field should get on-load focus. */
	protected Decision focusDecision = null;

	/** an icon for the display. */
	protected String icon = "!/ambrosia_library/icons/date.png";

	/** The alt text for the icon. */
	protected Message iconAlt = new UiMessage().setMessage("date-alt");

	/** Icon for showing invalid. */
	protected String invalidIcon = "!/ambrosia_library/icons/warning.png";

	/** The message selector for the invalid dismiss message. */
	protected Message invalidOk = new UiMessage().setMessage("ok");

	/** The number of columns per row for the box. */
	protected int numCols = 17;

	/** The number of rows for the text box. */
	protected int numRows = 1;

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

	/** The message for the validation alert. */
	protected Message validationMsg = new UiMessage().setMessage("date-edit-validate");

	/**
	 * No-arg constructor.
	 */
	public UiDateEdit()
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
	protected UiDateEdit(UiServiceImpl service, Element xml)
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

		// icon
		String icon = StringUtil.trimToNull(xml.getAttribute("icon"));
		if (icon != null) this.icon = icon;

		// icon
		settingsXml = XmlHelper.getChildElementNamed(xml, "icon");
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
			value = StringUtil.trimToZero(this.propertyReference.read(context, focus));
		}

		if (onEmptyAlert)
		{
			// this will become visible if a submit happens and the validation fails
			response.println("<div class=\"ambrosiaAlert\" style=\"display:none\" id=\"alert_" + id + "\">"
					+ this.onEmptyAlertMsg.getMessage(context, focus) + "</div>");

			// this marks the field as required
			// response.println("<span class=\"reqStarInline\">*</span>");
		}

		// the validation failure message
		String failureMsg = this.validationMsg.getMessage(context, focus);

		// the "failure" panel shown if requirements are not met
		response.println("<div class=\"ambrosiaConfirmPanel\" style=\"display:none; left:0px; top:0px; width:340px; height:120px\" id=\"failure_"
				+ id + "\">");
		response.println("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>");
		response.println("<td colspan=\"2\" style=\"padding:1em; white-space:normal; line-height:1em; \" align=\"left\">" + failureMsg + "</td>");
		response.println("</tr><tr>");
		response.println("<td style=\"padding:1em\" align=\"left\"><input type=\"button\" value=\"" + this.invalidOk.getMessage(context, focus)
				+ "\" onclick=\"hideConfirm('failure_" + id + "','');return false;\" " + "/></td>");
		response.println("</tr></table></div>");

		// validation function
		StringBuffer script = new StringBuffer();
		script.append("function popupInvalid_" + id + "()\n{\n  showConfirm('failure_" + id + "');\n}\n");
		context.addScript(script.toString());

		// title
		if (this.titleMessage != null)
		{
			response.print("<label class=\"ambrosiaComponentTitle\" for=\"" + id + "\">");
			response.print(this.titleMessage.getMessage(context, focus));
			response.println("</label>");
		}

		// icon text
		String alt = "";
		if (this.iconAlt != null)
		{
			alt = this.iconAlt.getMessage(context, focus);
		}

		// edit field
		response.print("<span style=\"white-space:nowrap;\"><input style=\"font-size:.8em;\" type=\"text\" id=\"" + id + "\" name=\"" + id + "\" size=\""
				+ Integer.toString(this.numCols) + "\" value=\"" + value + "\"" + (readOnly ? " disabled=\"disabled\"" : "")
				+ " onchange=\"ambrosiaDateChange(this, 'invalid_" + id + "');\"" + " />");
		if (this.icon != null)
		{
			// for the date picker popup
			context.addScript("function popupPicker_" + id + "()\n{\n  ambrosiaPopupDate(\"" + id + "\");\n}\n");
			response.print("<a href=\"#\" onclick=\"popupPicker_" + id + "();return false;\"><img src=\"" + context.getUrl(this.icon) + "\" alt=\""
					+ alt + "\" title=\"" + alt + "\" /></a>");
		}

		// validate failure alert (will display:inline when made visible)
		response.print("<div style=\"display:none\" id=\"invalid_" + id + "\">");
		response.print("<a href=\"#\" onclick=\"popupInvalid_" + id + "();return false;\" title=\"" + failureMsg + "\">");
		response.print("<img style=\"vertical-align:text-bottom;\" src=\"" + context.getUrl(this.invalidIcon) + "\" />");
		response.print("</a></div>");

		response.println("</span>");

		context.editComponentRendered(id);

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

		// pre-validate
		context.addScript("ambrosiaValidateDate(document.getElementById('" + id + "'), 'invalid_" + id + "');");

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public DateEdit setFocus(Decision decision)
	{
		this.focusDecision = decision;

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public DateEdit setIcon(String icon, String selector, PropertyReference... references)
	{
		this.icon = icon;
		this.iconAlt = new UiMessage().setMessage(selector, references);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public DateEdit setOnEmptyAlert(Decision decision, String selector, PropertyReference... references)
	{
		this.onEmptyAlertDecision = decision;
		this.onEmptyAlertMsg = new UiMessage().setMessage(selector, references);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public DateEdit setProperty(PropertyReference propertyReference)
	{
		this.propertyReference = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public DateEdit setReadOnly(Decision decision)
	{
		this.readOnly = decision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public DateEdit setTitle(String selector, PropertyReference... references)
	{
		this.titleMessage = new UiMessage().setMessage(selector, references);
		return this;
	}
}
