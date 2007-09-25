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
 * Selection presents a selection of one or more options for the user to select.
 */
public interface Selection extends Component
{
	/** orientation. */
	enum Orientation
	{
		horizontal, vertical
	};

	/**
	 * Add a component to the container for a particular selection, identified by the value.
	 * 
	 * @param value
	 *        The selection's value.
	 * @param component
	 *        the component to add.
	 */
	Selection addComponentToSelection(String value, Component component);

	/**
	 * Add a selection, one more choice the user can select.
	 * 
	 * @param selector
	 *        The message selector to display.
	 * @param value
	 *        The value to return if selected.
	 * @return self.
	 */
	Selection addSelection(String selector, String value);

	/**
	 * Set the property reference for the correct value.
	 * 
	 * @param propertyReference
	 *        The property reference for the correct value.
	 * @return self.
	 */
	Selection setCorrect(PropertyReference correctReference);

	/**
	 * Set the decision for including the correct markers.
	 * 
	 * @param decision
	 *        The decision for including the correct markers.
	 * @return self.
	 */
	Selection setCorrectDecision(Decision decision);

	/**
	 * Set the orientation of multiple selection choices.
	 * 
	 * @param orientation
	 *        The orientation.
	 * @return self.
	 */
	Selection setOrientation(Orientation orientation);

	/**
	 * Set the property reference for the encode / decode.
	 * 
	 * @param propertyReference
	 *        The property reference for encode / decode.
	 */
	Selection setProperty(PropertyReference propertyReference);

	/**
	 * Set the read-only decision.
	 * 
	 * @param decision
	 *        The read-only decision.
	 * @return self.
	 */
	Selection setReadOnly(Decision decision);

	/**
	 * Set the value that is decoded when the user makes the selection.
	 * 
	 * @param value
	 *        The value decoded when the user make the selection.
	 * @return self.
	 */
	Selection setSelectedValue(String value);

	/**
	 * Set the title text.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	Selection setTitle(String selector, PropertyReference... references);
}
