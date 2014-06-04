package org.semanticweb.ontop.reformulation.tests;

/*
 * #%L
 * ontop-quest-owlapi3
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
