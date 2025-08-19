package it.unibz.inf.ontop.model.term.functionsymbol.impl.raster;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public class RasterMaximumFunctionSymbolImpl extends AbstractRasterFunctionSymbolImpl{

    public RasterMaximumFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype xsdDatetime, RDFDatatype xsdStringDatatype, RDFDatatype wktLiteralType, RDFDatatype xsdDoubleDatatype) {
        super("RAS_SPATIAL_MAXIMUM", functionIRI, ImmutableList.of(xsdDatetime,  wktLiteralType, xsdStringDatatype),
                xsdDoubleDatatype);
    }

    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        //TODO
        // return termFactory.getRESPECTIVEDBFUNCTIONSymbols;  --------------------------------[STEP 01b]-----------------------------------
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();

        return termFactory.getRasterSpatialMaximum(subLexicalTerms.get(0), subLexicalTerms.get(1), subLexicalTerms.get(2));

    }

}
