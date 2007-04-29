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

package org.muse.ambrosia.api;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.i18n.InternationalizedMessages;

/**
 * View handles a single view of the UI.
 */
public interface View
{
	/**
	 * Handle GET
	 * 
	 * @param req
	 * @param res
	 * @param context
	 * @param params
	 */
	void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException;

	/**
	 * Handle POST
	 * 
	 * @param req
	 * @param res
	 * @param context
	 * @param params
	 */
	void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException;

	/**
	 * Access the view id. This forms the view's place in the tool URL space.
	 * 
	 * @return The view id.
	 */
	String getId();

	/**
	 * Get the messages for the view.
	 * 
	 * @return The messages for the view.
	 */
	InternationalizedMessages getMessages();
}
