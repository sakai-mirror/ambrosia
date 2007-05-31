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
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.muse.ambrosia.api.Context;
import org.muse.ambrosia.api.UiService;
import org.muse.ambrosia.api.View;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Web;

/**
 * A Simple Sludge Servlet Sakai Sample tool...
 */
public class AmbrosiaServlet extends HttpServlet
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(AmbrosiaServlet.class);

	/** Localized messages. */
	protected static ResourceLoader messages = new ResourceLoader("mneme-test-tool");

	/** Our self-injected ui service reference. */
	protected UiService ui = null;

	/** Set of static resource paths. */
	protected Set<String> resourcePaths = new HashSet<String>();

	/** The default view. */
	protected String defaultView = null;

	/** The error view. */
	protected String errorView = "error";

	/** The tool id for this tool. */
	protected String toolId = null;

	/**
	 * Shutdown the servlet.
	 */
	public void destroy()
	{
		M_log.info("destroy()");

		super.destroy();
	}

	/**
	 * Access the Servlet's information display.
	 * 
	 * @return servlet information.
	 */
	public String getServletInfo()
	{
		return "Ambrosia";
	}

	/**
	 * Initialize the servlet.
	 * 
	 * @param config
	 *        The servlet config.
	 * @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		// toolId, resourcePaths, defaultView
		this.toolId = config.getInitParameter("toolId");

		String resourcePaths = config.getInitParameter("resourcePaths");
		if (resourcePaths != null) this.resourcePaths.add(resourcePaths);

		this.defaultView = config.getInitParameter("defaultView");

		String errorView = config.getInitParameter("errorView");
		if (errorView != null) this.errorView = errorView;

		// self-inject
		ui = (UiService) ComponentManager.get(UiService.class);

		M_log.info("init()");
	}

	/**
	 * Respond to requests.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @throws ServletException.
	 * @throws IOException.
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// handle static resource requests
		if (ui.dispatchResource(req, res, getServletContext(), this.resourcePaths)) return;

		// // special courier handling
		// if ((req.getPathInfo() != null) && (req.getPathInfo().startsWith("/" + Destinations.courier.toString())))
		// {
		// courierGet(req, res);
		// return;
		// }

		// handle pathless requests
		if (ui.redirectToCurrentDestination(req, res, this.defaultView)) return;

		Context context = ui.prepareGet(req, res, null, this.defaultView);

		// get and split up the tool destination: 0 parts means must "/", otherwise parts[0] = "", parts[1] = the first part, etc.
		String path = context.getDestination();
		String[] parts = context.getDestination().split("/");

		// which destination?
		View destination = ui.getView(parts[1], this.toolId);
		if (destination == null)
		{
			M_log.warn("doGet: no view registered for: " + parts[1] + " tool: " + this.toolId);
			redirectError(req, res);
		}

		else
		{
			try
			{
				context.setMessages(destination.getMessages());
				destination.get(req, res, context, parts);
			}
			catch (IllegalArgumentException e)
			{
				redirectError(req, res);
			}
		}
	}

	/**
	 * Respond to requests.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @throws ServletException.
	 * @throws IOException.
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		Context context = ui.preparePost(req, res, null, this.defaultView);

		// get and split up the tool destination: 0 parts means must "/", otherwise parts[0] = "", parts[1] = the first part, etc.
		String path = context.getDestination();
		String[] parts = context.getDestination().split("/");

		// which destination?
		View destination = ui.getView(parts[1], this.toolId);
		if (destination == null)
		{
			redirectError(req, res);
		}

		else
		{
			try
			{
				context.setMessages(destination.getMessages());
				destination.post(req, res, context, parts);
			}
			catch (IllegalArgumentException e)
			{
				// redirect to error
				redirectError(req, res);
			}
		}
	}

	/**
	 * Send a redirect to the error destination.
	 * 
	 * @param req
	 * @param res
	 * @throws IOException
	 */
	protected void redirectError(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		String error = Web.returnUrl(req, "/" + this.errorView);
		res.sendRedirect(res.encodeRedirectURL(error));
	}
}
