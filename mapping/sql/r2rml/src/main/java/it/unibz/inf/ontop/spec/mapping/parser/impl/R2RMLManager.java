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


import com.google.common.collect.ImmutableList;
import eu.optique.r2rml.api.model.PredicateObjectMap;
import eu.optique.r2rml.api.model.RefObjectMap;
import eu.optique.r2rml.api.model.TriplesMap;
import eu.optique.r2rml.api.model.impl.InvalidR2RMLMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.SQLMappingFactory;
import it.unibz.inf.ontop.spec.mapping.impl.SQLMappingFactoryImpl;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class R2RMLManager {

	private static final SQLMappingFactory MAPPING_FACTORY = SQLMappingFactoryImpl.getInstance();
	private R2RMLParser r2rmlParser;
	private Graph myModel;
	private final TermFactory termFactory;
	private final TypeFactory typeFactory;
	private final TargetAtomFactory targetAtomFactory;

	/**
	 * Constructor to start parsing R2RML mappings from file.
	 * @param file - the full path of the file
	 * @param termFactory
	 * @param typeFactory
	 * @param targetAtomFactory
	 */
	public R2RMLManager(String file, TermFactory termFactory, TypeFactory typeFactory,
						TargetAtomFactory targetAtomFactory)
			throws RDFParseException, MappingIOException, RDFHandlerException {
		this(new File(file), termFactory, typeFactory, targetAtomFactory);
	}
	
	/**
	 * Constructor to start parsing R2RML mappings from file.
	 * @param file - the File object
	 * @param termFactory
	 * @param typeFactory
	 * @param targetAtomFactory
	 */
	public R2RMLManager(File file,TermFactory termFactory, TypeFactory typeFactory,
						TargetAtomFactory targetAtomFactory)
			throws MappingIOException, RDFParseException, RDFHandlerException {
		this.termFactory = termFactory;
		this.typeFactory = typeFactory;
		this.targetAtomFactory = targetAtomFactory;

		try {
            LinkedHashModel model = new LinkedHashModel();
			RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
			InputStream in = new FileInputStream(file);
			URL documentUrl = new URL("file://" + file);
			StatementCollector collector = new StatementCollector(model);
			parser.setRDFHandler(collector);
			parser.parse(in, documentUrl.toString());
			this.myModel = new RDF4J().asGraph(model);
			r2rmlParser = new R2RMLParser(termFactory, this.typeFactory);
		} catch (IOException e) {
			throw new MappingIOException(e);
		}

	}
	
	/**
	 * Constructor to start the parser from an RDF Model
	 * @param model - the sesame Model containing mappings
	 * @param termFactory
	 * @param typeFactory
	 * @param targetAtomFactory
	 */
	public R2RMLManager(Graph model, TermFactory termFactory, TypeFactory typeFactory,
						TargetAtomFactory targetAtomFactory){
		myModel = model;
		this.termFactory = termFactory;
		this.typeFactory = typeFactory;
		this.targetAtomFactory = targetAtomFactory;
		r2rmlParser = new R2RMLParser(termFactory, this.typeFactory);
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
		ImmutableList<TargetAtom> body = getMappingTripleAtoms(tm);
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

				ImmutableList.Builder<TargetAtom> bodyBuilder = ImmutableList.builder();

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
				
			List<ImmutableFunctionalTerm> joinPredicates = r2rmlParser.getBodyURIPredicates(pobm);
			for (ImmutableFunctionalTerm pred : joinPredicates) {
				//TODO:joinPredicates
				bodyBuilder.add(targetAtomFactory.getTripleTargetAtom(joinSubject1, pred, joinSubject2));   // objectAtom
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
	 * Get OBDA mapping body terms from R2RML TriplesMap
	 * @param tm
	 * @return
	 * @throws Exception
	 */
	private ImmutableList<TargetAtom> getMappingTripleAtoms(TriplesMap tm) throws Exception {
		//the body to return
		ImmutableList.Builder<TargetAtom> bodyBuilder = ImmutableList.builder();
		
		//get subject
		ImmutableTerm subjectAtom = r2rmlParser.getSubjectAtom(tm);
		
		//get any class predicates, construct atom Class(subject), add to body
		List<ImmutableFunctionalTerm> classPredicates = r2rmlParser.getClassPredicates();
		for (ImmutableFunctionalTerm classPred : classPredicates) {
			ImmutableTerm predFunction = termFactory.getImmutableUriTemplate(termFactory.getConstantLiteral(RDF.TYPE.toString())); ;
			bodyBuilder.add(targetAtomFactory.getTripleTargetAtom(subjectAtom, predFunction, classPred));   // objectAtom
		}		

		for (PredicateObjectMap pom : tm.getPredicateObjectMaps()) {
			//for each predicate object map

			//predicates that contain a variable are separately treated
			List<ImmutableFunctionalTerm> bodyURIPredicates = r2rmlParser.getBodyURIPredicates(pom);
			
			//get object atom
			ImmutableTerm objectAtom = r2rmlParser.getObjectAtom(pom);
			
			if (objectAtom == null) {
				// skip, object is a join
				continue;
			}

			
			//treat predicates
			for (ImmutableFunctionalTerm predFunction : bodyURIPredicates) {

				bodyBuilder.add(targetAtomFactory.getTripleTargetAtom(subjectAtom, predFunction, objectAtom));   // objectAtom
			}
		}
		return bodyBuilder.build();
	}
}
