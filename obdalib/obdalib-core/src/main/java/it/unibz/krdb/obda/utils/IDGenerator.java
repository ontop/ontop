package it.unibz.krdb.obda.utils;

import java.util.UUID;

public class IDGenerator {
	public static synchronized String getNextUniqueID(String prefix) {
		StringBuffer sb = new StringBuffer(prefix);
		sb.append(UUID.randomUUID().toString().replace("-", ""));
		return sb.toString();
	}
}
