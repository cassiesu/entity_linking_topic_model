package eth.nlp.common;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpression;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLProcessor {

	private static XMLProcessor singleton = null;

	private XPath xpath = null;
	private Map<String, XPathExpression> exprMap = null;

	public static XMLProcessor getInstance() {
		if (singleton != null)
			return singleton;
		singleton = new XMLProcessor();
		return singleton;
	}

	public XMLProcessor() {
		XPathFactory fac = XPathFactory.newInstance();
		xpath = fac.newXPath();
		exprMap = new HashMap<String, XPathExpression>();
	}

	public void reset() {
		exprMap.clear();
	}

	/**
	 * The following function is barely capable of copying such source element:
	 * <srcE> <son1E>son1's text</son1> <son2E>son2's text</son2E> ... </srcE>
	 */
	public void copyElement(Element srcE, Element dstE) {
		Document dstDoc = dstE.getOwnerDocument();
		NodeList nodeList = srcE.getChildNodes();

		addLineSeparator(dstE);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element nodeE = (Element) node;
				String nodeN = nodeE.getNodeName();
				String txt = nodeE.getTextContent();
				if (nodeN.equals("pubtime")) {
					txt = txt.replaceAll("(\\d{1,4}��\\d{1,2}��\\d{1,2}��)\\s*(\\d{1,2}:\\d{1,2})(:\\d{1,2})?", "$1 $2");
				}
				Element newE = dstDoc.createElement(nodeN);
				newE.setTextContent(txt);
				addElement(dstE, newE, false);
			}
		}
	}
	
	Pattern patt = Pattern.compile("(\\d{4}).(\\d{1,2}).(\\d{1,2}).\\s*(\\d{1,2}).(\\d{1,2})(.\\d{1,2})?");
	private String convert(String pubtime) {
		Matcher m = patt.matcher(pubtime);
		if(m.find()) {
			String year = m.group(1);
			
			String month = m.group(2);
			if(month.length() == 1) {
				month = "0" + month;
			}
			
			String day = m.group(3);
			if(day.length() == 1) {
				day = "0" + day;
			}
			
			String hour = m.group(4);
			if(hour.length() == 1) {
				hour = "0" + hour;
			}
			
			String min = m.group(5);
			if(min.length() == 1) {
				min = "0" + min;
			}
			
			pubtime = year + "��" + month + "��" + day + "��" + " " + hour + ":" + min;
			
		} else {
			new Exception("invalid pubtime format");
		}
		return pubtime;
	}
	
	public void addElementByPubtimeDesc(Element docsE, Element docE) throws Exception {
		XMLProcessor xml = XMLProcessor.getInstance();
		Element pubtimeE = xml.getDescendantElement(docE, "pubtime");
		String pubtime = convert(pubtimeE.getTextContent());
		pubtimeE.setTextContent(pubtime);

		NodeList nodeList = getDescendantElements(docsE, docE.getTagName());
		int length = nodeList.getLength();
		if (length == 0) {
			addElement(docsE, docE, true);
			return;
		}

		int low = 0, high = length - 1;
		int mid;
		while (low <= high) {
			mid = (low + high) / 2;
			Element nodeE = (Element) nodeList.item(mid);
			String __text = getDescendantText(nodeE, "pubtime");
			int cmp = __text.compareTo(pubtime);
			if (cmp == 0) {
				high = mid;
				break;
			} else if (cmp < 0)
				high = mid - 1;
			else
				low = mid + 1;
		}

		// newE should be inserted after item(high), or insert
		// before item(high+1) if fatherE has item(high+1)
		if (high == length - 1) {
			addElement(docsE, docE, false);
		} else {
			// high < length - 1
			Element nodeE = (Element) nodeList.item(high + 1);
			docsE.insertBefore(docE, nodeE);
			// insert a new line
			Document doc = docsE.getOwnerDocument();
			docsE.insertBefore(doc.createTextNode("\n"), nodeE);
		}
	}

	public void addElementBySorting(Element fatherE, Element newE, String sortingBy) throws Exception {
		String text = getDescendantText(newE, sortingBy);
		if (text == null || text.length() == 0)
			return;

		NodeList nodeList = getDescendantElements(fatherE, newE.getTagName());
		int length = nodeList.getLength();
		if (length == 0) {
			addElement(fatherE, newE, true);
			return;
		}

		int low = 0, high = length - 1;
		int mid;
		while (low <= high) {
			mid = (low + high) / 2;
			Element nodeE = (Element) nodeList.item(mid);
			String __text = getDescendantText(nodeE, sortingBy);
			int cmp = text.compareTo(__text);
			if (cmp == 0) {
				high = mid;
				break;
			}
			else if (cmp < 0)
				high = mid - 1;
			else
				low = mid + 1;
		}

		// newE should be inserted after item(high), or insert
		// before item(high+1) if fatherE has item(high+1)
		if (high == length - 1) {
			addElement(fatherE, newE, false);
		} else {
			// high < length - 1
			Element nodeE = (Element) nodeList.item(high + 1);
			fatherE.insertBefore(newE, nodeE);
			// insert a new line
			Document doc = fatherE.getOwnerDocument();
			fatherE.insertBefore(doc.createTextNode("\n"), nodeE);
		}
	}
	
	public String getFirstChildElementText(Element fatherE) {
		NodeList nodeList = fatherE.getChildNodes();
		for(int i=0;i<nodeList.getLength();i++) {
			Node child = nodeList.item(i);
			if(child.getNodeType() == Node.ELEMENT_NODE) {
				Element childE = (Element)child;
				return childE.getTextContent();
			}
		}
		return "";
 	}

	public Element getDescendantElement(Object ancestor, String path) throws Exception {
		XPathExpression expr = getExpression(path);
		Element descendantE = (Element) expr.evaluate(ancestor, XPathConstants.NODE);
		return descendantE;
	}

	public NodeList getDescendantElements(Object ancestor, String path) throws Exception {
		XPathExpression expr = getExpression(path);
		NodeList nodes = (NodeList) expr.evaluate(ancestor, XPathConstants.NODESET);
		return nodes;
	}

	public String getDescendantText(Object ancestor, String path)
			throws Exception {
		XPathExpression expr = getExpression(path);
		String descendantT = (String) expr.evaluate(ancestor, XPathConstants.STRING);
		return descendantT;
	}

	public XPathExpression getExpression(String path) throws Exception {
		XPathExpression expr = exprMap.get(path);
		if (expr == null) {
			expr = xpath.compile(path);
			exprMap.put(path, expr);
		}
		return expr;
	}

	public Document createXmlDocument() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dblder = factory.newDocumentBuilder();
		Document doc = dblder.newDocument();
		doc.setXmlStandalone(true);

		return doc;
	}

	public Document getXmlDocument(String xmlFilePath) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dblder = factory.newDocumentBuilder();
		Document doc = dblder.parse(new File(xmlFilePath));

		return doc;
	}

	public void addElement(Element fatherE, Element newSonE, boolean isFirst) {
		if (isFirst)
			addLineSeparator(fatherE);
		fatherE.appendChild(newSonE);
		addLineSeparator(fatherE);
	}

	public void addLineSeparator(Element E) {
		Document doc = E.getOwnerDocument();
		E.appendChild(doc.createTextNode("\n"));
	}

	public void writeToFile(Document doc, String dstfilepath) throws Exception {
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer trans = factory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(dstfilepath));
		trans.transform(source, result);
	}
}
