package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public class GeofGetSRIDFunctionSymbolImpl extends AbstractGeofIRIFunctionSymbolImpl {//AbstractGeofIRIFunctionSymbolImpl {

    // Default if no SRID found is defSRID, otherwise EPSG_URI + respective EPSG + ">"
    public static final String defSRID = "<http://www.opengis.net/def/crs/OGC/1.3/CRS84>";
    public static final String EPSG_URI = "<http://www.opengis.net/def/crs/EPSG/0/";

    public GeofGetSRIDFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, RDFDatatype xsdAnyUri) {
        super("GEOF_GETSRID", functionIRI, ImmutableList.of(wktLiteralType), xsdAnyUri);
    }

    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {

        // NOTE: Check if SRID is 0, given that ST_SRID will yield 0
        // NOTE2: Check does not work
        String srid_int = ((DBConstant) subLexicalTerms.get(2)).getValue();

        if (srid_int.equals("0")) {
            ImmutableTerm srid_imm = termFactory.getDBAsText(termFactory.getDBGetSRID(subLexicalTerms.get(0)));
            DBConstant srid_str = termFactory.getDBStringConstant(String.valueOf(srid_imm));
            // Construct the SRID as per given format
            return termFactory.getDBStringConstant(EPSG_URI + srid_str.getValue()+">");
        } else {
            // Return the default SRID "<http://www.opengis.net/def/crs/OGC/1.3/CRS84>"
            return termFactory.getDBStringConstant(defSRID);
        }
    }
}