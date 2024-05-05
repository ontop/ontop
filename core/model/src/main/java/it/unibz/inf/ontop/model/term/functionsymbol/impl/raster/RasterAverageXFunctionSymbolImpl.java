package it.unibz.inf.ontop.model.term.functionsymbol.impl.raster;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public class RasterAverageXFunctionSymbolImpl extends AbstractRasterFunctionSymbolImpl{

    public RasterAverageXFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype xsdIntegerDatatype, RDFDatatype wktLiteralType, RDFDatatype xsdDoubleType, RDFDatatype xsdStringDatatype) {
        super("RAS_SPATIAL_AVERAGE_X", functionIRI, ImmutableList.of(xsdIntegerDatatype, wktLiteralType, xsdDoubleType, xsdDoubleType, xsdDoubleType, xsdDoubleType, xsdStringDatatype),
                xsdIntegerDatatype);
    }

    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        //TODO
        // return termFactory.getRESPECTIVEDBFUNCTIONSymbols;  --------------------------------[STEP 01b]-----------------------------------
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();

        return termFactory.getRasterSpatialAverageX(subLexicalTerms.get(0), subLexicalTerms.get(1), subLexicalTerms.get(2), subLexicalTerms.get(3), subLexicalTerms.get(4), subLexicalTerms.get(5), subLexicalTerms.get(6));
    }


//    public RasterAverageXFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype xsdIntegerDatatype, RDFDatatype wktLiteralType, RDFDatatype xsdDoubleType, RDFDatatype xsdStringDatatype) {
//        super("RAS_SPATIAL_AVERAGE_X", functionIRI, ImmutableList.of(xsdIntegerDatatype, wktLiteralType, xsdStringDatatype, xsdStringDatatype, xsdStringDatatype, xsdStringDatatype, xsdStringDatatype),
//                xsdIntegerDatatype);
//    }
//
//    @Override
//    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
//        //TODO
//        // return termFactory.getRESPECTIVEDBFUNCTIONSymbols;  --------------------------------[STEP 01b]-----------------------------------
//        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();
//
//        return termFactory.getRasterSpatialAverageX(subLexicalTerms.get(0), subLexicalTerms.get(1), subLexicalTerms.get(2), subLexicalTerms.get(2), subLexicalTerms.get(2), subLexicalTerms.get(2), subLexicalTerms.get(2));
//
//    }

}
