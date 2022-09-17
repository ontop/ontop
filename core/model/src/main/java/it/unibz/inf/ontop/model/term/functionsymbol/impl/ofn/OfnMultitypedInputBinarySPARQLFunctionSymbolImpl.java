package it.unibz.inf.ontop.model.term.functionsymbol.impl.ofn;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.ReduciblePositiveAritySPARQLFunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;

public class OfnMultitypedInputBinarySPARQLFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    private final RDFTermType targetType;
    private final boolean isAlwaysInjective;
    /**
     * The corresponding function takes the term factory, two lexical terms and the meta type term as input.
     * Returns a DB term with the natural type associated to the target type.
     * @see it.unibz.inf.ontop.model.term.functionsymbol.impl.MultitypedInputUnarySPARQLFunctionSymbolImpl for one lexical term input
     */
    private final PentaFunction<TermFactory, ImmutableTerm, ImmutableTerm, ImmutableTerm, ImmutableTerm, ImmutableFunctionalTerm> lexicalTermFct;

    public OfnMultitypedInputBinarySPARQLFunctionSymbolImpl(@Nonnull String functionSymbolName, @Nonnull IRI functionIRI,
                                                            @Nonnull RDFTermType inputBaseType,
                                                            @Nonnull RDFTermType targetType,
                                                            boolean isAlwaysInjective,
                                                            PentaFunction<TermFactory, ImmutableTerm, ImmutableTerm,
                                                                    ImmutableTerm, ImmutableTerm,
                                                                    ImmutableFunctionalTerm> lexicalTermFct) {
        super(functionSymbolName, functionIRI, ImmutableList.of(inputBaseType, inputBaseType));
        this.targetType = targetType;
        this.isAlwaysInjective = isAlwaysInjective;
        this.lexicalTermFct = lexicalTermFct;
        if (targetType.isAbstract())
            throw new IllegalArgumentException("The target type must be concrete");
    }

    public OfnMultitypedInputBinarySPARQLFunctionSymbolImpl(@Nonnull String functionSymbolName, @Nonnull IRI functionIRI,
                                                            @Nonnull RDFTermType inputBaseType,
                                                            @Nonnull RDFTermType targetType,
                                                            boolean isAlwaysInjective,
                                                            DBTypeFactory dbTypeFactory,
                                                            Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct) {
        this(functionSymbolName, functionIRI, inputBaseType, targetType, isAlwaysInjective,
                createLatelyTypedFct(targetType, dbTypeFactory, dbFunctionSymbolFct));
    }

    public OfnMultitypedInputBinarySPARQLFunctionSymbolImpl(@Nonnull String functionSymbolName, @Nonnull String officialName,
                                                            @Nonnull RDFTermType inputBaseType,
                                                            @Nonnull RDFTermType targetType,
                                                            boolean isAlwaysInjective,
                                                            PentaFunction<TermFactory, ImmutableTerm, ImmutableTerm,
                                                                    ImmutableTerm, ImmutableTerm,
                                                                    ImmutableFunctionalTerm> lexicalTermFct) {
        super(functionSymbolName, officialName, ImmutableList.of(inputBaseType, inputBaseType));
        this.lexicalTermFct = lexicalTermFct;
        this.targetType = targetType;
        this.isAlwaysInjective = isAlwaysInjective;
        if (targetType.isAbstract())
            throw new IllegalArgumentException("The target type must be concrete");
    }

    public OfnMultitypedInputBinarySPARQLFunctionSymbolImpl(@Nonnull String functionSymbolName, @Nonnull String officialName,
                                                            @Nonnull RDFTermType inputBaseType,
                                                            @Nonnull RDFTermType targetType,
                                                            boolean isAlwaysInjective,
                                                            DBTypeFactory dbTypeFactory,
                                                            Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct) {
        this(functionSymbolName, officialName, inputBaseType, targetType, isAlwaysInjective,
                createLatelyTypedFct(targetType, dbTypeFactory, dbFunctionSymbolFct));
    }

    private static PentaFunction<TermFactory, ImmutableTerm, ImmutableTerm, ImmutableTerm, ImmutableTerm, ImmutableFunctionalTerm> createLatelyTypedFct(
            RDFTermType targetType,
            DBTypeFactory dbTypeFactory,
            Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct) {
        return (termFactory, lexicalTerm0, lexicalTerm1, rdfTermTypeTerm0, rdfTermTypeTerm1) ->
                termFactory.getBinaryLatelyTypedFunctionalTerm(lexicalTerm0, lexicalTerm1, rdfTermTypeTerm0, rdfTermTypeTerm1,
                        targetType.getClosestDBType(dbTypeFactory),
                        dbFunctionSymbolFct);
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                               ImmutableTerm returnedTypeTerm) {

        return termFactory.getConversion2RDFLexical(
                lexicalTermFct.apply(termFactory, subLexicalTerms.get(1), subLexicalTerms.get(0),
                        typeTerms.get(0), typeTerms.get(1)),
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

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(targetType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @FunctionalInterface
    public interface PentaFunction<S, T, U, V, W, R> {
        R apply(S var1, T var2, U var3, V var4, W var5);
    }
}

