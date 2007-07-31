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
import org.muse.ambrosia.api.Destination;
import org.muse.ambrosia.api.Message;
import org.muse.ambrosia.api.Navigation;
import org.muse.ambrosia.api.Pager;
import org.muse.ambrosia.api.PagingPropertyReference;
import org.muse.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiPager implements Pager.
 */
public class UiPager extends UiComponent implements Pager
{
	/** The current report message. */
	protected Message curMessage = null;

	/** The tool destination for clicks. */
	protected Destination destination = null;

	/** The first page icon. */
	protected String firstIcon = "!/ambrosia_library/icons/pager_first.png";

	/** The first page message. */
	protected Message firstMessage = new UiMessage().setMessage("pager-first");

	/** The last page icon. */
	protected String lastIcon = "!/ambrosia_library/icons/pager_last.png";

	/** The last page message. */
	protected Message lastMessage = new UiMessage().setMessage("pager-last");

	/** The next page icon. */
	protected String nextIcon = "!/ambrosia_library/icons/pager_next.png";

	/** The next page message. */
	protected Message nextMessage = new UiMessage().setMessage("pager-next");

	/** The model reference for the current page. */
	protected PropertyReference pagingModel = null;

	/** The prev page icon. */
	protected String prevIcon = "!/ambrosia_library/icons/pager_prev.png";

	/** The prev page message. */
	protected Message prevMessage = new UiMessage().setMessage("pager-prev");

	/** If true, we need to submit the form on the press. */
	protected boolean submit = false;

	/**
	 * Public no-arg constructor.
	 */
	public UiPager()
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
	protected UiPager(UiServiceImpl service, Element xml)
	{
		// do the text stuff
		super(service, xml);

		// icons - short form
		String icon = StringUtil.trimToNull(xml.getAttribute("first"));
		if (icon != null) this.firstIcon = icon;
		icon = StringUtil.trimToNull(xml.getAttribute("prev"));
		if (icon != null) this.prevIcon = icon;
		icon = StringUtil.trimToNull(xml.getAttribute("next"));
		if (icon != null) this.nextIcon = icon;
		icon = StringUtil.trimToNull(xml.getAttribute("last"));
		if (icon != null) this.lastIcon = icon;

		// short form for destination
		String destination = StringUtil.trimToNull(xml.getAttribute("destination"));
		if (destination != null)
		{
			setDestination(service.newDestination().setDestination(destination));
		}

		// icons
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "first");
		if (settingsXml != null)
		{
			icon = StringUtil.trimToNull(settingsXml.getAttribute("icon"));
			if (icon != null) this.firstIcon = icon;

			String selector = StringUtil.trimToNull(settingsXml.getAttribute("message"));
			if (selector != null)
			{
				this.firstMessage = new UiMessage().setMessage(selector);
			}

			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "message");
			if (innerXml != null)
			{
				this.firstMessage = new UiMessage(service, innerXml);
			}
		}
		settingsXml = XmlHelper.getChildElementNamed(xml, "prev");
		if (settingsXml != null)
		{
			icon = StringUtil.trimToNull(settingsXml.getAttribute("icon"));
			if (icon != null) this.prevIcon = icon;

			String selector = StringUtil.trimToNull(settingsXml.getAttribute("message"));
			if (selector != null)
			{
				this.prevMessage = new UiMessage().setMessage(selector);
			}

			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "message");
			if (innerXml != null)
			{
				this.prevMessage = new UiMessage(service, innerXml);
			}
		}
		settingsXml = XmlHelper.getChildElementNamed(xml, "next");
		if (settingsXml != null)
		{
			icon = StringUtil.trimToNull(settingsXml.getAttribute("icon"));
			if (icon != null) this.nextIcon = icon;

			String selector = StringUtil.trimToNull(settingsXml.getAttribute("message"));
			if (selector != null)
			{
				this.nextMessage = new UiMessage().setMessage(selector);
			}

			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "message");
			if (innerXml != null)
			{
				this.nextMessage = new UiMessage(service, innerXml);
			}
		}
		settingsXml = XmlHelper.getChildElementNamed(xml, "last");
		if (settingsXml != null)
		{
			icon = StringUtil.trimToNull(settingsXml.getAttribute("icon"));
			if (icon != null) this.lastIcon = icon;

			String selector = StringUtil.trimToNull(settingsXml.getAttribute("message"));
			if (selector != null)
			{
				this.lastMessage = new UiMessage().setMessage(selector);
			}

			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "message");
			if (innerXml != null)
			{
				this.lastMessage = new UiMessage(service, innerXml);
			}
		}

		// destination
		settingsXml = XmlHelper.getChildElementNamed(xml, "destination");
		if (settingsXml != null)
		{
			// let Destination parse this
			this.destination = new UiDestination(service, settingsXml);
		}

		// short for paging model reference
		String model = StringUtil.trimToNull(xml.getAttribute("paging"));
		if (model != null)
		{
			PropertyReference pRef = service.newPropertyReference().setReference(model);
			setPagingProperty(pRef);
		}

		// model
		settingsXml = XmlHelper.getChildElementNamed(xml, "paging");
		if (settingsXml != null)
		{
			PropertyReference pRef = service.parsePropertyReference(settingsXml);
			if (pRef != null) setPagingProperty(pRef);
		}

		// text
		settingsXml = XmlHelper.getChildElementNamed(xml, "message");
		if (settingsXml != null)
		{
			this.curMessage = new UiMessage(service, settingsXml);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void render(Context context, Object focus)
	{
		// included?
		if (!isIncluded(context, focus)) return;

		PrintWriter response = context.getResponseWriter();

		String pagingRef = this.pagingModel.getFullReference(context);

		Navigation first = new UiNavigation().setStyle(Navigation.Style.link).setDescription(this.firstMessage).setDestination(this.destination)
				.setIcon(this.firstIcon, Navigation.IconStyle.left).setDisabled(
						new UiDecision().setProperty(new UiPropertyReference().setReference(pagingRef + ".isFirst")));
		Navigation prev = new UiNavigation().setStyle(Navigation.Style.link).setDescription(this.prevMessage).setDestination(this.destination)
				.setIcon(this.prevIcon, Navigation.IconStyle.left).setDisabled(
						new UiDecision().setProperty(new UiPropertyReference().setReference(pagingRef + ".isFirst")));
		Navigation next = new UiNavigation().setStyle(Navigation.Style.link).setDescription(this.nextMessage).setDestination(this.destination)
				.setIcon(this.nextIcon, Navigation.IconStyle.left).setDisabled(
						new UiDecision().setProperty(new UiPropertyReference().setReference(pagingRef + ".isLast")));
		Navigation last = new UiNavigation().setStyle(Navigation.Style.link).setDescription(this.lastMessage).setDestination(this.destination)
				.setIcon(this.lastIcon, Navigation.IconStyle.left).setDisabled(
						new UiDecision().setProperty(new UiPropertyReference().setReference(pagingRef + ".isLast")));

		if (this.submit)
		{
			first.setSubmit();
			prev.setSubmit();
			next.setSubmit();
			last.setSubmit();
		}

		context.put(PagingPropertyReference.SELECTOR, PagingPropertyReference.FIRST);
		first.render(context, focus);

		context.put(PagingPropertyReference.SELECTOR, PagingPropertyReference.PREV);
		prev.render(context, focus);

		if (this.curMessage != null)
		{
			String msg = this.curMessage.getMessage(context, focus);
			if (msg != null)
			{
				response.println(msg);
			}
		}

		context.put(PagingPropertyReference.SELECTOR, PagingPropertyReference.NEXT);
		next.render(context, focus);

		context.put(PagingPropertyReference.SELECTOR, PagingPropertyReference.LAST);
		last.render(context, focus);
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager setDestination(Destination destination)
	{
		this.destination = destination;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager setFirstIcon(String url, String selector, PropertyReference... references)
	{
		this.firstIcon = url;
		this.firstMessage = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager setLastIcon(String url, String selector, PropertyReference... references)
	{
		this.lastIcon = url;
		this.lastMessage = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager setNextIcon(String url, String selector, PropertyReference... references)
	{
		this.nextIcon = url;
		this.nextMessage = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager setPagingProperty(PropertyReference propertyReference)
	{
		this.pagingModel = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager setPrevIcon(String url, String selector, PropertyReference... references)
	{
		this.prevIcon = url;
		this.prevMessage = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager setSubmit()
	{
		this.submit = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager setText(String selector, PropertyReference... references)
	{
		this.curMessage = new UiMessage().setMessage(selector, references);
		return this;
	}
}
