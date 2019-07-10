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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.type.impl.TermTypeInferenceTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class MappingDataTypeCompletion {

    private final boolean defaultDatatypeInferred;

    private static final Logger log = LoggerFactory.getLogger(MappingDataTypeCompletion.class);
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final TermTypeInferenceTools termTypeInferenceTools;

    /**
     * Constructs a new mapping data type resolution.
     * If no datatype is defined, then we use database metadata for obtaining the table column definition as the
     * default data-type.
     * @param termFactory
     * @param typeFactory
     * @param termTypeInferenceTools
     */
    public MappingDataTypeCompletion(boolean defaultDatatypeInferred,
                                     TermFactory termFactory, TypeFactory typeFactory,
                                     TermTypeInferenceTools termTypeInferenceTools) {
        this.defaultDatatypeInferred = defaultDatatypeInferred;
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.termTypeInferenceTools = termTypeInferenceTools;
    }


    /**
     * This method wraps the variable that holds data property values with a data type predicate.
     * It will replace the variable with a new function symbol and update the rule atom.
     * However, if the users already defined the data-type in the mapping, this method simply accepts the function symbol.
     */
    public ImmutableTerm insertVariableDataTyping(ImmutableTerm term, ImmutableMultimap<Variable, Attribute> termOccurenceIndex) {

        if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm function = (ImmutableFunctionalTerm) term;
            Predicate functionSymbol = function.getFunctionSymbol();
            if (functionSymbol instanceof DatatypePredicate ||
                    (functionSymbol instanceof URITemplatePredicate)
                    || (functionSymbol instanceof BNodePredicate)) {
                // NO-OP for already assigned datatypes, or object properties, or bnodes
                return term;
            }
            else if (functionSymbol instanceof OperationPredicate) {
                ImmutableList<ImmutableTerm> terms = function.getTerms().stream()
                        .map(t -> insertVariableDataTyping(t, termOccurenceIndex))
                        .collect(ImmutableCollectors.toList());
                return termFactory.getImmutableFunctionalTerm((OperationPredicate)functionSymbol, terms);
            }
            throw new IllegalArgumentException("Unsupported subtype of: " + Function.class.getSimpleName());
        }
        else if (term instanceof Variable) {
            Variable variable = (Variable) term;
            Collection<Attribute> list = termOccurenceIndex.get(variable);
            if (list == null)
                throw new UnboundTargetVariableException(variable);

            Optional<RDFDatatype> ot = list.stream()
                    .filter(a -> a.getType() != 0)
                    // TODO: refactor this (unsafe)!!!
                    .map(a -> (RDFDatatype) a.getTermType())
                    .findFirst();
                    Optional.empty();

            RDFDatatype type = getDatatype(ot, variable);
            ImmutableTerm newTerm = termFactory.getImmutableTypedTerm(variable, type);
            log.info("Datatype " + type + " for the value " + variable + " has been inferred from the database");
            return newTerm;
        }
        else if (term instanceof ValueConstant) {
            return termFactory.getImmutableTypedTerm(term, ((ValueConstant) term).getType());
        }

        throw new IllegalArgumentException("Unsupported subtype of: " + Term.class.getSimpleName());
    }

   /**
    * Following R2RML standard we do not infer the datatype for operation but we return the default value string
    */
    public ImmutableTerm insertOperationDatatyping(ImmutableTerm immutableTerm) {

        if (immutableTerm instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) immutableTerm;
            if (functionalTerm.getFunctionSymbol() instanceof OperationPredicate) {
                Optional<TermType> inferredType = termTypeInferenceTools.inferType(functionalTerm);
                if (inferredType.isPresent()) {
                    return termFactory.getImmutableTypedTerm(
                            deleteExplicitTypes(functionalTerm),
                            // TODO: refactor the cast
                            (RDFDatatype) inferredType.get());
                }
                else {
                    return termFactory.getImmutableTypedTerm(
                            functionalTerm,
                            getDatatype(Optional.empty(), immutableTerm));
                }
            }
        }
        return immutableTerm;
    }

    private ImmutableTerm deleteExplicitTypes(ImmutableTerm term) {
        if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm function = (ImmutableFunctionalTerm) term;
            ImmutableList<ImmutableTerm> terms = function.getTerms().stream()
                    .map(t -> deleteExplicitTypes(t))
                    .collect(ImmutableCollectors.toList());

            if (function.getFunctionSymbol() instanceof DatatypePredicate)
                return terms.get(0);
            else
                return termFactory.getImmutableFunctionalTerm(function.getFunctionSymbol(), terms);
        }
        return term;
    }


    private RDFDatatype getDatatype(Optional<RDFDatatype> type, ImmutableTerm term) {
        if (defaultDatatypeInferred)
            return type.orElseGet(typeFactory::getXsdStringDatatype);
        else {
            return type.orElseThrow(() -> new UnknownDatatypeRuntimeException("Impossible to determine the expected datatype for "+ term +"\n" +
                    "Possible solutions: \n" +
                    "- Add an explicit datatype in the mapping \n" +
                    "- Add in the .properties file the setting: ontop.inferDefaultDatatype = true\n" +
                    " and we will infer the default datatype (xsd:string)"));

        }
    }

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

