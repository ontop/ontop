package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.DatalogProgram;

import java.util.List;

/**
 * Extracted automatically from the implementation.
 * TODO: explain
 */
public interface QueryVocabularyValidator {
    boolean validatePredicates(DatalogProgram input);

    /*
* Substitute atoms based on the equivalence map.
*/
    DatalogProgram replaceEquivalences(DatalogProgram queries);

    CQIE replaceEquivalences(CQIE query, boolean inplace);

    void replaceEquivalences(List body);
}
