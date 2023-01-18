package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

public class GeofSymDifferenceFunctionSymbolImpl extends AbstractBinaryGeofWKTFunctionSymbolDirectImpl {
    public GeofSymDifferenceFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, ObjectRDFType iriType) {
        super("GEOF_SYMDIFFERENCE", functionIRI, ImmutableList.of(wktLiteralType, wktLiteralType), wktLiteralType);
    }

    @Override
    public BiFunction<ImmutableTerm, ImmutableTerm, ImmutableTerm> getDBFunction(TermFactory termFactory) {
        return termFactory::getDBSymDifference;
    }
}
