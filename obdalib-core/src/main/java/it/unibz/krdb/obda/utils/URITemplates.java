package it.unibz.krdb.obda.utils;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.exception.InvalidPrefixWritingException;
import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

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
	 * If {@code args = ["A", 1]}, then
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

	
	public static String getUriTemplateString(Function uriFunction) {
		Term term = uriFunction.getTerm(0);
		String template = term.toString();
		Iterator<Variable> vars = uriFunction.getVariables().iterator();
		String[] split = template.split("\\{\\}");
		int i = 0;
		template = "";
		while (vars.hasNext()) {
			template += split[i] + "{" + vars.next().toString() + "}";
			i++;
		}
		//the number of place holdes should be equal to the number of variables.
		if (split.length-i == 1){
			template += split[i];
			//we remove the quotes cos later the literal constructor adds them
			template= template.substring(1, template.length()-1);
		}else{
			throw new IllegalArgumentException("the number of place holdes should be equal to the number of variables.");
		}
		
		
		return template;
	}

	public static String getUriTemplateString(Function uriTemplate,
			PrefixManager prefixmng) {
		String template = getUriTemplateString(uriTemplate);
		try{
		template = prefixmng.getExpandForm(template);
		} catch (InvalidPrefixWritingException ex){
			// in this case, the we do not need to expand
		}
		return template;
	}


}
