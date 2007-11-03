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

import org.muse.ambrosia.api.Context;
import org.muse.ambrosia.api.IconPropertyReference;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Element;

/**
 * UiIconPropertyReference implements IconPropertyReference
 */
public class UiIconPropertyReference extends UiPropertyReference implements IconPropertyReference
{
	protected String name = null;

	/**
	 * No-arg constructor.
	 */
	public UiIconPropertyReference()
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
	protected UiIconPropertyReference(UiServiceImpl service, Element xml)
	{
		// do the property reference stuff
		super(service, xml);

		// icon
		String icon = StringUtil.trimToNull(xml.getAttribute("icon"));
		if (icon != null) setIcon(icon);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "icon";
	}

	/**
	 * {@inheritDoc}
	 */
	public String read(Context context, Object focus)
	{
		String iconName = this.name;

		// if the name is not set, see if we can get a value from the ref
		if (iconName == null)
		{
			iconName = super.read(context, focus);
		}

		// alt=\"" + Validator.escapeHtml(name) + "\"
		return "<img src=\"" + context.getUrl(iconName) + "\" />";
	}

	/**
	 * {@inheritDoc}
	 */
	public Object readObject(Context context, Object focus)
	{
		// if the name is not set, see if we can get a value from the ref
		if (this.name == null)
		{
			return super.readObject(context, focus);
		}

		return read(context, focus);
	}

	/**
	 * {@inheritDoc}
	 */
	public IconPropertyReference setIcon(String name)
	{
		this.name = name;
		return this;
	}
}
