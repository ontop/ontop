package it.unibz.inf.ontop.spec.mapping.transformer.impl;

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

import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.exception.UnknownDatatypeException;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BNodePredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.type.impl.TermTypeInferenceTools;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.IntStream;


public class MappingDataTypeCompletion {

    private final boolean defaultDatatypeInferred;

    private static final Logger log = LoggerFactory.getLogger(MappingDataTypeCompletion.class);
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final TermTypeInferenceTools termTypeInferenceTools;
    private final ImmutabilityTools immutabilityTools;

    /**
     * Constructs a new mapping data type resolution.
     * If no datatype is defined, then we use database metadata for obtaining the table column definition as the
     * default data-type.
     * //TODO: rewrite in a Datalog-free fashion
     * @param termFactory
     * @param typeFactory
     * @param termTypeInferenceTools
     * @param immutabilityTools
     */
    public MappingDataTypeCompletion(boolean defaultDatatypeInferred,
                                     TermFactory termFactory, TypeFactory typeFactory,
                                     TermTypeInferenceTools termTypeInferenceTools,
                                     ImmutabilityTools immutabilityTools) {
        this.defaultDatatypeInferred = defaultDatatypeInferred;
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.termTypeInferenceTools = termTypeInferenceTools;
        this.immutabilityTools = immutabilityTools;
    }

    /**
     * check if the term is {@code URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")}
     */

    public static boolean isURIRDFType(Term term) {
        if (term instanceof Function) {
            Function func = (Function) term;
            if (func.getArity() == 1 && (func.getFunctionSymbol() instanceof URITemplatePredicate)) {
                Term t0 = func.getTerm(0);
                if (t0 instanceof IRIConstant)
                    return ((IRIConstant) t0).getIRI().equals(RDF.TYPE);
                    // UGLY!! TODO: remove it
                else if (t0 instanceof ValueConstant)
                    return ((ValueConstant) t0).getValue().equals(RDF.TYPE.getIRIString());
            }
        }
        return false;
    }


    /**
     * This method wraps the variable that holds data property values with a data type predicate.
     * It will replace the variable with a new function symbol and update the rule atom.
     * However, if the users already defined the data-type in the mapping, this method simply accepts the function symbol.
     */
    public void insertVariableDataTyping(Term term, Function atom, int position,
                                          ImmutableMultimap<Variable, Attribute> termOccurenceIndex) {

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
            }
            else {
                throw new IllegalArgumentException("Unsupported subtype of: " + Function.class.getSimpleName());
            }
        }
        else if (term instanceof Variable) {
            Variable variable = (Variable) term;
            RDFDatatype type = getDataType(termOccurenceIndex.get(variable), variable);
            Term newTerm = termFactory.getTypedTerm(variable, type);
            log.info("Datatype " + type + " for the value " + variable + " of the property " + atom + " has been " +
                    "inferred " +
                    "from the database");
            atom.setTerm(position, newTerm);
        }
        else if (term instanceof ValueConstant) {
            Term newTerm = termFactory.getTypedTerm(term, ((ValueConstant) term).getType());
            atom.setTerm(position, newTerm);
        }
        else {
            throw new IllegalArgumentException("Unsupported subtype of: " + Term.class.getSimpleName());
        }
    }

   /**
    * Following r2rml standard we do not infer the datatype for operation but we return the default value string
    */
    public void insertOperationDatatyping(Term term, Function atom, int position) {

        ImmutableTerm immutableTerm = immutabilityTools.convertIntoImmutableTerm(term);

        if (immutableTerm instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm castTerm = (ImmutableFunctionalTerm) immutableTerm;
            Predicate functionSymbol = castTerm.getFunctionSymbol();
            if (functionSymbol instanceof OperationPredicate) {

                Optional<TermType> inferredType = termTypeInferenceTools.inferType(castTerm);
                if(inferredType.isPresent()){
                    // delete explicit datatypes of the operands
                    deleteExplicitTypes(term, atom, position);
                    // insert the datatype of the evaluated operation
                    atom.setTerm(
                            position,
                            termFactory.getTypedTerm(
                                    term,
                                    // TODO: refactor this cast
                                    (RDFDatatype) inferredType.get()
                            ));
                }
                else
                    {

                    if (defaultDatatypeInferred) {
                        atom.setTerm(position, termFactory.getTypedTerm(term, typeFactory.getXsdStringDatatype()));
                    }
                    else {
                        throw new UnknownDatatypeRuntimeException("Impossible to determine the expected datatype for the operation " + castTerm + "\n" +
                                "Possible solutions: \n" +
                                "- Add an explicit datatype in the mapping \n" +
                                "- Add in the .properties file the setting: ontop.inferDefaultDatatype = true\n" +
                                " and we will infer the default datatype (xsd:string)"
                        );
                    }
                }
            }
        }
    }

    public void deleteExplicitTypes(Term term, Function atom, int position) {
        if (term instanceof Function) {
            Function function = (Function) term;
            IntStream.range(0, function.getArity())
                    .forEach(i -> deleteExplicitTypes(
                            function.getTerm(i),
                            function,
                            i
                    ));
            if (function.isDataTypeFunction()) {
                atom.setTerm(position, function.getTerm(0));
            }
        }
    }

    /**
     * returns COL_TYPE for one of the datatype ids
     *
     * @param variable
     * @return
     */
    private RDFDatatype getDataType(Collection<Attribute> list, Variable variable) {

        if (list == null)
            throw new UnboundTargetVariableException(variable);

        Optional<RDFDatatype> type = Optional.empty();
        for (Attribute attribute : list) {
            if (attribute.getType() != 0 && !type.isPresent())
                // TODO: refactor this (unsafe)!!!
                type = Optional.of((RDFDatatype) attribute.getTermType());
        }
        if (defaultDatatypeInferred)
            return type.orElseGet(typeFactory::getXsdStringDatatype);
        else {
            return type.orElseThrow(() -> new UnknownDatatypeRuntimeException("Impossible to determine the expected datatype for the column "+ variable+"\n" +
                    "Possible solutions: \n" +
                    "- Add an explicit datatype in the mapping \n" +
                    "- Add in the .properties file the setting: ontop.inferDefaultDatatype = true\n" +
                    " and we will infer the default datatype (xsd:string)"));

        }
    }

    public static ImmutableMultimap<Variable, Attribute> createIndex(List<Function> body) {
        ImmutableMultimap.Builder<Variable, Attribute> termOccurenceIndex = ImmutableMultimap.builder();
        for (Function a : body) {
            if (a.getFunctionSymbol() instanceof RelationPredicate) {
                RelationDefinition td = ((RelationPredicate) a.getFunctionSymbol()).getRelationDefinition();
                List<Term> terms = a.getTerms();
                int i = 1; // position index
                for (Term t : terms) {
                    if (t instanceof Variable) {
                        termOccurenceIndex.put((Variable) t, td.getAttribute(i));
                    }
                    i++; // increase the position index for the next variable
                }
            }
        }
        return termOccurenceIndex.build();
    }

    /**
     * Should have been detected earlier!
     */
    private static class UnboundTargetVariableException extends OntopInternalBugException {

        protected UnboundTargetVariableException(Variable variable) {
            super("Unknown variable in the head of a mapping:" + variable + ". Should have been detected earlier !");
        }
    }

    public static class UnknownDatatypeRuntimeException extends RuntimeException { // workaround for lambdas
        private UnknownDatatypeRuntimeException(String message) {
            super(message);
        }
    }

}

