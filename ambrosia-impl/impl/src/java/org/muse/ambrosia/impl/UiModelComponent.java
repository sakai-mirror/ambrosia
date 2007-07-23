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

import org.muse.ambrosia.api.Component;
import org.muse.ambrosia.api.Context;
import org.muse.ambrosia.api.Message;
import org.muse.ambrosia.api.ModelComponent;
import org.muse.ambrosia.api.PropertyReference;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiTextEdit presents a text input for the user to edit.
 */
public class UiModelComponent extends UiComponent implements ModelComponent
{
	protected Message onEmptyAlertMsg = null;

	/** The PropertyReference for finding the UI component. */
	protected PropertyReference propertyReference = null;

	/**
	 * No-arg constructor.
	 */
	public UiModelComponent()
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
	protected UiModelComponent(UiServiceImpl service, Element xml)
	{
		// component stuff
		super(service, xml);

		// short for model
		String model = StringUtil.trimToNull(xml.getAttribute("model"));
		if (model != null)
		{
			PropertyReference pRef = service.newPropertyReference().setReference(model);
			setProperty(pRef);
		}

		// model
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "model");
		if (settingsXml != null)
		{
			PropertyReference pRef = service.parsePropertyReference(settingsXml);
			if (pRef != null) setProperty(pRef);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void render(Context context, Object focus)
	{
		// included?
		if (!isIncluded(context, focus)) return;

		// defined?
		if (this.propertyReference == null) return;

		// read the reference as an object
		Object o = this.propertyReference.readObject(context, focus);

		// did we get a Component?
		if (o == null) return;
		if (!(o instanceof Component)) return;

		// render it
		Component component = (Component) o;
		component.render(context, focus);
	}

	/**
	 * {@inheritDoc}
	 */
	public ModelComponent setProperty(PropertyReference propertyReference)
	{
		this.propertyReference = propertyReference;
		return this;
	}
}
