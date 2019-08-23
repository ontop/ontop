package it.unibz.inf.ontop.spec.dbschema;

import it.unibz.inf.ontop.exception.ImplicitDBContraintException;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * TODO: explain
 */
public interface PreProcessedImplicitDBConstraintExtractor {

    PreProcessedImplicitDBConstraintSet extract(@Nonnull File constraintFile) throws ImplicitDBContraintException;
}
