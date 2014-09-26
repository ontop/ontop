package it.unibz.krdb.obda.owlapi3.bootstrapping;

/*
 * #%L
 * ontop-obdalib-owlapi3
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

import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.owlapi3.directmapping.DirectMappingEngine;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.semanticweb.owlapi.model.OWLOntology;

public abstract class AbstractDBMetadata
{
	
	private OWLOntology onto;
	private OBDAModel model;
	private OBDADataSource source;
	
	protected DBMetadata getMetadata() throws Exception 
	{
		DBMetadata metadata = null;

			Connection conn = DriverManager.getConnection(source.getParameter(RDBMSourceParameterConstants.DATABASE_URL),
					source.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME), source.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD));
			metadata = JDBCConnectionManager.getMetaData(conn);
		

		return metadata;
	}
	
	protected void getOntologyAndDirectMappings(String baseuri, OWLOntology onto, OBDAModel model, OBDADataSource source) throws Exception {
		this.source = source;	
		DirectMappingEngine engine = new DirectMappingEngine(baseuri, model.getMappings(source.getSourceID()).size());
		this.model =  engine.extractMappings(model, source);
		this.onto =  engine.getOntology(onto, onto.getOWLOntologyManager(), model);
	}
	
	protected void getOntologyAndDirectMappings(DBMetadata metadata, String baseuri, OWLOntology onto, OBDAModel model, OBDADataSource source) throws Exception {
		this.source = source;	
		DirectMappingEngine engine = new DirectMappingEngine(metadata, baseuri, model.getMappings(source.getSourceID()).size());
		this.model =  engine.extractMappings(model, source);
		this.onto =  engine.getOntology(onto, onto.getOWLOntologyManager(), model);
	}
	
	protected OBDAModel getOBDAModel()
	{
		return this.model;
	}
	
	protected OWLOntology getOWLOntology()
	{
		return this.onto;
	}
	
}
