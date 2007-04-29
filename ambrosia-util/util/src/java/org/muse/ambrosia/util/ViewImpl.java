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

package org.muse.ambrosia.util;

import org.muse.ambrosia.api.UiService;
import org.muse.ambrosia.api.View;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.util.ResourceLoader;

/**
 * A View
 */
public abstract class ViewImpl implements View
{
	/** ui service reference. */
	protected UiService ui = null;

	/** The tool id. */
	protected String toolId = null;

	/** The view id. */
	protected String viewId = null;

	/** Localized messages. */
	protected InternationalizedMessages messages = null;

	/** messages bundle name. */
	protected String bundle = null;

	/**
	 * {@inheritDoc}
	 */
	public String getId()
	{
		return viewId;
	}

	/**
	 * {@inheritDoc}
	 */
	public InternationalizedMessages getMessages()
	{
		return this.messages;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		// register
		this.ui.registerView(this, this.toolId);

		// messages
		this.messages = new ResourceLoader(this.bundle);
	}

	/**
	 * Set the message bundle.
	 * 
	 * @param bundle
	 *        The message bundle.
	 */
	public void setBundle(String name)
	{
		this.bundle = name;
	}

	/**
	 * Set the tool id.
	 * 
	 * @param id
	 *        The tool id.
	 */
	public void setToolId(String id)
	{
		this.toolId = id;
	}

	/**
	 * Set the UI service.
	 * 
	 * @param service
	 *        The UI service.
	 */
	public void setUi(UiService service)
	{
		this.ui = service;
	}

	/**
	 * Set the vew id.
	 * 
	 * @param id
	 *        The view id.
	 */
	public void setViewId(String id)
	{
		this.viewId = id;
	}
}
