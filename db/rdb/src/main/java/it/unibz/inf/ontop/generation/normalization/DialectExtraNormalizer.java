package it.unibz.inf.ontop.generation.normalization;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

/**
 * Extra normalization for compensating limitation of the SQL dialect
 */
public interface DialectExtraNormalizer {

    IQTree transform(IQTree tree, VariableGenerator variableGenerator);
}
