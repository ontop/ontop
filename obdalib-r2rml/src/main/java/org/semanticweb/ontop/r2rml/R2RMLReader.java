package org.semanticweb.ontop.r2rml;

/*
 * #%L
 * ontop-obdalib-sesame
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
/**
 * @author timea bagosi
 * Class responsible to construct an OBDA model from an R2RML mapping file or graph.
 */
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import org.openrdf.model.Model;

public class R2RMLReader {
	
	private R2RMLManager manager;
	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	private OBDAModel obdaModel = fac.getOBDAModel();
	
	private Model m ;
	
	public R2RMLReader(Model m) {
		manager = new R2RMLManager(m);
		this.m = m;
	}
	
	public R2RMLReader(String file) throws Exception {
		this(new File(file));
	}
	
	public R2RMLReader(File file, OBDAModel model) throws Exception {
		this(file);
		obdaModel = model;
	}
	
	public R2RMLReader(File file) throws Exception {
		manager = new R2RMLManager(file);
		m = manager.getModel();
	}
	
	public void setOBDAModel(OBDAModel model)
	{
		this.obdaModel = model;
	}
		
	/**
	 * the method that gives the obda model based on the given graph
	 * @param sourceUri - the uri of the datasource of the model
	 * @return the read obda model
	 */
	public OBDAModel readModel(URI sourceUri){
		try {
			//add to the model the mappings retrieved from the manager
			obdaModel.addMappings(sourceUri, manager.getMappings(m));
		} catch (DuplicateMappingException e) {
			e.printStackTrace();
		}
		return obdaModel;
	}
	
	/**
	 * the method that gives the obda model based on the given graph
	 * @param datasource - the datasource of the model
	 * @return the read obda model
	 */
	public OBDAModel readModel(OBDADataSource dataSource){
		try {
			obdaModel.addSource(dataSource);
			URI sourceUri = dataSource.getSourceID();
			//add to the model the mappings retrieved from the manager
			obdaModel.addMappings(sourceUri, manager.getMappings(m));
			
		} catch (DuplicateMappingException e) {
			e.printStackTrace();
		}
		return obdaModel;
	}
	
	/**
	 * method to read the mappings from the graph
	 * @return list of obdaMappingAxioms
	 */
	public ArrayList<OBDAMappingAxiom> readMappings(){
		return manager.getMappings(m);
	}
	

	public static void main(String args[])
	{
		String file = "/Users/mindaugas/r2rml/test26.ttl";
		R2RMLReader reader = null;
		try {
			reader = new R2RMLReader(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ArrayList<OBDAMappingAxiom> axioms = reader.readMappings();
		for (OBDAMappingAxiom ax : axioms)
			System.out.println(ax);
		
	}

}
