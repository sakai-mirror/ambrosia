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
import java.util.Collection;

import org.muse.ambrosia.api.Context;
import org.muse.ambrosia.api.Decision;
import org.muse.ambrosia.api.Message;
import org.muse.ambrosia.api.PropertyReference;
import org.muse.ambrosia.api.Section;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * UiSection is a container within a user interface. Each interface should have one section, and may have many. Sections should not be nested.<br />
 * The section title is rendered, along with the controllers added to the section container.
 */
public class UiSection extends UiContainer implements Section
{
	/** The message for the anchor. */
	protected Message anchor = null;

	/** The reference to an entity to focus on. */
	protected PropertyReference focusReference = null;

	/** The inclusion decision for each entity. */
	protected Decision entityIncluded = null;

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
				this.included = service.parseDecisions(innerXml);
			}

			this.title = new UiMessage(service, settingsXml);
		}

		// anchor
		settingsXml = XmlHelper.getChildElementNamed(xml, "anchor");
		if (settingsXml != null)
		{
			this.anchor = new UiMessage(service, settingsXml);
		}

		// entity included
		settingsXml = XmlHelper.getChildElementNamed(xml, "entityIncluded");
		if (settingsXml != null)
		{
			Decision decision = service.parseDecisions(settingsXml);
			this.included = decision;
		}

		// focus
		settingsXml = XmlHelper.getChildElementNamed(xml, "focus");
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
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void render(Context context, Object focus)
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
		if (!isIncluded(context, focus)) return;

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
			for (Object o : c)
			{
				index++;

				// place the context item
				if (this.iteratorName != null)
				{
					context.put(this.iteratorName, o, this.iteratorReference.getEncoding(context, o, index));
				}

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
			}

			return;
		}

		// if iterating over an array, we will repeat our contents once for each one
		if ((iterator != null) && (iterator.getClass().isArray()))
		{
			Object[] c = (Object[]) iterator;
			int index = -1;
			for (Object o : c)
			{
				index++;

				// place the context item
				if (this.iteratorName != null)
				{
					context.put(this.iteratorName, o, this.iteratorReference.getEncoding(context, o, index));
				}

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
			}

			return;
		}

		// if no repeating entity, just render once
		renderContents(context, focus);
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
	public Section setIterator(PropertyReference reference, String name)
	{
		this.iteratorReference = reference;
		this.iteratorName = name;
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
			response.println("<a name=\"" + Validator.escapeHtml(this.anchor.getMessage(context, focus)) + "\"></a>");
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

		// body... being a container, let the base class render the contained
		super.render(context, focus);

		// end the section
		response.println("</div>");
	}
}
