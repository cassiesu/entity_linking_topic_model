package eth.nlp.common;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

public class Split {

	public static void main(String[] args) {
		System.out.println(Split.words(
				"The large rain in spains falls mainly .,\"\\?!:'. on the PLAINS</?[^<]+>\r\n, except when it's not exactly working that way! A I need+some= way. How~ will \"(this)\" \"Work\"?", 
				true)
			);
	}

	static HashSet stopwords = new HashSet();

	public static void addStopwords() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					"stopwords.txt"));

			while (br.ready()) {
				stopwords.add(br.readLine());
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static String words(String line, boolean flag) {			//flag=true -- use stemmer	flag=false -- not use stemmer
		if (stopwords.size() == 0)
			addStopwords();
		
		line = line.replaceAll("</?[^<]+>", "").replaceAll("\r|\n", "").replaceAll("  ", " ");
		Pattern p = Pattern.compile("[.…,\"\\?!:|'1234567890]");
        Matcher m = p.matcher(line);
        line = m.replaceAll("");
        
		String[] words = line
				.split("[ \t,\\.\"!?^$~()\\[\\]\\{\\}:;/\\\\<>+=%*]");
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < words.length; i++) {
			if (words[i] != null && !words[i].equals("")) {
				String word = words[i].toLowerCase();
				if (!stopwords.contains(word)) {
					if(flag) {
						sb.append(Stemmer.stem(word) + " ");		//Stemmer
					} else {
						sb.append(word + " ");
					}
					
				}
			}
		}
		
		return sb.toString();
	}

}