package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;

public class LexicalInequalityFunctionSymbolImpl extends AbstractLexicalNonStrictEqOrInequalityFunctionSymbol {

    private final InequalityLabel inequalityLabel;

    protected LexicalInequalityFunctionSymbolImpl(InequalityLabel inequalityLabel, MetaRDFTermType metaRDFTermType, RDFDatatype xsdBooleanType,
                                                  RDFDatatype xsdDateTimeType, RDFDatatype xsdStringType, DBTermType dbStringType,
                                                  DBTermType dbBooleanType,
                                                  RDFDatatype xsdDateTimeStampType,
                                                  RDFDatatype xsdDate) {
        super("LEX_" + inequalityLabel, metaRDFTermType, xsdBooleanType, xsdDateTimeType, xsdStringType,
                dbStringType, dbBooleanType, xsdDateTimeStampType, xsdDate);
        this.inequalityLabel = inequalityLabel;
    }

    @Override
    protected ImmutableTerm computeNumericEqualityOrInequality(ImmutableTerm dbTerm1, ImmutableTerm dbTerm2,
                                                               TermFactory termFactory,
                                                               VariableNullability variableNullability) {
        return termFactory.getDBNumericInequality(inequalityLabel, dbTerm1, dbTerm2)
                .simplify(variableNullability);
    }

    @Override
    protected ImmutableTerm computeBooleanEqualityOrInequality(ImmutableTerm dbTerm1, ImmutableTerm dbTerm2,
                                                               TermFactory termFactory, VariableNullability variableNullability) {
        return termFactory.getDBBooleanInequality(inequalityLabel, dbTerm1, dbTerm2)
                .simplify(variableNullability);
    }

    @Override
    protected ImmutableTerm computeStringEqualityOrInequality(ImmutableTerm dbTerm1, ImmutableTerm dbTerm2,
                                                              TermFactory termFactory, VariableNullability variableNullability) {
        return termFactory.getDBStringInequality(inequalityLabel, dbTerm1, dbTerm2)
                .simplify(variableNullability);
    }

    @Override
    protected ImmutableTerm computeDatetimeEqualityOrInequality(ImmutableTerm dbTerm1, ImmutableTerm dbTerm2,
                                                                TermFactory termFactory, VariableNullability variableNullability) {
        return termFactory.getDBDatetimeInequality(inequalityLabel, dbTerm1, dbTerm2)
                .simplify(variableNullability);
    }

    @Override
    protected ImmutableTerm computeDateEqualityOrInequality(ImmutableTerm dbTerm1, ImmutableTerm dbTerm2,
                                                            TermFactory termFactory, VariableNullability variableNullability) {
        return termFactory.getDBDateInequality(inequalityLabel, dbTerm1, dbTerm2)
                .simplify(variableNullability);
    }

    @Override
    protected ImmutableTerm computeDefaultSameTypeEqualityOrInequality(RDFTermType termType, ImmutableTerm dbTerm1,
                                                                       ImmutableTerm dbTerm2, TermFactory termFactory,
                                                                       VariableNullability variableNullability) {
        return termFactory.getNullConstant();
    }

    @Override
    protected ImmutableTerm computeDefaultDifferentTypeEqualityOrInequality(RDFTermType termType1, RDFTermType termType2,
                                                                            TermFactory termFactory) {
        return termFactory.getNullConstant();
    }
}
