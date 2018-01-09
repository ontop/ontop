package it.unibz.inf.ontop.spec.mapping.parser.impl;

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


import eu.optique.r2rml.api.model.impl.InvalidR2RMLMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.SQLMappingFactory;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.spec.mapping.impl.SQLMappingFactoryImpl;

import com.google.common.collect.ImmutableList;
import eu.optique.r2rml.api.model.Join;
import eu.optique.r2rml.api.model.PredicateObjectMap;
import eu.optique.r2rml.api.model.RefObjectMap;
import eu.optique.r2rml.api.model.TriplesMap;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.impl.TermUtils;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class R2RMLManager {

	private static final SQLMappingFactory MAPPING_FACTORY = SQLMappingFactoryImpl.getInstance();
	private R2RMLParser r2rmlParser;
	private Graph myModel;
	private final AtomFactory atomFactory;
	private final TermFactory termFactory;
	private final TypeFactory typeFactory;

	/**
	 * Constructor to start parsing R2RML mappings from file.
	 * @param file - the full path of the file
	 * @param atomFactory
	 * @param termFactory
	 * @param typeFactory
	 */
	public R2RMLManager(String file, AtomFactory atomFactory, TermFactory termFactory, TypeFactory typeFactory)
			throws RDFParseException, MappingIOException, RDFHandlerException {
		this(new File(file), atomFactory, termFactory, typeFactory);
	}
	
	/**
	 * Constructor to start parsing R2RML mappings from file.
	 * @param file - the File object
	 * @param atomFactory
	 * @param termFactory
	 * @param typeFactory
	 */
	public R2RMLManager(File file, AtomFactory atomFactory, TermFactory termFactory, TypeFactory typeFactory)
			throws MappingIOException, RDFParseException, RDFHandlerException {
		this.atomFactory = atomFactory;
		this.termFactory = termFactory;
		this.typeFactory = typeFactory;

		try {
            LinkedHashModel model = new LinkedHashModel();
			RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
			InputStream in = new FileInputStream(file);
			URL documentUrl = new URL("file://" + file);
			StatementCollector collector = new StatementCollector(model);
			parser.setRDFHandler(collector);
			parser.parse(in, documentUrl.toString());
			this.myModel = new RDF4J().asGraph(model);
			r2rmlParser = new R2RMLParser(atomFactory, termFactory, this.typeFactory);
		} catch (IOException e) {
			throw new MappingIOException(e);
		}

	}
	
	/**
	 * Constructor to start the parser from an RDF Model
	 * @param model - the sesame Model containing mappings
	 * @param atomFactory
	 * @param termFactory
	 * @param typeFactory
	 */
	public R2RMLManager(Graph model, AtomFactory atomFactory, TermFactory termFactory, TypeFactory typeFactory){
		myModel = model;
		this.atomFactory = atomFactory;
		this.termFactory = termFactory;
		this.typeFactory = typeFactory;
		r2rmlParser = new R2RMLParser(atomFactory, termFactory, this.typeFactory);
	}
	
	/**
	 * Get the Model of mappings
	 * @return the Model object containing the mappings
	 */
	public Graph getModel() {
		return myModel;
	}
	
	/**
	 * This method return the list of mappings from the Model main method to be
	 * called, assembles everything
	 * @param myModel - the Model structure containing mappings
	 * @return ArrayList<OBDAMappingAxiom> - list of mapping axioms read from the Model
	 */
	public ImmutableList<SQLPPTriplesMap> getMappings(Graph myModel) throws InvalidR2RMLMappingException {

		List<SQLPPTriplesMap> mappings = new ArrayList<SQLPPTriplesMap>();

		// retrieve the TriplesMap nodes
		Collection<TriplesMap> tripleMaps = r2rmlParser.getMappingNodes(myModel);

		for (TriplesMap tm : tripleMaps) {

			// for each node get a mapping
			SQLPPTriplesMap mapping;

			try {
				mapping = getMapping(tm);

                if(mapping!=null) {
                    // add it to the list of mappings
                    mappings.add(mapping);
                }

				// pass 2 - check for join conditions, add to list
				List<SQLPPTriplesMap> joinMappings = getJoinMappings(tripleMaps, tm);
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
	private SQLPPTriplesMap getMapping(TriplesMap tm) throws Exception {
		String sourceQuery = r2rmlParser.getSQLQuery(tm).trim();
		ImmutableList<ImmutableFunctionalTerm> body = getMappingTripleAtoms(tm);
		//Function head = getHeadAtom(body);
		//CQIE targetQuery = DATALOG_FACTORY.getCQIE(head, body);
		// TODO: consider a R2RML-specific type of triples map
		SQLPPTriplesMap mapping = new OntopNativeSQLPPTriplesMap("mapping-"+tm.hashCode(),
				MAPPING_FACTORY.getSQLQuery(sourceQuery), body);
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
	private List<SQLPPTriplesMap> getJoinMappings(Collection<TriplesMap> tripleMaps, TriplesMap tm) throws Exception {
		String sourceQuery = "";
		List<SQLPPTriplesMap> joinMappings = new ArrayList<SQLPPTriplesMap>();
		for (PredicateObjectMap pobm: tm.getPredicateObjectMaps()) {
			
			for(RefObjectMap robm : pobm.getRefObjectMaps()) {
				sourceQuery = robm.getJointQuery();
				
				List <Join> conds = robm.getJoinConditions();
				ImmutableList.Builder<ImmutableFunctionalTerm> bodyBuilder = ImmutableList.builder();
				List<ImmutableTerm> terms = new ArrayList<>();
				ImmutableTerm joinSubject1 = r2rmlParser.getSubjectAtom(tm);
				
				TriplesMap parent = robm.getParentMap();
				TriplesMap parentTriple = null;
				Iterator<TriplesMap> it = tripleMaps.iterator();
				while(it.hasNext()){
					TriplesMap current = it.next();
					if (current.equals(parent)) {
						parentTriple = current;
						break;
					}
				}
				
				ImmutableTerm joinSubject2 = r2rmlParser.getSubjectAtom(parentTriple);
				terms.add(joinSubject1);
				terms.add(joinSubject2);
				
			List<Predicate> joinPredicates = r2rmlParser.getBodyPredicates(pobm);
			for (Predicate pred : joinPredicates) {
				bodyBuilder.add(termFactory.getImmutableFunctionalTerm(pred, ImmutableList.copyOf(terms)));
			}

			//Function head = getHeadAtom(body);
			//CQIE targetQuery = DATALOG_FACTORY.getCQIE(head, body);
			
			if (sourceQuery.isEmpty()) {
				throw new Exception("Could not create source query for join in " + tm);
			}
			//finally, create mapping and add it to the list
                //use referenceObjectMap robm as id, because there could be multiple joinCondition in the same triple map
			// TODO: use a R2RML-specific class	instead
			SQLPPTriplesMap mapping = new OntopNativeSQLPPTriplesMap("mapping-join-"+robm.hashCode(),
					MAPPING_FACTORY.getSQLQuery(sourceQuery), bodyBuilder.build());
			System.out.println("WARNING joinMapping introduced : " + mapping);
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
		Function head = termFactory.getFunction(termFactory.getPredicate(Constants.QUERY_HEAD, arity), dvars);
		return head;
	}
	
	/**
	 * Get OBDA mapping body terms from R2RML TriplesMap
	 * @param tm
	 * @return
	 * @throws Exception
	 */
	private ImmutableList<ImmutableFunctionalTerm> getMappingTripleAtoms(TriplesMap tm) throws Exception {
		//the body to return
		ImmutableList.Builder<ImmutableFunctionalTerm> bodyBuilder = ImmutableList.builder();
		
		//get subject
		ImmutableTerm subjectAtom = r2rmlParser.getSubjectAtom(tm);
		
		//get any class predicates, construct atom Class(subject), add to body
		List<Predicate> classPredicates = r2rmlParser.getClassPredicates();
		for (Predicate classPred : classPredicates) {
			bodyBuilder.add(termFactory.getImmutableFunctionalTerm(classPred, subjectAtom));
		}		

		for (PredicateObjectMap pom : tm.getPredicateObjectMaps()) {
			//for each predicate object map
			
			//get body predicate
			List<Predicate> bodyPredicates = r2rmlParser.getBodyPredicates(pom);
			//predicates that contain a variable are separately treated
			List<ImmutableFunctionalTerm> bodyURIPredicates = r2rmlParser.getBodyURIPredicates(pom);
			
			//get object atom
			ImmutableTerm objectAtom = r2rmlParser.getObjectAtom(pom);
			
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
				if (bodyPred.toString().equals(IriConstants.RDF_TYPE)) {
					//create term triple(subjAtom, URI("...rdf_type"), objAtom)
					// if object is a predicate
					Set<Variable> vars = new HashSet<>();
					TermUtils.addReferencedVariablesTo(vars, objectAtom);
					if (vars.isEmpty()) { 	
						ImmutableFunctionalTerm funcObjectAtom = (ImmutableFunctionalTerm) objectAtom;
						ImmutableTerm term0 = funcObjectAtom.getTerm(0);
						if (term0 instanceof ImmutableFunctionalTerm) {
							ImmutableFunctionalTerm constPred = (ImmutableFunctionalTerm) term0;
							Predicate newpred = constPred.getFunctionSymbol();
							ImmutableFunctionalTerm bodyAtom = termFactory.getImmutableFunctionalTerm(newpred, subjectAtom);
							bodyBuilder.add(bodyAtom);
						}
						else if (term0 instanceof ValueConstant) {
							ValueConstant vconst = (ValueConstant) term0;
							String predName = vconst.getValue();
							Predicate newpred = termFactory.getPredicate(predName, 1);
							bodyBuilder.add(termFactory.getImmutableFunctionalTerm(newpred, subjectAtom));
						} 
						else 
							throw new IllegalStateException();
					}
					else { // if object is a variable
						// TODO (ROMAN): double check -- the list terms appears to accumulate the PO pairs
						//Predicate newpred = OBDAVocabulary.QUEST_TRIPLE_PRED;
						ImmutableFunctionalTerm rdftype = termFactory.getImmutableUriTemplate(
								termFactory.getConstantLiteral(IriConstants.RDF_TYPE));
						//terms.add(rdftype);
						//terms.add(objectAtom);
						ImmutableFunctionalTerm bodyAtom = atomFactory.getImmutableTripleAtom(subjectAtom, rdftype, objectAtom);
						bodyBuilder.add(bodyAtom); // termFactory.getFunction(newpred, terms)
					}
				} 
				else {
					// create predicate(subject, object) and add it to the body
					bodyBuilder.add(termFactory.getImmutableFunctionalTerm(bodyPred, subjectAtom, objectAtom));
				}
			}
			
			//treat predicates that contain a variable (column or template declarations)
			for (ImmutableFunctionalTerm predFunction : bodyURIPredicates) {
				//create triple(subj, predURIFunction, objAtom) terms
				//Predicate newpred = OBDAVocabulary.QUEST_TRIPLE_PRED;
				//terms.add(predFunction);
				//terms.add(objectAtom);
				bodyBuilder.add(atomFactory.getImmutableTripleAtom(subjectAtom, predFunction, objectAtom));   // objectAtom
			}
		}
		return bodyBuilder.build();
	}
}
