package it.unibz.inf.ontop.model.term.functionsymbol.impl.raster;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public class RasterAverageFunctionSymbolImpl extends AbstractRasterFunctionSymbolImpl {

//    protected final FunctionSymbolFactory functionSymbolFactory;

    public RasterAverageFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype xsdIntegerDatatype, RDFDatatype xsdDoubleType, RDFDatatype wktLiteralType, RDFDatatype xsdStringDatatype) {
        super("RAS_SPATIAL_AVERAGE", functionIRI, ImmutableList.of(xsdIntegerDatatype, xsdDoubleType, wktLiteralType, xsdStringDatatype),
                xsdDoubleType);
        //this.functionSymbolFactory = functionSymbolFactory;
    }

    /**
     * @param subLexicalTerms (geom1, geom2, unit)
     */
    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        //TODO
        // return termFactory.getRESPECTIVEDBFUNCTIONSymbols;  --------------------------------[STEP 04b]-----------------------------------
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();

        return termFactory.getRasterSpatialAverage(subLexicalTerms.get(0), subLexicalTerms.get(1), subLexicalTerms.get(2), subLexicalTerms.get(3));
//        return null;

    }

}