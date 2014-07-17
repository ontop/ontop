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

import static org.junit.Assert.assertTrue;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Return empty concepts and roles, based on the mappings. Given an ontology,
 * which is connected to a database via mappings, generate a suitable set of
 * queries that test if there are empty concepts, concepts that are no populated
 * to anything.
 */
public class QuestOWLEmptyEntitiesChecker {

	private Ontology onto;
	private QuestOWLConnection conn;

	Logger log = LoggerFactory.getLogger(QuestOWLEmptyEntitiesChecker.class);

	private List<Predicate> emptyConcepts = new ArrayList<Predicate>();
	private List<Predicate> emptyRoles = new ArrayList<Predicate>();

	/**
	 * Generate SPARQL queries to check if there are instances for each concept and role in the ontology
	 * 
	 * @param onto the OWLOntology needs to be translated in OWLAPI3, conn QuestOWL connection
	 * @throws Exception
	 */
	public QuestOWLEmptyEntitiesChecker(OWLOntology onto, QuestOWLConnection conn) throws Exception {
		OWLAPI3Translator translator = new OWLAPI3Translator();

		this.onto = translator.translate(onto);
		this.conn = conn;
		runQueries();

	}

	/**
	 * Generate SPARQL queries to check if there are instances for each concept and role in the ontology
	 * 
	 * @param translatedOntologyMerge the OWLAPI3 ontology, conn QuestOWL connection
	 * @throws Exception
	 */
	public QuestOWLEmptyEntitiesChecker(Ontology translatedOntologyMerge, QuestOWLConnection conn) throws Exception {
		this.onto = translatedOntologyMerge;
		this.conn = conn;
		runQueries();
	}

	/**
	 * Returns the empty concepts.
	 * 
	 * @return The empty concepts.
	 */
	public List<Predicate> getEmptyConcepts() {
		return emptyConcepts;
	}

	/**
	 * Returns the empty roles.
	 * 
	 * @return The empty roles.
	 */
	public List<Predicate> getEmptyRoles() {
		return emptyRoles;
	}

	/**
	 * Gets the total number of empty entities
	 * 
	 * @return The total number of empty entities.
	 * @throws Exception
	 */
	public int getNumberEmptyEntities() throws Exception {

		return emptyConcepts.size() + emptyRoles.size();
	}

	@Override
	public String toString() {
		String str = new String();

		int countC = emptyConcepts.size();
		str += String.format("- %s Empty %s ", countC, (countC == 1) ? "concept" : "concepts");
		int countR = emptyRoles.size();
		str += String.format("- %s Empty %s\n", countR, (countR == 1) ? "role" : "roles");
		return str;
	}

	private void runQueries() throws Exception {

		for (Predicate concept : onto.getConcepts()) {
			if (!runSPARQLConceptsQuery("<" + concept.getName() + ">")) {
				emptyConcepts.add(concept);
			}
		}
		log.debug(emptyConcepts.size() + " Empty concept/s: " + emptyConcepts);

		for (Predicate role : onto.getRoles()) {
			if (!runSPARQLRolesQuery("<" + role.getName() + ">")) {
				emptyRoles.add(role);
			}
		}
		log.debug(emptyRoles.size() + " Empty role/s: " + emptyRoles);

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
				assertTrue(false);
			}
			
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
				assertTrue(false);
			}
			
			st.close();

		}

	}

}
