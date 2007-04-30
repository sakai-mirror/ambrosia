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

import java.io.IOException;

import org.muse.ambrosia.api.Controller;
import org.muse.ambrosia.api.UiService;
import org.muse.ambrosia.api.View;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.core.io.ClassPathResource;

/**
 * A View
 */
public abstract class ViewImpl implements View
{
	/** Messages bundle name. */
	protected String bundle = null;

	/** Localized messages. */
	protected InternationalizedMessages messages = null;

	/** The tool id. */
	protected String toolId = null;

	/** The UI. */
	protected Controller ui = null;

	/** ui service reference. */
	protected UiService uiService = null;

	/** The view id. */
	protected String viewId = null;

	/** The view declaration xml path. */
	protected String viewPath = null;

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
		this.uiService.registerView(this, this.toolId);

		// messages
		this.messages = new ResourceLoader(this.bundle);

		// interface from XML in the class path
		if (viewPath != null)
		{
			try
			{
				ClassPathResource rsrc = new ClassPathResource(viewPath);
				this.ui = uiService.newInterface(rsrc.getInputStream());
			}
			catch (IOException e)
			{
			}
		}
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
		this.uiService = service;
	}

	/**
	 * Set the view id.
	 * 
	 * @param id
	 *        The view id.
	 */
	public void setViewId(String id)
	{
		this.viewId = id;
	}

	/**
	 * Set the view declaration xml path.
	 * 
	 * @param path
	 *        The view declaration xml path.
	 */
	public void setViewPath(String path)
	{
		this.viewPath = path;
	}
}
