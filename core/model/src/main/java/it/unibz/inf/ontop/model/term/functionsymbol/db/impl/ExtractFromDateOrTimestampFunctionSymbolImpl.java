package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.DATE;
import static it.unibz.inf.ontop.model.type.DBTermType.Category.DATETIME;

public class ExtractFromDateOrTimestampFunctionSymbolImpl extends UnaryDBFunctionSymbolWithSerializerImpl {
    protected ExtractFromDateOrTimestampFunctionSymbolImpl(String name, DBTermType inputDBType, DBTermType targetType,
                                                           DBFunctionSymbolSerializer serializer) {
        super(name, inputDBType, targetType, false, serializer);
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory, VariableNullability variableNullability) {
        var subTerm = newTerms.get(0);

        if (subTerm instanceof ImmutableFunctionalTerm) {
            var functionalTerm = (ImmutableFunctionalTerm) subTerm;
            var functionSymbol = functionalTerm.getFunctionSymbol();

            if ((functionSymbol instanceof DBTypeConversionFunctionSymbol)
                    && ((DBTypeConversionFunctionSymbol) functionSymbol).isSimple()
                    && ((DBTypeConversionFunctionSymbol) functionSymbol).getInputType()
                    .map(DBTermType::getCategory)
                    .filter(c -> c == DATE || c == DATETIME)
                    .isPresent())
                return super.buildTermAfterEvaluation(ImmutableList.of(functionalTerm.getTerm(0)), termFactory, variableNullability);
        }
        return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }
}
