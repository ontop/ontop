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
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.URITemplatePredicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.VariableImpl;
import it.unibz.krdb.obda.ontology.*;
import it.unibz.krdb.obda.owlrefplatform.core.EquivalenceMap;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.TBoxTraversal;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.TBoxTraverseListener;
import it.unibz.krdb.obda.utils.TypeMapper;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;
import it.unibz.krdb.sql.api.Attribute;

import java.util.*;

public class MappingDataTypeRepair {

	private DBMetadata metadata;

    private TBoxReasoner tBoxReasoner;

    private EquivalenceMap equivalenceMap;

	private Map<String, List<Object[]>> termOccurenceIndex;

    private Map<Predicate, Predicate> dataTypesMap;

	private static OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();



    /**
     * Constructs a new mapping data type resolution. The object requires an
     * ontology for obtaining the user defined data-type.
     * If no datatype is defined thant we use
     * database metadata for obtaining the table column definition as the
     * default data-type.
     *  @param tBoxReasoner
     *            The input TBox.
     * @param equivalenceMap
     * @param metadata The database metadata.
     *
     */
    public MappingDataTypeRepair(TBoxReasoner tBoxReasoner, EquivalenceMap equivalenceMap, DBMetadata metadata) {

        this.tBoxReasoner = tBoxReasoner;

        this.metadata = metadata;

        this.equivalenceMap = equivalenceMap;

        this.dataTypesMap =  new HashMap<>();

    }

    public void getDataTypeFromOntology(){


        /*
        Traverse the graph searching for dataProperty
         */
        TBoxTraversal.traverse(tBoxReasoner, new TBoxTraverseListener() {

            @Override
            public void onInclusion(Property sub, Property sup) {

            }

            @Override
            public void onInclusion(BasicClassDescription sub, BasicClassDescription sup) {
                //if sup is a datatype property  we store it in the map
                //it means that sub is of datatype sup

                Predicate supPredicate = sup.getPredicate();
                if(supPredicate.isDataTypePredicate()) {
                    dataTypesMap.put(sub.getPredicate(), supPredicate);
                }
            }


        });


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

    public void insertDataTyping(DatalogProgram mappingDatalog) throws OBDAException {

        getDataTypeFromOntology();

		List<CQIE> mappingRules = mappingDatalog.getRules();
		for (CQIE rule : mappingRules) {
			prepareIndex(rule);
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

                    Function normal = equivalenceMap.getNormal(atom);
                    Predicate datatype = dataTypesMap.get(normal.getFunctionSymbol());

                    //if a datatype was already assigned in the ontology
                    if(datatype!=null){

                        //check that no datatype mismatch is present
                        if(!functionSymbol.equals(datatype)){

                                throw new OBDAException("Ontology datatype for " + predicate + " does not confirm with datatype in mappings");

                        }
                    }

				} else {
					throw new OBDAException("Unknown data type predicate: "
							+ functionSymbol.getName());
				}
			} else if (term instanceof Variable) {

                Variable variable = (Variable) term;

                Predicate dataTypeFunctor = null;

                //check in the ontology if we have already information about the datatype
                if (predicate.isDataTypePredicate()){

                    Function normal = equivalenceMap.getNormal(atom);
                    //Check if a datatype was already assigned in the ontology
                    dataTypeFunctor= dataTypesMap.get(normal.getFunctionSymbol());

                }

                // If the term has no data-type predicate then by default the
                // predicate is created following the database metadata of
                // column type.
                if(dataTypeFunctor==null){

                    dataTypeFunctor = getDataTypeFunctor(variable);
                }

				Term newTerm = dfac.getFunction( dataTypeFunctor, variable);
				atom.setTerm(1, newTerm);
			}
		}
	}

//	private boolean isDataProperty(Predicate predicate) {
//		return predicate.getArity() == 2 && predicate.getType(1) == Predicate.COL_TYPE.LITERAL;
//	}

	private Predicate getDataTypeFunctor(Variable variable)throws OBDAException {
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

	private void prepareIndex(CQIE rule) {
		termOccurenceIndex = new HashMap<String, List<Object[]>>();
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
	}


}

