/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.uri;

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Variable;

import java.util.Iterator;

public class UriTemplateHelper {

	public static String getUriTemplateString(Function uriFunction) {
		String template = uriFunction.getTerm(0).toString();
		Iterator<Variable> vars = uriFunction.getVariables().iterator();
		String[] split = template.split("\\{\\}");
		int i = 0;
		template = "";
		while (vars.hasNext()) {
			template += split[i] + "{" + vars.next().toString() + "}";
			i++;
		}
		if (split.length-i > 1)
		template += split[i];

		return template;
	}
}
