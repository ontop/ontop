package it.unibz.inf.ontop.owlrefplatform.core.basicoperations;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MappingVocabularyValidator extends VocabularyValidator {

    private final NativeQueryLanguageComponentFactory nativeQLFactory;

    public MappingVocabularyValidator(TBoxReasoner reasoner, ImmutableOntologyVocabulary voc,
                               NativeQueryLanguageComponentFactory nativeQLFactory) {
        super(reasoner, voc);
        this.nativeQLFactory = nativeQLFactory;
    }

    /***
     * Given a collection of mappings and an equivalence map for classes and
     * properties, it returns a new collection in which all references to
     * class/properties with equivalents has been removed and replaced by the
     * equivalents.
     *
     * For example, given the map hasFather -> inverse(hasChild)
     *
     * If there is a mapping:
     *
     * q(x,y):- hasFather(x,y) <- SELECT x, y FROM t
     *
     * This will be replaced by the mapping
     *
     * q(x,y):- hasChild(y,x) <- SELECT x, y FROM t
     *
     * The same is done for classes.
     *
     * @param originalMappings
     * @return
     */
    public ImmutableList<OBDAMappingAxiom> replaceEquivalences(Collection<OBDAMappingAxiom> originalMappings) {

        Collection<OBDAMappingAxiom> result = new ArrayList<OBDAMappingAxiom>(originalMappings.size());
        for (OBDAMappingAxiom mapping : originalMappings) {
            List<Function> targetQuery = mapping.getTargetQuery();
            List<Function> newTargetQuery = replaceEquivalences(targetQuery);
            result.add(nativeQLFactory.create(mapping.getId(), mapping.getSourceQuery(), newTargetQuery));
        }
        return ImmutableList.copyOf(result);
    }
}
