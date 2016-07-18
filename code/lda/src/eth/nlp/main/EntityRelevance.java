package eth.nlp.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class EntityRelevance {
	
	public static void refineQEMatch() throws Exception{
		
		BufferedReader br = new BufferedReader(new FileReader("./data/dev_bold/query_entity_dev.txt"));
        
		FileWriter fw = new FileWriter(new File("./data/dev_bold/query_entity_dev_refined.txt"), true);
		PrintWriter pw = new PrintWriter(fw);
		
		try {
	        String line = br.readLine();
	        int order = 0;
	        while (line != null) {
//	            System.out.println(line);
	            String[] query_entity = line.split("\t");
	            
	            order ++;
	            if(order%100 ==0) {
	            	System.out.println(order);
	            }
	            pw.print(query_entity[0]);
	            
	            List<String> list = Arrays.asList(query_entity).subList(1, query_entity.length);
	            
	            List<String> newlist = new ArrayList<String>();
	    		for (String ele: list) {
	    			boolean flag = true;
	    			for (String newele: newlist) {
	    				if (newele.equalsIgnoreCase(ele)) {
	    					flag = false;
	    				}
	    			}
	    			if(flag) {
	    				newlist.add(ele);
	    				pw.print('\t' + ele);
	    			}
	    		}
	    		pw.print('\n');
	            line = br.readLine();
	        }
	        
	    } finally {
	        br.close();
	    }
        
        if (pw != null){
            pw.close();
        }
        if (fw != null){
            fw.close();
        }
        
	}
	
	public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
	    double dotProduct = 0.0;
	    double normA = 0.0;
	    double normB = 0.0;
	    for (int i = 0; i < vectorA.length; i++) {
	        dotProduct += vectorA[i] * vectorB[i];
	        normA += Math.pow(vectorA[i], 2);
	        normB += Math.pow(vectorB[i], 2);
	    }   
	    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}
	
	public static void main(String[] args) throws Exception{
		
//		refineQEMatch();
		
		String dir = "LDA-devbold-20-filtered-3000/";
		BufferedReader br = initReader("./data/dev_bold/query_entity_dev_refined.txt", "UTF-8");
		PrintStream ps = initPrintStream("./data/dev_bold/entity_relevence_dev_20_3000.txt", "UTF-8"); 	
		List<double[]> thetamatrix = readTheta(dir + "model-final.theta");
		
		String line = null;
		int curline = 0;
		int i = 0;
		
		while((line = br.readLine()) != null){
			String[] query_entity = line.split("\t");
			int size = query_entity.length;
			
			ps.print(query_entity[0]);
			double[] queryline = thetamatrix.get(curline);
			curline ++;
			
			Map<String, Double> map=new HashMap<String, Double>();
			for (i=0; i<size-1; i++){
				double[] entityline = thetamatrix.get(curline+i);
				double relevance = cosineSimilarity(queryline, entityline);
				map.put(query_entity[i+1], relevance);
			}
			// sort relevance
			List<Map.Entry<String,Double>> list=new ArrayList<>();  
	        list.addAll(map.entrySet());  
	        EntityRelevance.ValueComparator vc=new ValueComparator();
	        Collections.sort(list,vc);
	        
	        for(Iterator<Map.Entry<String,Double>> it=list.iterator();it.hasNext();)  
	        {  
	        	Entry<String, Double> pair = it.next();
	        	ps.print('\t'+ pair.getKey() + ":" + pair.getValue());
	        } 
	        
			ps.print('\n');
			
		}
		closeReader(br);
		closePrintStream(ps);
	
	}
	
	private static class ValueComparator implements Comparator<Map.Entry<String, Double>>  
    {  
        public int compare(Map.Entry<String,Double> m,Map.Entry<String,Double> n)  
        {  
            if(n.getValue()-m.getValue()>0) return 1;
            else if(n.getValue()-m.getValue()==0 )	return 0;
            else return -1;  
        }
    }
	
	private static List<double[]> readTheta(String filename) throws Exception{
		List<double[]> list = new ArrayList<double[]>();
		BufferedReader br = initReader(filename, "UTF-8");
		String line = null;
		while((line = br.readLine()) != null){
			String[] valuestrs = line.split(" ");
			double[] values = new double[valuestrs.length];
			for(int i=0; i<valuestrs.length; i++) {
				values[i] = Double.parseDouble(valuestrs[i]);
			}
			list.add(values);
		}
		closeReader(br);
		return list;
	}

	private static BufferedReader initReader(String fpath, String charset) throws Exception
	{
		FileInputStream fis = new FileInputStream(fpath);
		BufferedReader reader = new BufferedReader(new InputStreamReader(fis,charset));
		return reader;
	}
	
	private static void closeReader(BufferedReader reader) throws Exception
	{
		reader.close();
	}
	
	private static PrintStream initPrintStream(String fpath, String charset) throws Exception
	{
		FileOutputStream fos = new FileOutputStream(fpath);
		PrintStream ps = new PrintStream(fos, false, "UTF-8");
		return ps;
	}
	
	private static void closePrintStream(PrintStream ps) throws Exception
	{
		ps.flush();
		ps.close();
	}
	
	
}
