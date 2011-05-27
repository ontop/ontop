package inf.unibz.it.obda.utils;

import inf.unibz.it.obda.model.Query;

import java.util.Enumeration;

public class QueryUtils {
	public static void copyQueryModifiers(Query q1, Query q2) {
		Enumeration<Object> keys = q1.getQueryModifiers().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			q2.getQueryModifiers().put(key, q1.getQueryModifiers().get(key));
		}
	}
}
