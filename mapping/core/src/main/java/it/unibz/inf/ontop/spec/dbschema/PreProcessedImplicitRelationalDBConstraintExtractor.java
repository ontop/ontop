package it.unibz.inf.ontop.spec.dbschema;

import it.unibz.inf.ontop.exception.ImplicitDBContraintException;

import javax.annotation.Nonnull;
import java.io.File;

public interface PreProcessedImplicitRelationalDBConstraintExtractor {

    PreProcessedImplicitRelationalDBConstraintSet extract(@Nonnull File constraintFile)
            throws ImplicitDBContraintException;
}
