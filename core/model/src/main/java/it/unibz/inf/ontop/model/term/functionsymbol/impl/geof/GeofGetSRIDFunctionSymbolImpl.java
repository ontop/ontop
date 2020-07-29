package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.SPARQLFunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.GEOF;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.Optional;

public class GeofGetSRIDFunctionSymbolImpl extends AbstractGeofIRIFunctionSymbolImpl {//AbstractGeofIRIFunctionSymbolImpl {

    public static final String defSRID = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";

    public GeofGetSRIDFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, RDFDatatype xsdAnyUri) {
        super("GEOF_GETSRID", functionIRI, ImmutableList.of(wktLiteralType), xsdAnyUri);
    }

    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {

        ImmutableTerm term = subLexicalTerms.get(0);

        if (term instanceof NonGroundFunctionalTerm) {
            NonGroundFunctionalTerm f = (NonGroundFunctionalTerm) term;
            FunctionSymbol fs = f.getFunctionSymbol();
            if (fs instanceof DBConcatFunctionSymbol) {
                // TEMPLATE FOUND!
                // DBConcatFunctionSymbol concat = (DBConcatFunctionSymbol) fs;
                if (f.getTerm(0) instanceof DBConstant) {
                    DBConstant t = (DBConstant) f.getTerm(0);
                    String tt = t.getValue();
                    if (tt.startsWith("<") && tt.indexOf(">") > 0) {
                        String srid = tt.substring(1, tt.indexOf(">"));
                        return termFactory.getDBStringConstant(srid);
                    }
                }
            }
        }

        return termFactory.getDBStringConstant(defSRID);
    }
}
