package it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing;

/*
 * #%L
 * ontop-reformulation-core
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

import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.exception.UnknownDatatypeException;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.FunctionalTermImpl;
import it.unibz.inf.ontop.sql.Attribute;
import it.unibz.inf.ontop.sql.Relation2DatalogPredicate;
import it.unibz.inf.ontop.sql.RelationDefinition;
import it.unibz.inf.ontop.sql.RelationID;
import it.unibz.inf.ontop.utils.JdbcTypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

public class MappingDataTypeCompletion {

	private final DBMetadata metadata;

    private static final Logger log = LoggerFactory.getLogger(MappingDataTypeCompletion.class);
    private final JdbcTypeMapper jdbcTypeMapper;

    /**
     * Constructs a new mapping data type resolution.
     * If no datatype is defined, then we use database metadata for obtaining the table column definition as the
     * default data-type.
     *
     * @param metadata The database metadata.
     */
    public MappingDataTypeCompletion(DBMetadata metadata)
            throws PredicateRedefinitionException {
        this.metadata = metadata;
        /**
         * TODO: retrieve from Guice
         */
        this.jdbcTypeMapper =  JdbcTypeMapper.getInstance();
    }

    /**
     * This method wraps the variable that holds data property values with a data type predicate.
     * It will replace the variable with a new function symbol and update the rule atom.
     * However, if the users already defined the data-type in the mapping, this method simply accepts the function symbol.
     */

    public void insertDataTyping(CQIE rule) throws MappingException {
        Function atom = rule.getHead();
        Predicate predicate = atom.getFunctionSymbol();
        if (predicate.getArity() == 2) { // we check both for data and object property
            Term term = atom.getTerm(1); // the second argument only
    		Map<String, List<IndexedPosititon>> termOccurenceIndex = createIndex(rule.getBody());
            insertDataTyping(term, atom, 1, termOccurenceIndex);
        }
	}

    private void insertDataTyping(Term term, Function atom, int position, Map<String, List<IndexedPosititon>> termOccurenceIndex)
            throws MappingException {
        Predicate predicate = atom.getFunctionSymbol();

        if (term instanceof Function) {
            Function function = (Function) term;
            Predicate functionSymbol = function.getFunctionSymbol();

            if (functionSymbol instanceof URITemplatePredicate || functionSymbol instanceof BNodePredicate) {
                // NO-OP for object properties
            }

            /** If it is a concat or replace function, can have a datatype assigned to its alias (function with datatype predicate)
             *  or if no information about the datatype is assigned we will assign the value from the ontology
             if present or the information from the database will be used.
             */

            else if (function.isOperation()) {

//            	Function normal = qvv.getNormal(atom);
//                Datatype dataType = dataTypesMap.get(normal.getFunctionSymbol());

                //Check if a datatype was already assigned in the ontology
//                if (dataType != null) {
//                    //assign the datatype of the ontology
//                    if (!isBooleanDB2(dataType.getPredicate())) {
//                        Predicate replacement = dataType.getPredicate();
//                        Term newTerm = DATA_FACTORY.getFunction(replacement, function);
//                        atom.setTerm(position, newTerm);
//                    }
//                }
//                else {
                    for (int i = 0; i < function.getArity(); i++)
                        insertDataTyping(function.getTerm(i), function, i, termOccurenceIndex);
//                }
            }
            else if (function.isDataTypeFunction()) {
//
//                Function normal = qvv.getNormal(atom);
////                Datatype dataType = dataTypesMap.get(normal.getFunctionSymbol());
//
//                //if a datatype was already assigned in the ontology
//                if (dataType != null && isBooleanDB2(dataType.getPredicate())) {
//
//                        Variable variable = (Variable) normal.getTerm(1);
//
//                        //No Boolean datatype in DB2 database, the value in the database is used
//                        Predicate.COL_TYPE type = getDataType(termOccurenceIndex, variable);
//                        Term newTerm = DATA_FACTORY.getTypedTerm(variable, type);
//                        log.warn("Datatype for the value " +variable + " of the property "+ predicate+ " has been inferred from the database");
//                        atom.setTerm(position, newTerm);
//                    }
                }
           else
               throw new UnknownDatatypeException("Unexpected function: " + functionSymbol.getName());
        }
        else if (term instanceof Variable) {

            //check in the ontology if we have already information about the datatype

//            Function normal = qvv.getNormal(atom);
//            Datatype dataType = dataTypesMap.get(normal.getFunctionSymbol());

            // The predicate is created following the database metadata of
            // column type.
            Variable variable = (Variable) term;

            Term newTerm;
//            if (dataType == null || isBooleanDB2(dataType.getPredicate())) {
            Predicate.COL_TYPE type = getDataType(termOccurenceIndex, variable);
            newTerm = DATA_FACTORY.getTypedTerm(variable, type);
            log.warn("Datatype for the value " +variable + " of the property "+ predicate+ " has been inferred from the database");
//            }
//            else {
//                Predicate replacement = dataType.getPredicate();
//                newTerm = DATA_FACTORY.getFunction(replacement, variable);
//            }

            atom.setTerm(position, newTerm);
        }
        else if (term instanceof ValueConstant) {
            Term newTerm = DATA_FACTORY.getTypedTerm(term, Predicate.COL_TYPE.LITERAL);
            atom.setTerm(position, newTerm);
        }
    }

    /**
     * returns COL_TYPE for one of the datatype ids
     * @param termOccurenceIndex
     * @param variable
     * @return
     */

	private Predicate.COL_TYPE getDataType(Map<String, List<IndexedPosititon>> termOccurenceIndex, Variable variable)
            throws InvalidMappingException {


		List<IndexedPosititon> list = termOccurenceIndex.get(variable.getName());
		if (list == null)
			throw new InvalidMappingException("Unknown term in head");

		// ROMAN (10 Oct 2015): this assumes the first occurrence is a database relation!
		//                      AND THAT THERE ARE NO CONSTANTS IN ARGUMENTS!
		IndexedPosititon ip = list.get(0);

		RelationID tableId = Relation2DatalogPredicate.createRelationFromPredicateName(metadata.getQuotedIDFactory(), ip.atom.getFunctionSymbol());
		RelationDefinition td = metadata.getRelation(tableId);
		Attribute attribute = td.getAttribute(ip.pos);

		Predicate.COL_TYPE type =  jdbcTypeMapper.getPredicate(attribute.getType());
		return type;
	}

	private static class IndexedPosititon {
		final Function atom;
		final int pos;

		IndexedPosititon(Function atom, int pos) {
			this.atom = atom;
			this.pos = pos;
		}
	}

	private static Map<String, List<IndexedPosititon>> createIndex(List<Function> body) {
		Map<String, List<IndexedPosititon>> termOccurenceIndex = new HashMap<>();
		for (Function a : body) {
			List<Term> terms = a.getTerms();
			int i = 1; // position index
			for (Term t : terms) {
				if (t instanceof Variable) {
					Variable var = (Variable) t;
					List<IndexedPosititon> aux = termOccurenceIndex.get(var.getName());
					if (aux == null)
						aux = new LinkedList<>();
					aux.add(new IndexedPosititon(a, i));
					termOccurenceIndex.put(var.getName(), aux);
					i++; // increase the position index for the next variable
				}
				else if (t instanceof FunctionalTermImpl) {
					// NO-OP
				}
				else if (t instanceof ValueConstant) {
					// NO-OP
				}
				else if (t instanceof URIConstant) {
					// NO-OP
				}
			}
		}
		return termOccurenceIndex;
	}
}

