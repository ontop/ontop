package it.unibz.inf.ontop.model.term.functionsymbol.impl.ofn;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.ReduciblePositiveAritySPARQLFunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.Optional;

public class OfnSimpleBinarySPARQLFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    private final RDFTermType inputType;
    private final RDFTermType targetType;
    private final boolean isAlwaysInjective;
    private final TriFunction<TermFactory, ImmutableTerm, ImmutableTerm, ImmutableFunctionalTerm> dbFunctionalTermFct;

    public OfnSimpleBinarySPARQLFunctionSymbolImpl(@Nonnull String name, IRI functionIRI,
                                                   RDFTermType inputType, RDFTermType targetType,
                                                   boolean isAlwaysInjective,
                                                   TriFunction<TermFactory, ImmutableTerm, ImmutableTerm, ImmutableFunctionalTerm> dbFunctionalTermFct) {
        super(name, functionIRI, ImmutableList.of(inputType, inputType));
        this.inputType = inputType;
        this.targetType = targetType;
        this.isAlwaysInjective = isAlwaysInjective;
        this.dbFunctionalTermFct = dbFunctionalTermFct;
    }

    protected OfnSimpleBinarySPARQLFunctionSymbolImpl(@Nonnull String name, String officialName,
                                                      RDFTermType inputType, RDFTermType targetType,
                                                      boolean isAlwaysInjective,
                                                      TriFunction<TermFactory, ImmutableTerm, ImmutableTerm, ImmutableFunctionalTerm> dbFunctionalTermFct) {
        super(name, officialName, ImmutableList.of(inputType, inputType));
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
                        termFactory.getConversionFromRDFLexical2DB(subLexicalTerms.get(1), inputType),
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

    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {
        R apply(T var1, U var2, V var3);
    }
}

