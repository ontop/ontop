package it.unibz.inf.ontop.model.term.functionsymbol.impl.raster;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBMathBinaryOperator;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.FunctionSymbolFactoryImpl;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.geof.AbstractGeofDoubleFunctionSymbolImpl;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.geof.DistanceUnit;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.geof.GeoUtils;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.geof.WKTLiteralValue;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class RasterAverageFunctionSymbolImpl extends AbstractRasterFunctionSymbolImpl {

    protected final FunctionSymbolFactory functionSymbolFactory;

    public RasterAverageFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype xsdStringDatatype, RDFDatatype wktLiteralType, RDFDatatype xsdIntegerDatatype, RDFDatatype xsdInputDoubleType, RDFDatatype xsdDoubleType, FunctionSymbolFactoryImpl functionSymbolFactory) {
        super("RASTER_AVERAGE", functionIRI,
                ImmutableList.of(xsdStringDatatype, wktLiteralType, xsdIntegerDatatype, xsdInputDoubleType),
                xsdDoubleType);
        this.functionSymbolFactory = functionSymbolFactory;
    }


    /**
     * @param subLexicalTerms (geom1, geom2, unit)
     */
    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        //TODO
//        return termFactory.getRESPECTIVEDBFUNCTIONSymbols;
        return null;

    }

}