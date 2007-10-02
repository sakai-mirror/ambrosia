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
import java.util.List;
import java.util.Map;

import org.muse.ambrosia.api.Component;
import org.muse.ambrosia.api.Context;
import org.muse.ambrosia.api.Decision;
import org.muse.ambrosia.api.EntityList;
import org.muse.ambrosia.api.EntityListColumn;
import org.muse.ambrosia.api.Footnote;
import org.muse.ambrosia.api.Message;
import org.muse.ambrosia.api.Navigation;
import org.muse.ambrosia.api.Pager;
import org.muse.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * UiEntityList implements EntityList.
 */
public class UiEntityList extends UiComponent implements EntityList
{
	/** Columns for this list. */
	protected List<EntityListColumn> columns = new ArrayList<EntityListColumn>();

	/** Text message to use if there are no items to show in the list. */
	protected Message emptyTitle = null;

	/** The enitity actions defined related to this column. */
	protected List<Component> entityActions = new ArrayList<Component>();

	/** A single decision for each possible heading - order matches that in headingMessages. */
	protected List<Decision> headingDecisions = new ArrayList<Decision>();

	/** A message for each possible heading - order matches that in headingDecisions. */
	protected List<Message> headingMessages = new ArrayList<Message>();

	/** A navigation for each possible heading - order matches that in headingDecisions. */
	protected List<Navigation> headingNavigations = new ArrayList<Navigation>();

	/** The inclusion decision for each entity. */
	protected Decision entityIncluded = null;

	/** The context name for the current iteration object. */
	protected String iteratorName = null;

	/** The reference to an entity to iterate over. */
	protected PropertyReference iteratorReference = null;

	/** The pager for the list. */
	protected Pager pager = null;

	/** Rendering style. */
	protected Style style = Style.flat;

	/** The message for the title. */
	protected Message title = null;

	/** The include decision array for the title. */
	protected Decision titleIncluded = null;

	/**
	 * No-arg constructor.
	 */
	public UiEntityList()
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
	protected UiEntityList(UiServiceImpl service, Element xml)
	{
		// component stuff
		super(service, xml);

		// empty title
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "emptyTitle");
		if (settingsXml != null)
		{
			this.emptyTitle = new UiMessage(service, settingsXml);
		}

		// entity included
		settingsXml = XmlHelper.getChildElementNamed(xml, "entityIncluded");
		if (settingsXml != null)
		{
			Decision decision = service.parseDecisions(settingsXml);
			this.entityIncluded = decision;
		}

		// iterator
		settingsXml = XmlHelper.getChildElementNamed(xml, "iterator");
		if (settingsXml != null)
		{
			// short cut for model
			String model = StringUtil.trimToNull(settingsXml.getAttribute("model"));
			if (model != null)
			{
				this.iteratorReference = new UiPropertyReference().setReference(model);
			}

			// name
			String name = StringUtil.trimToNull(settingsXml.getAttribute("name"));
			if (name != null)
			{
				this.iteratorName = name;
			}

			// inner model
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				PropertyReference pRef = service.parsePropertyReference(innerXml);
				if (pRef != null) this.iteratorReference = pRef;
			}
		}

		// style
		String style = StringUtil.trimToNull(xml.getAttribute("style"));
		if (style != null)
		{
			if ("FLAT".equals(style))
			{
				setStyle(Style.flat);
			}
			else if ("FORM".equals(style))
			{
				setStyle(Style.form);
			}
		}

		// title
		settingsXml = XmlHelper.getChildElementNamed(xml, "title");
		if (settingsXml != null)
		{
			this.title = new UiMessage(service, settingsXml);
		}

		// title includede
		settingsXml = XmlHelper.getChildElementNamed(xml, "titleIncluded");
		if (settingsXml != null)
		{
			this.titleIncluded = service.parseDecisions(settingsXml);
		}

		// columns
		settingsXml = XmlHelper.getChildElementNamed(xml, "columns");
		if (settingsXml != null)
		{
			NodeList contained = settingsXml.getChildNodes();
			for (int i = 0; i < contained.getLength(); i++)
			{
				Node node = contained.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element containedXml = (Element) node;

					// let the service parse this as a column
					EntityListColumn column = service.parseEntityListColumn(containedXml);
					if (column != null) this.columns.add(column);
				}
			}
		}

		// headings
		settingsXml = XmlHelper.getChildElementNamed(xml, "headings");
		if (settingsXml != null)
		{
			NodeList contained = settingsXml.getChildNodes();
			for (int i = 0; i < contained.getLength(); i++)
			{
				Node node = contained.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element containedXml = (Element) node;
					if ("heading".equals(containedXml.getTagName()))
					{
						Decision d = service.parseDecisions(containedXml);
						Message m = null;
						Navigation n = null;

						Element innerXml = XmlHelper.getChildElementNamed(containedXml, "message");
						if (innerXml != null)
						{
							m = new UiMessage(service, innerXml);
						}

						innerXml = XmlHelper.getChildElementNamed(containedXml, "navigation");
						if (innerXml != null)
						{
							n = new UiNavigation(service, innerXml);
						}

						this.headingDecisions.add(d);
						this.headingMessages.add(m);
						this.headingNavigations.add(n);
					}
				}
			}
		}

		// entityActions
		settingsXml = XmlHelper.getChildElementNamed(xml, "entityActions");
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
						this.entityActions.add(c);
					}
				}
			}
		}

		// pager
		settingsXml = XmlHelper.getChildElementNamed(xml, "pager");
		if (settingsXml != null)
		{
			this.pager = new UiPager(service, settingsXml);
		}

		// we need an id
		if (this.id == null) autoId();
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityList addColumn(EntityListColumn column)
	{
		this.columns.add(column);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityList addEntityAction(Component action)
	{
		this.entityActions.add(action);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityList addHeading(Decision decision, Navigation navigation)
	{
		this.headingDecisions.add(decision);
		this.headingMessages.add(null);
		this.headingNavigations.add(navigation);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityList addHeading(Decision decision, String selector, PropertyReference... properties)
	{
		this.headingDecisions.add(decision);
		this.headingMessages.add(new UiMessage().setMessage(selector, properties));
		this.headingNavigations.add(null);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public void render(Context context, Object focus)
	{
		PrintWriter response = context.getResponseWriter();

		// included?
		if (!isIncluded(context, focus)) return;

		// get an id
		int idRoot = context.getUniqueId();
		String id = this.getClass().getSimpleName() + "_" + idRoot;

		// check if the summary row is needed
		boolean summaryNeeded = false;

		// the data
		Collection data = (Collection) this.iteratorReference.readObject(context, focus);
		boolean empty = ((data == null) || (data.isEmpty()));

		renderEntityActions(context, focus, idRoot);

		// columns one time text
		int colNum = 0;
		for (EntityListColumn c : this.columns)
		{
			// included?
			if (!c.included(context)) continue;

			String text = c.getPrefixText(context, focus, getId() + "_" + idRoot + "_" + colNum);
			if (text != null)
			{
				response.println(text);
			}

			colNum++;
		}

		// title, if there is one and there is data
		if ((this.title != null) && (isTitleIncluded(context, focus)))
		{
			response.println("<div class =\"ambrosiaInstructions\">" + Validator.escapeHtml(this.title.getMessage(context, focus)) + "</div>");
		}

		response.println("<div class=\"ambrosiaEntityList\">");

		// start the table
		response.println("<table class=\"ambrosiaEntityListTable "
				+ ((this.style == Style.flat) ? "ambrosiaEntityListFlat" : "ambrosiaEntityListForm") + "\" cellpadding=\"0\" cellspacing=\"0\" >");

		// do we need headers? not if we have no headers defined
		// also, count the cols
		int cols = 0;
		boolean needHeaders = false;
		for (EntityListColumn c : this.columns)
		{
			// included?
			if (!c.included(context)) continue;

			cols++;

			if (c.getTitle() != null)
			{
				needHeaders = true;
			}
		}

		if (needHeaders)
		{
			// columns headers
			response.println("<thead><tr>");
			colNum = 0;
			for (EntityListColumn c : this.columns)
			{
				// included?
				if (!c.included(context)) continue;

				Message title = c.getTitle();
				if (title != null)
				{
					// submit?
					boolean submit = c.getSortSubmit();

					// navigation render id for sort
					String sortId = id + "_s" + colNum;

					// if this is the sort column
					if ((c.getSortingDecision() != null) && (c.getSortingDecision().decide(context, focus)))
					{
						// show the asc or desc... each a nav to the sort asc or desc
						boolean asc = true;
						if ((c.getSortingAscDecision() != null) && (!c.getSortingAscDecision().decide(context, focus)))
						{
							asc = false;
						}

						String icon = null;
						String iconAlt = null;
						if (asc)
						{
							icon = c.getSortAscIcon();
							if (c.getSortAscMsg() != null) iconAlt = c.getSortAscMsg().getMessage(context, focus);
						}
						else
						{
							icon = c.getSortDescIcon();
							if (c.getSortDescMsg() != null) iconAlt = c.getSortDescMsg().getMessage(context, focus);
						}

						String destination = null;
						if (asc)
						{
							// we are already ascending, so encode the descending destination
							if (c.getSortDestinationDesc() != null) destination = c.getSortDestinationDesc().getDestination(context, focus);
						}
						else
						{
							// we are already descending, so encode the ascending destination
							if (c.getSortDestinationAsc() != null) destination = c.getSortDestinationAsc().getDestination(context, focus);
						}

						// TODO: do submit!
						UiNavigation.generateLinkScript(context, sortId, false, false, submit, destination, (String) context.get("sakai.return.url"),
								false);
						response.println("<th scope=\"col\""
								+ (c.getCentered() ? " style=\"text-align:center\"" : "")
								+ "><a href=\"#\" onclick=\"act_"
								+ sortId
								+ "();\">"
								+ Validator.escapeHtml(title.getMessage(context, focus))
								+ ((icon != null) ? ("&nbsp;<img src=\"" + context.getUrl(icon) + "\""
										+ ((iconAlt != null) ? (" alt=\"" + Validator.escapeHtml(iconAlt) + "\"") : "") + " />") : "") + "</a></th>");
					}

					// not currently sorting... can we sort?
					else if ((c.getSortingDecision() != null) && (c.getSortingAscDecision() != null) && (c.getSortDestinationAsc() != null)
							&& (c.getSortDestinationDesc() != null))
					{
						UiNavigation.generateLinkScript(context, sortId, false, false, submit, c.getSortDestinationAsc().getDestination(context,
								focus), (String) context.get("sakai.return.url"), false);
						response.println("<th scope=\"col\"" + (c.getCentered() ? " style=\"text-align:center\"" : "")
								+ "><a href=\"#\" onclick=\"act_" + sortId + "();\">" + Validator.escapeHtml(title.getMessage(context, focus))
								+ "</a></th>");
					}

					// no sort
					else
					{
						response.println("<th scope=\"col\"" + (c.getCentered() ? " style=\"text-align:center\"" : "") + ">"
								+ Validator.escapeHtml(title.getMessage(context, focus)) + "</th>");
					}
				}

				// for no title defined, put out a placeholder title
				else
				{
					response.println("<th scope=\"col\"" + (c.getCentered() ? " style=\"text-align:center\"" : "") + ">" + "" + "</th>");
				}

				colNum++;
			}

			response.println("</tr></thead>");
		}

		// keep track of footnotes we need to display after the list, mapped to the footmark used in the columns
		Map<Footnote, String> footnotes = new HashMap<Footnote, String>();

		// The mark characters for footnotes... TODO: better? -ggolden
		String footnoteMarks = "*^@$&!#";

		// track the row number (0 based)
		int row = -1;

		// data
		if (!empty)
		{
			int index = -1;
			for (Object entity : data)
			{
				index++;

				// place the context item
				if (this.iteratorName != null)
				{
					context.put(this.iteratorName, entity, this.iteratorReference.getEncoding(context, entity, index));
				}

				// check if this entity is to be included
				if ((this.entityIncluded != null) && (!this.entityIncluded.decide(context, entity))) continue;

				// track the row number
				row++;

				// insert any heading that applies, each as a separate row
				int h = 0;
				for (Decision headingDecision : this.headingDecisions)
				{
					if (headingDecision.decide(context, entity))
					{
						Message headingMessage = this.headingMessages.get(h);
						if (headingMessage != null)
						{
							response.println("<tr><td style=\"padding:1em;\" colspan=\"" + cols + "\">" + headingMessage.getMessage(context, entity)
									+ "</td></tr>");
						}

						else
						{
							Navigation nav = this.headingNavigations.get(h);
							if (nav != null)
							{
								response.print("<tr><td style=\"padding:1em;\" colspan=\"" + cols + "\">");
								nav.render(context, entity);
								response.println("</td></tr>");
							}
						}
					}
					h++;
				}

				response.println("<tr>");
				colNum = 0;
				for (EntityListColumn c : this.columns)
				{
					// included?
					if (!c.included(context)) continue;

					// will we need a summary row?
					if ((!summaryNeeded) && (c.isSummaryRequired()))
					{
						summaryNeeded = true;
					}

					response.print("<td style=\"");
					if (c.getWidth() != null)
					{
						response.print("width:" + c.getWidth().toString() + "px;");
					}
					else if (c.getWidthEm() != null)
					{
						response.print("width:" + c.getWidthEm().toString() + "em;");
					}
					if (c.getIsNoWrap())
					{
						response.print("white-space:nowrap;");
					}
					if (c.getCentered())
					{
						response.print("text-align:center;");
					}
					response.print("vertical-align:middle;\">");

					// if the entity is to be included in this column
					if (c.getIsEntityIncluded(context, entity))
					{
						// get our navigation anchor href, and if we are doing selection or not for this entity
						String href = c.getEntityNavigationDestination(context, entity);
						if (href != null)
						{
							String navId = id + "_r" + row + "_c_" + colNum;
							UiNavigation.generateLinkScript(context, navId, false, false, c.getEntityNavigationSubmit(), href, (String) context
									.get("sakai.return.url"), false);
							response.print("<a style=\"text-decoration:none !important\" href=\"#\" onclick=\"act_" + navId + "();\">");
						}

						// get the column's value for display
						String value = c.getDisplayText(context, entity, row, getId() + "_" + idRoot + "_" + colNum);

						// alert?
						boolean alert = c.alert(context, entity);

						// the display
						if (alert) response.print("<span class=\"ambrosiaAlertColor\">");

						if (value != null) response.print(value);

						if (alert) response.print("</span>");

						if (href != null)
						{
							response.print("</a>");
						}

						// footnote?
						for (Footnote footnote : c.getFootnotes())
						{
							if (footnote.apply(context, entity))
							{
								// have we dont this one yet? Add it if needed
								String mark = footnotes.get(footnote);
								if (mark == null)
								{
									mark = footnoteMarks.substring(0, 1);
									footnoteMarks = footnoteMarks.substring(1);
									footnotes.put(footnote, mark);
								}

								// mark the output
								response.print(" " + mark);
							}
						}

						// navigations
						if (!c.getNavigations().isEmpty())
						{
							response.print("<div class=\"ambrosiaUnderNav\" style=\"line-height: 1em;\">");
							for (Component navigation : c.getNavigations())
							{
								navigation.render(context, entity);
							}
							response.print("</div>");
						}
					}

					// otherwise show a message
					else if (c.getNotIncludedText() != null)
					{
						response.print(context.getMessages().getString(c.getNotIncludedText()));
					}

					response.println("</td>");

					colNum++;
				}
				response.println("</tr>");

				// remove the context item
				if (this.iteratorName != null)
				{
					context.remove(this.iteratorName);
				}
			}
		}

		// summary row
		if (summaryNeeded)
		{
			response.println("<tr>");
			for (EntityListColumn c : this.columns)
			{
				// included?
				if (!c.included(context)) continue;

				response.print("<td style=\"");
				if (c.getWidth() != null)
				{
					response.print("width:" + c.getWidth().toString() + "px;");
				}
				else if (c.getWidthEm() != null)
				{
					response.print("width:" + c.getWidthEm().toString() + "em;");
				}
				if (c.getIsNoWrap())
				{
					response.print("white-space:nowrap;");
				}
				if (c.getCentered())
				{
					response.print("text-align:center;");
				}
				response.print("vertical-align:middle;\">");

				if (c.isSummaryRequired())
				{
					c.renderSummary(context, focus);
				}

				response.println("</td>");
			}
			response.println("</tr>");
		}

		response.println("</table>");

		// columns one time text
		colNum = 0;
		for (EntityListColumn c : this.columns)
		{
			// included?
			if (!c.included(context)) continue;

			String text = c.getOneTimeText(context, focus, getId() + "_" + idRoot + "_" + colNum, row + 1);
			if (text != null)
			{
				response.println(text);
			}

			colNum++;
		}

		// empty title, if there is no data (or no entities passed the entityIncluded test so no rows were generated)
		if ((this.emptyTitle != null) && (empty || (row == -1)))
		{
			response.println("<div class =\"ambrosiaInstructions\">" + Validator.escapeHtml(this.emptyTitle.getMessage(context, focus)) + "</div>");
		}

		// footnotes
		for (Footnote f : footnotes.keySet())
		{
			if (f.getText() != null)
			{
				response.println("<div class =\"ambrosiaInstructions\">" + footnotes.get(f) + " "
						+ Validator.escapeHtml(f.getText().getMessage(context, focus)) + "</div>");
			}
		}

		response.println("</div>");
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityList setEmptyTitle(String selector, PropertyReference... properties)
	{
		this.emptyTitle = new UiMessage().setMessage(selector, properties);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityList setEntityIncluded(Decision inclusionDecision)
	{
		this.entityIncluded = inclusionDecision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityList setIterator(PropertyReference reference, String name)
	{
		this.iteratorReference = reference;
		this.iteratorName = name;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityList setPager(Pager pager)
	{
		this.pager = pager;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityList setStyle(Style style)
	{
		this.style = style;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityList setTitle(String selector, PropertyReference... properties)
	{
		this.title = new UiMessage().setMessage(selector, properties);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityList setTitleIncluded(Decision... decision)
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
	 * Render an entity action bar if any actions are defined for the list or its columns
	 * 
	 * @param context
	 *        The context.
	 * @param focus
	 *        The focus.
	 */
	protected void renderEntityActions(Context context, Object focus, int idRoot)
	{
		// collect the actions from the columns
		List<Component> actions = new ArrayList<Component>();
		for (EntityListColumn c : this.columns)
		{
			// included?
			if (!c.included(context)) continue;

			actions.addAll(c.getEntityActions());
		}

		// if we have none, do nothing
		if (actions.isEmpty() && this.entityActions.isEmpty() && (this.pager == null)) return;

		// render the bar
		// TODO: uniquely as an entity action bar, not a nav bar!
		PrintWriter response = context.getResponseWriter();

		// the bar
		// TODO: width?
		response.println("<div class=\"ambrosiaNavigationBar\">");

		// wrap the items
		response.println("<div class=\"ambrosiaNavigationItems\">");

		// render any column-related ones
		int colNum = 0;
		for (EntityListColumn col : this.columns)
		{
			// included?
			if (!col.included(context)) continue;

			// get the name to be used for this column
			String name = getId() + "_" + idRoot + "_" + colNum;

			// special setup in context for the related field
			context.put("ambrosia.navigation.related.id", name);

			for (Component c : col.getEntityActions())
			{
				c.render(context, focus);

				// add a divider
				response.println("<span class=\"ambrosiaDivider\">&nbsp;</span>");
			}

			// clear the related field in the context
			context.put("ambrosia.navigation.related.id", null);

			colNum++;
		}

		// divide the column ones from the general ones
		if (!actions.isEmpty() && !this.entityActions.isEmpty())
		{
			response.println("<span class=\"ambrosiaDivider\">&nbsp;</span>");
		}

		// render any general ones
		for (Component c : this.entityActions)
		{
			c.render(context, focus);

			// add a divider
			response.println("<span class=\"ambrosiaDivider\">&nbsp;</span>");
		}

		// divide again if we have a pager
		if ((!this.entityActions.isEmpty()) || (!actions.isEmpty()))
		{
			response.println("<span class=\"ambrosiaDivider\">&nbsp;</span>");
		}

		// render the pager
		if (this.pager != null) this.pager.render(context, focus);

		response.println("</div></div>");
	}
}
