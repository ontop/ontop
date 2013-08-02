/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core;

import java.util.LinkedList;

import junit.framework.TestCase;

public class TestURIIdentifier extends TestCase {

	public void test_1() throws Exception {

		LinkedList<String> sql = new LinkedList<String>();
		LinkedList<String> cqs = new LinkedList<String>();

		StringBuilder query = new StringBuilder("SELECT * FROM table, etable( est lasldflasd ) table2 WHERE table2.x NOT IN ETABLE   ( some sparql query) asdf;");
		while (true) {
			String[] splitquery = query.toString().split("[eE][tT][aA][bB][lL][eE]\\s*\\(.+?\\)", 2);
			if (splitquery.length > 1) {
				sql.add(splitquery[0]);
				query.delete(0, splitquery[0].length());
				int position = query.toString().indexOf(splitquery[1]);
				
				String regex = query.toString().substring(0,position); 
				
				cqs.add(regex.substring(regex.indexOf("(")+1, regex.length()-1));
				query = new StringBuilder(splitquery[1]);
				
			} else {
				sql.add(splitquery[0]);
				break;
			}
			
			
		}
		for (String sqli: sql) {
//			System.out.println(sqli);
		}
//		System.out.println("now the CQs");
		for (String cqs1: cqs) {
//			System.out.println(cqs1);
		}
		
		

		// String[] content = new String[query.length() -1 ];

		// int length = 0;
		// for (int i =0; i < splitquery.length; i++) {
		// length += splitquery[0].length();
		// content[i] = query.substring(beginIndex, endIndex)
		// }
	}
}
