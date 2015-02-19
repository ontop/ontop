package it.unibz.krdb.obda.owlrefplatform.core.abox;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class XsdDatatypeConverter {
	
	/**
	 * @see http://www.w3.org/TR/xmlschema11-2/#boolean
	 * @param value from the lexical space of xsd:boolean
	 * @return boolean
	 */

	public static boolean parseXsdBoolean(String value) {
		
		if (value.equals("true") || value.equals("1")) 
			return true;
		else if (value.equals("false") || value.equals("0")) 
			return false;

		throw new RuntimeException("Invalid lexical form for xsd:boolean. Found: " + value);		
	}
	

	// TODO: double-check the formats
	
	private static final String[] formatStrings = { 
				"yyyy-MM-dd HH:mm:ss.SS", 
				"yyyy-MM-dd HH:mm:ss.S", 
				"yyyy-MM-dd HH:mm:ss", 
				"yyyy-MM-dd",
				"yyyy-MM-dd'T'HH:mm:ssz" };
	
	/**
	 * @see http://www.w3.org/TR/xmlschema11-2/#dateTime
	 * @param lit
	 * @return
	 */
	
	public static Timestamp parseXsdDateTime(String value) {

		for (String formatString : formatStrings) {
			try {
				long time = new SimpleDateFormat(formatString).parse(value).getTime();
				Timestamp ts = new Timestamp(time);
				return ts;
			} 
			catch (ParseException e) {
			}
		}
		throw new RuntimeException("Invalid lexical form for xsd:dateTime. Found: " + value);		
	}

	

}
