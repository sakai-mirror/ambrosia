/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Regents of the University of Michigan & Foothill College, ETUDES Project
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

// functions for Ambrosia applications (1.1)

function trim(s)
{
	return s.replace(/^\s+/g, "").replace(/\s+$/g, "");
}

function showConfirm(name)
{
	var el = document.getElementById(name);
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
	var el = document.getElementById(name);

	if (parseInt(el.style.top) > (-1 * (parseInt(el.style.height))))
	{
		el.style.top = (parseInt(el.style.top) - 10) + "px";
		setTimeout("hideConfirm('" + name + "','')",10);
	}
	else
	{
		el.style.top = (-1 * (parseInt(el.style.height) + 10)) + "px"
		el.style.display = "none";
		if (confirmedAction != "") eval(confirmedAction);
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

function getFloat(str)
{
	var rv = parseFloat(str);
	
	// return 0 if its not a parsable int
	if (isNaN(rv)) rv = 0.0;
	return rv;
}

function ambrosiaDurationChange(source, validateId)
{
	ambrosiaValidateDuration(source, validateId);
}

function ambrosiaValidateDuration(source, validateId)
{
	if (source == null) return true;
	var reg = new RegExp("^[0-9]+:[0-9]{2}$", "i");

	var str = trim(source.value);
	if (str != "")
	{
		if (reg.exec(str) == null)
		{
			ambrosiaShowInline(validateId);
			return false;
		}
	}

	ambrosiaHideInline(validateId);
	return true;
}

function ambrosiaDateChange(source, validateId)
{
	ambrosiaValidateDate(source, validateId);
}

function ambrosiaValidateDate(source, validateId)
{
	if (source == null) return true;
	// Dec 1, 2007 12:00 AM
	var reg = new RegExp("^(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec) [0-3]?[0-9]{1}, [0-9]{4} (0|00|1|01|2|02|3|03|4|04|5|05|6|06|7|07|8|08|9|09|10|11|12|13|14|15|16|17|18|19|20|21|22|23):[0-5]{1}[0-9]{1} (am|pm){1}$", "i");

	var str = trim(source.value);
	if (str != "")
	{
		if (reg.exec(str) == null)
		{
			ambrosiaShowInline(validateId);
			return false;
		}
	}

	ambrosiaHideInline(validateId);
	return true;
}
function ambrosiaCountChange(source, shadowId, summaryId, min, max, validateId)
{
	// validate
	if (ambrosiaValidateInt(source, min, max, validateId))
	{
		// then summary
		if (summaryId != null) ambrosiaCountSummaryInt(source, shadowId, summaryId)
	}
}

function ambrosiaValidateInt(source, min, max, validateId)
{
	if (source == null) return true;
	var str = trim(source.value);
	if (str != "")
	{
		var value = parseInt(str, 10);
		if (isNaN(value))
		{
			ambrosiaShowInline(validateId);
			return false;
		}
		if (value != str)
		{
			ambrosiaShowInline(validateId);
			return false;
		}

		if ((min != null) && (value < parseInt(min, 10)))
		{
			ambrosiaShowInline(validateId);
			return false;
		}

		if ((max != null) && (value > parseInt(max, 10)))
		{
			ambrosiaShowInline(validateId);
			return false;
		}
	}

	ambrosiaHideInline(validateId);
	return true;
}

function ambrosiaCountSummaryInt(source, shadowId, summaryId)
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
		// (for a field) summary.value = getInt(summary.value) - getInt(oldValue) + getInt(newValue);
		summary.innerHTML = getInt(summary.innerHTML) - getInt(oldValue) + getInt(newValue);
	}
}

function ambrosiaFloatChange(source, shadowId, summaryId, defaultValue, min, max, validateId)
{
	// validate
	if (ambrosiaValidateFloat(source, min, max, validateId))
	{
		// then summary
		if (summaryId != null) ambrosiaCountSummaryFloat(source, shadowId, summaryId, defaultValue)
	}
}

function ambrosiaValidateFloat(source, min, max, validateId)
{
	if (source == null) return true;
	var str = trim(source.value);
	if (str != "")
	{
		var value = parseFloat(str, 10);
		if (isNaN(value))
		{
			ambrosiaShowInline(validateId);
			return false;
		}
		if (value != str)
		{
			ambrosiaShowInline(validateId);
			return false;
		}

		if ((min != null) && (value < parseFloat(min, 10)))
		{
			ambrosiaShowInline(validateId);
			return false;
		}

		if ((max != null) && (value > parseFloat(max, 10)))
		{
			ambrosiaShowInline(validateId);
			return false;
		}
	}

	ambrosiaHideInline(validateId);
	return true;
}

function ambrosiaCountSummaryFloat(source, shadowId, summaryId, defaultValue)
{
	// get the objects
	var summary = document.getElementById(summaryId);
	var shadow = document.getElementById(shadowId);
	
	var oldValue = 0;
	var newValue = source.value;

	// apply the default if the newValue is blank
	if (newValue == "")
	{
		newValue = defaultValue;
		source.value = defaultValue;
	}

	// read the old value and store the new value if we have a shadow
	if (shadow != null)
	{
		oldValue = shadow.value;
		shadow.value = newValue;
	}
	
	// update the summary
	if (summary != null)
	{
		summary.value = getFloat(summary.value) - getFloat(oldValue) + getFloat(newValue);
	}
}

function ambrosiaSubmit(destination)
{
	document.form0.destination_.value=destination;
	document.form0.submit();
}

function ambrosiaNavigate(enabled, enableFunction, confirm, confirmDivId, validateFlag, submit, destination, root, requirementsFunction, requirementsDivId)
{
	if (requirementsFunction != null)
	{
		if (!eval(requirementsFunction))
		{
			showConfirm(requirementsDivId);
			return;
		}
	}
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

// dependencies is an array of arrays, the inner array[0] is the selection value, a reversed flag, and the rest are field ids
function ambrosiaSelectDependencies(selected, dependencies)
{
	for (var d=0; d < dependencies.length; d++)
	{
		var list = dependencies[d];
		var value = list[0];
		var reversed = list[1];
		var doIt = (selected == value);
		if (reversed) doIt = !doIt;
		if (doIt)
		{
			for (var i=2; i < list.length; i++)
			{
				var target = document.getElementById(list[i]);
				if (target == null) continue;
				if (target.disabled == true)
				{
					target.disabled = false;
					if (target.type == "radio")
					{
						target.checked = true;
					}
				}
			}
		}
		
		else
		{
			for (var i=2; i < list.length; i++)
			{
				var target = document.getElementById(list[i]);
				if (target == null) continue;
				target.disabled = true;
				if (target.type == "text")
				{
					target.value = "";
				}
				else if (target.type == "radio")
				{
					target.checked = false;
				}
				if (target.onchange) target.onchange();
			}
		}
	}
}

function ambrosiaTextOptions(obj, textId)
{
	if (obj == null) return;
	var txt = document.getElementById(textId);
	if (txt == null) return;

	if (obj.value != "")
	{
		txt.value = obj.value;
	}
	
	obj.value = "";
}

function ambrosiaNextSibling(obj, tag)
{
	var next = obj.nextSibling;
	while (next && next.nodeName != tag)
	{
		next = next.nextSibling;
	}
	return next;
}

function ambrosiaPrevSibling(obj, tag)
{
	var prev = obj.previousSibling;
	while (prev && prev.nodeName != tag)
	{
		prev = prev.previousSibling;
	}
	return prev;
}

function ambrosiaFirstSibling(obj, tag)
{
	var first = obj;
	if (first != null)
	{
		var tmp = first;
		while (tmp != null)
		{
			tmp = ambrosiaPrevSibling(tmp, tag);
			if (tmp != null) first = tmp;
		}
	}
	return first;
}

function ambrosiaNthSibling(obj, tag, n)
{
	// n is 1 based
	var count = 1;	
	var candidate = ambrosiaFirstSibling(obj, "TR");
	while ((candidate != null) && (count < n))
	{
		count++;
		candidate = ambrosiaNextSibling(candidate, "TR");
	}
	
	return candidate;
}

function ambrosiaWhichSibling(obj, tag)
{
	var count = 1;	
	var candidate = ambrosiaFirstSibling(obj, "TR");
	while ((candidate != null) && (candidate != obj))
	{
		count++;
		candidate = ambrosiaNextSibling(candidate, "TR");
	}
	if (candidate == null) return 0;
	return count;
}

function ambrosiaParent(obj, tag)
{
	if (obj == null) return null;

	var up = obj.parentNode;
	if (up == null) return null;
	if (up.nodeName == tag)
	{
		return up;
	}
	return ambrosiaParent(up, tag);
}

function ambrosiaFindChild(obj, tag, idRoot)
{
	if (obj == null) return null;
	if ((obj.nodeName == tag) && (obj.id != null) && (obj.id.substring(0, idRoot.length) == idRoot)) return obj;
	if (obj.childNodes == null) return null;
	for (var i = 0; i < obj.childNodes.length; i++)
	{
		var candidate = ambrosiaFindChild(obj.childNodes[i], tag, idRoot);
		if (candidate != null) return candidate;
	}
}

function ambrosiaFirstChild(obj, tag)
{
	if (obj == null) return null;
	if (obj.nodeName == tag) return obj;
	if (obj.childNodes == null) return null;
	for (var i = 0; i < obj.childNodes.length; i++)
	{
		var candidate = ambrosiaFirstChild(obj.childNodes[i], tag);
		if (candidate != null) return candidate;
	}
}

function ambrosiaTableReorderRowPosition(innerObj, position)
{
	var toPos = parseInt(position);
	if (isNaN(toPos)) return true;

	var obj = innerObj;
	if (obj.nodeName != "TR")
	{
		obj = ambrosiaParent(obj, "TR");
	}
	if (obj == null) return true;
	
	var objPos = ambrosiaWhichSibling(obj, "TR");

	if (toPos < objPos)
	{
		var target = ambrosiaNthSibling(obj, "TR", toPos);
		obj.parentNode.insertBefore(obj, target);
		return false;
	}
	else
	{
		var target = ambrosiaNthSibling(obj, "TR", toPos);
		if (target) target = ambrosiaNextSibling(target, "TR");
		if (target)
		{
			obj.parentNode.insertBefore(obj, target);
		}
		else
		{
			obj.parentNode.appendChild(obj);
		}
	}
}

function ambrosiaRenumberSelect(selectIdRoot, innerObj)
{
	var obj = innerObj;
	if (obj.nodeName != "TR")
	{
		obj = ambrosiaParent(obj, "TR");
	}
	if (obj == null) return true;

	var target = ambrosiaFirstSibling(obj, "TR");
	var index = 0;
	while (target != null)
	{
		// find a select in target that has a name matching selectIdRoot
		var select = ambrosiaFindChild(target, "SELECT", selectIdRoot);
		if (select != null)
		{
			// change the options to have the nth selected
			ambrosiaSetNthSelected(select, index);
		}
		target = ambrosiaNextSibling(target, "TR");
		index++;
	}
}

function ambrosiaSetNthSelected(obj, index)
{
	// index is 0 based
	var option = ambrosiaFirstChild(obj, "OPTION");
	var i = 0;
	while (option != null)
	{
		if (i == index)
		{
			option.selected = true;
		}
		else
		{
			option.selected = false;
		}
		option = ambrosiaNextSibling(option, "OPTION");
		i++;
	}
}

function ambrosiaTableReorderPosition(innerObj, position, selectIdRoot)
{
	ambrosiaTableReorderRowPosition(innerObj, position);
	ambrosiaRenumberSelect(selectIdRoot, innerObj);
}

function ambrosiaTableReorder(event, innerObj)
{
// window.event || event for ie?
	if ((event == null) || (innerObj == null)) return true;
	var code = event.keyCode;

	if ((code != 38) && (code != 40)) return true;

	var obj = innerObj;
	if (obj.nodeName != "TR")
	{
		obj = ambrosiaParent(obj, "TR");
	}
	if (obj == null) return true;

	if (code == 38)
	{
		var prev = ambrosiaPrevSibling(obj, "TR");
		if (prev)
		{
			obj.parentNode.insertBefore(obj, prev);
			innerObj.focus();
		}
		return false;
	}

	if (code == 40)
	{
		var next = ambrosiaNextSibling(obj, "TR");
		if (next) next = ambrosiaNextSibling(next, "TR");
		if (next)
		{
			obj.parentNode.insertBefore(obj, next);
			innerObj.focus();
		}
		else
		{
			obj.parentNode.appendChild(obj);
			innerObj.focus();
		}
		return false;
	}
	
	return true;
}

function ambrosiaCountChecked(name)
{
	var objs = document.getElementsByName(name);
	var count = 0;
	for (var i=0; i < objs.length; i++)
	{
		if (objs[i].name == name)
		{
			if (objs[i].checked) count++;
		}
	}
	return count;
}

function ambrosiaToggleVisibility(name)
{
	var el = document.getElementById(name);
	if (el == null) return;
	if (el.style.visibility == "hidden")
	{
		el.style.visibility = "visible";
		el.style.overflow = "auto";
	}
	else
	{
		el.style.overflow = "hidden";
		el.style.visibility = "hidden";
	}
}

function ambrosiaShowInline(name)
{
	var el = document.getElementById(name);
	if (el == null) return;
	if (el.style.display == "none")
		el.style.display = "inline";
}

function ambrosiaHideInline(name)
{
	var el = document.getElementById(name);
	if (el == null) return;
	if (el.style.display == "inline")
		el.style.display = "none";
}

function ambrosiaSetupHtmlEdit(name, docsArea)
{
	ambrosiaSetupHtmlEditTiny(name, docsArea);
	//ambrosiaSetupHtmlEditFck(name, docsArea);
}

var ambrosiaFileBrowserDestination = null;

function ambrosiaFileBrowser(field_name, url, type, win)
{
	tinyMCE.activeEditor.windowManager.open(
	{
		file : ambrosiaFileBrowserDestination + "/" + type,
		width : 700,
		height : 500,
		resizable : "yes",
		close_previous : "no",
		scrollbars : "yes"
	},
	{
		window : win,
		input : field_name
	});
	return false;
}

function ambrosiaChooseAttachment(url)
{
	var win = tinyMCEPopup.getWindowArg("window");
	win.document.getElementById(tinyMCEPopup.getWindowArg("input")).value = url;
	if (win.getImageData) win.getImageData();
	tinyMCEPopup.close();
}

function ambrosiaDoneAttachments()
{
	tinyMCEPopup.close();
}

var ambrosiaTinyCss = null;

function ambrosiaTinyInit(picker)
{
	if (picker == null)
	{
		tinyMCE.init(
		{
			mode: "textareas",
			editor_selector: "ambrosiaHtmlEdit",
			convert_urls: false,
			plugins: "safari,fullscreen",
			theme: "advanced",
			theme_advanced_buttons1: "fullscreen,separator,undo,redo,separator,bold,italic,underline,strikethrough,sub,sup,separator,outdent,indent,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,separator,link,unlink,image,separator,fontselect,fontsizeselect,forecolorpicker,backcolorpicker,charmap,separator,code",
			theme_advanced_buttons2: "",
			theme_advanced_buttons3: "",
			extended_valid_elements: "+a[id|style|rel|rev|charset|hreflang|dir|lang|tabindex|accesskey|type|name|href|target:_blank|title|class|onfocus|onblur|onclick|" + 
									 "ondblclick|onmousedown|onmouseup|onmouseover|onmousemove|onmouseout|onkeypress|onkeydown|onkeyup]",
			fullscreen_new_window : true,
			fullscreen_settings :
				{
					theme_advanced_toolbar_location : "top"
				},
			tab_focus: ":prev,:next",
			content_css: (ambrosiaTinyCss == null) ? "" : ambrosiaTinyCss
		});
		tinyMCE.init(
		{
			mode: "textareas",
			editor_selector: "ambrosiaHtmlEditSmall",
			convert_urls: false,
			plugins: "safari,fullscreen",
			theme: "advanced",
			theme_advanced_buttons1: "undo,redo,separator,bold,italic,underline,strikethrough,sub,sup,separator,outdent,indent,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist",
			theme_advanced_buttons2: "fullscreen,separator,link,unlink,image,separator,fontselect,fontsizeselect,forecolorpicker,backcolorpicker,charmap,separator,code",
			theme_advanced_buttons3: "",
			extended_valid_elements: "+a[id|style|rel|rev|charset|hreflang|dir|lang|tabindex|accesskey|type|name|href|target:_blank|title|class|onfocus|onblur|onclick|" + 
									 "ondblclick|onmousedown|onmouseup|onmouseover|onmousemove|onmouseout|onkeypress|onkeydown|onkeyup]",
			fullscreen_new_window : true,
			fullscreen_settings :
				{
					theme_advanced_toolbar_location : "top",
					theme_advanced_buttons1: "fullscreen,separator,undo,redo,separator,bold,italic,underline,strikethrough,sub,sup,separator,outdent,indent,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,separator,link,unlink,image,separator,fontselect,fontsizeselect,forecolorpicker,backcolorpicker,charmap,separator,code",
					theme_advanced_buttons2: ""
				},
			tab_focus: ":prev,:next",
			content_css: (ambrosiaTinyCss == null) ? "" : ambrosiaTinyCss
		});
	}
	else
	{
		ambrosiaFileBrowserDestination = picker;

		tinyMCE.init(
		{
			mode: "textareas",
			editor_selector: "ambrosiaHtmlEdit",
			convert_urls: false,
			plugins: "safari,fullscreen",
			theme: "advanced",
			theme_advanced_buttons1: "fullscreen,separator,undo,redo,separator,bold,italic,underline,strikethrough,sub,sup,separator,outdent,indent,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,separator,link,unlink,image,separator,fontselect,fontsizeselect,forecolorpicker,backcolorpicker,charmap,separator,code",
			theme_advanced_buttons2: "",
			theme_advanced_buttons3: "",
			file_browser_callback: "ambrosiaFileBrowser",
			extended_valid_elements: "+a[id|style|rel|rev|charset|hreflang|dir|lang|tabindex|accesskey|type|name|href|target:_blank|title|class|onfocus|onblur|onclick|" + 
									 "ondblclick|onmousedown|onmouseup|onmouseover|onmousemove|onmouseout|onkeypress|onkeydown|onkeyup]",
			fullscreen_new_window : true,
			fullscreen_settings :
				{
					theme_advanced_toolbar_location : "top"
				},
			tab_focus: ":prev,:next",
			content_css: (ambrosiaTinyCss == null) ? "" : ambrosiaTinyCss
		});
		tinyMCE.init(
		{
			mode: "textareas",
			editor_selector: "ambrosiaHtmlEditSmall",
			convert_urls: false,
			plugins: "safari,fullscreen",
			theme: "advanced",
			theme_advanced_buttons1: "undo,redo,separator,bold,italic,underline,strikethrough,sub,sup,separator,outdent,indent,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist",
			theme_advanced_buttons2: "fullscreen,separator,link,unlink,image,separator,fontselect,fontsizeselect,forecolorpicker,backcolorpicker,charmap,separator,code",
			theme_advanced_buttons3: "",
			file_browser_callback: "ambrosiaFileBrowser",
			extended_valid_elements: "+a[id|style|rel|rev|charset|hreflang|dir|lang|tabindex|accesskey|type|name|href|target:_blank|title|class|onfocus|onblur|onclick|" + 
									 "ondblclick|onmousedown|onmouseup|onmouseover|onmousemove|onmouseout|onkeypress|onkeydown|onkeyup]",
			fullscreen_new_window : true,
			fullscreen_settings :
				{
					theme_advanced_toolbar_location : "top",
					theme_advanced_buttons1: "fullscreen,separator,undo,redo,separator,bold,italic,underline,strikethrough,sub,sup,separator,outdent,indent,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,separator,link,unlink,image,separator,fontselect,fontsizeselect,forecolorpicker,backcolorpicker,charmap,separator,code",
					theme_advanced_buttons2: ""
				},
			tab_focus: ":prev,:next",
			content_css: (ambrosiaTinyCss == null) ? "" : ambrosiaTinyCss
		});		
	}
}

function ambrosiaEnableHtmlEdit(htmlComponent)
{
	if (!htmlComponent.enabled)
	{
		htmlComponent.enabled = true;

		if (htmlComponent.renderedId != null)
		{
			var renderedEl = document.getElementById(htmlComponent.renderedId);
			if (renderedEl != null) renderedEl.style.display = "none";
		}

		if (htmlComponent.toggleId != null)
		{
			var toggleEl = document.getElementById(htmlComponent.toggleId);
			if (toggleEl != null) toggleEl.style.display = "none";
		}

		var textAreaIdEl = document.getElementById(htmlComponent.textAreaId);
		textAreaIdEl.style.display = "";
	
		tinyMCE.execCommand("mceAddControl", false, htmlComponent.textAreaId);
	}
}

function ambrosiaPopupDate(id)
{
	// get the current date value
	var el = document.getElementById(id);
	if (el == null) return;
	
	var timeStamp = new Date(el.value);
	if (isNaN(timeStamp)) timeStamp = new Date();

	var popup = new calendar2(el);
	if (popup == null) return;
	popup.popup();
	//el.select();
}
