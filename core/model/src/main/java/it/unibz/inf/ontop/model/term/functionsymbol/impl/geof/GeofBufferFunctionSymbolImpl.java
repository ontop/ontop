package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.vocabulary.UOM;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public class GeofBufferFunctionSymbolImpl extends AbstractGeofWKTFunctionSymbolImpl {
    public GeofBufferFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, RDFDatatype decimalType, ObjectRDFType iriType) {
        super("GEOF_BUFFER", functionIRI, ImmutableList.of(wktLiteralType, decimalType, iriType), wktLiteralType);
    }

    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        String unit = ((DBConstant) subLexicalTerms.get(2)).getValue();
        // TODO: change the implementation according to the SRID. Now it assumes that the the SRID of the input geometry and the unit match.
        if (UOM.METRE.getIRIString().equals(unit)) {
            return termFactory.getDBAsText(termFactory.getDBBuffer(subLexicalTerms.get(0), subLexicalTerms.get(1)));
        } else if (UOM.METRE.getIRIString().equals(unit)) {
            return termFactory.getDBAsText(termFactory.getDBBuffer(subLexicalTerms.get(0), subLexicalTerms.get(1)));
        } else {
            throw new IllegalArgumentException("Unexpected unit: " + unit);
        }
    }
}
