package eth.nlp.htmlparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import eth.nlp.common.Split;

public class ParserTest {
	
	public static String contentFilter(String content) throws Exception{
        content = Split.words(content, true);		//filter stopwords and use stemming 
		return content;
	}
	
	public static String parseBing(String query) throws Exception{
		StringBuffer sb = new StringBuffer();
		Connection conn = Jsoup.connect("https://www.bing.com/search?q=" + query.replace(" ", "+"));
		conn.timeout(20000);
		Document doc = conn.get();
		Elements snippets = doc.select(".b_algo");
		for(Element snippet: snippets) {
			String title = snippet.select("h2 a").html().replace("<strong>", "").replace("</strong>", "");
			title = contentFilter(title);
			sb.append(title + " ");
			String content = snippet.select(".b_caption p").html();
			content = contentFilter(content);
			sb.append(content + " ");
		}
//		System.out.println(query);
//		System.out.println(sb.toString().length());
		return sb.toString();
	}
	
	public static Map<String,String> parseWiki(List<String> list) throws Exception{
		/**
		 * Filter same wikipages
		 */
		List<String> newlist = new ArrayList<String>();
		for (String ele: list) {
			boolean flag = true;
			for (String newele: newlist) {
				if (newele.equalsIgnoreCase(ele)) {
					flag = false;
				}
			}
			if(flag) newlist.add(ele);			
		}
		
		Map<String, String> map = new HashMap<String, String>();
		for(String entity: newlist) {
			StringBuffer sb = new StringBuffer();
			Connection conn = Jsoup.connect("https://en.wikipedia.org/wiki/" + entity);
			conn.timeout(20000);
			Document doc = conn.get();
			Elements paras = doc.select("#mw-content-text > p");
			for(Element para: paras) {
				if(!"".equals(para.html())) {
					String content = para.html();
					content = contentFilter(content);
					sb.append(content + " ");	
				}
				if(sb.length()>=1500) {
					break;
				}
			}
//			System.out.println(entity);
//			System.out.println(sb.toString().length());
			map.put(entity, sb.toString());
		}
		return map;
	}
	
	public static void main(String[] args) throws Exception{
		String dir = "./data/test_bold/";
		BufferedReader br = new BufferedReader(new FileReader(dir+"query_entity_test_refined.txt"));
		
		org.dom4j.Document document = DocumentHelper.createDocument();  
        org.dom4j.Element root = document.addElement("docs");
        
		try {
	        String line = br.readLine();
	        int order = 0;
	        while (line != null) {
//	            System.out.println(line);
	            String[] query_entity = line.split("\t");
	            
	            order ++;
	            if(order%10 ==0) {
	            	System.out.println(order);
	            }
	            
	            String query = query_entity[0];
	            List<String> entities = Arrays.asList(query_entity).subList(1, query_entity.length);
	            
	            String content = parseBing(query);
	            org.dom4j.Element doc = root.addElement("doc");
	            doc.addElement("title").addText(order + "\t" + query);
	            doc.addElement("splitcontent").addText(content);
	            
	            Map entitymap = parseWiki(entities);
	            Iterator iter = entitymap.entrySet().iterator(); 
	            while (iter.hasNext()) { 
	            	Map.Entry entry = (Map.Entry) iter.next(); 
	            	String key = (String)entry.getKey(); 
	            	String val = (String)entry.getValue();
	            	org.dom4j.Element wikidoc = root.addElement("doc");
	            	wikidoc.addElement("title").addText(order + "\t" + key);
	            	wikidoc.addElement("splitcontent").addText(val);
	            }
	            
	            line = br.readLine();
	        }
	        
	    } finally {
	        br.close();
	    }
		
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("UTF-8");
		XMLWriter writer = new XMLWriter(new FileWriter(new File(  
                dir + "document.xml")), format);  
        writer.write(document); 
        writer.close();
	}
	
}
