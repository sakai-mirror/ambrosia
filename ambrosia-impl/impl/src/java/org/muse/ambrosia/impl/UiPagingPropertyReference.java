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
import org.muse.ambrosia.api.Paging;
import org.muse.ambrosia.api.PagingPropertyReference;
import org.w3c.dom.Element;

/**
 * UiPagingPropertyReference implements PagingPropertyReference.
 */
public class UiPagingPropertyReference extends UiPropertyReference implements PagingPropertyReference
{
	/**
	 * No-arg constructor.
	 */
	public UiPagingPropertyReference()
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
	protected UiPagingPropertyReference(UiServiceImpl service, Element xml)
	{
		// do the property reference stuff
		super(service, xml);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "paging";
	}

	/**
	 * {@inheritDoc}
	 */
	public Object readObject(Context context, Object focus)
	{
		Object rv = super.readObject(context, focus);

		if (rv instanceof Paging)
		{
			Paging p = (Paging) rv;

			String selector = (String) context.get(SELECTOR);
			if (selector != null)
			{
				if (FIRST.equals(selector))
				{
					rv = p.getFirst();
				}
				else if (PREV.equals(selector))
				{
					rv = p.getPrev();
				}
				else if (NEXT.equals(selector))
				{
					rv = p.getNext();
				}
				else if (LAST.equals(selector))
				{
					rv = p.getLast();
				}
				else if (SIZE.equals(selector))
				{
					Integer size = (Integer) context.get(SELECTOR_SIZE);
					rv = p.resize(size);
				}
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	protected String format(Context context, Object value)
	{
		if (!(value instanceof Paging)) return super.format(context, value);
		Paging p = (Paging) value;

		// encode current and size
		return p.getCurrent().toString() + "-" + p.getSize().toString();
	}
}
