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
import java.util.ArrayList;
import java.util.List;

import org.muse.ambrosia.api.Component;
import org.muse.ambrosia.api.Context;
import org.muse.ambrosia.api.Decision;
import org.muse.ambrosia.api.EntityDisplayRow;
import org.muse.ambrosia.api.Message;
import org.muse.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * UiEntityDisplayRow implements EntityDisplayRow.
 */
public class UiEntityDisplayRow implements EntityDisplayRow
{
	/** Components contained in this container. */
	protected List<Component> contained = new ArrayList<Component>();

	/** The include decision. */
	protected Decision included = null;

	/** The PropertyReference for this row. */
	protected PropertyReference propertyReference = null;

	/** The message for the column title. */
	protected Message title = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiEntityDisplayRow()
	{
	}

	/**
	 * Construct from a dom element.
	 * 
	 * @param service
	 *        The UiService.
	 * @param xml
	 *        The dom element.
	 */
	protected UiEntityDisplayRow(UiServiceImpl service, Element xml)
	{
		// included decisions
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "included");
		if (settingsXml != null)
		{
			this.included = service.parseDecisions(settingsXml);
		}

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

		settingsXml = XmlHelper.getChildElementNamed(xml, "title");
		if (settingsXml != null)
		{
			// let Message parse this
			this.title = new UiMessage(service, settingsXml);
		}

		settingsXml = XmlHelper.getChildElementNamed(xml, "model");
		if (settingsXml != null)
		{
			PropertyReference pRef = service.parsePropertyReference(settingsXml);
			if (pRef != null) setProperty(pRef);
		}

		// components
		settingsXml = XmlHelper.getChildElementNamed(xml, "container");
		if (settingsXml != null)
		{
			NodeList contained = settingsXml.getChildNodes();
			for (int i = 0; i < contained.getLength(); i++)
			{
				Node node = contained.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element componentXml = (Element) node;

					// create a component from each node in the container
					Component c = service.parseComponent(componentXml);
					if (c != null)
					{
						this.contained.add(c);
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityDisplayRow add(Component component)
	{
		this.contained.add(component);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayText(Context context, Object entity)
	{
		// set the context to capture instead of adding to the output
		context.setCollecting();

		// start with the property
		if (getProperty() != null)
		{
			PrintWriter response = context.getResponseWriter();
			String value = getProperty().read(context, entity);
			response.print(value);
		}

		// render the contained
		for (Component c : this.contained)
		{
			c.render(context, entity);
		}

		// get the captured text, resetting to output mode
		String rv = context.getCollected();

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyReference getProperty()
	{
		return this.propertyReference;
	}

	/**
	 * {@inheritDoc}
	 */
	public Message getTitle()
	{
		return this.title;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean included(Context context, Object focus)
	{
		if ((this.included != null) && (!this.included.decide(context, focus))) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityDisplayRow setIncluded(Decision decision)
	{
		this.included = decision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityDisplayRow setProperty(PropertyReference propertyReference)
	{
		this.propertyReference = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityDisplayRow setTitle(String selector, PropertyReference... references)
	{
		this.title = new UiMessage().setMessage(selector, references);
		return this;
	}
}
