package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDAQuery;

public class QueryUtils {
	
	public static void copyQueryModifiers(OBDAQuery source, OBDAQuery target) {
		target.getQueryModifiers().copy(source.getQueryModifiers());
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
