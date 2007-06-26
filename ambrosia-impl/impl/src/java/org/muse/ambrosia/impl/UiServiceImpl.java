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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.muse.ambrosia.api.Alert;
import org.muse.ambrosia.api.Alias;
import org.muse.ambrosia.api.AndDecision;
import org.muse.ambrosia.api.Attachments;
import org.muse.ambrosia.api.AutoColumn;
import org.muse.ambrosia.api.BarChart;
import org.muse.ambrosia.api.BooleanPropertyReference;
import org.muse.ambrosia.api.CompareDecision;
import org.muse.ambrosia.api.ConstantPropertyReference;
import org.muse.ambrosia.api.Container;
import org.muse.ambrosia.api.Context;
import org.muse.ambrosia.api.ContextInfoPropertyReference;
import org.muse.ambrosia.api.Component;
import org.muse.ambrosia.api.CountdownTimer;
import org.muse.ambrosia.api.Courier;
import org.muse.ambrosia.api.DatePropertyReference;
import org.muse.ambrosia.api.Decision;
import org.muse.ambrosia.api.DecisionDelegate;
import org.muse.ambrosia.api.Decoder;
import org.muse.ambrosia.api.Destination;
import org.muse.ambrosia.api.DistributionChart;
import org.muse.ambrosia.api.Divider;
import org.muse.ambrosia.api.DurationPropertyReference;
import org.muse.ambrosia.api.EntityDisplay;
import org.muse.ambrosia.api.EntityList;
import org.muse.ambrosia.api.EntityListColumn;
import org.muse.ambrosia.api.Evaluation;
import org.muse.ambrosia.api.FileUpload;
import org.muse.ambrosia.api.FillIn;
import org.muse.ambrosia.api.Footnote;
import org.muse.ambrosia.api.FormatDelegate;
import org.muse.ambrosia.api.Gap;
import org.muse.ambrosia.api.HasValueDecision;
import org.muse.ambrosia.api.HtmlPropertyReference;
import org.muse.ambrosia.api.IconKey;
import org.muse.ambrosia.api.IconPropertyReference;
import org.muse.ambrosia.api.Instructions;
import org.muse.ambrosia.api.Interface;
import org.muse.ambrosia.api.Match;
import org.muse.ambrosia.api.MenuBar;
import org.muse.ambrosia.api.Message;
import org.muse.ambrosia.api.Navigation;
import org.muse.ambrosia.api.NavigationBar;
import org.muse.ambrosia.api.OrDecision;
import org.muse.ambrosia.api.Password;
import org.muse.ambrosia.api.PastDateDecision;
import org.muse.ambrosia.api.PropertyColumn;
import org.muse.ambrosia.api.PropertyReference;
import org.muse.ambrosia.api.PropertyRow;
import org.muse.ambrosia.api.Section;
import org.muse.ambrosia.api.Selection;
import org.muse.ambrosia.api.SelectionColumn;
import org.muse.ambrosia.api.Text;
import org.muse.ambrosia.api.TextEdit;
import org.muse.ambrosia.api.TextPropertyReference;
import org.muse.ambrosia.api.UiService;
import org.muse.ambrosia.api.UrlPropertyReference;
import org.muse.ambrosia.api.UserInfoPropertyReference;
import org.muse.ambrosia.api.Value;
import org.muse.ambrosia.api.Controller;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * UiServiceImpl is ...
 * </p>
 */
public class UiServiceImpl implements UiService
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(UiServiceImpl.class);

	/*************************************************************************************************************************************************
	 * Abstractions, etc.
	 ************************************************************************************************************************************************/

	/**
	 * Recognize a path that is a resource request. It must have an "extension", i.e. a dot followed by characters that do not include a slash.
	 * 
	 * @param prefixes
	 *        a set of prefix strings; one of which must match the first part of the path.
	 * @param path
	 *        The path to check
	 * @return true if the path is a resource request, false if not.
	 */
	protected boolean isResourceRequest(Set<String> prefixes, String path)
	{
		// we need some path
		if ((path == null) || (path.length() <= 1)) return false;

		// the first part of the path needs to be present in the prefixes
		String[] prefix = StringUtil.splitFirst(path.substring(1), "/");
		if (!prefixes.contains(prefix[0])) return false;

		// we need a last dot
		int pos = path.lastIndexOf(".");
		if (pos == -1) return false;

		// we need that last dot to be the end of the path, not burried in the path somewhere (i.e. no more slashes after the last
		// dot)
		String ext = path.substring(pos);
		if (ext.indexOf("/") != -1) return false;

		// ok, it's a resource request
		return true;
	}

	/*************************************************************************************************************************************************
	 * Dependencies
	 ************************************************************************************************************************************************/

	/** Dependency: SessionManager */
	protected SessionManager m_sessionManager = null;

	/** Dependency: ThreadLocal */
	protected ThreadLocalManager m_threadLocalManager = null;

	/**
	 * Dependency: SessionManager.
	 * 
	 * @param service
	 *        The SessionManager.
	 */
	public void setSessionManager(SessionManager service)
	{
		m_sessionManager = service;
	}

	/**
	 * Dependency: ThreadLocalManager.
	 * 
	 * @param service
	 *        The ThreadLocalManager.
	 */
	public void setThreadLocalManager(ThreadLocalManager service)
	{
		m_threadLocalManager = service;
	}

	/*************************************************************************************************************************************************
	 * Configuration
	 ************************************************************************************************************************************************/

	/*************************************************************************************************************************************************
	 * Init and Destroy
	 ************************************************************************************************************************************************/

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		M_log.info("init()");
	}

	/*************************************************************************************************************************************************
	 * UiService implementation
	 ************************************************************************************************************************************************/

	/*************************************************************************************************************************************************
	 * Component factory methods
	 ************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public Alert newAlert()
	{
		return new UiAlert();
	}

	/**
	 * {@inheritDoc}
	 */
	public Alias newAlias()
	{
		return new UiAlias();
	}

	/**
	 * {@inheritDoc}
	 */
	public AndDecision newAndDecision()
	{
		return new UiAndDecision();
	}

	/**
	 * {@inheritDoc}
	 */
	public Attachments newAttachments()
	{
		return new UiAttachments();
	}

	/**
	 * {@inheritDoc}
	 */
	public AutoColumn newAutoColumn()
	{
		return new UiAutoColumn();
	}

	/**
	 * {@inheritDoc}
	 */
	public BarChart newBarChart()
	{
		return new UiBarChart();
	}

	/**
	 * {@inheritDoc}
	 */
	public BooleanPropertyReference newBooleanPropertyReference()
	{
		return new UiBooleanPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public CompareDecision newCompareDecision()
	{
		return new UiCompareDecision();
	}

	/**
	 * {@inheritDoc}
	 */
	public ConstantPropertyReference newConstantPropertyReference()
	{
		return new UiConstantPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public Container newContainer()
	{
		return new UiContainer();
	}

	/**
	 * {@inheritDoc}
	 */
	public Context newContext()
	{
		return new UiContext();
	}

	/**
	 * {@inheritDoc}
	 */
	public ContextInfoPropertyReference newContextInfoPropertyReference()
	{
		return new UiContextInfoPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public Component newComponent()
	{
		return new UiComponent();
	}

	/**
	 * {@inheritDoc}
	 */
	public CountdownTimer newCountdownTimer()
	{
		return new UiCountdownTimer();
	}

	/**
	 * {@inheritDoc}
	 */
	public Courier newCourier()
	{
		return new UiCourier();
	}

	/**
	 * {@inheritDoc}
	 */
	public DatePropertyReference newDatePropertyReference()
	{
		return new UiDatePropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public Decision newDecision()
	{
		return new UiDecision();
	}

	/**
	 * {@inheritDoc}
	 */
	public Decoder newDecoder()
	{
		return new UiDecoder();
	}

	/**
	 * {@inheritDoc}
	 */
	public Destination newDestination()
	{
		return new UiDestination();
	}

	/**
	 * {@inheritDoc}
	 */
	public DistributionChart newDistributionChart()
	{
		return new UiDistributionChart();
	}

	/**
	 * {@inheritDoc}
	 */
	public Divider newDivider()
	{
		return new UiDivider();
	}

	/**
	 * {@inheritDoc}
	 */
	public DurationPropertyReference newDurationPropertyReference()
	{
		return new UiDurationPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityDisplay newEntityDisplay()
	{
		return new UiEntityDisplay();
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityList newEntityList()
	{
		return new UiEntityList();
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn newEntityListColumn()
	{
		return new UiEntityListColumn();
	}

	/**
	 * {@inheritDoc}
	 */
	public Evaluation newEvaluation()
	{
		return new UiEvaluation();
	}

	/**
	 * {@inheritDoc}
	 */
	public FileUpload newFileUpload()
	{
		return new UiFileUpload();
	}

	/**
	 * {@inheritDoc}
	 */
	public FillIn newFillIn()
	{
		return new UiFillIn();
	}

	/**
	 * {@inheritDoc}
	 */
	public Footnote newFootnote()
	{
		return new UiFootnote();
	}

	/**
	 * {@inheritDoc}
	 */
	public Gap newGap()
	{
		return new UiGap();
	}

	/**
	 * {@inheritDoc}
	 */
	public HasValueDecision newHasValueDecision()
	{
		return new UiHasValueDecision();
	}

	/**
	 * {@inheritDoc}
	 */
	public HtmlPropertyReference newHtmlPropertyReference()
	{
		return new UiHtmlPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public IconKey newIconKey()
	{
		return new UiIconKey();
	}

	/**
	 * {@inheritDoc}
	 */
	public IconPropertyReference newIconPropertyReference()
	{
		return new UiIconPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public Instructions newInstructions()
	{
		return new UiInstructions();
	}

	/**
	 * {@inheritDoc}
	 */
	public Interface newInterface()
	{
		return new UiInterface();
	}

	/**
	 * {@inheritDoc}
	 */
	public Match newMatch()
	{
		return new UiMatch();
	}

	/**
	 * {@inheritDoc}
	 */
	public MenuBar newMenuBar()
	{
		return new UiMenuBar();
	}

	/**
	 * {@inheritDoc}
	 */
	public Message newMessage()
	{
		return new UiMessage();
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation newNavigation()
	{
		return new UiNavigation();
	}

	/**
	 * {@inheritDoc}
	 */
	public NavigationBar newNavigationBar()
	{
		return new UiNavigationBar();
	}

	/**
	 * {@inheritDoc}
	 */
	public OrDecision newOrDecision()
	{
		return new UiOrDecision();
	}

	/**
	 * {@inheritDoc}
	 */
	public PastDateDecision newPastDateDecision()
	{
		return new UiPastDateDecision();
	}

	/**
	 * {@inheritDoc}
	 */
	public Password newPassword()
	{
		return new UiPassword();
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyColumn newPropertyColumn()
	{
		return new UiPropertyColumn();
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyReference newPropertyReference()
	{
		return new UiPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyRow newPropertyRow()
	{
		return new UiPropertyRow();
	}

	/**
	 * {@inheritDoc}
	 */
	public Section newSection()
	{
		return new UiSection();
	}

	/**
	 * {@inheritDoc}
	 */
	public Selection newSelection()
	{
		return new UiSelection();
	}

	/**
	 * {@inheritDoc}
	 */
	public SelectionColumn newSelectionColumn()
	{
		return new UiSelectionColumn();
	}

	/**
	 * {@inheritDoc}
	 */
	public Text newText()
	{
		return new UiText();
	}

	/**
	 * {@inheritDoc}
	 */
	public TextEdit newTextEdit()
	{
		return new UiTextEdit();
	}

	/**
	 * {@inheritDoc}
	 */
	public TextPropertyReference newTextPropertyReference()
	{
		return new UiTextPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public UserInfoPropertyReference newUserInfoPropertyReference()
	{
		return new UiUserInfoPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public UrlPropertyReference newUrlPropertyReference()
	{
		return new UiUrlPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public Value newValue()
	{
		return new UiValue();
	}

	/*************************************************************************************************************************************************
	 * Interface loading from XML
	 ************************************************************************************************************************************************/

	/**
	 * Check the element's tag for a known Component tag, and create the component from the tag.
	 * 
	 * @param xml
	 *        The element.
	 */
	protected Component parseComponent(Element xml)
	{
		if (xml.getTagName().equals("alert")) return new UiAlert(this, xml);
		if (xml.getTagName().equals("alias")) return new UiAlias(this, xml);
		if (xml.getTagName().equals("attachments")) return new UiAttachments(this, xml);
		if (xml.getTagName().equals("countdownTimer")) return new UiCountdownTimer(this, xml);
		if (xml.getTagName().equals("courier")) return new UiCourier(this, xml);
		if (xml.getTagName().equals("divider")) return new UiDivider(this, xml);
		if (xml.getTagName().equals("entityDisplay")) return new UiEntityDisplay(this, xml);
		if (xml.getTagName().equals("entityList")) return new UiEntityList(this, xml);
		if (xml.getTagName().equals("evaluation")) return new UiEvaluation(this, xml);
		if (xml.getTagName().equals("fileUpload")) return new UiFileUpload(this, xml);
		if (xml.getTagName().equals("fillIn")) return new UiFillIn(this, xml);
		if (xml.getTagName().equals("gap")) return new UiGap(this, xml);
		if (xml.getTagName().equals("iconKey")) return new UiIconKey(this, xml);
		if (xml.getTagName().equals("instructions")) return new UiInstructions(this, xml);
		if (xml.getTagName().equals("interface")) return new UiInterface(this, xml);
		if (xml.getTagName().equals("match")) return new UiMatch(this, xml);
		if (xml.getTagName().equals("menuBar")) return new UiMenuBar();
		if (xml.getTagName().equals("navigation")) return new UiNavigation(this, xml);
		if (xml.getTagName().equals("navigationBar")) return new UiNavigationBar(this, xml);
		if (xml.getTagName().equals("password")) return new UiPassword(this, xml);
		if (xml.getTagName().equals("section")) return new UiSection(this, xml);
		if (xml.getTagName().equals("selection")) return new UiSelection(this, xml);
		if (xml.getTagName().equals("text")) return new UiText(this, xml);
		if (xml.getTagName().equals("textEdit")) return new UiTextEdit(this, xml);

		return null;
	}

	/**
	 * Create the appropriate Decision based on the XML element.
	 * 
	 * @param xml
	 *        The xml element.
	 * @return a Decision object.
	 */
	protected Decision parseDecision(Element xml)
	{
		if (xml == null) return null;

		if (xml.getTagName().equals("hasValueDecision")) return new UiHasValueDecision(this, xml);
		if (xml.getTagName().equals("compareDecision")) return new UiCompareDecision(this, xml);
		if (xml.getTagName().equals("andDecision")) return new UiAndDecision(this, xml);
		if (xml.getTagName().equals("orDecision")) return new UiOrDecision(this, xml);
		if (xml.getTagName().equals("pastDateDecision")) return new UiPastDateDecision(this, xml);

		if (!xml.getTagName().equals("decision")) return null;

		String type = StringUtil.trimToNull(xml.getAttribute("type"));
		if ("hasValue".equals(type)) return new UiHasValueDecision(this, xml);
		if ("compare".equals(type)) return new UiCompareDecision(this, xml);
		if ("and".equals(type)) return new UiAndDecision(this, xml);
		if ("or".equals(type)) return new UiOrDecision(this, xml);
		if ("pastDate".equals(type)) return new UiPastDateDecision(this, xml);

		return new UiDecision(this, xml);
	}

	/**
	 * Parse a set of decisions from this element and its children.
	 * 
	 * @param xml
	 *        The element tree to parse.
	 * @return An array of decisions, or null if there are none.
	 */
	protected Decision[] parseArrayDecisions(Element xml)
	{
		Decision[] rv = null;

		List<Decision> decisions = new ArrayList<Decision>();

		// short form for decision is TRUE
		String decisionTrue = StringUtil.trimToNull(xml.getAttribute("decision"));
		if ("TRUE".equals(decisionTrue))
		{
			Decision decision = new UiDecision().setProperty(new UiConstantPropertyReference().setValue("true"));
			decisions.add(decision);
		}

		NodeList contained = xml.getChildNodes();
		for (int i = 0; i < contained.getLength(); i++)
		{
			Node node = contained.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				Element containedXml = (Element) node;

				// let the service parse this as a decision
				Decision decision = parseDecision(containedXml);
				if (decision != null) decisions.add(decision);
			}
		}

		if (!decisions.isEmpty())
		{
			rv = decisions.toArray(new Decision[decisions.size()]);
		}

		return rv;
	}

	/**
	 * Parse a set of decisions from this element and its children into a single AND decision.
	 * 
	 * @param xml
	 *        The element tree to parse.
	 * @return The decisions wrapped in a single AND decision, or null if there are none.
	 */
	protected Decision parseDecisions(Element xml)
	{
		Decision[] decisions = parseArrayDecisions(xml);
		if (decisions == null) return null;

		if (decisions.length == 1)
		{
			return decisions[0];
		}

		AndDecision rv = new UiAndDecision().setRequirements(decisions);
		return rv;
	}

	/**
	 * Create the appropriate PropertyReference based on the XML element.
	 * 
	 * @param xml
	 *        The xml element.
	 * @return a PropertyReference object.
	 */
	protected PropertyReference parsePropertyReference(Element xml)
	{
		if (xml == null) return null;

		if (xml.getTagName().equals("booleanModel")) return new UiBooleanPropertyReference(this, xml);
		if (xml.getTagName().equals("constantModel")) return new UiConstantPropertyReference(this, xml);
		if (xml.getTagName().equals("contextInfoModel")) return new UiContextInfoPropertyReference(this, xml);
		if (xml.getTagName().equals("dateModel")) return new UiDatePropertyReference(this, xml);
		if (xml.getTagName().equals("durationModel")) return new UiDurationPropertyReference(this, xml);
		if (xml.getTagName().equals("htmlModel")) return new UiHtmlPropertyReference(this, xml);
		if (xml.getTagName().equals("iconModel")) return new UiIconPropertyReference(this, xml);
		if (xml.getTagName().equals("textModel")) return new UiTextPropertyReference(this, xml);
		if (xml.getTagName().equals("urlModel")) return new UiUrlPropertyReference(this, xml);
		if (xml.getTagName().equals("userInfoModel")) return new UiUserInfoPropertyReference(this, xml);

		if (!xml.getTagName().equals("model")) return null;

		String type = StringUtil.trimToNull(xml.getAttribute("type"));
		if ("boolean".equals(type)) return new UiBooleanPropertyReference(this, xml);
		if ("constant".equals(type)) return new UiConstantPropertyReference(this, xml);
		if ("contextInfo".equals(type)) return new UiContextInfoPropertyReference(this, xml);
		if ("date".equals(type)) return new UiDatePropertyReference(this, xml);
		if ("duration".equals(type)) return new UiDurationPropertyReference(this, xml);
		if ("html".equals(type)) return new UiHtmlPropertyReference(this, xml);
		if ("icon".equals(type)) return new UiIconPropertyReference(this, xml);
		if ("text".equals(type)) return new UiTextPropertyReference(this, xml);
		if ("url".equals(type)) return new UiUrlPropertyReference(this, xml);
		if ("userInfo".equals(type)) return new UiUserInfoPropertyReference(this, xml);

		return new UiPropertyReference(this, xml);
	}

	/**
	 * Create the appropriate EntityListColumn based on the XML element.
	 * 
	 * @param xml
	 *        The xml element.
	 * @return a EntityListColumn object.
	 */
	protected EntityListColumn parseEntityListColumn(Element xml)
	{
		if (xml == null) return null;

		if (xml.getTagName().equals("autoColumn")) return new UiAutoColumn();
		if (xml.getTagName().equals("modelColumn")) return new UiPropertyColumn(this, xml);
		if (xml.getTagName().equals("selectionColumn")) return new UiSelectionColumn(this, xml);

		if (!xml.getTagName().equals("column")) return null;

		String type = StringUtil.trimToNull(xml.getAttribute("type"));
		if ("auto".equals(type)) return new UiAutoColumn();
		if ("model".equals(type)) return new UiPropertyColumn(this, xml);
		if ("selection".equals(type)) return new UiSelectionColumn(this, xml);

		return new UiEntityListColumn(this, xml);
	}

	/**
	 * Create the appropriate PropertyRow based on the XML element.
	 * 
	 * @param xml
	 *        The xml element.
	 * @return a PropertyRow object.
	 */
	protected PropertyRow parsePropertyRow(Element xml)
	{
		if (xml == null) return null;

		// if (xml.getTagName().equals("modelColumn")) return new UiPropertyColumn(this, xml);

		if (!xml.getTagName().equals("row")) return null;

		// TODO: support types?
		String type = StringUtil.trimToNull(xml.getAttribute("type"));
		// if ("model".equals(type)) return new UiPropertyColumn(this, xml);

		return new UiPropertyRow(this, xml);
	}

	/**
	 * {@inheritDoc}
	 */
	public Interface newInterface(InputStream in)
	{
		UiInterface iface = null;
		Document doc = Xml.readDocumentFromStream(in);
		if ((doc == null) || (!doc.hasChildNodes())) return iface;

		// allowing for comments, use the first element node that is our interface
		NodeList nodes = doc.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node node = nodes.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;
			if (!(((Element) node).getTagName().equals("interface"))) continue;

			// build the interface from this element
			iface = new UiInterface(this, (Element) node);
			break;
		}

		// else we have an invalid
		if (iface == null)
		{
			M_log.warn("newInterface: element \"interface\" not found in stream xml");
			iface = new UiInterface();
		}

		return iface;
	}

	/*************************************************************************************************************************************************
	 * Response handling methods
	 ************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public String decode(HttpServletRequest req, Context context)
	{
		UiDecoder decoder = new UiDecoder();
		String destination = decoder.decode(req, context);

		return destination;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean dispatchResource(HttpServletRequest req, HttpServletResponse res, ServletContext context, Set<String> prefixes)
			throws IOException, ServletException
	{
		// see if we have a resource request
		String path = req.getPathInfo();
		if (isResourceRequest(prefixes, path))
		{
			// get a dispatcher to the path
			RequestDispatcher resourceDispatcher = context.getRequestDispatcher(path);
			if (resourceDispatcher != null)
			{
				resourceDispatcher.forward(req, res);
				return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean redirectToCurrentDestination(HttpServletRequest req, HttpServletResponse res, String home) throws IOException
	{
		// get the Tool session
		ToolSession toolSession = m_sessionManager.getCurrentToolSession();

		// check the path in the request - if null, we will either send them home or to the current destination
		String destination = req.getPathInfo();
		if (destination == null)
		{
			// do we have a current destination?
			destination = (String) toolSession.getAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION);

			// if not, set it to home
			if (destination == null)
			{
				destination = "/" + ((home != null) ? home : "");
			}

			// redirect
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));

			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Context prepareGet(HttpServletRequest req, HttpServletResponse res, InternationalizedMessages messages, String home) throws IOException
	{
		// get the Tool session
		ToolSession toolSession = m_sessionManager.getCurrentToolSession();

		// record the current destination before this request; i.e. the previous destination
		String previousDestination = (String) toolSession.getAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION);
		m_threadLocalManager.set("ambrosia_" + ActiveTool.TOOL_ATTR_CURRENT_DESTINATION, previousDestination);

		// keep track (manually, for now) of our current destination
		String destination = req.getPathInfo();
		if (destination == null) destination = "/" + ((home != null) ? home : "");
		toolSession.setAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION, destination);

		// fragment or not?
		boolean fragment = Boolean.TRUE.toString().equals(req.getAttribute(Tool.FRAGMENT));

		if (!fragment)
		{
			// setup type and no caching
			res.setContentType("text/html; charset=UTF-8");
			res.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
			res.addDateHeader("Last-Modified", System.currentTimeMillis());
			res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
			res.addHeader("Pragma", "no-cache");
		}

		// our response writer
		PrintWriter out = res.getWriter();

		UiContext context = new UiContext();
		context.setMessages(messages);
		context.setDestination(destination);
		context.setPreviousDestination(previousDestination);
		context.setResponseWriter(out);
		context.put(UiContext.FRAGMENT, Boolean.valueOf(fragment));
		context.put("sakai.html.head", req.getAttribute("sakai.html.head"));
		context.put("sakai.html.body.onload", req.getAttribute("sakai.html.body.onload"));
		context.put("sakai.return.url", Web.returnUrl(req, ""));

		String destinationUrl = Web.returnUrl(req, destination);
		context.put("sakai.destination.url", destinationUrl);
		context.put("sakai_destination", destination);

		// setup that a POST to this destination will be expected
		String previousExpected = (String) toolSession.getAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION + ".expected");
		m_threadLocalManager.set("ambrosia_" + ActiveTool.TOOL_ATTR_CURRENT_DESTINATION + ".expected", previousExpected);
		toolSession.setAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION + ".expected", destination);

		// // setup a valid POST receipt for this tool destination
		// Object postCoordinator = toolSession.getAttribute("sakai.post.coordinator");
		// if (postCoordinator == null)
		// {
		// postCoordinator = new Object();
		// toolSession.setAttribute("sakai.post.coordinator", postCoordinator);
		// }
		// synchronized (postCoordinator)
		// {
		// toolSession.setAttribute(destinationUrl, destinationUrl);
		// }

		return context;
	}

	/**
	 * {@inheritDoc}
	 */
	public void undoPrepareGet(HttpServletRequest req, HttpServletResponse res)
	{
		// get the Tool session
		ToolSession toolSession = m_sessionManager.getCurrentToolSession();

		String destination = (String) m_threadLocalManager.get("ambrosia_" + ActiveTool.TOOL_ATTR_CURRENT_DESTINATION);
		toolSession.setAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION, destination);

		String expected = (String) m_threadLocalManager.get("ambrosia_" + ActiveTool.TOOL_ATTR_CURRENT_DESTINATION + ".expected");
		toolSession.setAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION + ".expected", expected);
	}

	/**
	 * {@inheritDoc}
	 */
	public Context preparePost(HttpServletRequest req, HttpServletResponse res, InternationalizedMessages messages, String home)
	{
		// get the Tool session
		ToolSession toolSession = m_sessionManager.getCurrentToolSession();

		// record the current destination before this request; i.e. the previous destination
		String previousDestination = (String) toolSession.getAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION);

		// compute, but do not set the current destination - a post will never be a default destination
		String destination = req.getPathInfo();
		if (destination == null) destination = "/" + ((home != null) ? home : "");

		// does this destination matches the destination we are expecting?
		boolean expected = !StringUtil.different((String) toolSession.getAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION + ".expected"),
				destination);

		// clear the destination so we get only one match
		toolSession.removeAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION + ".expected");

		// Object postCoordinator = toolSession.getAttribute("sakai.post.coordinator");
		// if (postCoordinator != null)
		// {
		// synchronized (postCoordinator)
		// {
		// String postReceipt = (String) toolSession.getAttribute(destinationUrl);
		// toolSession.removeAttribute(destinationUrl);
		//
		// if (postReceipt == null) // TODO: or value?
		// {
		// // unexpected post
		// expected = false;
		// }
		// }
		// }

		String destinationUrl = Web.returnUrl(req, destination);

		UiContext context = new UiContext();
		context.setMessages(messages);
		context.setDestination(destination);
		context.setPreviousDestination(previousDestination);
		context.put("sakai.destination.url", destinationUrl);
		context.setPostExpected(expected);

		return context;
	}

	/**
	 * {@inheritDoc}
	 */
	public void render(Component ui, Context context)
	{
		context.setUi(ui);
		ui.render(context, null);
	}

	/*************************************************************************************************************************************************
	 * View methods
	 ************************************************************************************************************************************************/

	/** Registered views - keyed by toolId-viewId. */
	protected Map<String, Controller> m_controllers = new HashMap<String, Controller>();

	/** Registered format delegates - keyed by toolId-id. */
	protected Map<String, FormatDelegate> m_formatDelegates = new HashMap<String, FormatDelegate>();

	/** Registered decision delegates - keyed by toolId-id. */
	protected Map<String, DecisionDelegate> m_decisionDelegates = new HashMap<String, DecisionDelegate>();

	/**
	 * {@inheritDoc}
	 */
	public void registerController(Controller controller, String toolId)
	{
		m_controllers.put(toolId + "-" + controller.getPath(), controller);
	}

	/**
	 * {@inheritDoc}
	 */
	public Controller getController(String id, String toolId)
	{
		return m_controllers.get(toolId + "-" + id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerFormatDelegate(FormatDelegate delegate, String id, String toolId)
	{
		m_formatDelegates.put(toolId + "-" + id, delegate);
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerDecisionDelegate(DecisionDelegate delegate, String id, String toolId)
	{
		m_decisionDelegates.put(toolId + "-" + id, delegate);
	}

	/**
	 * {@inheritDoc}
	 */
	public FormatDelegate getFormatDelegate(String id, String toolId)
	{
		return m_formatDelegates.get(toolId + "-" + id);
	}

	/**
	 * {@inheritDoc}
	 */
	public DecisionDelegate getDecisionDelegate(String id, String toolId)
	{
		return m_decisionDelegates.get(toolId + "-" + id);
	}
}
