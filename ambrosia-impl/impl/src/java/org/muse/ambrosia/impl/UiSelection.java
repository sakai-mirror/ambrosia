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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.muse.ambrosia.api.Component;
import org.muse.ambrosia.api.Container;
import org.muse.ambrosia.api.Context;
import org.muse.ambrosia.api.Decision;
import org.muse.ambrosia.api.Message;
import org.muse.ambrosia.api.PropertyReference;
import org.muse.ambrosia.api.RenderListener;
import org.muse.ambrosia.api.Selection;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * UiSelection presents a selection for the user to choose or not.<br />
 * The text can be either a property reference or a message.
 */
public class UiSelection extends UiComponent implements Selection
{
	/** The value we use if the user does not selecet the selection. */
	protected String notSelectedValue = "false";

	/** The orientation for multiple selection choices. */
	protected Orientation orientation = Orientation.vertical;

	/**
	 * The PropertyReference for encoding and decoding this selection - this is what will be updated with the end-user's selection choice, and what
	 * value seeds the display.
	 */
	protected PropertyReference propertyReference = null;

	/** The read only decision. */
	protected Decision readOnly = null;

	/** The value we find if the user selects the selection. */
	protected String selectedValue = "true";

	/** Containers holding dependent components to a selection, keyed by the selection value. */
	protected Map<String, Container> selectionContainers = new HashMap<String, Container>();

	/** The set of messages for multiple selection choices. */
	protected List<Message> selectionMessages = new ArrayList<Message>();

	/** The set of values for multiple selection choices. */
	protected List<String> selectionValues = new ArrayList<String>();

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

		// orientation
		String orientation = StringUtil.trimToNull(xml.getAttribute("orientation"));
		if ((orientation != null) && (orientation.equals("HORIZONTAL"))) setOrientation(Orientation.horizontal);

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

		// selection choices
		settingsXml = XmlHelper.getChildElementNamed(xml, "selectionChoices");
		if (settingsXml != null)
		{
			NodeList contained = settingsXml.getChildNodes();
			for (int i = 0; i < contained.getLength(); i++)
			{
				Node node = contained.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element innerXml = (Element) node;
					if ("selectionChoice".equals(innerXml.getTagName()))
					{
						String selector = StringUtil.trimToNull(innerXml.getAttribute("selector"));
						value = StringUtil.trimToNull(innerXml.getAttribute("value"));
						addSelection(selector, value);

						// is there a container?
						if (XmlHelper.getChildElementNamed(innerXml, "container") != null)
						{
							Container container = new UiContainer(service, innerXml);
							this.selectionContainers.put(value, container);
						}
					}
				}
			}
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
	public Selection addComponentToSelection(String value, Component component)
	{
		Container container = this.selectionContainers.get(value);
		if (container == null)
		{
			container = new UiContainer();
			this.selectionContainers.put(value, container);
		}

		container.add(component);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Selection addSelection(String selector, String value)
	{
		this.selectionValues.add(value);
		this.selectionMessages.add(new UiMessage().setMessage(selector));

		return this;
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
		String dependencyId = id + "_dependencies";

		PrintWriter response = context.getResponseWriter();

		// read the current value
		String value = "false";
		if (this.propertyReference != null)
		{
			value = this.propertyReference.read(context, focus);
		}

		response.println("<div class=\"ambrosiaSelection\">");

		if (this.selectionValues.isEmpty())
		{
			// convert to boolean
			boolean checked = Boolean.parseBoolean(value);

			// the check box
			response.println("<input type=\"checkbox\" name=\"" + id + "\" id=\"" + id + "\" value=\"" + this.selectedValue + "\" "
					+ (checked ? "CHECKED" : "") + (readOnly ? " disabled=\"disabled\"" : "") + " />");
		}

		else
		{
			final StringBuffer dependency = new StringBuffer();
			dependency.append("var " + dependencyId + "=[");
			String startingValue = null;
			boolean needDependencies = false;
			String onclick = "";
			if (!this.selectionContainers.isEmpty())
			{
				onclick = "onclick=\"ambrosiaSelectDependencies(this.value, " + dependencyId + ")\" ";
			}

			for (int i = 0; i < this.selectionValues.size(); i++)
			{
				Message msg = this.selectionMessages.get(i);
				String message = msg.getMessage(context, focus);
				String val = this.selectionValues.get(i);

				boolean selected = (value == null) ? false : value.equals(val);
				if (selected)
				{
					startingValue = val;
				}

				// the radio button
				response.println("<input " + onclick + "type=\"radio\" name=\"" + id + "\" id=\"" + id + "\" value=\"" + val + "\" "
						+ (selected ? "CHECKED" : "") + (readOnly ? " disabled=\"disabled\"" : "") + " /> " + message);

				// container of dependent components
				Container container = this.selectionContainers.get(val);
				if (container != null)
				{
					needDependencies = true;

					dependency.append("[\"" + val + "\",");
					RenderListener listener = new RenderListener()
					{
						public void componentRendered(String id)
						{
							dependency.append("\"" + id + "\",");
						}
					};

					// listen for any dependent edit components being rendered
					context.addEditComponentRenderListener(listener);

					// render the dependent components
					container.render(context, focus);

					// stop listening
					context.removeEditComponentRenderListener(listener);

					dependency.setLength(dependency.length() - 1);
					dependency.append("],");
				}

				// contained stuff will most likely include a break...
				if ((this.orientation == Orientation.vertical) && (container == null))
				{
					response.println("<br />");
				}
			}

			if (needDependencies)
			{
				dependency.setLength(dependency.length() - 1);
				dependency.append("];\n");
				context.addScript(dependency.toString());
				context.addScript("ambrosiaSelectDependencies(\"" + startingValue + "\", " + dependencyId + ");\n");
			}
		}

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
	public Selection setOrientation(Orientation orientation)
	{
		this.orientation = orientation;
		return this;
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
