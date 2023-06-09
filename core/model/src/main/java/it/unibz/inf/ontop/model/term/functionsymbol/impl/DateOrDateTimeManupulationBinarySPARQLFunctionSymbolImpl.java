package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.RDFTermTypeConstantImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DateOrDateTimeManupulationBinarySPARQLFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    private final boolean isAlwaysInjective;
    /**
     * The corresponding function takes the term factory and two lexical terms as input.
     * Returns a DB term with the natural type taken from the first argument.
     */
    private final Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct;


    public DateOrDateTimeManupulationBinarySPARQLFunctionSymbolImpl(@Nonnull String functionSymbolName, @Nonnull IRI functionIRI,
                                                                    @Nonnull RDFTermType inputDateType,
                                                                    @Nonnull RDFTermType inputSecondType,
                                                                    boolean isAlwaysInjective,
                                                                    Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct) {
        super(functionSymbolName, functionIRI, ImmutableList.of(inputDateType, inputSecondType));
        this.isAlwaysInjective = isAlwaysInjective;
        this.dbFunctionSymbolFct = dbFunctionSymbolFct;
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                               ImmutableTerm returnedTypeTerm) {
        var argumentType = getArgumentType(typeTerms.get(0))
                .orElseThrow(() -> new IllegalArgumentException("Invalid term type")); //TODO-Damian change exception.

        var functionSymbol = dbFunctionSymbolFct.apply(argumentType.getClosestDBType(termFactory.getTypeFactory().getDBTypeFactory()));
        if(!functionSymbol.isPresent()) {
            return termFactory.getImmutableFunctionalTerm(this, subLexicalTerms);
        }

        return termFactory.getConversion2RDFLexical(
                termFactory.getImmutableFunctionalTerm(
                        functionSymbol.get(),
                        termFactory.getConversionFromRDFLexical2DB(argumentType.getClosestDBType(termFactory.getTypeFactory().getDBTypeFactory()), subLexicalTerms.get(0), argumentType),
                        subLexicalTerms.get(1)
                    ), argumentType);
    }

    private Optional<RDFTermType> getArgumentType(ImmutableTerm typeTerm) {
        if(typeTerm instanceof Constant) {
            return Optional.of(((RDFTermTypeConstantImpl)typeTerm).getRDFTermType());
        }
        else if(isRDFFunctionalTerm(typeTerm)) //TODO-Damian this is probably not correct.
        {
            return Optional.ofNullable(typeTerm.inferType()
                    .map(inf -> (RDFTermType) inf.getTermType()
                            .orElse(null))
                    .orElse(null)
            );
        }
        return Optional.empty();
    }

    protected boolean isRDFFunctionalTerm(ImmutableTerm term) {
        return (term instanceof ImmutableFunctionalTerm)
                && (((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof RDFTermFunctionSymbol);
    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms,
                                            ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                            VariableNullability variableNullability) {
        return typeTerms.get(0);
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return isAlwaysInjective;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return terms.get(0).inferType();
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {
        R apply(T var1, U var2, V var3);
    }
}

