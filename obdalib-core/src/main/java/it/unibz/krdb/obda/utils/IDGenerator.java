/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.utils;

import java.util.UUID;

public class IDGenerator {
	public static synchronized String getNextUniqueID(String prefix) {
		StringBuilder sb = new StringBuilder(prefix);
		sb.append(UUID.randomUUID().toString().replace("-", ""));
		return sb.toString();
	}
}
