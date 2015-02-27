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

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.VariableImpl;
import it.unibz.krdb.obda.ontology.*;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.VocabularyValidator;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.TBoxTraversal;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.TBoxTraverseListener;
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
    private Map<Predicate, Datatype> getDataTypeFromOntology(TBoxReasoner reasoner){

    	final Map<Predicate, Datatype> dataTypesMap = new HashMap<Predicate, Datatype>();
    	
        /*
        Traverse the graph searching for dataProperty
         */
        TBoxTraversal.traverse(reasoner, new TBoxTraverseListener() {

            @Override
            public void onInclusion(ObjectPropertyExpression sub, ObjectPropertyExpression sup) {
            }
            @Override
            public void onInclusion(DataPropertyExpression sub, DataPropertyExpression sup) {
            }
            @Override
            public void onInclusion(ClassExpression sub, ClassExpression sup) {
            }

            @Override
            public void onInclusion(DataRangeExpression sub, DataRangeExpression sup) {
                //if sup is a datatype property  we store it in the map
                //it means that sub is of datatype sup
            	if (sup instanceof Datatype) {
            		Datatype supDataType = (Datatype)sup;
            		Predicate key;
            		if (sub instanceof Datatype) {
            			// datatype inclusion
            			key = ((Datatype)sub).getPredicate();
            		}
            		else if (sub instanceof DataPropertyRangeExpression) {
            			// range 
            			key = ((DataPropertyRangeExpression)sub).getProperty().getPredicate();
            		}
            		else
            			return;
            		
        			if (dataTypesMap.containsKey(key))
                        throw new PredicateRedefinitionException("Predicate " + key + " with " + dataTypesMap.get(key) + " is redefined as " + supDataType + " in the ontology");
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
                 * @param mappingRules
                 *            The set of mapping axioms.
                 * @throws OBDAException
                 */

    public void insertDataTyping(List<CQIE> mappingRules, TBoxReasoner reasoner) throws OBDAException {


        //get all the datatypes in the ontology
    	 Map<Predicate, Datatype> dataTypesMap;

        try {
            dataTypesMap = getDataTypeFromOntology(reasoner);
        } catch (PredicateRedefinitionException pe){
            throw new OBDAException(pe);
        }


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
				Predicate functionSymbol = function.getFunctionSymbol();

				if (functionSymbol instanceof URITemplatePredicate || functionSymbol instanceof BNodePredicate) {
					// NO-OP for object properties
					continue;
				}


                /** If it is a concat or replace function, can have a datatype assigned to its alias (function with datatype predicate)
                 *  or if no information about the datatype is assigned we will assign the value from the ontology
                 if present or the information from the database will be used.
                 */

				if (functionSymbol.isStringOperationPredicate()){


                    insertDataTypingStringPredicate(atom, function, termOccurenceIndex, qvv, dataTypesMap);



                }

				else if (functionSymbol.isDataTypePredicate()) {

                    Function normal = qvv.getNormal(atom);
                    Datatype dataType = dataTypesMap.get(normal.getFunctionSymbol());

                    //if a datatype was already assigned in the ontology
                    if (dataType != null) {

                        //check that no datatype mismatch is present
                        if(!functionSymbol.equals(dataType.getPredicate())){

                                throw new OBDAException("Ontology datatype " + dataType + " for " + predicate + "\ndoes not correspond to datatype " + functionSymbol + " in mappings");

                        }
                        
                        if(isBooleanDB2(dataType.getPredicate())) {

                            Variable variable = (Variable)  normal.getTerm(1);

                            //No Boolean datatype in DB2 database, the value in the database is used
                            Predicate.COL_TYPE type = getDataType(termOccurenceIndex, variable);
                            Term newTerm = fac.getTypedTerm(variable, type); 
                            atom.setTerm(1, newTerm);
                        }
                    }
				} 
				else {
					throw new OBDAException("Unknown data type predicate: " + functionSymbol.getName());
				}

			} else if (term instanceof Variable) {

                Variable variable = (Variable) term;

                //check in the ontology if we have already information about the datatype

                Function normal = qvv.getNormal(atom);
                    //Check if a datatype was already assigned in the ontology
                Datatype dataType = dataTypesMap.get(normal.getFunctionSymbol());



                // If the term has no data-type predicate then by default the
                // predicate is created following the database metadata of
                // column type.
                Term newTerm;
                if (dataType == null || isBooleanDB2(dataType.getPredicate())) {
                	Predicate.COL_TYPE type = getDataType(termOccurenceIndex, variable);
                	newTerm = fac.getTypedTerm(variable, type);
                }
                else {
                	Predicate replacement = dataType.getPredicate();
                	newTerm = fac.getFunction(replacement, variable);
                }

				atom.setTerm(1, newTerm);
			}
		}
	}

    private void insertDataTypingStringPredicate(Function atom, Function function, Map<String, List<Object[]>> termOccurenceIndex, VocabularyValidator qvv,  Map<Predicate, Datatype> dataTypesMap) throws OBDAException {

        //check in the ontology if we have already information about the datatype

        Function normal = qvv.getNormal(atom);
        //Check if a datatype was already assigned in the ontology
        Datatype dataType = dataTypesMap.get(normal.getFunctionSymbol());

        //assign the datatype of the ontology
        if (dataType != null ) {
            if (!isBooleanDB2(dataType.getPredicate())) {
                Term newTerm;

                Predicate replacement = dataType.getPredicate();
                newTerm = fac.getFunction(replacement, function);

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


            if (fac.getDatatypeFactory().isBoolean(dataType)) {

                log.warn("Boolean dataType do not exist in DB2 database, the value in the database metadata is used instead.");
                return true;
            }
        }
        return false;
    }
    
//	private boolean isDataProperty(Predicate predicate) {
//		return predicate.getArity() == 2 && predicate.getType(1) == Predicate.COL_TYPE.LITERAL;
//	}
    
    /**
     * returns COL_TYPE for one of the datatype ids
     * @param termOccurenceIndex
     * @param variable
     * @return
     * @throws OBDAException
     */

	private Predicate.COL_TYPE getDataType(Map<String, List<Object[]>> termOccurenceIndex, Variable variable) throws OBDAException {
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

		Predicate.COL_TYPE type =  fac.getJdbcTypeMapper().getPredicate(attribute.getType());
		return type;
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

