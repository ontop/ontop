package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.BiFunction;


public class SimpleUnarySPARQLFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    private final RDFTermType inputType;
    private final RDFTermType targetType;
    private final boolean isAlwaysInjective;
    private final BiFunction<TermFactory, ImmutableTerm, ImmutableFunctionalTerm> dbFunctionalTermFct;

    protected SimpleUnarySPARQLFunctionSymbolImpl(@Nonnull String name, IRI functionIRI,
                                                  RDFTermType inputType, RDFTermType targetType,
                                                  boolean isAlwaysInjective,
                                                  BiFunction<TermFactory, ImmutableTerm, ImmutableFunctionalTerm> dbFunctionalTermFct) {
        super(name, functionIRI, ImmutableList.of(inputType));
        this.inputType = inputType;
        this.targetType = targetType;
        this.isAlwaysInjective = isAlwaysInjective;
        this.dbFunctionalTermFct = dbFunctionalTermFct;
    }

    protected SimpleUnarySPARQLFunctionSymbolImpl(@Nonnull String name, String officialName,
                                                  RDFTermType inputType, RDFTermType targetType,
                                                  boolean isAlwaysInjective,
                                                  BiFunction<TermFactory, ImmutableTerm, ImmutableFunctionalTerm> dbFunctionalTermFct) {
        super(name, officialName, ImmutableList.of(inputType));
        this.inputType = inputType;
        this.targetType = targetType;
        this.isAlwaysInjective = isAlwaysInjective;
        this.dbFunctionalTermFct = dbFunctionalTermFct;
    }

    /**
     * If the child type is xsd:string or a language tag, then returns it.
     *
     */
    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(targetType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                               ImmutableTerm returnedTypeTerm) {
        return termFactory.getConversion2RDFLexical(
                dbFunctionalTermFct.apply(
                        termFactory,
                        termFactory.getConversionFromRDFLexical2DB(subLexicalTerms.get(0), inputType)),
                targetType);
    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms,
                                            ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                            VariableNullability variableNullability) {
        return termFactory.getRDFTermTypeConstant(targetType);
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return isAlwaysInjective;
    }
}
