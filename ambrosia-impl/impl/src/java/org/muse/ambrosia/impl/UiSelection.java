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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.muse.ambrosia.api.Component;
import org.muse.ambrosia.api.Container;
import org.muse.ambrosia.api.Context;
import org.muse.ambrosia.api.Decision;
import org.muse.ambrosia.api.Destination;
import org.muse.ambrosia.api.Message;
import org.muse.ambrosia.api.Navigation;
import org.muse.ambrosia.api.PropertyReference;
import org.muse.ambrosia.api.RenderListener;
import org.muse.ambrosia.api.Selection;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * UiSelection presents a selection for the user to choose or not.<br />
 * The text can be either a property reference or a message.
 */
public class UiSelection extends UiComponent implements Selection
{
	/** Decision for including the correct markers. */
	protected Decision correctDecision = null;

	/** Icon to use to show correct. */
	protected String correctIcon = "!/ambrosia_library/icons/correct.png";

	/** The correct message. */
	protected Message correctMessage = new UiMessage().setMessage("correct");

	/** A model reference to a value that is considered "correct" for correct/incorrect marking. */
	protected PropertyReference correctReference = null;

	/** Dropdown # lines to display. */
	protected int height = 1;

	/** Icon to use to show incorrect. */
	protected String incorrectIcon = "!/ambrosia_library/icons/incorrect.png";

	/** The incorrect message. */
	protected Message incorrectMessage = new UiMessage().setMessage("incorrect");

	/** The context name for the current iteration object when using selectionReference. */
	protected String iteratorName = null;

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

	/** The ref to the selectionReference collection that pulls out the display text for each selection. */
	protected PropertyReference selectionDisplayReference = null;

	/** The set of messages for multiple selection choices. */
	protected List<Message> selectionMessages = new ArrayList<Message>();

	/** The ref to a Collection or [] in the model that will populate the selection. */
	protected PropertyReference selectionReference = null;

	/** The ref to the selectionReference collection that pulls out the value for each selection. */
	protected PropertyReference selectionValueReference = null;

	/** The set of values for multiple selection choices. */
	protected List<String> selectionValues = new ArrayList<String>();

	/** If set, use this instead of sigleSelect to see if we are going to be single or multiple select. */
	protected Decision singleSelectDecision = null;

	/** The destination to submit if we are submitting on change. */
	protected Destination submitDestination = null;

	/** if set, submit on change and use the value selected as the destination. */
	protected boolean submitValue = false;

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

		// short for correct
		String correct = StringUtil.trimToNull(xml.getAttribute("correct"));
		if (model != null)
		{
			PropertyReference pRef = service.newPropertyReference().setReference(correct);
			setCorrect(pRef);
		}

		// short form for destination - attribute "destination" as the destination
		String destination = StringUtil.trimToNull(xml.getAttribute("destination"));
		if (destination != null)
		{
			setDestination(service.newDestination().setDestination(destination));
		}

		// selected value
		String value = StringUtil.trimToNull(xml.getAttribute("value"));
		if (value != null) this.selectedValue = value;

		// orientation
		String orientation = StringUtil.trimToNull(xml.getAttribute("orientation"));
		if (orientation != null)
		{
			if (orientation.equals("HORIZONTAL"))
			{
				setOrientation(Orientation.horizontal);
			}
			else if (orientation.equals("DROPDOWN"))
			{
				setOrientation(Orientation.dropdown);
			}
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

		// correct
		settingsXml = XmlHelper.getChildElementNamed(xml, "correct");
		if (settingsXml != null)
		{
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				PropertyReference pRef = service.parsePropertyReference(innerXml);
				if (pRef != null) setCorrect(pRef);
			}
		}

		// correct decision
		settingsXml = XmlHelper.getChildElementNamed(xml, "correctDecision");
		if (settingsXml != null)
		{
			setCorrectDecision(service.parseDecisions(settingsXml));
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

		// selection choices from model
		settingsXml = XmlHelper.getChildElementNamed(xml, "selectionModel");
		if (settingsXml != null)
		{
			String name = StringUtil.trimToNull(settingsXml.getAttribute("name"));
			if (name != null) this.iteratorName = name;

			// short for model
			model = StringUtil.trimToNull(settingsXml.getAttribute("model"));
			if (model != null)
			{
				this.selectionReference = service.newPropertyReference().setReference(model);
			}

			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				this.selectionReference = service.parsePropertyReference(innerXml);
			}

			// short for value model
			model = StringUtil.trimToNull(settingsXml.getAttribute("value"));
			if (model != null)
			{
				this.selectionValueReference = service.newPropertyReference().setReference(model);
			}

			// value model
			innerXml = XmlHelper.getChildElementNamed(settingsXml, "valueModel");
			if (innerXml != null)
			{
				Element modelXml = XmlHelper.getChildElementNamed(settingsXml, "model");
				if (modelXml != null)
				{
					this.selectionValueReference = service.parsePropertyReference(modelXml);
				}
			}

			// short for display model
			model = StringUtil.trimToNull(settingsXml.getAttribute("display"));
			if (model != null)
			{
				this.selectionDisplayReference = service.newPropertyReference().setReference(model);
			}

			// display model
			innerXml = XmlHelper.getChildElementNamed(settingsXml, "displayModel");
			if (innerXml != null)
			{
				Element modelXml = XmlHelper.getChildElementNamed(settingsXml, "model");
				if (modelXml != null)
				{
					this.selectionDisplayReference = service.parsePropertyReference(modelXml);
				}
			}
		}

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

		// single select
		settingsXml = XmlHelper.getChildElementNamed(xml, "singleSelect");
		if (settingsXml != null)
		{
			this.singleSelectDecision = service.parseDecisions(settingsXml);
		}

		// short for height
		String height = StringUtil.trimToNull(xml.getAttribute("height"));
		if (height != null)
		{
			this.setHeight(Integer.parseInt(height));
		}

		// submit value
		String submitValue = StringUtil.trimToNull(xml.getAttribute("submitValue"));
		if ((submitValue != null) && ("TRUE".equals(submitValue)))
		{
			this.submitValue = true;
		}

		// submitDestination
		settingsXml = XmlHelper.getChildElementNamed(xml, "destination");
		if (settingsXml != null)
		{
			// let Destination parse this
			this.submitDestination = new UiDestination(service, settingsXml);
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

		// single select?
		boolean single = true;
		if (this.singleSelectDecision != null)
		{
			single = this.singleSelectDecision.decide(context, focus);
		}

		// find values and display text
		List<String> values = new ArrayList<String>(this.selectionValues);

		List<String> display = new ArrayList<String>();
		if (!this.selectionMessages.isEmpty())
		{
			for (Message msg : this.selectionMessages)
			{
				display.add(msg.getMessage(context, focus));
			}
		}

		// add in any from the model
		if ((this.selectionValueReference != null) && (this.selectionDisplayReference != null) && (this.selectionReference != null))
		{
			// get the main collection
			Collection collection = null;
			Object obj = this.selectionReference.readObject(context, focus);
			if (obj != null)
			{
				if (obj instanceof Collection)
				{
					collection = (Collection) obj;
				}

				else if (obj.getClass().isArray())
				{
					Object[] array = (Object[]) obj;
					collection = new ArrayList(array.length);
					for (Object o : array)
					{
						collection.add(o);
					}
				}
			}

			// if we got something
			if (collection != null)
			{
				// like iteration, make each object available then get the value and display
				int index = -1;
				for (Object o : collection)
				{
					index++;

					// place the item
					if (this.iteratorName != null)
					{
						context.put(this.iteratorName, o, this.selectionReference.getEncoding(context, o, index));
					}

					values.add(this.selectionValueReference.read(context, o));
					display.add(this.selectionDisplayReference.read(context, o));

					// remove item
					if (this.iteratorName != null)
					{
						context.remove(this.iteratorName);
					}
				}
			}
		}

		// read the current value(s)
		Set<String> value = new HashSet<String>();
		if (this.propertyReference != null)
		{
			Object obj = this.propertyReference.readObject(context, focus);
			if (obj != null)
			{
				// any sort of collection
				if (obj instanceof Collection)
				{
					for (Object o : ((Collection) obj))
					{
						value.add(o.toString());
					}
				}

				// any sort of array
				if (obj.getClass().isArray())
				{
					for (Object o : ((Object[]) obj))
					{
						value.add(o.toString());
					}
				}

				// otherwise take it as a string
				else
				{
					value.add(obj.toString());
				}
			}
		}

		if (this.orientation == Orientation.dropdown)
		{
			renderDropdown(context, focus, readOnly, single, values, display, value);
			return;
		}

		// generate some ids
		int idRoot = context.getUniqueId();
		String id = this.getClass().getSimpleName() + "_" + idRoot;
		String decodeId = "decode_" + idRoot;
		String dependencyId = id + "_dependencies";

		PrintWriter response = context.getResponseWriter();

		// read the "correct" value
		String correctValue = null;
		boolean includeCorrectMarkers = false;
		if ((this.correctReference != null) && ((this.correctDecision == null) || (this.correctDecision.decide(context, focus))))
		{
			correctValue = this.correctReference.read(context, focus);
			if (correctValue != null)
			{
				includeCorrectMarkers = true;
			}
		}

		response.println("<div class=\"ambrosiaSelection\">");

		if (values.isEmpty())
		{
			String onclick = "";

			if (this.submitDestination != null)
			{
				String destination = this.submitDestination.getDestination(context, focus);
				onclick = "onclick=\"ambrosiaSubmit('" + destination + "')\" ";
			}
			else if (this.submitValue)
			{
				onclick = "onclick=\"ambrosiaSubmit(this.value)\" ";
			}

			// convert to boolean
			boolean checked = value.contains("true");

			// if we are doing correct marking
			if (includeCorrectMarkers)
			{
				// if checked, mark as correct or not
				if (checked)
				{
					// is the value correct?
					boolean correct = Boolean.parseBoolean(correctValue) == checked;

					if (correct)
					{
						response.print("<img src=\"" + context.getUrl(this.correctIcon) + "\" alt=\""
								+ Validator.escapeHtml(this.correctMessage.getMessage(context, focus)) + "\" />");
					}
					else
					{
						response.print("<img src=\"" + context.getUrl(this.incorrectIcon) + "\" alt=\""
								+ Validator.escapeHtml(this.incorrectMessage.getMessage(context, focus)) + "\" />");
					}
				}

				// else leave a placeholder
				else
				{
					response.println("<div style=\"float:left;width:16px\">&nbsp;</div>");
				}
			}

			// the check box
			response.println("<input " + onclick + "type=\"checkbox\" name=\"" + id + "\" id=\"" + id + "\" value=\"" + this.selectedValue + "\" "
					+ (checked ? "CHECKED" : "") + (readOnly ? " disabled=\"disabled\"" : "") + " />");

			// the decode directive
			if ((this.propertyReference != null) && (!readOnly))
			{
				response.println("<input type=\"hidden\" name=\"" + decodeId + "\" value =\"" + id + "\" />" + "<input type=\"hidden\" name=\""
						+ "prop_" + decodeId + "\" value=\"" + this.propertyReference.getFullReference(context) + "\" />"
						+ "<input type=\"hidden\" name=\"" + "null_" + decodeId + "\" value=\"" + this.notSelectedValue + "\" />");
			}

			// title after (right) for the single check box version
			if (this.titleMessage != null)
			{
				response.println("<label for=\"" + id + "\">");
				response.println(this.titleMessage.getMessage(context, focus));
				response.println("</label>");
			}
		}

		else
		{
			// title first for multiple choices
			if (this.titleMessage != null)
			{
				response.println("<label for=\"" + id + "\">");
				response.println(this.titleMessage.getMessage(context, focus) + "<br />");
				response.println("</label>");
			}

			final StringBuffer dependency = new StringBuffer();
			dependency.append("var " + dependencyId + "=[");
			String startingValue = null;
			boolean needDependencies = false;
			String onclick = "";
			if (!this.selectionContainers.isEmpty())
			{
				onclick = "onclick=\"ambrosiaSelectDependencies(this.value, " + dependencyId + ")\" ";
			}

			else if (this.submitDestination != null)
			{
				String destination = this.submitDestination.getDestination(context, focus);
				onclick = "onclick=\"ambrosiaSubmit('" + destination + "')\" ";
			}
			else if (this.submitValue)
			{
				onclick = "onclick=\"ambrosiaSubmit(this.value)\" ";
			}

			for (int i = 0; i < values.size(); i++)
			{
				String val = values.get(i);
				String message = "";
				if (i < display.size())
				{
					message = display.get(i);
				}

				boolean selected = value.contains(val);
				if (selected)
				{
					startingValue = val;
				}

				// if we are doing correct marking
				if (includeCorrectMarkers)
				{
					// if checked, mark as correct or not
					if (selected)
					{
						// is this one the correct one?
						boolean correct = correctValue.equals(val);

						if (correct)
						{
							response.print("<img src=\"" + context.getUrl(this.correctIcon) + "\" alt=\""
									+ Validator.escapeHtml(this.correctMessage.getMessage(context, focus)) + "\" />");
						}
						else
						{
							response.print("<img src=\"" + context.getUrl(this.incorrectIcon) + "\" alt=\""
									+ Validator.escapeHtml(this.incorrectMessage.getMessage(context, focus)) + "\" />");
						}
					}

					// else leave a placeholder
					else
					{
						response.println("<div style=\"float:left;width:16px\">&nbsp;</div>");
					}
				}

				// use a radio for single select
				if (single)
				{
					// the radio button
					response.println("<input " + onclick + "type=\"radio\" name=\"" + id + "\" id=\"" + id + "\" value=\"" + val + "\" "
							+ (selected ? "CHECKED" : "") + (readOnly ? " disabled=\"disabled\"" : "") + " /> " + message);
				}

				// for multiple selection, use a checkbox set
				else
				{
					response.println("<input " + onclick + "type=\"checkbox\" name=\"" + id + "\" id=\"" + id + "\" value=\"" + val + "\" "
							+ (selected ? "CHECKED" : "") + (readOnly ? " disabled=\"disabled\"" : "") + " /> " + message);
				}

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

			// the decode directive
			if ((this.propertyReference != null) && (!readOnly))
			{
				response.println("<input type=\"hidden\" name=\"" + decodeId + "\" value =\"" + id + "\" />" + "<input type=\"hidden\" name=\""
						+ "prop_" + decodeId + "\" value=\"" + this.propertyReference.getFullReference(context) + "\" />");
			}
		}

		response.println("</div>");
	}

	/**
	 * {@inheritDoc}
	 */
	public Selection setCorrect(PropertyReference correctReference)
	{
		this.correctReference = correctReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Selection setCorrectDecision(Decision decision)
	{
		this.correctDecision = decision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Selection setDestination(Destination destination)
	{
		this.submitDestination = destination;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Selection setHeight(int height)
	{
		this.height = height;

		return this;
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
	public Selection setSelectionModel(PropertyReference modelRef, String iteratorName, PropertyReference valueRef, PropertyReference displayRef)
	{
		this.selectionReference = modelRef;
		this.iteratorName = iteratorName;
		this.selectionValueReference = valueRef;
		this.selectionDisplayReference = displayRef;

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Selection setSingleSelectDecision(Decision decision)
	{
		this.singleSelectDecision = decision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Selection setSubmitValue()
	{
		this.submitValue = true;
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

	/**
	 * {@inheritDoc}
	 */
	protected void renderDropdown(Context context, Object focus, boolean readOnly, boolean single, List<String> values, List<String> display,
			Set<String> value)
	{
		// generate some ids
		int idRoot = context.getUniqueId();
		String id = this.getClass().getSimpleName() + "_" + idRoot;
		String decodeId = "decode_" + idRoot;

		PrintWriter response = context.getResponseWriter();

		// Note: correct / incorrect markings not supported for dropdown

		// TODO:
		response.println("<div class=\"ambrosiaSelection\">");

		// title
		if (this.titleMessage != null)
		{
			response.println("<label for=\"" + id + "\">");
			response.println(this.titleMessage.getMessage(context, focus));
			response.println("</label>");
		}

		response.println("<select size=\"" + Integer.toString(this.height) + "\" " + (single ? "" : "multiple ") + "name=\"" + id + "\" id=\"" + id
				+ "\"" + (readOnly ? " disabled=\"disabled\"" : "") + ">");

		// TODO: must have selection values

		String onclick = "";
		if (this.submitDestination != null)
		{
			String destination = this.submitDestination.getDestination(context, focus);
			onclick = "onclick=\"ambrosiaSubmit('" + destination + "')\" ";
		}
		else if (this.submitValue)
		{
			onclick = "onclick=\"ambrosiaSubmit(this.value)\" ";
		}

		// TODO: selectionContainers not supported

		for (int i = 0; i < values.size(); i++)
		{
			String val = values.get(i);
			String message = "";
			if (i < display.size())
			{
				message = display.get(i);
			}

			boolean selected = value.contains(val);

			// the option
			response.println("<option " + onclick + "value=\"" + val + "\" " + (selected ? "SELECTED" : "") + ">" + message + "</option>");
		}

		response.println("</select>");

		// the decode directive
		if ((this.propertyReference != null) && (!readOnly))
		{
			response.println("<input type=\"hidden\" name=\"" + decodeId + "\" value =\"" + id + "\" />" + "<input type=\"hidden\" name=\"" + "prop_"
					+ decodeId + "\" value=\"" + this.propertyReference.getFullReference(context) + "\" />");
		}

		response.println("</div>");
	}
}
