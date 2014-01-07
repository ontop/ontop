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

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;

public class QueryUtils {

	public static void copyQueryModifiers(OBDAQuery source, OBDAQuery target) {
		target.getQueryModifiers().copy(source.getQueryModifiers());
	}

	public static boolean isBoolean(DatalogProgram query) {
		for (CQIE rule : query.getRules()) {
			if (!isBoolean(rule))
				return false;
		}
		return true;
	}

	public static boolean isBoolean(CQIE query) {
		return query.getHead().getArity() == 0;
	}

	public static boolean isGrounded(Term term) {
		boolean result = true;
		if (term instanceof Variable) {
			result = false;
		} else if (term instanceof Function) {
			Function func = (Function) term;
			for (Term subTerm : func.getTerms()) {
				if (!isGrounded(subTerm))
					result = false;
			}
		} 
		return result;
	}
}
