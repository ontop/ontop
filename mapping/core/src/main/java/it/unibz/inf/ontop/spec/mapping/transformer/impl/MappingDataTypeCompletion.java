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
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.type.impl.TermTypeInferenceTools;
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
     * This method wraps the variable that holds data property values with a data type predicate.
     * It will replace the variable with a new function symbol and update the rule atom.
     * However, if the users already defined the data-type in the mapping, this method simply accepts the function symbol.
     */
    public Term insertVariableDataTyping(Term term, ImmutableMultimap<Variable, Attribute> termOccurenceIndex) {

        if (term instanceof Function) {
            Function function = (Function) term;
            Predicate functionSymbol = function.getFunctionSymbol();
            if (function.isDataTypeFunction() ||
                    (functionSymbol instanceof URITemplatePredicate)
                    || (functionSymbol instanceof BNodePredicate)) {
                // NO-OP for already assigned datatypes, or object properties, or bnodes
                return term;
            }
            else if (function.isOperation()) {
                ImmutableList.Builder<Term> termBuilder = ImmutableList.builder();
                for (int i = 0; i < function.getArity(); i++) {
                    termBuilder.add(insertVariableDataTyping(function.getTerm(i), termOccurenceIndex));
                }
                return termFactory.getFunction(functionSymbol, termBuilder.build());
            }
            throw new IllegalArgumentException("Unsupported subtype of: " + Function.class.getSimpleName());
        }
        else if (term instanceof Variable) {
            Variable variable = (Variable) term;
            RDFDatatype type = getDataType(termOccurenceIndex.get(variable), variable);
            Term newTerm = termFactory.getTypedTerm(variable, type);
            log.info("Datatype " + type + " for the value " + variable + " has been inferred from the database");
            return newTerm;
        }
        else if (term instanceof ValueConstant) {
            return termFactory.getTypedTerm(term, ((ValueConstant) term).getType());
        }

        throw new IllegalArgumentException("Unsupported subtype of: " + Term.class.getSimpleName());
    }

   /**
    * Following R2RML standard we do not infer the datatype for operation but we return the default value string
    */
    public ImmutableTerm insertOperationDatatyping(ImmutableTerm immutableTerm) {

        if (immutableTerm instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm castTerm = (ImmutableFunctionalTerm) immutableTerm;
            Predicate functionSymbol = castTerm.getFunctionSymbol();
            if (functionSymbol instanceof OperationPredicate) {
                Optional<TermType> inferredType = termTypeInferenceTools.inferType(castTerm);
                if (inferredType.isPresent()) {
                    // delete explicit datatypes of the operands
                    ImmutableTerm newTerm = deleteExplicitTypes(castTerm);
                    // insert the datatype of the evaluated operation
                    return termFactory.getImmutableTypedTerm(newTerm, (RDFDatatype) inferredType.get()); // TODO: refactor this cast
                }
                else {
                    if (defaultDatatypeInferred) {
                        return termFactory.getImmutableTypedTerm(castTerm, typeFactory.getXsdStringDatatype());
                    }
                    throw new UnknownDatatypeRuntimeException("Impossible to determine the expected datatype for the operation " + castTerm + "\n" +
                            "Possible solutions: \n" +
                            "- Add an explicit datatype in the mapping \n" +
                            "- Add in the .properties file the setting: ontop.inferDefaultDatatype = true\n" +
                            " and we will infer the default datatype (xsd:string)"
                    );
                }
            }
        }
        return immutableTerm;
    }

    public ImmutableTerm deleteExplicitTypes(ImmutableTerm term) {
        if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm function = (ImmutableFunctionalTerm) term;
            ImmutableList.Builder<ImmutableTerm> termBuilder = ImmutableList.builder();
            IntStream.range(0, function.getArity())
                    .forEach(i -> termBuilder.add(deleteExplicitTypes(function.getTerm(i))));
            ImmutableList<ImmutableTerm> terms = termBuilder.build();

            if (function.getFunctionSymbol() instanceof DatatypePredicate)
                return terms.get(0);
            else
                return termFactory.getImmutableFunctionalTerm(function.getFunctionSymbol(), terms);
        }
        return term;
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

