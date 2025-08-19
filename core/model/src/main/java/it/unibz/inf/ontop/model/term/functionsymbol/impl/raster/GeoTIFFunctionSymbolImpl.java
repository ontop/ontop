package it.unibz.inf.ontop.model.term.functionsymbol.impl.raster;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public class GeoTIFFunctionSymbolImpl extends AbstractRasterFunctionSymbolImpl {

    public GeoTIFFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype xsdDatetime, RDFDatatype wktLiteralType, RDFDatatype xsdStringDatatype, RDFDatatype xsdDoubleType) {
        super("RAS_GEOTIFF", functionIRI, ImmutableList.of(xsdDatetime, wktLiteralType, xsdStringDatatype, xsdStringDatatype, xsdDoubleType), xsdStringDatatype);
    }

    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        //TODO
        // return termFactory.getRESPECTIVEDBFUNCTIONSymbols;  --------------------------------[STEP 01b]-----------------------------------
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();

        return termFactory.getGeoTIF(subLexicalTerms.get(0), subLexicalTerms.get(1), subLexicalTerms.get(2), subLexicalTerms.get(3), subLexicalTerms.get(4));

    }
}