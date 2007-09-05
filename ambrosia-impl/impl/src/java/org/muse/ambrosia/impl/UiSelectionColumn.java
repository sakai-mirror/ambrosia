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

import java.util.Collection;

import org.muse.ambrosia.api.Context;
import org.muse.ambrosia.api.Decision;
import org.muse.ambrosia.api.Message;
import org.muse.ambrosia.api.PropertyReference;
import org.muse.ambrosia.api.SelectionColumn;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Element;

/**
 * UiSelectionColumn implements SelectionColum.
 */
public class UiSelectionColumn extends UiEntityListColumn implements SelectionColumn
{
	/** The Message for the label text. */
	protected Message label = null;

	/** The decision to control the onEmptyAlert. */
	protected Decision onEmptyAlertDecision = null;

	/** The message for the onEmptyAlert. */
	protected Message onEmptyAlertMsg = null;

	/** The PropertyReference for decoding this column - this is what will be updated with the selected value(s). */
	protected PropertyReference propertyReference = null;

	/** The rad-only decision. */
	protected Decision readOnly = null;

	/** If true, we are selecting a single value, else we can select many values. */
	protected boolean singleSelect = true;

	/** If set, use this instead of sigleSelect to see if we are going to be single or multiple select. */
	protected Decision singleSelectDecision = null;

	/** The PropertyReference for encoding this column - this is how the entity will be identified. */
	protected PropertyReference valuePropertyReference = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiSelectionColumn()
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
	protected UiSelectionColumn(UiServiceImpl service, Element xml)
	{
		// entity list column stuff
		super(service, xml);

		// label
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "label");
		if (settingsXml != null)
		{
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "message");
			if (innerXml != null) this.label = new UiMessage(service, innerXml);
		}

		// multiple select
		String multiple = StringUtil.trimToNull(xml.getAttribute("multiple"));
		if ((multiple != null) && ("TRUE".equals(multiple))) setMultiple();

		// single select
		String single = StringUtil.trimToNull(xml.getAttribute("single"));
		if ((single != null) && ("TRUE".equals(multiple))) setSingle();

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

		// short for model
		String model = StringUtil.trimToNull(xml.getAttribute("model"));
		if (model != null)
		{
			PropertyReference pRef = service.newPropertyReference().setReference(model);
			setProperty(pRef);
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

		// single select
		settingsXml = XmlHelper.getChildElementNamed(xml, "singleSelect");
		if (settingsXml != null)
		{
			this.singleSelectDecision = service.parseDecisions(settingsXml);
		}

		// value
		settingsXml = XmlHelper.getChildElementNamed(xml, "value");
		if (settingsXml != null)
		{
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				PropertyReference pRef = service.parsePropertyReference(innerXml);
				if (pRef != null) setValueProperty(pRef);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayText(Context context, Object entity, int row, String id)
	{
		// read only?
		boolean readOnly = false;
		if (this.readOnly != null)
		{
			readOnly = this.readOnly.decide(context, entity);
		}

		// alert if empty at submit?
		boolean onEmptyAlert = false;
		if (this.onEmptyAlertMsg != null)
		{
			onEmptyAlert = true;
			if (this.onEmptyAlertDecision != null)
			{
				onEmptyAlert = this.onEmptyAlertDecision.decide(context, entity);
			}
		}

		// read the entity id for this entity / column
		String value = null;
		if (this.valuePropertyReference != null)
		{
			value = this.valuePropertyReference.read(context, entity);
		}

		// if there's no identity refernce, use the row number
		else
		{
			value = Integer.toString(row);
		}

		// read the encode / decode property, and see if this should be seeded as selected
		boolean checked = false;
		if (this.propertyReference != null)
		{
			// read the raw value
			Object current = this.propertyReference.readObject(context, entity);
			if (current != null)
			{
				// deal with multiple value array
				if (current.getClass().isArray())
				{
					// checked if value is in there
					for (Object o : (Object[]) current)
					{
						if (o != null)
						{
							checked = value.equals(o.toString());
							if (checked) break;
						}
					}
				}

				// or a multi value collection
				else if (current instanceof Collection)
				{
					// checked if value is in there
					for (Object o : (Collection) current)
					{
						if (o != null)
						{
							checked = value.equals(o.toString());
							if (checked) break;
						}
					}
				}

				// deal with single value
				else
				{
					checked = value.equals(current.toString());
				}
			}
		}

		StringBuffer rv = new StringBuffer();

		// single select?
		boolean single = this.singleSelect;
		if (this.singleSelectDecision != null)
		{
			single = this.singleSelectDecision.decide(context, entity);
		}

		// form a row-unique id, using the overall id and the row
		String uid = id + "_" + row;

		// for single selection, use a radio set
		if (single)
		{
			rv.append("<input type=\"radio\" id=\"" + uid + "\" name=\"" + id + "\" value=\"" + value + "\" " + (checked ? "CHECKED" : "")
					+ (readOnly ? " disabled=\"disabled\"" : "") + " />");
		}

		// for multiple selection, use a checkbox set
		else
		{
			rv.append("<input type=\"checkbox\" id=\"" + uid + "\" name=\"" + id + "\" value=\"" + value + "\" " + (checked ? "CHECKED" : "")
					+ (readOnly ? " disabled=\"disabled\"" : "") + " />");
		}

		// the label
		if (this.label != null)
		{
			String labelValue = this.label.getMessage(context, entity);
			if (labelValue != null)
			{
				String label = "<label for=\"" + uid + "\">" + labelValue + "</label>";
				rv.append(label);
			}
		}

		return rv.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getOneTimeText(Context context, Object focus, String id, int numRows)
	{
		// read only?
		boolean readOnly = false;
		if (this.readOnly != null)
		{
			readOnly = this.readOnly.decide(context, focus);
		}

		if (readOnly) return "";

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

		// generate some ids
		String decodeId = "decode_" + id;

		StringBuffer rv = new StringBuffer();

		// we need one hidden field to direct the decoding of all the columns
		rv.append("<input type=\"hidden\" name=\"" + decodeId + "\" value =\"" + id + "\" />" + "<input type=\"hidden\" name=\"" + "prop_" + decodeId
				+ "\" value=\"" + this.propertyReference.getFullReference(context) + "\" />");

		// for onEmptyAlert, add some client-side validation
		if (onEmptyAlert)
		{
			if (numRows > 0)
			{
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < numRows; i++)
				{
					buf.append("!document.getElementById('" + id + "_" + Integer.toString(i) + "').checked &&");
				}
				buf.setLength(buf.length() - 3);

				context.addValidation("	if (" + buf.toString() + ")\n" + "	{\n" + "		if (document.getElementById('alert_" + id
						+ "').style.display == \"none\")\n" + "		{\n" + "			document.getElementById('alert_" + id + "').style.display = \"\";\n"
						+ "			rv=false;\n" + "		}\n" + "	}\n");
			}
		}

		return rv.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPrefixText(Context context, Object focus, String id)
	{
		// read only?
		boolean readOnly = false;
		if (this.readOnly != null)
		{
			readOnly = this.readOnly.decide(context, focus);
		}

		if (readOnly) return null;

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

		if (!onEmptyAlert) return null;

		// this will become visible if a submit happens and the validation fails
		return "<div class=\"ambrosiaAlert\" style=\"display:none\" id=\"alert_" + id + "\">"
				+ Validator.escapeHtml(this.onEmptyAlertMsg.getMessage(context, focus)) + "</div>";
	}

	/**
	 * {@inheritDoc}
	 */
	public SelectionColumn setLabel(String selector, PropertyReference... references)
	{
		this.label = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public SelectionColumn setMultiple()
	{
		this.singleSelect = false;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public SelectionColumn setOnEmptyAlert(Decision decision, String selector, PropertyReference... references)
	{
		this.onEmptyAlertDecision = decision;
		this.onEmptyAlertMsg = new UiMessage().setMessage(selector, references);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public SelectionColumn setProperty(PropertyReference propertyReference)
	{
		this.propertyReference = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public SelectionColumn setReadOnly(Decision decision)
	{
		this.readOnly = decision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public SelectionColumn setSingle()
	{
		this.singleSelect = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public SelectionColumn setSingleSelectDecision(Decision decision)
	{
		this.singleSelectDecision = decision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public SelectionColumn setValueProperty(PropertyReference propertyReference)
	{
		this.valuePropertyReference = propertyReference;
		return this;
	}
}
