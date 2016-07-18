package eth.nlp.main;

import java.io.*;
import java.util.*;

import eth.nlp.common.Term;

public class WordFilter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub		
		try{
			String dir = "./data/dev_bold/"	;
			StringBuilder line = new StringBuilder();
			
			Map<Term,Term> map = new HashMap<Term,Term>();
			Term tempTerm = new Term();
			
			BufferedReader reader = initReader(dir + "RawSplitData.txt", "UTF-8");
			String fContent = null;
			
			int fIdx = 0;
			while((fContent = reader.readLine()) != null){
				System.out.println(++fIdx);
				String[] words = fContent.split(" ");
				for (String word :words){
					if(word.length()>=2 && !word.startsWith("&")) {
						tempTerm.setTermTxt(word);
						Term term = map.get(tempTerm);
						if(term != null){
							term.addTermNum(1);
						}else{
							term = new Term(word);
							map.put(term, term);
						}
					}
				}
				
			}
			closeReader(reader);
			
			PriorityQueue<Term> queue = new PriorityQueue<Term>(map.keySet());
			int size = queue.size();
			System.out.println("size:"+size);
			int downL = 1, upL = fIdx * 3 / 2; //down limit and up limit
			for(int i=0;i<size;i++){
				Term term = queue.poll();
				if(term.getTermNum()<=downL || term.getTermNum()>=upL)
					map.remove(term);
			}
			System.out.println("deleted:"+(size-map.size())+",left:"+map.size());			

			System.out.println("-------------------------------------------------------------------");
			
			//lda.dat		
			reader = initReader(dir + "RawSplitData.txt", "UTF-8");			
			PrintStream ps = initPrintStream(dir + "FilteredRawSplitData.txt", "UTF-8"); 			
			
			ps.print(fIdx-1);
			fIdx = 0;
			
			while((fContent = reader.readLine()) != null){
				System.out.println(++fIdx);
				String[] words = fContent.split(" ");
				line.setLength(0);
				
				for (String word :words){
					if(!word.startsWith("&")) {
						tempTerm.setTermTxt(word);
						if(map.containsKey(tempTerm)){
							line.append(word +" ");
						}
					}
				}
				ps.println(line.toString());
			}
			
			closeReader(reader);
			closePrintStream(ps);
		}catch(Exception e){
			e.printStackTrace();
		}
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