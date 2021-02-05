package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

import javax.annotation.Nonnull;

import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.GT;

public class GreaterThanSPARQLFunctionSymbolImpl extends AbstractBinaryComparisonSPARQLFunctionSymbol {

    private final RDFDatatype xsdBooleanType;
    private final DBTermType dbBooleanType;

    protected GreaterThanSPARQLFunctionSymbolImpl(@Nonnull RDFTermType rdfRootType, RDFDatatype xsdBooleanType,
                                                  DBTermType dbBooleanType) {
        super("SP_GT", SPARQL.GREATER_THAN, rdfRootType, xsdBooleanType);
        this.xsdBooleanType = xsdBooleanType;
        this.dbBooleanType = dbBooleanType;
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        ImmutableExpression expression = termFactory.getLexicalInequality(GT, subLexicalTerms.get(0), typeTerms.get(0),
                subLexicalTerms.get(1), typeTerms.get(1));
        return termFactory.getConversion2RDFLexical(dbBooleanType, expression, xsdBooleanType);
    }
}
