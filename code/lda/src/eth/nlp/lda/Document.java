package eth.nlp.lda;

import java.util.Vector;

public class Document {

	//----------------------------------------------------
	//Instance Variables
	//----------------------------------------------------
	public int [] words;
	public String rawStr;
	public int length;
	
	//----------------------------------------------------
	//Constructors
	//----------------------------------------------------
	public Document(){
		words = null;
		rawStr = "";
		length = 0;
	}
	
	public Document(int length){
		this.length = length;
		rawStr = "";
		words = new int[length];
	}
	
	public Document(int length, int [] words){
		this.length = length;
		rawStr = "";
		//this.words = words.clone();
		this.words = new int[length];
		for (int i =0 ; i < length; ++i){
			this.words[i] = words[i];
		}
	}
	
	public Document(int length, int [] words, String rawStr){
		this.length = length;
		this.rawStr = rawStr;
		
		this.words = new int[length];
		for (int i =0 ; i < length; ++i){
			this.words[i] = words[i];
		}
	}
	
	public Document(Vector<Integer> doc){
		this.length = doc.size();
		rawStr = "";
		this.words = new int[length];
		for (int i = 0; i < length; i++){
			this.words[i] = doc.get(i);
		}
	}
	
	public Document(Vector<Integer> doc, String rawStr){
		this.length = doc.size();
		this.rawStr = rawStr;
		this.words = new int[length];
		for (int i = 0; i < length; ++i){
			this.words[i] = doc.get(i);
		}
	}
}
