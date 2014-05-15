package org.semanticweb.ontop.renderer;

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

import java.util.Iterator;
import java.util.List;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.DatalogProgram;

/**
 * A utility class to render a Datalog Program object into its representational
 * string.
 */
public class DatalogProgramRenderer {

	/**
	 * Transforms the given <code>DatalogProgram</code> into a string
	 */
	public static String encode(DatalogProgram input) {
		List<CQIE> list = input.getRules();
		Iterator<CQIE> it =list.iterator();
		StringBuilder sb = new StringBuilder();
		while(it.hasNext()){
			CQIE q = it.next();
			if(sb.length()>0){
				sb.append("\n");
			}
			sb.append(q);
		}
		return sb.toString();
	}

	private DatalogProgramRenderer() {
		// Prevent initialization
	}
}
