package it.unibz.krdb.obda.owlapi3.directmapping;

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

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.TableDefinition;

public class OntoExpansion {
	
	private String baseURI;
	
	public OntoExpansion(){
		this.baseURI=new String("http://example.org/");
	}
	
	public void setURI(String uri){
		this.baseURI = new String(uri);
	}

	
	public void enrichOntology(DBMetadata md, OWLOntology rootOntology) throws OWLOntologyStorageException{
		for (TableDefinition td : md.getTables()) {
			OntoSchema os = new OntoSchema(td);
			os.setBaseURI(this.baseURI);
			os.enrichOntology(rootOntology);
		}
	}

}
