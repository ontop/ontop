package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

public class GeofEhEqualsFunctionSymbolImpl  extends AbstractGeofBooleanFunctionSymbolDirectImpl {

    public GeofEhEqualsFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, RDFDatatype xsdBooleanType) {
        super("GEOF_EH_EQUALS", functionIRI, ImmutableList.of(wktLiteralType, wktLiteralType), xsdBooleanType);
    }

    @Override
    public BiFunction<ImmutableTerm, ImmutableTerm, ImmutableTerm> getDBFunction(TermFactory termFactory) {
        return termFactory::getDBSTEquals;
    }
}
