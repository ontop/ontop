/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.reformulation.tests;

import java.util.HashMap;

public class OntologyGeneratorExpression {

	private String id = null;
	private String abox = null;
	private String tbox = null;
	private HashMap<String,String> queries = null;
	private HashMap<String,String> queriesid = null;
	public String getId() {
		return id;
	}
	
	public OntologyGeneratorExpression(String id, String tbox, String abox){
		this.id = id;
		this.abox = abox;
		this.tbox = tbox;
		queries = new HashMap<String,String>();
		queriesid = new HashMap<String, String>();
	}
	
	public String getAbox() {
		return abox;
	}
	public String getTbox() {
		return tbox;
	}
	public HashMap<String, String> getQueries() {
		return queries;
	}
	public HashMap<String, String> getQueriesIds() {
		return queriesid;
	}
	public void addQuery(String q, String result){
		queries.put(q,result);
	}
	public void addQueryID(String q, String id){
		queriesid.put(q,id);
	}
}
