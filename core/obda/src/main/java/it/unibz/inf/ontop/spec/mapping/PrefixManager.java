package it.unibz.inf.ontop.spec.mapping;

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

import com.google.common.collect.ImmutableMap;

/**
 * Abstracts the prefix mapping mechanism.
 */
public interface PrefixManager {

	String getURIDefinition(String prefix);

	String getPrefix(String uri);

	String getDefaultPrefix();
	
	ImmutableMap<String, String> getPrefixMap();

	String getShortForm(String uri);

	String getShortForm(String uri, boolean insideQuotes);

	String getExpandForm(String prefixedName);
	
	String getExpandForm(String prefixedName, boolean insideQuotes);
	
	boolean contains(String prefix);

	String DEFAULT_PREFIX = ":";
}
