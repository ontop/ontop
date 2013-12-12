package it.unibz.krdb.obda.utils;

import java.util.UUID;

public class IDGenerator {
	public static synchronized String getNextUniqueID(String prefix) {
		StringBuilder sb = new StringBuilder(prefix);
		sb.append(UUID.randomUUID().toString().replace("-", ""));
		return sb.toString();
	}
}
