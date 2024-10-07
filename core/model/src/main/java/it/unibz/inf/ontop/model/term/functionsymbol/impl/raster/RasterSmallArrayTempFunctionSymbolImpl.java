package it.unibz.inf.ontop.model.term.functionsymbol.impl.raster;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public class RasterSmallArrayTempFunctionSymbolImpl extends AbstractRasterFunctionSymbolImpl{

    public RasterSmallArrayTempFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype xsdIntegerDatatype, RDFDatatype ignoredXsdIntegerDatatype, RDFDatatype xsdStringDatatype) {
        super("RAS_CLIP_SMALL_ARRAY_TEMPORAL", functionIRI, ImmutableList.of(xsdIntegerDatatype, xsdIntegerDatatype, xsdStringDatatype),
                xsdStringDatatype);
    }

    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        //TODO
        // return termFactory.getRESPECTIVEDBFUNCTIONSymbols;  --------------------------------[STEP 01b]-----------------------------------
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();

        return termFactory.getRasterSmallArrayTemp(subLexicalTerms.get(0), subLexicalTerms.get(1), subLexicalTerms.get(2));

    }

}

