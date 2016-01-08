package it.unibz.krdb.obda.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class EncodeForURI {

	private static final Logger log = LoggerFactory.getLogger(EncodeForURI.class);
	
	public static final ImmutableMap<String, String> TABLE = ImmutableMap.<String, String>builder()
			.put("%20", " ") // optional
			.put("%21", "!")
			.put("%22", "''") // ROMAN (24 Dec 2015): why is it not "?
			.put("%23", "#")
			.put("%24", "$")
			.put("%26", "&") 
			// ROMAN (24 Dec 2015): what about %27 = '?
			.put("%28", "(")
			.put("%29", ")") 
			.put("%2A", "*") // ROMAN (24 Dec 2015): it was 42, i.e., in decimal!
			.put("%2B", "+")
			.put("%2C", ",")
			.put("%2F", "/")
			.put("%3A", ":")
			.put("%3B", ";")
			.put("%3D", "=")
			.put("%3F", "?")
			.put("%40", "@")
			.put("%5B", "[")
			.put("%5D", "]") 
			.build();
	
	
	/***
	 * Given a string representing a URI, this method will return a new String 
	 * in which all percent encoded characters (e.g., %20) will
	 * be restored to their original characters (e.g., ' '). 
	 * This is necessary to transform some URIs into the original database values.
	 * 
	 * @param encodedURI
	 * @return
	 */
	
	public static String decodeURIEscapeCodes(String encodedURI) {
		
		int length = encodedURI.length();
		StringBuilder strBuilder = new StringBuilder(length+20);
		char[] codeBuffer = new char[3];
		
		for (int i = 0; i < length; i++) {
			char c = encodedURI.charAt(i);

			if (c != '%') {
				// base case, the character is a normal character, just append
				strBuilder.append(c);
				continue;
			}

			// found a escape, processing the code and replacing it by
			// the original value that should be found on the DB. This
			// should not be used all the time, only when working in
			// virtual mode... we need to fix this with a FLAG.
			// First we get the 2 chars next to %
			codeBuffer[0] = '%';
			codeBuffer[1] = encodedURI.charAt(i + 1);
			codeBuffer[2] = encodedURI.charAt(i + 2);

			// now we check if they match any of our escape codes, if
			// they do the char to be inserted is put in codeBuffer otherwise
			String code = String.copyValueOf(codeBuffer);
			String rep = TABLE.get(code);
			if (rep != null)
				strBuilder.append(rep);
			else {
				// This was not an escape code, so we just append the characters and continue;
				log.warn("Error decoding an encoded URI from the query. Problematic code: {}\nProblematic URI: {}", code, encodedURI);
				strBuilder.append(codeBuffer);
			}
			i += 2;
		}
		return strBuilder.toString();
	}
}
