package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.ofn.OfnMultitypedInputBinarySPARQLFunctionSymbolImpl;
import it.unibz.inf.ontop.model.term.impl.RDFTermTypeConstantImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.Ontop;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DateTruncSPARQLFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    private final static String[] supportedDateParts = new String[] {
            "microsecond",
            "millisecond",
            "microseconds",
            "milliseconds",
            "second",
            "minute",
            "hour",
            "day",
            "week",
            "month",
            "quarter",
            "year",
            "decade",
            "century",
            "millennium"
    };

    private final Function<String, DBFunctionSymbol> dbFunctionSymbolFunction;
    private final RDFTermType targetType;


    public DateTruncSPARQLFunctionSymbolImpl(@Nonnull RDFTermType inputDateType,
                                             @Nonnull RDFTermType inputSecondType,
                                             @Nonnull Function<String, DBFunctionSymbol> dbFunctionSymbolFunction) {
        super("SP_DATE_TRUNC", Ontop.DATE_TRUNC, ImmutableList.of(inputDateType, inputSecondType));
        this.targetType = inputDateType;
        this.dbFunctionSymbolFunction = dbFunctionSymbolFunction;
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                               ImmutableTerm returnedTypeTerm) {
        if(!(subLexicalTerms.get(1) instanceof Constant)) {
            //TODO - create more specialized exception to throw here.
            throw new RuntimeException("Only constants are supported as Date-Part parameter.");
        }
        var constant = (Constant)subLexicalTerms.get(1);
        var value = constant.getValue();
        if(!Arrays.stream(supportedDateParts)
                .anyMatch(part -> part.equalsIgnoreCase(value))) {
            //TODO - create more specialized exception to throw here.
            throw new RuntimeException(String.format("Date-Part %s is not supported.", value));
        }

        return termFactory.getConversion2RDFLexical(
                termFactory.getImmutableFunctionalTerm(
                        dbFunctionSymbolFunction.apply(value),
                        termFactory.getConversionFromRDFLexical2DB(targetType.getClosestDBType(termFactory.getTypeFactory().getDBTypeFactory()), subLexicalTerms.get(0), targetType),
                        subLexicalTerms.get(1)
                ), targetType);
    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms,
                                            ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                            VariableNullability variableNullability) {
        return termFactory.getRDFTermTypeConstant(targetType);
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(targetType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}

