package org.semanticweb.ontop.quest;

/*
 * #%L
 * ontop-sparql-compliance
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

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to store the result set information. Please refer to the
 * <code>ResultSetInfoSchema</code> to look at the supported attributes.
 */
public class ResultSetInfo {

	private Map<String, Object> info = new HashMap<String, Object>();
	
	public ResultSetInfo() {
		// NO-OP
	}
	
	public ResultSetInfo(Map<String, Object> otherInfo) {
		info.putAll(otherInfo);
	}
	
	public void put(String key, Object value) {
		info.put(key, value);
	}
	
	public Object get(String key) {
		return info.get(key);
	}
}
