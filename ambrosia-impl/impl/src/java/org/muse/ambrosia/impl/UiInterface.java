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
import org.muse.ambrosia.api.Interface;
import org.muse.ambrosia.api.Message;
import org.muse.ambrosia.api.ModeBar;
import org.muse.ambrosia.api.PropertyReference;
import org.muse.ambrosia.api.UiService;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * UiInterface implements Interface.
 */
public class UiInterface extends UiContainer implements Interface
{
	/** The message selector and properties for the footer. */
	protected Message footer = null;

	/** Components contained in the footer. */
	protected List<Component> footerComponents = new ArrayList<Component>();

	/** The message selector and properties for the header. */
	protected Message header = null;

	/** Components contained in the header. */
	protected List<Component> headerComponents = new ArrayList<Component>();

	/** Components contained in the mode container. */
	protected List<Component> modeContainer = new ArrayList<Component>();

	/** If we want to disable browser auto-complete. */
	protected boolean noAutoComplete = false;

	/** The message selector and properties for the sub-header. */
	protected Message subHeader = null;

	/** Components contained in the sub-header. */
	protected List<Component> subHeaderComponents = new ArrayList<Component>();

	/** The message selector and properties for the title. */
	protected Message title = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiInterface()
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
	protected UiInterface(UiServiceImpl service, Element xml)
	{
		// do the container thing
		super(service, xml);

		// short form for title - attribute "title" as the selector
		String title = StringUtil.trimToNull(xml.getAttribute("title"));
		if (title != null)
		{
			setTitle(title);
		}

		// short form for header - attribute "header" as the selector
		String header = StringUtil.trimToNull(xml.getAttribute("header"));
		if (header != null)
		{
			setHeader(header);
		}

		// short form for sub-header - attribute "subHeader" as the selector
		String subHeader = StringUtil.trimToNull(xml.getAttribute("subHeader"));
		if (subHeader != null)
		{
			setSubHeader(subHeader);
		}

		// short form for footer - attribute "footer" as the selector
		String footer = StringUtil.trimToNull(xml.getAttribute("footer"));
		if (footer != null)
		{
			setFooter(footer);
		}

		String autoComplete = StringUtil.trimToNull(xml.getAttribute("autoComplete"));
		if ((autoComplete != null) && ("FALSE".equals(autoComplete)))
		{
			setNoAutoComplete();
		}

		// sub-element configuration
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "title");
		if (settingsXml != null)
		{
			// let Message parse this
			this.title = new UiMessage(service, settingsXml);
		}

		settingsXml = XmlHelper.getChildElementNamed(xml, "header");
		if (settingsXml != null)
		{
			// let Message parse this
			this.header = new UiMessage(service, settingsXml);

			// contained
			Element container = XmlHelper.getChildElementNamed(settingsXml, "container");
			if (container != null)
			{
				NodeList contained = container.getChildNodes();
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
							addHeader(c);
						}
					}
				}
			}
		}

		settingsXml = XmlHelper.getChildElementNamed(xml, "subHeader");
		if (settingsXml != null)
		{
			// let Message parse this
			this.subHeader = new UiMessage(service, settingsXml);

			// contained
			Element container = XmlHelper.getChildElementNamed(settingsXml, "container");
			if (container != null)
			{
				NodeList contained = container.getChildNodes();
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
							addSubHeader(c);
						}
					}
				}
			}
		}

		settingsXml = XmlHelper.getChildElementNamed(xml, "footer");
		if (settingsXml != null)
		{
			// let Message parse this
			this.footer = new UiMessage(service, settingsXml);

			// contained
			Element container = XmlHelper.getChildElementNamed(settingsXml, "container");
			if (container != null)
			{
				NodeList contained = container.getChildNodes();
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
							addFooter(c);
						}
					}
				}
			}
		}

		settingsXml = XmlHelper.getChildElementNamed(xml, "modeContainer");
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
						this.modeContainer.add(c);
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Interface add(Component component)
	{
		super.add(component);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Interface addFooter(Component c)
	{
		this.footerComponents.add(c);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Interface addHeader(Component c)
	{
		this.headerComponents.add(c);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Interface addSubHeader(Component c)
	{
		this.subHeaderComponents.add(c);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		PrintWriter response = context.getResponseWriter();

		boolean fragment = ((Boolean) context.get(Context.FRAGMENT)).booleanValue();

		// start
		if (!fragment)
		{
			response
					.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
			response.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
			response.println("<head>");
			response.println("<meta http-equiv=\"Content-Style-Type\" content=\"text/css\" />");

			// include the portal's stuff in head (css and js links)
			String headInclude = (String) context.get("sakai.html.head");
			if (headInclude != null)
			{
				response.println(headInclude);
			}

			// our js
			response.println("<script type=\"text/javascript\" language=\"JavaScript\" src=\"/ambrosia_library/js/ambrosia_" + UiService.VERSION
					+ ".js\"></script>\n");

			// for rich editing
			response.println("<script type=\"text/javascript\" language=\"JavaScript\" src=\"/tiny_mce/tiny_mce/tiny_mce.js" + "\"></script>\n");

			// our css
			response.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/ambrosia_library/skin/ambrosia_" + UiService.VERSION + ".css\" />");

			// use our title
			// TODO: we might want to send in the placement title and deal with that...
			response.print("<title>");
			if (this.title != null)
			{
				response.print(Validator.escapeHtml(this.title.getMessage(context, focus)));
			}
			response.println("</title>");

			response.print("</head><body");

			// do the body the portal wants to do
			String onload = (String) context.get("sakai.html.body.onload");

			// if we didn't get an onload, add one for stand-alone
			if (onload == null)
			{
				onload = "setMainFrameHeight('');";
			}

			response.print(" onload=\"" + onload + "\"");

			response.println(">");
		}

		// for Safari, add a frame so that we don't get stuck in their back/forward cache (see
		// http://developer.apple.com/internet/safari/faq.html#anchor5)
		response.println("<iframe style=\"height:0px;width:0px;visibility:hidden\" src=\"about:blank\">");
		response.println("this frame prevents back forward cache in Safari");
		response.println("</iframe>");

		// pick a name for the form, and store this name in the context for other components to reference.
		String name = "form" + context.getUniqueId();
		context.setFormName(name);

		// wrap up in a form - back to the current destination
		String href = (String) context.get("sakai.destination.url");
		response.println("<div class=\"ambrosiaInterface\">");
		response.println("<form name=\"" + name + "\" method=\"post\" action=\"" + href
				+ "\" enctype=\"multipart/form-data\" onsubmit=\"return validate();\" " + (this.noAutoComplete ? "autocomplete=\"off\" " : "") + ">");

		// put in a hidden field that can be set with a tool destination (for use when submitting the form without a normal submit
		// button
		response.println("<input type=\"hidden\" name =\"" + "destination_" + "\" value=\"\" />");

		// mode bar components, if defined
		for (Component component : this.modeContainer)
		{
			component.render(context, focus);
		}

		// header, if defined
		if ((this.header != null) || (!this.headerComponents.isEmpty()))
		{
			response.println("<div class=\"ambrosiaInterfaceHeader\">");

			// the message, if defiend
			if (this.header != null)
			{
				response.println(this.header.getMessage(context, focus));
			}

			// the components, if defined
			for (Component c : this.headerComponents)
			{
				c.render(context, focus);
			}

			response.println("</div>");
		}

		// sub-header, even if not defined
		response.println("<div class=\"ambrosiaInterfaceSubHeader\">");
		if ((this.subHeader != null) || (!this.subHeaderComponents.isEmpty()))
		{
			// the message, if defiend
			if (this.subHeader != null)
			{
				response.println(this.subHeader.getMessage(context, focus));
			}

			// the components, if defined
			for (Component c : this.subHeaderComponents)
			{
				response.println("<div>");
				c.render(context, focus);
				response.println("</div>");
			}
		}
		response.println("</div>");

		// body... being a container, let the base class render the contained
		super.render(context, focus);

		// footer, even if not defined
		response.println("<div class=\"ambrosiaInterfaceFooter\">");
		if ((this.footer != null) || (!this.footerComponents.isEmpty()))
		{
			// the message, if defiend
			if (this.footer != null)
			{
				response.println(this.footer.getMessage(context, focus));
			}

			// the components, if defined
			for (Component c : this.footerComponents)
			{
				response.println("<div>");
				c.render(context, focus);
				response.println("</div>");
			}
		}
		response.println("</div>");

		// end
		response.println("</form></div>");

		// scripts
		response.println("<script language=\"JavaScript\">");
		
		// for tiny_mce
		response.println("tinyMCE.init({mode : \"textareas\",editor_selector : \"ambrosiaHtmlEdit\",});");

		// validation
		response.println("var enableValidate=true;");
		response.println("function validate()");
		response.println("{");
		response.println("  if (!enableValidate) return true;");
		response.println("  var rv=true;");

		String validation = context.getValidation();
		if (validation != null)
		{
			response.println(validation);
		}

		// reset the enableValidate, so the next submit skips validation
		response.println("  enableValidate=false;\n");

		response.println("  return rv;");
		response.println("}");

		// add a variable that components can use to set / test if we have submitted already
		response.println("var submitted=false;");

		// add any other script we have accumulated
		String script = context.getScript();
		if (script != null)
		{
			response.println(script);
		}

		// and any focus path ids
		List<String> focusIds = context.getFocusIds();
		if ((focusIds != null) && (!focusIds.isEmpty()))
		{
			StringBuffer buf = new StringBuffer();
			for (String id : focusIds)
			{
				buf.append("\"" + id + "\",");
			}
			buf.setLength(buf.length() - 1);

			response.println("focus_path = [" + buf.toString() + "];");
		}

		response.println("</script>");

		// the blocker for disabling the screen after a navigation
		// Note: set to 0, can set to like 0.1 for a visual effect, perhaps with a color of #BBBBBB for a very slight visual effect
		// -ggolden
		// response
		// .println("<div id=\"blocker\" style=\"width:100%; height:100%; position:absolute; left:0px; top:0px; background-color:#000000; opacity:0;
		// display:none;\"></div>");

		if (!fragment)
		{
			response.println("</body></html>");
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Interface setFooter(String selector, PropertyReference... references)
	{
		this.footer = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Interface setHeader(String selector, PropertyReference... references)
	{
		this.header = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Interface setModeBar(Component bar)
	{
		this.modeContainer.add(bar);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Interface setNoAutoComplete()
	{
		this.noAutoComplete = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Interface setSubHeader(String selector, PropertyReference... references)
	{
		this.subHeader = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Interface setTitle(String selector, PropertyReference... references)
	{
		this.title = new UiMessage().setMessage(selector, references);
		return this;
	}
}
