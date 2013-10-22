/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.quest;

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
