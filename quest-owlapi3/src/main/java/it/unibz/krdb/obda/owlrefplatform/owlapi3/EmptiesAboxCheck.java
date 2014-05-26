package it.unibz.krdb.obda.owlrefplatform.owlapi3;

/*
 * #%L
 * ontop-obdalib-core
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

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.PunningException;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class about the ABox materialization.
 */
public class EmptiesAboxCheck {

	private Ontology onto;
	private QuestOWLConnection conn;

	Logger log = LoggerFactory.getLogger(EmptiesAboxCheck.class);

	
	private List<String> emptyConcepts = new ArrayList<String>();
	private List<String> emptyRoles = new ArrayList<String>();
	private Set<BasicClassDescription> emptyBasicConcepts = new HashSet<BasicClassDescription>();
	private Set<Property> emptyProperties = new HashSet<Property>();
	
	/**
	 * Inserts the OBDA model to this utility class.
	 * 
	 * @param model
	 *            The mandatory OBDA model.
	 * @throws Exception 
	 */
	public EmptiesAboxCheck(OWLOntology onto, QuestOWLConnection conn) throws Exception {
		OWLAPI3Translator translator = new OWLAPI3Translator();

		this.onto = translator.translate(onto);
		this.conn = conn;
		refresh();
		
	}

	/**
	 * Returns the empty concepts.
	 * 
	 * @return The empty concepts.
	 */
	public  List<String> getEmptyConcepts() {
		return emptyConcepts;
	}
	
	/**
	 * Returns the empty roles.
	 * 
	 * @return The empty roles.
	 */
	public  List<String> getEmptyRoles() {
		return emptyRoles;
	}


	
	/**
	 * Gets the total number of triples from all the data sources and mappings.
	 * 
	 * @return The total number of triples.
	 * @throws Exception
	 */
	public int getNumberEmpties() throws Exception {
		
		return emptyConcepts.size() + emptyRoles.size();
	}

	@Override
	public String toString() {
		String str = new String(); 
		
			int countC = emptyConcepts.size();
			str += String.format("- %s Empty %s\n", countC,  (countC == 1) ? "concept" : "concepts");
			int countR = emptyRoles.size();
			str += String.format("- %s Empty %s\n", countR,  (countR == 1 ) ? "role" : "roles");
		return str;
	}

	public void refresh() throws Exception {


		int c = 0; // number of empty concepts
		for (Predicate concept : onto.getConcepts()) {
			if (!runSPARQLConceptsQuery("<" + concept.getName() + ">")) {
				emptyConcepts.add(concept.getName());
				c++;
			}
		}
		log.info(c + " Empty concept/s: " + emptyConcepts);

		int r = 0; // number of empty roles
		for (Predicate role : onto.getRoles()) {
			if (!runSPARQLRolesQuery("<" + role.getName() + ">")) {
				emptyRoles.add(role.getName());
				r++;
			}
		}
		log.info(r + " Empty role/s: " + emptyRoles);

		
	}
	
	private boolean runSPARQLConceptsQuery(String description) throws Exception {
		String query = "SELECT ?x WHERE {?x a " + description + ".}";
		QuestOWLStatement st = conn.createStatement();
		try {
			QuestOWLResultSet rs = st.executeTuple(query);
			return (rs.nextRow());

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
			}
//			conn.close();
			st.close();

		}

	}

	private boolean runSPARQLRolesQuery(String description) throws Exception {
		String query = "SELECT * WHERE {?x " + description + " ?y.}";
		QuestOWLStatement st = conn.createStatement();
		try {
			QuestOWLResultSet rs = st.executeTuple(query);
			return (rs.nextRow());

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
			}
//			conn.close();
			st.close();

		}

	}

	
}
