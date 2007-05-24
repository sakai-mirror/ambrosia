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

import java.util.List;

import org.muse.ambrosia.api.Alias;
import org.muse.ambrosia.api.Context;
import org.muse.ambrosia.api.Controller;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiAlias presents implements Alias.
 */
public class UiAlias extends UiController implements Alias
{
	/** The id of the controller to render. */
	protected String to = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiAlias()
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
	protected UiAlias(UiServiceImpl service, Element xml)
	{
		super(service, xml);

		// to
		String to = StringUtil.trimToNull(xml.getAttribute("to"));
		if (to != null) setTo(to);
	}

	/**
	 * {@inheritDoc}
	 */
	public void render(Context context, Object focus)
	{
		if (this.to == null) return;

		// included?
		if (!isIncluded(context, focus)) return;

		// find the other guys
		List<Controller> controllers = context.findControllers(this.to);

		// and render them
		for (Controller c : controllers)
		{
			// block the infinite loop of rendering me!
			if (c != this)
			{
				c.render(context, focus);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Alias setTo(String to)
	{
		this.to = to;
		return this;
	}
}
