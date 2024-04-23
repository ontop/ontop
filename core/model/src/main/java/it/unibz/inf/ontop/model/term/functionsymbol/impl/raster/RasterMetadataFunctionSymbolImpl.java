package it.unibz.inf.ontop.model.term.functionsymbol.impl.raster;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public class RasterMetadataFunctionSymbolImpl extends AbstractRasterFunctionSymbolImpl {

//    protected final FunctionSymbolFactory functionSymbolFactory;

    public RasterMetadataFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype xsdIntegerDatatype, RDFDatatype xsdStringDatatype) {
        super("RAS_GET_META", functionIRI, ImmutableList.of(xsdIntegerDatatype, xsdStringDatatype),
                xsdStringDatatype);
        //this.functionSymbolFactory = functionSymbolFactory;
    }

    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        //TODO
        // Created new class for the newly added function name
        // return termFactory.getRESPECTIVEDBFUNCTIONSymbols; --------------------------------[STEP 01b]-----------------------------------
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();

        return termFactory.getRasterMetadata(subLexicalTerms.get(0), subLexicalTerms.get(1)); // return rasterId and rasterName
//        return null;

    }
}