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

import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.impl.FunctionalTermImpl;
import it.unibz.inf.ontop.model.term.functionsymbol.BNodePredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.impl.TermTypeInferenceTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;


public class MappingDataTypeCompletion {

    private final DBMetadata metadata;

    private static final Logger log = LoggerFactory.getLogger(MappingDataTypeCompletion.class);

    /**
     * Constructs a new mapping data type resolution.
     * If no datatype is defined, then we use database metadata for obtaining the table column definition as the
     * default data-type.
     * //TODO: rewrite in a Datalog-free fashion
     *
     * @param metadata The database metadata.
     */
    public MappingDataTypeCompletion(DBMetadata metadata) {
        this.metadata = metadata;
    }

    public void insertDataTyping(CQIE rule) {
        Function atom = rule.getHead();
        Predicate predicate = atom.getFunctionSymbol();
        if (predicate.getArity() == 2) { // we check both for data and object property
            Term term = atom.getTerm(1); // the second argument only
            Map<String, List<IndexedPosition>> termOccurenceIndex = createIndex(rule.getBody());
            // Infer variable datatypes
            insertVariableDataTyping(term, atom, 1, termOccurenceIndex);
            // Infer operation datatypes from variable datatypes
            insertOperationDatatyping(term, atom, 1);
        }
    }

    /**
     * This method wraps the variable that holds data property values with a data type predicate.
     * It will replace the variable with a new function symbol and update the rule atom.
     * However, if the users already defined the data-type in the mapping, this method simply accepts the function symbol.
     */
    private void insertVariableDataTyping(Term term, Function atom, int position,
                                          Map<String, List<IndexedPosition>> termOccurenceIndex) {
        Predicate predicate = atom.getFunctionSymbol();

        if (term instanceof Function) {
            Function function = (Function) term;
            Predicate functionSymbol = function.getFunctionSymbol();
            if (function.isDataTypeFunction() ||
                    (functionSymbol instanceof URITemplatePredicate)
                    || (functionSymbol instanceof BNodePredicate)) {
                // NO-OP for already assigned datatypes, or object properties, or bnodes
            }
            else if (function.isOperation()) {
                for (int i = 0; i < function.getArity(); i++) {
                    insertVariableDataTyping(function.getTerm(i), function, i, termOccurenceIndex);
                }
            } else {
                throw new IllegalArgumentException("Unsupported subtype of: " + Function.class.getSimpleName());
            }
        } else if (term instanceof Variable) {
            Variable variable = (Variable) term;
            Term newTerm;
            Predicate.COL_TYPE type = getDataType(termOccurenceIndex, variable);
            newTerm = TERM_FACTORY.getTypedTerm(variable, type);
            log.warn("Datatype for the value " + variable + " of the property " + predicate + " has been inferred from the database");
            atom.setTerm(position, newTerm);
        } else if (term instanceof ValueConstant) {
            Term newTerm = TERM_FACTORY.getTypedTerm(term, ((ValueConstant) term).getType());
            atom.setTerm(position, newTerm);
        } else {
            throw new IllegalArgumentException("Unsupported subtype of: " + Term.class.getSimpleName());
        }
    }

    /**
     * Infers inductively the datatypes of (evaluated) operations, from their operands.
     * After execution, only the outermost operation is assigned a type.
     */
    private void insertOperationDatatyping(Term term, Function atom, int position) {

        if (term instanceof Function) {
            Function castTerm = (Function) term;
            if (castTerm.isOperation()) {
                Optional<TermType> inferredType = TermTypeInferenceTools.inferType(castTerm);
                if(inferredType.isPresent()){
                    // delete explicit datatypes of the operands
                    deleteExplicitTypes(term, atom, position);
                    // insert the datatype of the evaluated operation
                    atom.setTerm(
                            position,
                            TERM_FACTORY.getTypedTerm(
                                    term,
                                    inferredType.get().getColType()
                            ));
                }else {
                    throw new IllegalStateException("A type should be inferred for operation " + castTerm);
                }
            }
        }
    }

    private void deleteExplicitTypes(Term term, Function atom, int position) {
        if(term instanceof Function){
            Function castTerm = (Function) term;
            IntStream.range(0, castTerm.getArity())
                    .forEach(i -> deleteExplicitTypes(
                            castTerm.getTerm(i),
                            castTerm,
                            i
                    ));
            if(castTerm.isDataTypeFunction()){
                atom.setTerm(position, castTerm.getTerm(0));
            }
        }
    }

    /**
     * returns COL_TYPE for one of the datatype ids
     *
     * @param termOccurenceIndex
     * @param variable
     * @return
     */
    private Predicate.COL_TYPE getDataType(Map<String, List<IndexedPosition>> termOccurenceIndex, Variable variable) {


        List<IndexedPosition> list = termOccurenceIndex.get(variable.getName());
        if (list == null)
            throw new UnboundTargetVariableException(variable);

        // ROMAN (10 Oct 2015): this assumes the first occurrence is a database relation!
        //                      AND THAT THERE ARE NO CONSTANTS IN ARGUMENTS!
        IndexedPosition ip = list.get(0);

        RelationID tableId = Relation2Predicate.createRelationFromPredicateName(metadata.getQuotedIDFactory(), ip.atom
                .getFunctionSymbol());
        RelationDefinition td = metadata.getRelation(tableId);
        Attribute attribute = td.getAttribute(ip.pos);

        return metadata.getColType(attribute)
                // Default datatype : XSD_STRING
                .orElse(Predicate.COL_TYPE.STRING);
    }

    private static class IndexedPosition {
        final Function atom;
        final int pos;

        IndexedPosition(Function atom, int pos) {
            this.atom = atom;
            this.pos = pos;
        }
    }

    private static Map<String, List<IndexedPosition>> createIndex(List<Function> body) {
        Map<String, List<IndexedPosition>> termOccurenceIndex = new HashMap<>();
        for (Function a : body) {
            List<Term> terms = a.getTerms();
            int i = 1; // position index
            for (Term t : terms) {
                if (t instanceof Variable) {
                    Variable var = (Variable) t;
                    List<IndexedPosition> aux = termOccurenceIndex.get(var.getName());
                    if (aux == null)
                        aux = new LinkedList<>();
                    aux.add(new IndexedPosition(a, i));
                    termOccurenceIndex.put(var.getName(), aux);
                    i++; // increase the position index for the next variable
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

    /**
     * Should have been detected earlier!
     */
    private static class UnboundTargetVariableException extends OntopInternalBugException {

        protected UnboundTargetVariableException(Variable variable) {
            super("Unknown variable in the head of a mapping:" + variable + ". Should have been detected earlier !");
        }
    }

}

