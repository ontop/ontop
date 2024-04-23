package it.unibz.inf.ontop.model.term.functionsymbol.impl.raster;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public class RasterSmallArraySpatialFunctionSymbolImpl extends AbstractRasterFunctionSymbolImpl{

    public RasterSmallArraySpatialFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype xsdIntegerDatatype, RDFDatatype xsdStringDatatype) {
        super("RAS_CLIP_SMALL_ARRAY_SPATIAL", functionIRI, ImmutableList.of(xsdIntegerDatatype, xsdStringDatatype),
                xsdStringDatatype);
    }

    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        //TODO
        // return termFactory.getRESPECTIVEDBFUNCTIONSymbols;  --------------------------------[STEP 01b]-----------------------------------
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();
        return termFactory.getRasterSmallArraySpatial(subLexicalTerms.get(0), subLexicalTerms.get(1));

    }

}
