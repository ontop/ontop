package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.IRI;

import java.util.function.BiFunction;

// direct implementation by translating to a corresponding DB function
public abstract class AbstractGeofBooleanFunctionSymbolDirectImpl extends AbstractGeofBooleanFunctionSymbolImpl{

    protected AbstractGeofBooleanFunctionSymbolDirectImpl(String functionSymbolName, IRI functionIRI, ImmutableList<TermType> inputTypes, RDFDatatype xsdBooleanType) {
        super(functionSymbolName, functionIRI, inputTypes, xsdBooleanType);
    }


    @Override
    protected ImmutableTerm computeDBBooleanTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        WKTLiteralValue v0 = GeoUtils.extractWKTLiteralValue(termFactory, subLexicalTerms.get(0));
        WKTLiteralValue v1 = GeoUtils.extractWKTLiteralValue(termFactory, subLexicalTerms.get(1));

        if (!v0.getSRID().equals(v1.getSRID())) {
            throw new IllegalArgumentException(String.format("SRIDs do not match: %s and %s", v0.getSRID(), v1.getSRID()));
        }

        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();

        // For inconsistent datatypes, cast everything to geography for ST_INTERSECTS and geometry for the rest
        if (!consistenGeoDataTypes(v0, v1)) {
            if (this.getOfficialName() != "http://www.opengis.net/def/function/geosparql/sfIntersects") {
                return (v0.getGeometry().inferType().isPresent() &&
                        v0.getGeometry().inferType().get().getTermType().get().toString() == "GEOGRAPHY")
                    ? getDBFunction(termFactory).apply(
                            termFactory.getDBCastFunctionalTerm(dbTypeFactory.getDBGeometryType() ,v0.getGeometry()),
                            v1.getGeometry().simplify())
                    : getDBFunction(termFactory).apply(
                        v0.getGeometry(),
                        termFactory.getDBCastFunctionalTerm(dbTypeFactory.getDBGeometryType() ,v1.getGeometry())
                                .simplify());
            }
            if (this.getOfficialName() == "http://www.opengis.net/def/function/geosparql/sfIntersects") {
                return (v0.getGeometry().inferType().isPresent() &&
                        v0.getGeometry().inferType().get().getTermType().get().toString() == "GEOMETRY")
                        ? getDBFunction(termFactory).apply(
                        termFactory.getDBCastFunctionalTerm(dbTypeFactory.getDBGeographyType() ,v0.getGeometry()),
                        v1.getGeometry().simplify())
                        : getDBFunction(termFactory).apply(
                        v0.getGeometry(),
                        termFactory.getDBCastFunctionalTerm(dbTypeFactory.getDBGeographyType() ,v1.getGeometry())
                                .simplify());
            }
        }

        return getDBFunction(termFactory).apply(v0.getGeometry(), v1.getGeometry()).simplify();
    }

    abstract public BiFunction<ImmutableTerm, ImmutableTerm, ImmutableTerm> getDBFunction(TermFactory termFactory) ;

    /**
     * Check if there is a geography / geometry mix in the function
     */
    protected boolean consistenGeoDataTypes (WKTLiteralValue v0, WKTLiteralValue v1) {
        boolean v0_castGeom = v0.getGeometry().inferType().isPresent() &&
                v0.getGeometry().inferType().get().getTermType().get().toString() == "GEOGRAPHY";
        boolean v1_castGeom = v0.getGeometry().inferType().isPresent() &&
                v0.getGeometry().inferType().get().getTermType().get().toString() == "GEOGRAPHY";

        return (!(v0_castGeom ^ !v1_castGeom));
    }
}
