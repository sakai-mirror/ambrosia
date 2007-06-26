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
import org.muse.ambrosia.api.Component;
import org.muse.ambrosia.api.Decision;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiComponent implements Component.
 */
public class UiComponent implements Component
{
	/** The id of this element - can be referenced by an alias, for instance. */
	protected String id = null;

	/** The include decision. */
	protected Decision included = null;

	/**
	 * Public no-arg constructor.
	 */
	public UiComponent()
	{
	}

	/**
	 * Construct from a dom element.
	 * 
	 * @param service
	 *        The UiService.
	 * @param xml
	 *        The dom element.
	 */
	protected UiComponent(UiServiceImpl service, Element xml)
	{
		// included decisions
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "included");
		if (settingsXml != null)
		{
			this.included = service.parseDecisions(settingsXml);
		}

		// id
		String id = StringUtil.trimToNull(xml.getAttribute("id"));
		if (id != null) setId(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId()
	{
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isIncluded(Context context, Object focus)
	{
		if (this.included == null) return true;
		return this.included.decide(context, focus);
	}

	/**
	 * {@inheritDoc}
	 */
	public void render(Context context, Object focus)
	{
	}

	/**
	 * {@inheritDoc}
	 */
	public Component setId(String id)
	{
		this.id = id;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public Component setIncluded(Decision... decision)
	{
		if (decision != null)
		{
			if (decision.length == 1)
			{
				this.included = decision[0];
			}

			else
			{
				this.included = new UiAndDecision().setRequirements(decision);
			}
		}

		return this;
	}
}
