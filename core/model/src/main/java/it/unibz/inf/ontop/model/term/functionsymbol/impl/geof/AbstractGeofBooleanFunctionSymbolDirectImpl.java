package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.IRI;

import java.util.function.BiFunction;

// direct implementation by translating to a corresponding DB function
public abstract class AbstractGeofBooleanFunctionSymbolDirectImpl extends AbstractGeofBooleanFunctionSymbolImpl {

    protected AbstractGeofBooleanFunctionSymbolDirectImpl(String functionSymbolName, IRI functionIRI, ImmutableList<TermType> inputTypes, RDFDatatype xsdBooleanType) {
        super(functionSymbolName, functionIRI, inputTypes, xsdBooleanType);
    }

    @Override
    protected ImmutableTerm computeDBBooleanTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        WKTLiteralValue v0 = GeoUtils.extractWKTLiteralValue(termFactory, subLexicalTerms.get(0));
        WKTLiteralValue v1 = GeoUtils.extractWKTLiteralValue(termFactory, subLexicalTerms.get(1));

        DBTermType geometryType = termFactory.getTypeFactory().getDBTypeFactory().getDBGeometryType();

        ImmutableTerm input0 = removeTextCast(v0.getGeometry(), geometryType, termFactory);
        ImmutableTerm input1 = removeTextCast(v1.getGeometry(), geometryType, termFactory);

        return getDBFunction(termFactory).apply(input0, input1).simplify();
    }

    abstract public BiFunction<ImmutableTerm, ImmutableTerm, ImmutableTerm> getDBFunction(TermFactory termFactory) ;
}
