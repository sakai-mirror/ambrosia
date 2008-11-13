/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
 * 
 * Portions completed before September 1, 2008
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

package org.etudes.ambrosia.impl;

import java.io.PrintWriter;
import java.util.Collection;

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.PropertyReference;
import org.etudes.ambrosia.api.Section;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiSection implements Section.
 */
public class UiSection extends UiContainer implements Section
{
	/** The message for the anchor. */
	protected Message anchor = null;

	/** The inclusion decision for each entity. */
	protected Decision entityIncluded = null;

	/** The reference to an entity to focus on. */
	protected PropertyReference focusReference = null;

	/** Message to use if the iterator is empty. */
	protected Message iteratorEmpty = null;

	/** The context name for the current iteration object. */
	protected String iteratorName = null;

	/** The reference to an entity to iterate over. */
	protected PropertyReference iteratorReference = null;

	/** The message for the title. */
	protected Message title = null;

	/** The highlight decision for the title. */
	protected Decision titleHighlighted = null;

	/** The include decision for the title. */
	protected Decision titleIncluded = null;

	/** The treatment. */
	protected String treatment = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiSection()
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
	protected UiSection(UiServiceImpl service, Element xml)
	{
		// do the container thing
		super(service, xml);

		// short form for title - attribute "title" as the selector
		String title = StringUtil.trimToNull(xml.getAttribute("title"));
		if (title != null)
		{
			setTitle(title);
		}

		// title
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "title");
		if (settingsXml != null)
		{
			String highlighted = StringUtil.trimToNull(settingsXml.getAttribute("highlighted"));
			if ((highlighted != null) && ("TRUE".equals(highlighted)))
			{
				this.titleHighlighted = new UiDecision().setProperty(new UiConstantPropertyReference().setValue("true"));
			}

			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "highlighted");
			if (innerXml != null)
			{
				this.titleHighlighted = service.parseDecisions(innerXml);
			}

			innerXml = XmlHelper.getChildElementNamed(settingsXml, "included");
			if (innerXml != null)
			{
				this.titleIncluded = service.parseDecisions(innerXml);
			}

			this.title = new UiMessage(service, settingsXml);
		}

		// anchor
		settingsXml = XmlHelper.getChildElementNamed(xml, "anchor");
		if (settingsXml != null)
		{
			this.anchor = new UiMessage(service, settingsXml);
		}

		// treatment
		String treatment = xml.getAttribute("treatment");
		if (treatment != null)
		{
			setTreatment(treatment);
		}

		// entity included
		settingsXml = XmlHelper.getChildElementNamed(xml, "entityIncluded");
		if (settingsXml != null)
		{
			Decision decision = service.parseDecisions(settingsXml);
			this.entityIncluded = decision;
		}

		// focus
		settingsXml = XmlHelper.getChildElementNamed(xml, "focusOn");
		if (settingsXml != null)
		{
			// short for model
			String model = StringUtil.trimToNull(settingsXml.getAttribute("model"));
			if (model != null)
			{
				this.focusReference = service.newPropertyReference().setReference(model);
			}

			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				this.focusReference = service.parsePropertyReference(innerXml);
			}
		}

		// iterator
		settingsXml = XmlHelper.getChildElementNamed(xml, "iterator");
		if (settingsXml != null)
		{
			String name = StringUtil.trimToNull(settingsXml.getAttribute("name"));
			if (name != null) this.iteratorName = name;

			// short for model
			String model = StringUtil.trimToNull(settingsXml.getAttribute("model"));
			if (model != null)
			{
				this.iteratorReference = service.newPropertyReference().setReference(model);
			}

			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				this.iteratorReference = service.parsePropertyReference(innerXml);
			}

			// if iterator is empty
			innerXml = XmlHelper.getChildElementNamed(settingsXml, "empty");
			if (innerXml != null)
			{
				this.iteratorEmpty = new UiMessage(service, innerXml);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		PrintWriter response = context.getResponseWriter();

		// the focus
		if (this.focusReference != null)
		{
			Object f = this.focusReference.readObject(context, focus);
			if (f != null)
			{
				focus = f;
			}
		}

		// included?
		if (!isIncluded(context, focus)) return false;

		// the iterator
		Object iterator = null;
		if (this.iteratorReference != null)
		{
			iterator = this.iteratorReference.readObject(context, focus);
		}

		// if iterating over a Collection, we will repeat our contents once for each one
		if ((iterator != null) && (iterator instanceof Collection))
		{
			Collection c = (Collection) iterator;
			int index = -1;
			if (c.isEmpty())
			{
				if (this.iteratorEmpty != null)
				{
					response.println("<div class =\"ambrosiaInstructions\">" + this.iteratorEmpty.getMessage(context, focus) + "</div>");
				}

				return true;
			}

			for (Object o : c)
			{
				index++;

				// place the context item
				if (this.iteratorName != null)
				{
					context.put(this.iteratorName, o, this.iteratorReference.getEncoding(context, o, index));
				}

				// place the iteration index
				context.put("ambrosia_iteration_index", Integer.toString(index));

				// check if this entity is to be included
				if ((this.entityIncluded == null) || (this.entityIncluded.decide(context, o)))
				{
					renderContents(context, o);
				}

				// remove the context item
				if (this.iteratorName != null)
				{
					context.remove(this.iteratorName);
				}

				context.remove("ambrosia_iteration_index");
			}

			return true;
		}

		// if iterating over an array, we will repeat our contents once for each one
		if ((iterator != null) && (iterator.getClass().isArray()))
		{
			Object[] c = (Object[]) iterator;
			int index = -1;

			if (c.length == 0)
			{
				if (this.iteratorEmpty != null)
				{
					response.println("<div class =\"ambrosiaInstructions\">" + this.iteratorEmpty.getMessage(context, focus) + "</div>");
				}

				return true;
			}

			for (Object o : c)
			{
				index++;

				// place the context item
				if (this.iteratorName != null)
				{
					context.put(this.iteratorName, o, this.iteratorReference.getEncoding(context, o, index));
				}

				// place the iteration index
				context.put("ambrosia_iteration_index", Integer.toString(index));

				// check if this entity is to be included
				if ((this.entityIncluded == null) || (this.entityIncluded.decide(context, o)))
				{
					renderContents(context, o);
				}

				// remove the context item
				if (this.iteratorName != null)
				{
					context.remove(this.iteratorName);
				}

				context.remove("ambrosia_iteration_index");
			}

			return true;
		}

		// if no repeating entity, just render once
		renderContents(context, focus);

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Section setAnchor(String selection, PropertyReference... references)
	{
		this.anchor = new UiMessage().setMessage(selection, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Section setEntityIncluded(Decision inclusionDecision)
	{
		this.entityIncluded = inclusionDecision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Section setFocus(PropertyReference entityReference)
	{
		this.focusReference = entityReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Section setIterator(PropertyReference reference, String name, Message empty)
	{
		this.iteratorReference = reference;
		this.iteratorName = name;
		this.iteratorEmpty = empty;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Section setTitle(String selection, PropertyReference... references)
	{
		this.title = new UiMessage().setMessage(selection, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Section setTitleHighlighted(Decision... decision)
	{
		if (decision != null)
		{
			if (decision.length == 1)
			{
				this.titleHighlighted = decision[0];
			}
			else
			{
				this.titleHighlighted = new UiAndDecision().setRequirements(decision);
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Section setTitleIncluded(Decision... decision)
	{
		if (decision != null)
		{
			if (decision.length == 1)
			{
				this.titleIncluded = decision[0];
			}
			else
			{
				this.titleIncluded = new UiAndDecision().setRequirements(decision);
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Section setTreatment(String treatment)
	{
		this.treatment = treatment;
		return this;
	}

	/**
	 * Check if this title is highlighted.
	 * 
	 * @param context
	 *        The Context.
	 * @param focus
	 *        The object focus.
	 * @return true if highlighted, false if not.
	 */
	protected boolean isTitleHighlighted(Context context, Object focus)
	{
		if (this.titleHighlighted == null) return false;
		return this.titleHighlighted.decide(context, focus);
	}

	/**
	 * Check if this title is included.
	 * 
	 * @param context
	 *        The Context.
	 * @param focus
	 *        The object focus.
	 * @return true if included, false if not.
	 */
	protected boolean isTitleIncluded(Context context, Object focus)
	{
		if (this.titleIncluded == null) return true;
		return this.titleIncluded.decide(context, focus);
	}

	/**
	 * Render the section for a single entity
	 * 
	 * @param context
	 *        The context.
	 * @param focus
	 *        The focus object.
	 */
	protected void renderContents(Context context, Object focus)
	{
		PrintWriter response = context.getResponseWriter();

		// start the section
		response.println("<div class=\"ambrosiaSection\">");

		// anchor
		if (this.anchor != null)
		{
			String anchorStr = this.anchor.getMessage(context, focus);
			response.println("<a id=\"" + anchorStr + "\" name=\"" + anchorStr + "\"></a>");
		}

		// title
		if ((this.title != null) && (isTitleIncluded(context, focus)))
		{
			if (isTitleHighlighted(context, focus))
			{
				response.print("<div class=\"ambrosiaSectionHeaderHighlight\">");
			}
			else
			{
				response.print("<div class=\"ambrosiaSectionHeader\">");
			}
			response.print(this.title.getMessage(context, focus));
			response.println("</div>");
		}

		boolean closeDiv = false;
		if ("evaluation".equals(this.treatment))
		{
			response.println("<div class=\"ambrosiaSectionEvaluation\">");
			closeDiv = true;
		}
		else if ("indented".equals(this.treatment))
		{
			response.println("<div class=\"ambrosiaSectionIndented\">");
			closeDiv = true;
		}

		// body... being a container, let the base class render the contained
		super.render(context, focus);

		if (closeDiv)
		{
			response.println("</div>");
		}

		// end the section
		response.println("</div>");
	}
}
