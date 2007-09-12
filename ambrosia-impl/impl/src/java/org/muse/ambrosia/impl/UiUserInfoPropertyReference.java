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
import org.muse.ambrosia.api.UserInfoPropertyReference;
import org.muse.ambrosia.api.ContextInfoPropertyReference.Selector;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Element;

/**
 * UiUserInfoPropertyReference handles user id values by providing some user information for the display.
 */
public class UiUserInfoPropertyReference extends UiPropertyReference implements UserInfoPropertyReference
{
	/** The user info we want. */
	protected UserInfoPropertyReference.Selector selector = UserInfoPropertyReference.Selector.displayName;

	/**
	 * No-arg constructor.
	 */
	public UiUserInfoPropertyReference()
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
	protected UiUserInfoPropertyReference(UiServiceImpl service, Element xml)
	{
		// property reference stuff
		super(service, xml);

		// selector
		String selector = StringUtil.trimToNull(xml.getAttribute("selector"));
		if ("DISPLAYNAME".equals(selector)) setSelector(Selector.displayName);
		if ("SORTNAME".equals(selector)) setSelector(Selector.sortName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "userInfo";
	}

	/**
	 * {@inheritDoc}
	 */
	public UserInfoPropertyReference setSelector(UserInfoPropertyReference.Selector property)
	{
		this.selector = property;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	protected String format(Context context, Object value)
	{
		if (value == null) return super.format(context, value);
		if (!(value instanceof String)) return super.format(context, value);

		try
		{
			User user = UserDirectoryService.getUser((String) value);

			String selected = null;

			switch (this.selector)
			{
				case displayName:
				{
					selected = user.getDisplayName();
					break;
				}
				case sortName:
				{
					selected = user.getSortName();
					break;
				}
			}
			return Validator.escapeHtml(selected);
		}
		catch (UserNotDefinedException e)
		{
			return Validator.escapeHtml((String) value);
		}
	}
}
