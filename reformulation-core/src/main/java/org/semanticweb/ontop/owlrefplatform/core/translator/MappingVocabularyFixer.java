package org.semanticweb.ontop.owlrefplatform.core.translator;

        import org.semanticweb.ontop.model.OBDAModel;
        import org.semanticweb.ontop.model.Predicate;

        import java.util.Set;

/**
 * Fixes the OBDA model
 */
public interface MappingVocabularyFixer {
    OBDAModel fixOBDAModel(OBDAModel model, Set<Predicate> vocabulary);
}
