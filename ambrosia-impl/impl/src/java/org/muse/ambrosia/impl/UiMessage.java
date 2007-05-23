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

import java.util.ArrayList;
import java.util.List;

import org.muse.ambrosia.api.Context;
import org.muse.ambrosia.api.Message;
import org.muse.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * UiMessage is a message from the message bundle that can have property reference parameters.
 */
public class UiMessage implements Message
{
	/** A set of additional properties to put in the message. */
	protected PropertyReference[] references = null;

	/** The message selector. */
	protected String selector = null;

	/**
	 * {@inheritDoc}
	 */
	public Message setMessage(String selector, PropertyReference... references)
	{
		this.selector = selector;
		this.references = references;
		return this;
	}
	
	/**
	 * Public no-arg constructor.
	 */
	public UiMessage()
	{
		
	}
	
	/**
	 * Construct from a dom element.  The definition may be in the element or in a child.
	 * 
	 * @param service
	 *        the UiService.
	 * @param xml
	 *        The dom element.
	 */
	protected UiMessage(UiServiceImpl service, Element xml)
	{
		// xml may be tag name "message".  If not, find one in the children
		if (!xml.getTagName().equals("message"))
		{
			xml = XmlHelper.getChildElementNamed(xml, "message");
		}
		
		if (xml != null)
		{
			String selector = StringUtil.trimToNull(xml.getAttribute("selector"));
			
			// use all the direct model references
			List<PropertyReference> refs = new ArrayList<PropertyReference>();
			NodeList settings = xml.getChildNodes();
			for (int i = 0; i < settings.getLength(); i++)
			{
				Node node = settings.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element settingsXml = (Element) node;
					PropertyReference pRef = service.parsePropertyReference(settingsXml);
					if (pRef != null) refs.add(pRef);
				}
			}
			
			// convert the refs into an array
			PropertyReference[] refsArray = new PropertyReference[0];
			refsArray = refs.toArray(refsArray);
	
			// set
			this.selector = selector;
			this.references = refsArray;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage(Context context, Object focus)
	{
		// if no references, use just the selector message
		if ((references == null) || (references.length == 0))
		{
			if (selector != null)
			{
				return context.getMessages().getString(selector);
			}
			return null;
		}
		
		// if there is no selector, just read the first reference as the value
		if (selector == null)
		{
			if ((references != null) && (references.length == 1))
			{
				return references[0].read(context, focus);
			}
			return null;
		}

		// put the property reference into args for the message
		Object args[] = new Object[references.length];
		int i = 0;
		for (PropertyReference reference : references)
		{
			String value = reference.read(context, focus);
			
			// if any are null, null the entire message
			// TODO: make this an option rather than default behavior? -ggolden
			// if (value == null) value = "";
			if (value == null) return null;

			args[i++] = value;
		}

		return context.getMessages().getFormattedMessage(selector, args);
	}
}
