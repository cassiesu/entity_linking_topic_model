package eth.nlp.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Scanner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Util {
	
	private static XMLProcessor mXmlProcessor = XMLProcessor.getInstance();
	
	public static void myAssert(boolean isOK) {
		if(!isOK) {
			// output the exceptional message
			new Exception("myAssert fails").printStackTrace();
			System.exit(-1);
		}
	}
	
	public static boolean isNullOrEmpty(String str) {
		if(str == null || str.matches("\\s*")) {
			return true;
		}
		return false;
	}
	
	public static BufferedReader getBufferedReader(String path, String encoding) throws Exception{
		FileInputStream fis = new FileInputStream(path);
		InputStreamReader isReader = new InputStreamReader(fis,encoding);
		BufferedReader reader = new BufferedReader(isReader);
		return reader;
	}
	
	public static BufferedWriter getBufferedWriter(String path, String encoding) throws Exception{
		FileOutputStream fis = new FileOutputStream(path);
		OutputStreamWriter isWriter = new OutputStreamWriter(fis,encoding);
		BufferedWriter Writer = new BufferedWriter(isWriter);
		return Writer;
	}
	
	public static String getSplitContent(String srcXmlFile, boolean includeNumber) throws Exception {

		Document doc = mXmlProcessor.getXmlDocument(srcXmlFile);
		NodeList nodeList = mXmlProcessor.getDescendantElements(doc, "/docs/doc");
		int docNumber = nodeList.getLength();
		
		StringBuilder strb = new StringBuilder();
		if(includeNumber) {
			strb.append(String.valueOf(docNumber)).append("\n");
		}
		
		for (int i = 0; i < docNumber; i++) {
			Element docE = (Element) nodeList.item(i);
			String splitcontent = mXmlProcessor.getDescendantText(docE, "splitcontent");
			strb.append(splitcontent).append("\n");
		}
		
		return strb.toString();
	}
	
	public static void outputSplitContent(String srcXmlFile, String dstFile, boolean includeNumber) throws Exception {
		Document doc = mXmlProcessor.getXmlDocument(srcXmlFile);
		NodeList nodeList = mXmlProcessor.getDescendantElements(doc, "/docs/doc");
		outputSplitContent(nodeList, dstFile, includeNumber);
	}
	
	public static void outputSplitContent(NodeList docList, String dstFile, boolean includeNumber) throws Exception {
		int docNumber = docList.getLength();
		
		PrintStream ps = new PrintStream(dstFile, "UTF-8");
		if(includeNumber) {
			ps.println(docNumber);
		}
		
		for (int i = 0; i < docNumber; i++) {
			Element docE = (Element) docList.item(i);
			ps.println(mXmlProcessor.getDescendantText(docE, "splitcontent"));
		}
		
		ps.flush();
		ps.close();
	}
	
	public static void outputContentByTopic(String srcXmlFile, String dstFile, 
			int topicId, String thetaFile, int topicNumber) throws Exception {
		Document doc = mXmlProcessor.getXmlDocument(srcXmlFile);
		NodeList nodeList = mXmlProcessor.getDescendantElements(doc, "/docs/doc");
		
		Scanner scan = new Scanner(new FileInputStream(thetaFile), "utf-8");
		PrintStream ps = new PrintStream(new File(dstFile), "utf-8");
		ps.println("documents belong to topic " + topicId);
		
		for(int i = 0; i < nodeList.getLength(); i++) {
			double __maxWeight = 0, __topicWeight = 0.0;
			
			for(int k = 0; k < topicNumber; k++) {
				double __weight = scan.nextDouble();
				if(__weight >= __maxWeight ) {
					__maxWeight = __weight;
				}
				if(k == topicId) {
					__topicWeight = __weight;
				}
			}
			
			if(__topicWeight >= __maxWeight || __maxWeight - __topicWeight < 0.0001) {		//衡量doc是不是支持该topic需要制定标准
				Element docE = (Element)nodeList.item(i);
				ps.println(mXmlProcessor.getDescendantText(docE, "content"));
				ps.println();
			}
		}
		
		if(scan.hasNextDouble()) {
			throw new Exception("theta file hasn't be finished reading!");
		}
		
		ps.close();
		scan.close();
	}
	
	public static void main(String args[]) throws Exception {
		outputContentByTopic("Corpora\\Lianghui\\2011\\Data-RD.xml", "D:\\topic15.txt", 15, "Corpora\\Lianghui\\2011\\LDA\\model-final.theta", 120);
	}
}
