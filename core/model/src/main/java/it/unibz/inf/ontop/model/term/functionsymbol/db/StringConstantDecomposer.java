package it.unibz.inf.ontop.model.term.functionsymbol.db;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.DBConstant;

import java.util.Optional;

@FunctionalInterface
public interface StringConstantDecomposer {

    /**
     * Returns empty if the constant cannot be decomposed
     *  (because it typically does not match the expected pattern)
     */
    Optional<ImmutableList<DBConstant>> decompose(DBConstant stringConstant);
}
