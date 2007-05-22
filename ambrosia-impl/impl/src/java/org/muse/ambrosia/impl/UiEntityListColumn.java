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

import java.util.ArrayList;
import java.util.List;

import org.muse.ambrosia.api.Context;
import org.muse.ambrosia.api.Controller;
import org.muse.ambrosia.api.Decision;
import org.muse.ambrosia.api.Destination;
import org.muse.ambrosia.api.EntityListColumn;
import org.muse.ambrosia.api.Footnote;
import org.muse.ambrosia.api.Message;
import org.muse.ambrosia.api.Navigation;
import org.muse.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * AutoColumn provides automatic numbering for columns in an entity list.
 */
public class UiEntityListColumn implements EntityListColumn
{
	/** The decision to display the column for this entry as an alert. */
	protected Decision alert = null;

	/** The centered setting. */
	protected boolean centered = false;

	/** Controllers contained in this container. */
	protected List<Controller> contained = new ArrayList<Controller>();

	/** The inclusion decision for each entity. */
	protected Decision entityIncluded = null;

	/** The naviations to use for the main text of the column. */
	protected List<Navigation> entityNavigations = new ArrayList<Navigation>();

	/** Footnotes for this column. */
	protected List<Footnote> footnotes = new ArrayList<Footnote>();

	/** The include decision. */
	protected Decision included = null;

	protected List<Navigation> navigations = new ArrayList<Navigation>();

	/** The message selector for text to show if an entity is not included in this column. */
	protected String notIncludedText = null;

	/** The no-wrapping indicator for the column. */
	protected boolean noWrap = false;

	/** The destination that leads to this column asc sort. */
	protected Destination sortAsc = null;

	/** The Message describing the sort asc. icon. */
	protected Message sortAscIconMsg = null;

	/** The icon path for the sort asc icon. */
	protected String sortAscIconPath = null;

	/** The destination that leads to this column desc sort. */
	protected Destination sortDesc = null;

	/** The Message describing the sort desc. icon. */
	protected Message sortDescIconMsg = null;

	/** The icon path for the sort desc. icon. */
	protected String sortDescIconPath = null;

	/** The decision that tells if this column is currently the sort column. */
	protected Decision sorting = null;

	/** The decision that tells if this column is doing asc, not desc sort (if false, it is doing desc, not asc) */
	protected Decision sortingAsc = null;

	/** The message for the column title. */
	protected Message title = null;

	/** The column width (in pixels). */
	protected Integer width = null;

	/** The column width (in em). */
	protected Integer widthEm = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiEntityListColumn()
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
	protected UiEntityListColumn(UiServiceImpl service, Element xml)
	{
		// alert
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "alert");
		if (settingsXml != null)
		{
			Decision decision = service.parseDecisions(settingsXml);
			setAlert(decision);
		}

		// short form for centered
		String centered = StringUtil.trimToNull(xml.getAttribute("centered"));
		if ((centered != null) && ("TRUE".equals(centered)))
		{
			setCentered();
		}

		// entity included
		settingsXml = XmlHelper.getChildElementNamed(xml, "entityIncluded");
		if (settingsXml != null)
		{
			// the message if not included
			String selector = StringUtil.trimToNull(xml.getAttribute("selector"));

			Decision decision = service.parseDecisions(settingsXml);
			setEntityIncluded(decision, selector);
		}

		// included
		settingsXml = XmlHelper.getChildElementNamed(xml, "included");
		if (settingsXml != null)
		{
			this.included = service.parseDecisions(settingsXml);
		}

		// no wrap
		String noWrap = StringUtil.trimToNull(xml.getAttribute("wrap"));
		if ((noWrap != null) && ("FALSE".equals(noWrap)))
		{
			setNoWrap();
		}

		// sort
		settingsXml = XmlHelper.getChildElementNamed(xml, "sort");
		if (settingsXml != null)
		{
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "active");
			if (innerXml != null)
			{
				this.sorting = service.parseDecisions(innerXml);
			}

			innerXml = XmlHelper.getChildElementNamed(settingsXml, "direction");
			if (innerXml != null)
			{
				this.sortingAsc = service.parseDecisions(innerXml);
			}

			innerXml = XmlHelper.getChildElementNamed(settingsXml, "asc");
			if (innerXml != null)
			{
				this.sortAscIconPath = StringUtil.trimToNull(innerXml.getAttribute("icon"));

				Element wayInnerXml = XmlHelper.getChildElementNamed(innerXml, "message");
				if (wayInnerXml != null)
				{
					this.sortAscIconMsg = new UiMessage(service, wayInnerXml);
				}

				wayInnerXml = XmlHelper.getChildElementNamed(innerXml, "destination");
				if (wayInnerXml != null)
				{
					this.sortAsc = new UiDestination(service, wayInnerXml);
				}
			}

			innerXml = XmlHelper.getChildElementNamed(settingsXml, "desc");
			if (innerXml != null)
			{
				this.sortDescIconPath = StringUtil.trimToNull(innerXml.getAttribute("icon"));

				Element wayInnerXml = XmlHelper.getChildElementNamed(innerXml, "message");
				if (wayInnerXml != null)
				{
					this.sortDescIconMsg = new UiMessage(service, wayInnerXml);
				}

				wayInnerXml = XmlHelper.getChildElementNamed(innerXml, "destination");
				if (wayInnerXml != null)
				{
					this.sortDesc = new UiDestination(service, wayInnerXml);
				}
			}
		}

		// short form for title - attribute "title" as the selector
		String title = StringUtil.trimToNull(xml.getAttribute("title"));
		if (title != null)
		{
			setTitle(title);
		}

		// title
		settingsXml = XmlHelper.getChildElementNamed(xml, "title");
		if (settingsXml != null)
		{
			this.title = new UiMessage(service, settingsXml);
		}

		// width - pixels
		String width = StringUtil.trimToNull(xml.getAttribute("pixels"));
		if (width != null)
		{
			try
			{
				setWidth(Integer.parseInt(width));
			}
			catch (NumberFormatException e)
			{
			}
		}

		// width - em
		width = StringUtil.trimToNull(xml.getAttribute("em"));
		if (width != null)
		{
			try
			{
				setWidthEm(Integer.parseInt(width));
			}
			catch (NumberFormatException e)
			{
			}
		}

		// controllers
		settingsXml = XmlHelper.getChildElementNamed(xml, "contained");
		if (settingsXml != null)
		{
			NodeList contained = settingsXml.getChildNodes();
			for (int i = 0; i < contained.getLength(); i++)
			{
				Node node = contained.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element controllerXml = (Element) node;

					// create a controller from each node in the container
					Controller c = service.parseController(controllerXml);
					if (c != null)
					{
						this.contained.add(c);
					}
				}
			}
		}

		// entity navigations
		settingsXml = XmlHelper.getChildElementNamed(xml, "entityNavigations");
		if (settingsXml != null)
		{
			NodeList contained = settingsXml.getChildNodes();
			for (int i = 0; i < contained.getLength(); i++)
			{
				Node node = contained.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element innerXml = (Element) node;
					if ("navigation".equals(innerXml.getTagName()))
					{
						Navigation n = new UiNavigation(service, innerXml);
						this.entityNavigations.add(n);
					}
				}
			}
		}

		// footnotes
		settingsXml = XmlHelper.getChildElementNamed(xml, "footnotes");
		if (settingsXml != null)
		{
			NodeList contained = settingsXml.getChildNodes();
			for (int i = 0; i < contained.getLength(); i++)
			{
				Node node = contained.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element innerXml = (Element) node;
					if ("footnote".equals(innerXml.getTagName()))
					{
						Footnote f = new UiFootnote();
						this.footnotes.add(f);
					}
				}
			}
		}

		// navigations
		settingsXml = XmlHelper.getChildElementNamed(xml, "navigations");
		if (settingsXml != null)
		{
			NodeList contained = settingsXml.getChildNodes();
			for (int i = 0; i < contained.getLength(); i++)
			{
				Node node = contained.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element innerXml = (Element) node;
					if ("navigation".equals(innerXml.getTagName()))
					{
						Navigation n = new UiNavigation(service, innerXml);
						this.navigations.add(n);
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn add(Controller controller)
	{
		this.contained.add(controller);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn addEntityNavigation(Navigation navigation)
	{
		this.entityNavigations.add(navigation);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn addFootnote(Footnote footnote)
	{
		this.footnotes.add(footnote);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn addNavigation(Navigation navigation)
	{
		this.navigations.add(navigation);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean alert(Context context, Object focus)
	{
		if ((this.alert != null) && (this.alert.decide(context, focus))) return true;

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getCentered()
	{
		return this.centered;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayText(Context context, Object entity, int row, int idRoot)
	{
		// set the context to capture instead of adding to the output
		context.setCollecting();

		// render the contained
		for (Controller c : this.contained)
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
	public String getEntityNavigationDestination(Context context, Object focus)
	{
		if (this.entityNavigations == null) return null;

		for (Navigation n : this.entityNavigations)
		{
			String destination = n.getDestination(context, focus);
			if (destination != null) return destination;
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Footnote> getFootnotes()
	{
		return this.footnotes;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getIsEntityIncluded(Context context, Object focus)
	{
		if ((this.entityIncluded != null) && (!this.entityIncluded.decide(context, focus))) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getIsNoWrap()
	{
		return this.noWrap;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Navigation> getNavigations()
	{
		return navigations;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNotIncludedText()
	{
		return this.notIncludedText;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getOneTimeText(Context context, Object focus, int idRoot, int numRows)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPrefixText(Context context, Object focus, int idRoot)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSortAscIcon()
	{
		return this.sortAscIconPath;
	}

	/**
	 * {@inheritDoc}
	 */
	public Message getSortAscMsg()
	{
		return this.sortAscIconMsg;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSortDescIcon()
	{
		return this.sortDescIconPath;
	}

	/**
	 * {@inheritDoc}
	 */
	public Message getSortDescMsg()
	{
		return this.sortDescIconMsg;
	}

	/**
	 * {@inheritDoc}
	 */
	public Destination getSortDestinationAsc()
	{
		return this.sortAsc;
	}

	/**
	 * {@inheritDoc}
	 */
	public Destination getSortDestinationDesc()
	{
		return this.sortDesc;
	}

	/**
	 * {@inheritDoc}
	 */
	public Decision getSortingAscDecision()
	{
		return this.sortingAsc;
	}

	/**
	 * {@inheritDoc}
	 */
	public Decision getSortingDecision()
	{
		return this.sorting;
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
	public Integer getWidth()
	{
		return this.width;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getWidthEm()
	{
		return this.widthEm;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean included(Context context)
	{
		if ((this.included != null) && (!this.included.decide(context, null))) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setAlert(Decision alertDecision)
	{
		this.alert = alertDecision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setCentered()
	{
		this.centered = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setEntityIncluded(Decision inclusionDecision, String notIncludedText)
	{
		this.entityIncluded = inclusionDecision;
		this.notIncludedText = notIncludedText;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setIncluded(Decision decision)
	{
		this.included = decision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setNoWrap()
	{
		this.noWrap = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setSortDestination(Destination asc, Destination desc)
	{
		this.sortAsc = asc;
		this.sortDesc = desc;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setSortIcons(String ascUrl, Message ascDescription, String descUrl, Message descDescription)
	{
		this.sortAscIconPath = ascUrl;
		this.sortAscIconMsg = ascDescription;
		this.sortDescIconPath = descUrl;
		this.sortDescIconMsg = descDescription;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setSorting(Decision sorting, Decision ascNotDesc)
	{
		this.sorting = sorting;
		this.sortingAsc = ascNotDesc;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setTitle(String selector, PropertyReference... references)
	{
		this.title = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setWidth(int width)
	{
		this.width = new Integer(width);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn setWidthEm(int width)
	{
		this.widthEm = new Integer(width);
		return this;
	}
}
