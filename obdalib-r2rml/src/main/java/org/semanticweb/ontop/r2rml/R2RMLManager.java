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
 * @author timea bagosi, mindaugas slusnys
 * Class responsible of parsing R2RML mappings from file or from an RDF Model
 */


import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.StatementCollector;

import eu.optique.api.mapping.Join;
import eu.optique.api.mapping.PredicateObjectMap;
import eu.optique.api.mapping.RefObjectMap;
import eu.optique.api.mapping.TriplesMap;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDALibConstants;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.ValueConstant;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.model.impl.TermUtils;

public class R2RMLManager {
	
	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	private R2RMLParser r2rmlParser;
	private Model myModel;
	
	/**
	 * Constructor to start parsing R2RML mappings from file.
	 * @param file - the full path of the file
	 */
	public R2RMLManager(String file) throws RDFParseException, IOException, RDFHandlerException {
		this(new File(file));
	}
	
	/**
	 * Constructor to start parsing R2RML mappings from file.
	 * @param file - the File object
	 */
	public R2RMLManager(File file) throws IOException, RDFParseException, RDFHandlerException {

			myModel = new LinkedHashModel();			
			RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
			InputStream in = new FileInputStream(file);
			URL documentUrl = new URL("file://" + file);
			StatementCollector collector = new StatementCollector(myModel);
			parser.setRDFHandler(collector);
			parser.parse(in, documentUrl.toString());
			r2rmlParser = new R2RMLParser();

	}
	
	/**
	 * Constructor to start the parser from an RDF Model
	 * @param model - the sesame Model containing mappings
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
	public ImmutableList<OBDAMappingAxiom> getMappings(Model myModel) {

		List<OBDAMappingAxiom> mappings = new ArrayList<OBDAMappingAxiom>();

		// retrieve the TriplesMap nodes
		Collection<TriplesMap> tripleMaps = r2rmlParser.getMappingNodes(myModel);

		for (TriplesMap tm : tripleMaps) {

			// for each node get a mapping
			OBDAMappingAxiom mapping;

			try {
				mapping = getMapping(tm);

                if(mapping!=null) {
                    // add it to the list of mappings
                    mappings.add(mapping);
                }

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
		return ImmutableList.copyOf(mappings);
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
		OBDAMappingAxiom mapping = fac.getMappingAxiom("mapping-" + tm.hashCode(), fac.getSQLQuery(sourceQuery), targetQuery);
        if (body.isEmpty()){
            //we do not have a target query
            System.out.println("WARNING a mapping without target query will not be introduced : "+ mapping.toString());
            return null;
        }
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
            //use referenceObjectMap robm as id, because there could be multiple joinCondition in the same triple map
            OBDAMappingAxiom mapping = fac.getMappingAxiom("mapping-join-" + robm.hashCode(),
					fac.getSQLQuery(sourceQuery), targetQuery);
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
		Set<Variable> vars = new HashSet<>();
		for (Function bodyAtom : body) {
			TermUtils.addReferencedVariablesTo(vars, bodyAtom);
		}
		int arity = vars.size();
		List<Term> dvars = new ArrayList<Term>(vars);
		Function head = fac.getFunction(fac.getPredicate(OBDALibConstants.QUERY_HEAD, arity), dvars);
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
			//List<Term> terms = new ArrayList<Term>();
			//terms.add(subjectAtom);
			
			
			for (Predicate bodyPred : bodyPredicates) {
				//for each predicate if there are more in the same node
				
				//check if predicate = rdf:type
				if (bodyPred.toString().equals(OBDAVocabulary.RDF_TYPE)) {
					//create term triple(subjAtom, URI("...rdf_type"), objAtom)
					// if object is a predicate
					Set<Variable> vars = new HashSet<>();
					TermUtils.addReferencedVariablesTo(vars, objectAtom);
					if (vars.isEmpty()) { 	
						Function funcObjectAtom = (Function) objectAtom;
						Term term0 = funcObjectAtom.getTerm(0);
						if (term0 instanceof Function) {
							Function constPred = (Function) term0;
							Predicate newpred = constPred.getFunctionSymbol();
							Function bodyAtom = fac.getFunction(newpred, subjectAtom);
							body.add(bodyAtom);
						}
						else if (term0 instanceof ValueConstant) {							
							ValueConstant vconst = (ValueConstant) term0;
							String predName = vconst.getValue();
							Predicate newpred = fac.getPredicate(predName, 1);
							Function bodyAtom = fac.getFunction(newpred, subjectAtom);
							body.add(bodyAtom);
						} 
						else 
							throw new IllegalStateException();
					}
					else { // if object is a variable
						// TODO (ROMAN): double check -- the list terms appears to accumulate the PO pairs
						//Predicate newpred = OBDAVocabulary.QUEST_TRIPLE_PRED;
						Function rdftype = fac.getUriTemplate(fac.getConstantLiteral(OBDAVocabulary.RDF_TYPE));
						//terms.add(rdftype);
						//terms.add(objectAtom);
						Function bodyAtom = fac.getTripleAtom(subjectAtom, rdftype, objectAtom);
						body.add(bodyAtom); // fac.getFunction(newpred, terms)
					}
				} 
				else {
					// create predicate(subject, object) and add it to the body
					Function bodyAtom = fac.getFunction(bodyPred, subjectAtom, objectAtom);
					body.add(bodyAtom);
				}
			}
			
			//treat predicates that contain a variable (column or template declarations)
			for (Function predFunction : bodyURIPredicates) {
				//create triple(subj, predURIFunction, objAtom) terms
				//Predicate newpred = OBDAVocabulary.QUEST_TRIPLE_PRED;
				//terms.add(predFunction);
				//terms.add(objectAtom);
				Function bodyAtom = fac.getTripleAtom(subjectAtom, predFunction, objectAtom);
				body.add(bodyAtom);   // objectAtom
			}
		}
		return body;
	}
}
