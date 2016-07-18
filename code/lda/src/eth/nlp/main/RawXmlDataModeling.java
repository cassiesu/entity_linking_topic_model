package eth.nlp.main;

import java.io.File;

import org.kohsuke.args4j.CmdLineParser;

import eth.nlp.common.Util;
import eth.nlp.lda.Estimator;
import eth.nlp.lda.LDACmdOption;

/**
 * Raw XML data is the data obtained by crawler, within which 
 * much of duplication may exist. Before exercising LDA modeling, 
 * we need remove those duplication first.
 *
 */
public class RawXmlDataModeling {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String dir = "./data/dev_bold/";
		String XmlFile = "document.xml";          // the name of the file which is to store XML data stripped of duplication
		
		int topicCount = 20;
	
		/**
		 * create LDA directory
		 */
		File ldaDirFile = new File("LDA-devbold-" + topicCount + "-filtered-3000");
		if(!ldaDirFile.exists() || !ldaDirFile.isDirectory()) {
			Util.myAssert(ldaDirFile.mkdir());
		}
//		File file = new File(dir + "RawSplitData.txt");
//		Util.outputSplitContent(dir + XmlFile, file.getAbsolutePath(), true);
			
		/**
		 * Exercise LDA modeling
		 */
		String arguments[] = new String[]{"-est", "-alpha", "0.5", "-dfile", "FilteredRawSplitData.txt", "-dir", 
				ldaDirFile.getAbsolutePath(), "-niters", "3000", "-ntopics", 
				String.valueOf(topicCount), "-savestep", "3000", "-twords", "50"};
		LDACmdOption option = new LDACmdOption();
		CmdLineParser parser = new CmdLineParser(option);
		parser.parseArgument(arguments);
		Estimator estimator = new Estimator();
		estimator.init(option);
		estimator.estimate();
	
	}
}
