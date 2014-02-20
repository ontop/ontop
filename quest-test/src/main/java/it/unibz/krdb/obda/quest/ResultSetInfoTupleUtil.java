package it.unibz.krdb.obda.quest;

/*
 * #%L
 * ontop-test
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

import static it.unibz.krdb.obda.quest.ResultSetInfoSchema.RESULTSET_SIZE;
import static it.unibz.krdb.obda.quest.ResultSetInfoSchema.THROWN_EXCEPTION;

import java.util.Set;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.Value;

public class ResultSetInfoTupleUtil {

	public static ResultSetInfo toResuleSetInfo(Set<Statement> resultGraph) {
		ResultSetInfo rsInfo = new ResultSetInfo();
		for (Statement stmt : resultGraph) {
			URI predicate = getPredicate(stmt);
			if (predicate.equals(RESULTSET_SIZE)) {
				rsInfo.put("counter", getIntValue(stmt));
			} else if (predicate.equals(THROWN_EXCEPTION)) {
				rsInfo.put("thrownException", getStringValue(stmt));
			} else if (predicate.equals(RDF.TYPE)) {
				// NO-OP
			} else {
				throw new RuntimeException("Unsupported URI: " + predicate);
			}
		}
		return rsInfo;
	}
	
	private static URI getPredicate(Statement statement) {
		return statement.getPredicate();
	}
	
	private static String getStringValue(Statement statement) {
		return getObject(statement).stringValue();
	}
	
	private static int getIntValue(Statement statement) {
		return Integer.parseInt(getObject(statement).stringValue());
	}
	
	private static Value getObject(Statement statement) {
		return statement.getObject();
	}
}
