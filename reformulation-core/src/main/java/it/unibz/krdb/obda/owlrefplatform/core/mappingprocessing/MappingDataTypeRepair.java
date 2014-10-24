package it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing;

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

import it.unibz.krdb.obda.model.BNodePredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.URITemplatePredicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.*;
import it.unibz.krdb.obda.ontology.*;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.VocabularyValidator;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.TBoxTraversal;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.TBoxTraverseListener;
import it.unibz.krdb.obda.utils.TypeMapper;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;
import it.unibz.krdb.sql.api.Attribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MappingDataTypeRepair {

	private final DBMetadata metadata;

  	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
    private static final Logger log = LoggerFactory.getLogger(MappingDataTypeRepair.class);

    /**
     * Constructs a new mapping data type resolution. The object requires an
     * ontology for obtaining the user defined data-type.
     * 
     * If no datatype is defined than we use
     * database metadata for obtaining the table column definition as the
     * default data-type.
     * 
     * @param metadata The database metadata.
     */
    public MappingDataTypeRepair(DBMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Private method that gets the datatypes already present in the ontology and stores them in a map
     * It will be used later in insertDataTyping
     */
    private Map<Predicate, DataType> getDataTypeFromOntology(TBoxReasoner reasoner){

    	final Map<Predicate, DataType> dataTypesMap = new HashMap<Predicate, DataType>();
    	
        /*
        Traverse the graph searching for dataProperty
         */
        TBoxTraversal.traverse(reasoner, new TBoxTraverseListener() {

            @Override
            public void onInclusion(Property sub, Property sup) {
            }

            @Override
            public void onInclusion(BasicClassDescription sub, BasicClassDescription sup) {
                //if sup is a datatype property  we store it in the map
                //it means that sub is of datatype sup
            	if (sup instanceof DataType) {
            		DataType supDataType = (DataType)sup;
            		Predicate key;
            		if (sub instanceof DataType) {
            			// datatype inclusion
            			key = ((DataType)sub).getPredicate();
            		}
            		else if (sub instanceof PropertySomeRestriction) {
            			// range 
            			key = ((PropertySomeRestriction)sub).getPredicate();
            		}
            		else
            			return;
            		
        			if (dataTypesMap.containsKey(key))
                        System.err.println("Predicate " + key + " with " + dataTypesMap.get(key) + " is redfined as " + supDataType + " in the ontology");
        			dataTypesMap.put(key, supDataType);
            	}
            }
        });
		return dataTypesMap;
    }

                /**
                 * This method wraps the variable that holds data property values with a
                 * data type predicate. It will replace the variable with a new function
                 * symbol and update the rule atom. However, if the users already defined
                 * the data-type in the mapping, this method simply accepts the function
                 * symbol.
                 *
                 * @param mappingDatalog
                 *            The set of mapping axioms.
                 * @throws OBDAException
                 */

    public void insertDataTyping(List<CQIE> mappingRules, TBoxReasoner reasoner) throws OBDAException {

        //get all the datatypes in the ontology
    	 Map<Predicate, DataType> dataTypesMap = getDataTypeFromOntology(reasoner);

		VocabularyValidator qvv = new VocabularyValidator(reasoner);

		for (CQIE rule : mappingRules) {
			Map<String, List<Object[]>> termOccurenceIndex = createIndex(rule);
			Function atom = rule.getHead();
			Predicate predicate = atom.getFunctionSymbol();
			if (!(predicate.getArity() == 2)) { // we check both for data and object property
				continue;
			}

			// If the predicate is a data property
			Term term = atom.getTerm(1);

			if (term instanceof Function) {
				Function function = (Function) term;

				if (function.getFunctionSymbol() instanceof URITemplatePredicate || function.getFunctionSymbol() instanceof BNodePredicate) {
					// NO-OP for object properties
					continue;
				}

				Predicate functionSymbol = function.getFunctionSymbol();
				if (functionSymbol.isDataTypePredicate()) {

                    Function normal = qvv.getNormal(atom);
                    DataType dataType = dataTypesMap.get(normal.getFunctionSymbol());

                    //if a datatype was already assigned in the ontology
                    if (dataType != null) {

                        //check that no datatype mismatch is present
                        if(!functionSymbol.equals(dataType.getPredicate())){

                                throw new OBDAException("Ontology datatype " + dataType + " for " + predicate + "\ndoes not correspond to datatype " + functionSymbol + " in mappings");

                        }
                    }
                    if(isBooleanDB2(dataType.getPredicate())){

                        Variable variable = (Variable)  normal.getTerm(1);

                        //No Boolean datatype in DB2 database, the value in the database is used
                        Predicate replacement = getDataTypeFunctor(termOccurenceIndex, variable);
                        Term newTerm = fac.getFunction(replacement, variable);
                        atom.setTerm(1, newTerm);
                    }

				} else {
					throw new OBDAException("Unknown data type predicate: "
							+ functionSymbol.getName());
				}

			} else if (term instanceof Variable) {

                Variable variable = (Variable) term;

                //check in the ontology if we have already information about the datatype

                Function normal = qvv.getNormal(atom);
                    //Check if a datatype was already assigned in the ontology
                DataType dataType = dataTypesMap.get(normal.getFunctionSymbol());



                // If the term has no data-type predicate then by default the
                // predicate is created following the database metadata of
                // column type.
                Predicate replacement;
                if (dataType == null || isBooleanDB2(dataType.getPredicate()) ){
                	replacement = getDataTypeFunctor(termOccurenceIndex, variable);
                }
                else 
                	replacement = dataType.getPredicate();

				Term newTerm = fac.getFunction(replacement, variable);
				atom.setTerm(1, newTerm);
			}
		}
	}

    /**
     * Private method, since DB2 does not support boolean value, we use the database metadata value
     * @param dataType
     * @return boolean to check if the database is DB2 and we assign  a boolean value
     */
    private boolean isBooleanDB2(Predicate dataType){

        String databaseName = metadata.getDatabaseProductName();
        String databaseDriver = metadata.getDriverName();
        if(databaseName!= null && databaseName.contains("DB2")
                || databaseDriver != null && databaseDriver.contains("IBM")){


            if(dataType.equals(OBDAVocabulary.XSD_BOOLEAN)){

                log.warn("Boolean dataType do not exist in DB2 database, the value in the database metadata is used instead.");
                return true;
            }
        }
        return false;
    }
    
//	private boolean isDataProperty(Predicate predicate) {
//		return predicate.getArity() == 2 && predicate.getType(1) == Predicate.COL_TYPE.LITERAL;
//	}

	private Predicate getDataTypeFunctor(Map<String, List<Object[]>> termOccurenceIndex, Variable variable) throws OBDAException {
		List<Object[]> list = termOccurenceIndex.get(variable.getName());
		if (list == null) {
			throw new OBDAException("Unknown term in head");
		}
		Object[] o = list.get(0);
		Function atom = (Function) o[0];
		Integer pos = (Integer) o[1];

		Predicate functionSymbol = atom.getFunctionSymbol();
		String tableName = functionSymbol.toString();
		DataDefinition tableMetadata = metadata.getDefinition(tableName);

		Attribute attribute = tableMetadata.getAttribute(pos);

		return TypeMapper.getInstance().getPredicate(attribute.getType());
	}

	private Map<String, List<Object[]>> createIndex(CQIE rule) {
		Map<String, List<Object[]>> termOccurenceIndex = new HashMap<String, List<Object[]>>();
		List<Function> body = rule.getBody();
		Iterator<Function> it = body.iterator();
		while (it.hasNext()) {
			Function a = (Function) it.next();
			List<Term> terms = a.getTerms();
			int i = 1; // position index
			for (Term t : terms) {
				if (t instanceof AnonymousVariable) {
					i++; // increase the position index to evaluate the next
							// variable
				} else if (t instanceof VariableImpl) {
					Object[] o = new Object[2];
					o[0] = a; // atom
					o[1] = i; // position index
					List<Object[]> aux = termOccurenceIndex
							.get(((VariableImpl) t).getName());
					if (aux == null) {
						aux = new LinkedList<Object[]>();
					}
					aux.add(o);
					termOccurenceIndex.put(((VariableImpl) t).getName(), aux);
					i++; // increase the position index to evaluate the next
							// variable
				} else if (t instanceof FunctionalTermImpl) {
					// NO-OP
				} else if (t instanceof ValueConstant) {
					// NO-OP
				} else if (t instanceof URIConstant) {
					// NO-OP
				}
			}
		}
		return termOccurenceIndex;
	}


}

