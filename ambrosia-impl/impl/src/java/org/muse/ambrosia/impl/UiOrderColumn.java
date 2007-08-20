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
import org.muse.ambrosia.api.OrderColumn;
import org.muse.ambrosia.api.PropertyReference;
import org.muse.ambrosia.api.SelectionColumn;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Element;

/**
 * UiOrderColumn implements OrderColumn.
 */
public class UiOrderColumn extends UiEntityListColumn implements OrderColumn
{
	/** The PropertyReference for decoding this column - this is what will be updated with the selected value(s). */
	protected PropertyReference propertyReference = null;

	/** The PropertyReference for encoding this column - this is how the entity will be identified. */
	protected PropertyReference valuePropertyReference = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiOrderColumn()
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
	protected UiOrderColumn(UiServiceImpl service, Element xml)
	{
		// entity list column stuff
		super(service, xml);

		// short for model
		String model = StringUtil.trimToNull(xml.getAttribute("model"));
		if (model != null)
		{
			PropertyReference pRef = service.newPropertyReference().setReference(model);
			setProperty(pRef);
		}

		// model
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "model");
		if (settingsXml != null)
		{
			PropertyReference pRef = service.parsePropertyReference(settingsXml);
			if (pRef != null) setProperty(pRef);
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
	public String getDisplayText(Context context, Object entity, int row, int idRoot)
	{
		// generate some ids
		String id = this.getClass().getSimpleName() + "_" + idRoot;

		// read only?
		boolean readOnly = false;

		// alert if empty at submit?
		boolean onEmptyAlert = false;

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

		StringBuffer rv = new StringBuffer();

		// form a row-unique id, using the overall id and the row
		String uid = id + row;

		rv.append("<input type=\"hidden\" name=\"" + id + "\" value=\"" + value + "\" />");

		// TODO: icon
		rv.append("<a href=\"#\" onclick=\"this.focus(); return false;\" onkeypress=\"return ambrosiaTableReorder(event, this);\">link</a>");

		return rv.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getOneTimeText(Context context, Object focus, int idRoot, int numRows)
	{
		// generate some ids
		String id = this.getClass().getSimpleName() + "_" + idRoot;
		String decodeId = "decode_" + idRoot;

		StringBuffer rv = new StringBuffer();

		// we need one hidden field to direct the decoding of all the columns
		rv.append("<input type=\"hidden\" name=\"" + decodeId + "\" value =\"" + id + "\" />" + "<input type=\"hidden\" name=\"" + "prop_" + decodeId
				+ "\" value=\"" + this.propertyReference.getFullReference(context) + "\" />");

		return rv.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public OrderColumn setProperty(PropertyReference propertyReference)
	{
		this.propertyReference = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public OrderColumn setValueProperty(PropertyReference propertyReference)
	{
		this.valuePropertyReference = propertyReference;
		return this;
	}
}
