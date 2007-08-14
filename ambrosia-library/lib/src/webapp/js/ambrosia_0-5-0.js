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

// functions for Ambrosia applications

function trim(s)
{
	return s.replace(/^\s+/g, "").replace(/\s+$/g, "");
}

function showConfirm(name)
{
	el = document.getElementById(name);
	if (el.style.display == "none")
	{
		el.style.left = ((document.body.scrollWidth / 2) - (parseInt(el.style.width) / 2)) + "px";
		el.style.top = (-1 * (parseInt(el.style.height) + 10)) + "px";
		if (parent) parent.window.scrollTo(0,0);
		window.scrollTo(0,0);
	}
	el.style.display = "";

	if (parseInt(el.style.top) < -10)
	{
		el.style.top = (parseInt(el.style.top) + 10) + "px";
		setTimeout("showConfirm('" + name + "')",10);
	}
	else
	{
		el.style.top = "0px";
	}
}

var confirmedAction="";

function hideConfirm(name, action)
{
	if (action != "") confirmedAction = action;
	el = document.getElementById(name);

	if (parseInt(el.style.top) > (-1 * (parseInt(el.style.height))))
	{
		el.style.top = (parseInt(el.style.top) - 10) + "px";
		setTimeout("hideConfirm('" + name + "','')",10);
	}
	else
	{
		el.style.top = (-1 * (parseInt(el.style.height) + 10)) + "px"
		el.style.display = "none";
		eval(confirmedAction);
		confirmedAction="";
	}
}

function getInt(str)
{
	// assume radix 10
	var rv = parseInt(str, 10);
	
	// return 0 if its not a parsable int
	if (isNaN(rv)) rv = 0;
	return rv;
}

function ambrosiaCountSummary(source, shadowId, summaryId)
{
	// get the objects
	var summary = document.getElementById(summaryId);
	var shadow = document.getElementById(shadowId);
	
	var oldValue = 0;
	var newValue = source.value;

	// read the old value and store the new value if we have a shadow
	if (shadow != null)
	{
		oldValue = shadow.value;
		shadow.value = newValue;
	}
	
	// update the summary
	if (summary != null)
	{
		summary.value = getInt(summary.value) - getInt(oldValue) + getInt(newValue);
	}
}

function ambrosiaNavigate(enabled, enableFunction, confirm, confirmDivId, validateFlag, submit, destination, root)
{
	if (!enabled)
	{
		if (confirm)
		{
			eval(enableFunction);
			showConfirm(confirmDivId);
 			return;
 		}
 		else
 		{
 			return;
 		}
	}
	if (submitted)
	{
		return;
	}
	if ((!validateFlag) || validate())
	{
		submitted=true;
		if (submit)
		{
			document.form0.destination_.value=destination;
			document.form0.submit();
		}
		else
		{
			document.location=root + destination;
		}
	}
}
