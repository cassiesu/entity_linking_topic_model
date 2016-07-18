package eth.nlp.lda;

public class Conversion {
	public static String ZeroPad( int number, int width )
	{
	      StringBuffer result = new StringBuffer("");
	      for( int i = 0; i < width-Integer.toString(number).length(); i++ )
	         result.append( "0" );
	      result.append( Integer.toString(number) );
	     
	      return result.toString();
	}
}
