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

/**
 * FloatEdit presents a float input for the user to edit a float.
 */
public interface FloatEdit extends Component
{
	/**
	 * Set a decision to enable on-load cursor focus on this field.
	 * 
	 * @param decision
	 *        The decision.
	 * @return self.
	 */
	FloatEdit setFocus(Decision decision);

	/**
	 * Set an icon
	 * 
	 * @param url
	 *        The full URL to the icon
	 * @param selector
	 *        The message selector (for the alt text for the icon).
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	// CountEdit setIcon(String url, String selector, PropertyReference... references);
	/**
	 * Set an alert that will triger once on submit if the field is empty.
	 * 
	 * @param decision
	 *        The decision to include the alert (if null, the alert is unconditionally included).
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of PropertyReferences to form the additional values in the formatted message.
	 */
	FloatEdit setOnEmptyAlert(Decision decision, String selector, PropertyReference... references);

	/**
	 * Set the property reference for the encode / decode.
	 * 
	 * @param propertyReference
	 *        The property reference for encode / decode.
	 */
	FloatEdit setProperty(PropertyReference propertyReference);

	/**
	 * Set the read-only decision.
	 * 
	 * @param decision
	 *        The read-only decision.
	 * @return self.
	 */
	FloatEdit setReadOnly(Decision decision);

	/**
	 * Set the size of the text edit box in columns.
	 * 
	 * @param cols
	 *        The number of columns to show.
	 * @return self.
	 */
	FloatEdit setSize(int cols);

	/**
	 * Set the component id that the value in this component will sum to in the UI.
	 * 
	 * @param id
	 *        The component id that the value in this component will sum to in the UI.
	 * @return self.
	 */
	FloatEdit setSumToId(String id);

	/**
	 * Set the title text.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 */
	FloatEdit setTitle(String selector, PropertyReference... references);
}
