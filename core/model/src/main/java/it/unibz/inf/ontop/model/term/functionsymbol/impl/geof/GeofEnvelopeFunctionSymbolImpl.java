package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBMathBinaryOperator;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.UOM;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class GeofEnvelopeFunctionSymbolImpl extends AbstractUnaryGeofWKTFunctionSymbolDirectImpl {
    public GeofEnvelopeFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType) {
        super("GEOF_ENVELOPE", functionIRI, ImmutableList.of(wktLiteralType), wktLiteralType);
    }

    @Override
    public Function<ImmutableTerm, ImmutableTerm> getDBFunction(TermFactory termFactory) {
        return termFactory::getDBEnvelope;
    }
}
