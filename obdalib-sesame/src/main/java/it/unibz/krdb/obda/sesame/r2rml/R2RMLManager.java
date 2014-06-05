package it.unibz.krdb.obda.sesame.r2rml;

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
 * @author timea bagosi, mindaugas slusnys
 * Class responsible of parsing R2RML mappings from file or from an RDF Model
 */

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDALibConstants;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;

import eu.optique.api.mapping.Join;
import eu.optique.api.mapping.PredicateMap;
import eu.optique.api.mapping.PredicateObjectMap;
import eu.optique.api.mapping.RefObjectMap;
import eu.optique.api.mapping.TriplesMap;

public class R2RMLManager {
	
	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	private R2RMLParser r2rmlParser;
	private Model myModel;
	
	/**
	 * Constructor to start parsing R2RML mappings from file.
	 * @param file - the full path of the file
	 */
	public R2RMLManager(String file) {
		this(new File(file));
	}
	
	/**
	 * Constructor to start parsing R2RML mappings from file.
	 * @param file - the File object
	 */
	public R2RMLManager(File file) {
		try {
			myModel = new LinkedHashModel();			
			RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
			InputStream in = new FileInputStream(file);
			URL documentUrl = new URL("file://" + file);
			StatementCollector collector = new StatementCollector(myModel);
			parser.setRDFHandler(collector);
			parser.parse(in, documentUrl.toString());
			r2rmlParser = new R2RMLParser();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructor to start the parser from an RDF Model
	 * @param Model - the sesame Model containing mappings
	 */
	public R2RMLManager(Model model){
		myModel = model;
		r2rmlParser = new R2RMLParser();
	}
	
	/**
	 * Get the Model of mappings
	 * @return the Model object containing the mappings
	 */
	public Model getModel() {
		return myModel;
	}
	
	/**
	 * This method return the list of mappings from the Model main method to be
	 * called, assembles everything
	 * @param myModel - the Model structure containing mappings
	 * @return ArrayList<OBDAMappingAxiom> - list of mapping axioms read from the Model
	 */
	public ArrayList<OBDAMappingAxiom> getMappings(Model myModel) {

		ArrayList<OBDAMappingAxiom> mappings = new ArrayList<OBDAMappingAxiom>();

		// retrieve the TriplesMap nodes
		Collection<TriplesMap> tripleMaps = r2rmlParser.getMappingNodes(myModel);

		for (TriplesMap tm : tripleMaps) {

			// for each node get a mapping
			OBDAMappingAxiom mapping;

			try {
				mapping = getMapping(tm);

				// add it to the list of mappings
				mappings.add(mapping);

				// pass 2 - check for join conditions, add to list
				List<OBDAMappingAxiom> joinMappings = getJoinMappings(tripleMaps, tm);
				if (joinMappings != null) {
					mappings.addAll(joinMappings);
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return mappings;
	}
	/**
	 * Get OBDA mapping axiom from R2RML TriplesMap 
	 * @param tm
	 * @return
	 * @throws Exception
	 */
	private OBDAMappingAxiom getMapping(TriplesMap tm) throws Exception {
		String sourceQuery = r2rmlParser.getSQLQuery(tm);
		List<Function> body = getMappingTripleAtoms(tm);
		Function head = getHeadAtom(body);
		CQIE targetQuery = fac.getCQIE(head, body);
		OBDAMappingAxiom mapping = fac.getRDBMSMappingAxiom("mapping-"+tm.hashCode(), sourceQuery, targetQuery);
		return mapping;
	}
	
	/**
	 * Get join OBDA mapping axiom from R2RML TriplesMap
	 * @param tripleMaps
	 * @param tm
	 * @return
	 * @throws Exception
	 */
	private List<OBDAMappingAxiom> getJoinMappings(Collection<TriplesMap> tripleMaps, TriplesMap tm) throws Exception {
		String sourceQuery = "";
		List<OBDAMappingAxiom> joinMappings = new ArrayList<OBDAMappingAxiom>();
		for (PredicateObjectMap pobm: tm.getPredicateObjectMaps()) {
			
			for(RefObjectMap robm : pobm.getRefObjectMaps()) {
				sourceQuery = robm.getJointQuery();
				
				List <Join> conds = robm.getJoinConditions();
				List<Function> body = new ArrayList<Function>();
				List<Term> terms = new ArrayList<Term>();
				Term joinSubject1 = r2rmlParser.getSubjectAtom(tm);
				
				Resource parent = (Resource) robm.getParentMap(Resource.class);
				TriplesMap parentTriple = null;
				Iterator<TriplesMap> it = tripleMaps.iterator();
				while(it.hasNext()){
					TriplesMap current = it.next();
					if (current.getResource(Resource.class).equals(parent)) {
						parentTriple = current;
						break;
					}
				}
				
				Term joinSubject2 = r2rmlParser.getSubjectAtom(parentTriple);
				terms.add(joinSubject1);
				terms.add(joinSubject2);
				
			List<Predicate> joinPredicates = r2rmlParser.getBodyPredicates(pobm);
			for (Predicate pred : joinPredicates) {
				Function bodyAtom = fac.getFunction(pred, terms);
				body.add(bodyAtom);
			}

			Function head = getHeadAtom(body);
			CQIE targetQuery = fac.getCQIE(head, body);
			
			if (sourceQuery.isEmpty()) {
				throw new Exception("Could not create source query for join in "+tm.toString());
			}
			//finally, create mapping and add it to the list
			OBDAMappingAxiom mapping = fac.getRDBMSMappingAxiom("mapping-join-"+tm.hashCode(), sourceQuery, targetQuery);
			System.out.println("WARNING joinMapping introduced : "+mapping.toString());
			joinMappings.add(mapping);
		}
			
		}
		return joinMappings;
	}
	
	/**
	 * Get OBDA mapping head
	 * @param body
	 * @return
	 */
	private Function getHeadAtom(List<Function> body) {
		Set<Variable> vars = new HashSet<Variable>();
		for (Function bodyAtom : body) {
			 vars.addAll(bodyAtom.getReferencedVariables());
		}
		int arity = vars.size();
		List<Term> dvars = new ArrayList<Term>(vars);
		Function head = fac.getFunction(fac.getPredicate(OBDALibConstants.QUERY_HEAD, arity, null), dvars);
		return head;
	}
	
	/**
	 * Get OBDA mapping body terms from R2RML TriplesMap
	 * @param tm
	 * @return
	 * @throws Exception
	 */
	private List<Function> getMappingTripleAtoms(TriplesMap tm) throws Exception {
		//the body to return
		List<Function> body = new ArrayList<Function>();
		
		//get subject
		Term subjectAtom = r2rmlParser.getSubjectAtom(tm);		
		
		//get any class predicates, construct atom Class(subject), add to body
		List<Predicate> classPredicates = r2rmlParser.getClassPredicates();
		for (Predicate classPred : classPredicates) {
			body.add(fac.getFunction(classPred, subjectAtom));
		}		

		for (PredicateObjectMap pom : tm.getPredicateObjectMaps()) {
			//for each predicate object map
			
			//get body predicate
			List<Predicate> bodyPredicates = r2rmlParser.getBodyPredicates(pom);
			//predicates that contain a variable are separately treated
			List<Function> bodyURIPredicates = r2rmlParser.getBodyURIPredicates(pom);
			
			//get object atom
			Term objectAtom = r2rmlParser.getObjectAtom(pom);
			
		
			
			if (objectAtom == null) {
				// skip, object is a join
				continue;
			}
			
			// construct the atom, add it to the body
			List<Term> terms = new ArrayList<Term>();
			terms.add(subjectAtom);
			
			
			for (Predicate bodyPred : bodyPredicates) {
				//for each predicate if there are more in the same node
				
				//check if predicate = rdf:type
				if (bodyPred.toString().equals(OBDAVocabulary.RDF_TYPE)) {
					//create term triple(subjAtom, URI("...rdf_type"), objAtom)
					// if object is a predicate
					if (objectAtom.getReferencedVariables().isEmpty()) { 	
						Function constPred = (Function) ((Function) objectAtom).getTerm(0);
						Predicate newpred = constPred.getFunctionSymbol();
						Function newAtom = fac.getFunction(newpred, subjectAtom);
						body.add(newAtom);
					}else{ // if object is a variable
						Predicate newpred = OBDAVocabulary.QUEST_TRIPLE_PRED;
						Predicate uriPred = fac.getUriTemplatePredicate(1);
						Function rdftype = fac.getFunction(uriPred, fac.getConstantLiteral(OBDAVocabulary.RDF_TYPE));
						terms.add(rdftype);
						terms.add(objectAtom);
						body.add(fac.getFunction(newpred, terms));
					}
				} else {
					// create predicate(subject, object) and add it to the body
					terms.add(objectAtom);
					Function bodyAtom = fac.getFunction(bodyPred, terms);
					body.add(bodyAtom);
				}
			}
			
			//treat predicates that contain a variable (column or template declarations)
			for (Function predFunction : bodyURIPredicates) {
				//create triple(subj, predURIFunction, objAtom) terms
				Predicate newpred = OBDAVocabulary.QUEST_TRIPLE_PRED;
				terms.add(predFunction);
				terms.add(objectAtom);
				body.add(fac.getFunction(newpred, terms));
			}
		}
		return body;
	}
}
