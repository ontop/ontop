package it.unibz.krdb.obda.io;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
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

import java.util.Map;

/**
 * Abstracts the prefix mapping mechanism.
 */
public interface PrefixManager {
	
	public static final String DEFAULT_PREFIX = ":";
	
	/**
	 * Registers a prefix. Leave blank for BASE prefix.
	 * 	 
	 * @param name
	 * 			The prefix name (without the colon).
	 * @param uri
	 * 			The URI definition for the given prefix.
	 */
	public void addPrefix(String prefix, String uri);

	public String getURIDefinition(String prefix);

	public String getPrefix(String uri);

	public String getDefaultPrefix();
	
	public Map<String, String> getPrefixMap();

	public String getShortForm(String uri);

	public String getShortForm(String uri, boolean insideQuotes);

	public String getExpandForm(String prefixedName);
	
	public String getExpandForm(String prefixedName, boolean insideQuotes);
	
	public boolean contains(String prefix);
	
	public void clear();
}
