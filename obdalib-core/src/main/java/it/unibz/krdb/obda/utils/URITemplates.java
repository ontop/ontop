package it.unibz.krdb.obda.utils;

import java.util.Arrays;
import java.util.Collection;

/**
 * A utility class for URI templates
 * 
 * @author xiao
 *
 */
public class URITemplates {
	
	private static final String PLACE_HOLDER = "{}";
	private static final int PLACE_HOLDER_LENGTH = PLACE_HOLDER.length();
	
	/**
	 * This method instantiates the input uri template by arguments
	 * 
	 * <p>
	 * 
	 * Example:
	 * <p>
	 * 
	 * If {@code args = ["A", a]}, then
	 * 
	 * {@code  URITemplates.format("http://example.org/{}/{}", args)} 
	 * results {@code "http://example.org/A/1" }
	 * 
	 * 
	 * @see #format(String, Object...)
	 * 
	 * @param uriTemplate
	 * @param args
	 * @return
	 */
	public static String format(String uriTemplate, Collection<?> args) {
		
		StringBuilder sb = new StringBuilder();
		
		int beginIndex = 0;
		
		for(Object arg : args){
			
			int endIndex = uriTemplate.indexOf(PLACE_HOLDER, beginIndex);
			
			sb.append(uriTemplate.subSequence(beginIndex, endIndex)).append(arg);
			
			beginIndex = endIndex + PLACE_HOLDER_LENGTH;
		}
		
		sb.append(uriTemplate.substring(beginIndex));
		
		return sb.toString();
		
	}

	/**
	 * This method instantiates the input uri template by arguments
	 * 
	 * <p>
	 * 
	 * Example:
	 * <p>
	 * 
	 * {@code  URITemplates.format("http://example.org/{}/{}", "A", 1)} results {@code "http://example.org/A/1" }
	 * 
	 * @param uriTemplate
	 * @param args
	 * @return
	 */
	public static String format(String uriTemplate, Object... args) {
		return format(uriTemplate, Arrays.asList(args));
	}


}
