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

import org.muse.ambrosia.api.Component;
import org.muse.ambrosia.api.Context;
import org.muse.ambrosia.api.ModeBar;
import org.muse.ambrosia.api.NavigationBar;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiModeBar implements ModeBar
 */
public class UiModeBar extends UiSection implements ModeBar
{
	/** The width (in some css value such as "60em" or "100px" or "90%" etc.) */
	protected String width = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiModeBar()
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
	protected UiModeBar(UiServiceImpl service, Element xml)
	{
		// do the container thing
		super(service, xml);

		String width = StringUtil.trimToNull(xml.getAttribute("width"));
		if (width != null) this.width = width;
	}

	/**
	 * Render the navigation bar
	 * 
	 * @param context
	 *        The context.
	 * @param focus
	 *        The focus object.
	 */
	protected void renderContents(Context context, Object focus)
	{
		// TODO: uniquely as a mode bar, not a nav bar!
		PrintWriter response = context.getResponseWriter();

		// the bar
		response.println("<div class=\"ambrosiaNavigationBar\"" + (this.width != null ? (" style=\"width: " + this.width + ";\"") : "") + ">");

		// wrap the items
		response.println("<div class=\"ambrosiaNavigationItems\">");

		// render
		for (Component c : this.contained)
		{
			c.render(context, focus);
		}

		response.println("</div></div>");
	}
}
