package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Query;

import java.util.Enumeration;

public class QueryUtils {
	
	public static void copyQueryModifiers(Query q1, Query q2) {
		Enumeration<Object> keys = q1.getQueryModifiers().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			q2.getQueryModifiers().put(key, q1.getQueryModifiers().get(key));
		}
	}
	
	public static boolean isBoolean(DatalogProgram query) {
		for (CQIE rule: query.getRules()) {
			if (!isBoolean(rule))
				return false;
		}
		return true;
	}
	
	public static boolean isBoolean(CQIE query) {
		return query.getHead().getArity() == 0;
	}
}
