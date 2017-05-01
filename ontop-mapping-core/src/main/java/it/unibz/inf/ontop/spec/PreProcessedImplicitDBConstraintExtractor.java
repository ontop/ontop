package it.unibz.inf.ontop.spec;

import it.unibz.inf.ontop.exception.ImplicitDBContraintException;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * TODO: explain
 */
public interface PreProcessedImplicitDBConstraintExtractor {

    PreProcessedImplicitDBContraintSet extract(@Nonnull File constraintFile) throws ImplicitDBContraintException;
}
