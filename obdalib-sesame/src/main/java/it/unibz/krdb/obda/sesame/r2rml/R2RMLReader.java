package it.unibz.krdb.obda.sesame.r2rml;

/*
 * #%L
 * ontop-obdalib-sesame
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.openrdf.model.Graph;

import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.impl.CQIEImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

public class R2RMLReader {
	
	private R2RMLManager manager;
	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	private OBDAModel obdaModel = fac.getOBDAModel();
	
	private Graph graph ;
	
	public R2RMLReader(Graph graph) {
		manager = new R2RMLManager(graph);
		this.graph = graph;
	}
	
	public R2RMLReader(String file)
	{
		this(new File(file));
	}
	
	public R2RMLReader(File file, OBDAModel model)
	{
		this(file);
		obdaModel = model;
	}
	
	public R2RMLReader(File file)
	{
		manager = new R2RMLManager(file);
		graph = manager.getGraph();
	}
	
	public void setOBDAModel(OBDAModel model)
	{
		this.obdaModel = model;
	}
		
	public OBDAModel readModel(URI sourceUri){
		try {
			//add to the model the mappings retrieved from the manager
			obdaModel.addMappings(sourceUri, manager.getMappings(graph));
		} catch (DuplicateMappingException e) {
			e.printStackTrace();
		}
		return obdaModel;
	}
	
	public ArrayList<OBDAMappingAxiom> readMappings(){
		return manager.getMappings(graph);
	}
	

	public static void main(String args[])
	{
		String file = "/Users/timi/Documents/hdd/Project/Test Cases/mapping1.ttl";
	//	"C:/Project/Timi/Workspace/obdalib-parent/quest-rdb2rdf-compliance/src/main/resources/D014/r2rmla.ttl";
	//"C:/Project/Timi/Workspace/obdalib-parent/quest-rdb2rdf-compliance/src/main/resources/D004/WRr2rmlb.ttl";
	
		R2RMLReader reader = new R2RMLReader(file);
		ArrayList<OBDAMappingAxiom> axioms = reader.readMappings();
		for (OBDAMappingAxiom ax : axioms)
			System.out.println(ax);
		
	}

}
