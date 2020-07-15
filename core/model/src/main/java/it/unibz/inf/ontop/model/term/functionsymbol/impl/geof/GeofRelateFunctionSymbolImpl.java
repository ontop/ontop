package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;


public class GeofRelateFunctionSymbolImpl extends AbstractGeofAnyTypeFunctionSymbolImpl {

    public GeofRelateFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, RDFDatatype xsdStringType) {
        super("GEOF_RELATEM", functionIRI, ImmutableList.of(wktLiteralType, wktLiteralType), xsdStringType);
    }

    public GeofRelateFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, RDFDatatype xsdStringType, RDFDatatype xsdBooleanType) {
        super("GEOF_RELATE", functionIRI, ImmutableList.of(wktLiteralType, wktLiteralType, xsdStringType), xsdBooleanType);
    }


    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        if (subLexicalTerms.size() == 2) {
        return termFactory.getDBRelateMatrix(subLexicalTerms.get(0), subLexicalTerms.get(1));
        } else {
        return termFactory.getDBRelate(subLexicalTerms.get(0), subLexicalTerms.get(1), subLexicalTerms.get(2));
        }
    }
}
