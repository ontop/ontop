package it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.utils.IDGenerator;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

public class MappingSameAs {

    /* add the inverse of the same as present in the mapping */

    public static Collection<OBDAMappingAxiom> addSameAsInverse(Collection<OBDAMappingAxiom> mappings,
                                                                NativeQueryLanguageComponentFactory nativeQLFactory) {
        final ImmutableList<OBDAMappingAxiom> newMappingsForInverseSameAs = mappings.stream()
                // the targets are already split. We have only one target atom
                .filter(map -> map.getTargetQuery().get(0).getFunctionSymbol().getName().equals(OBDAVocabulary.SAME_AS))
                .map(map -> {
                    Function target = map.getTargetQuery().get(0);
                    String newId = IDGenerator.getNextUniqueID(map.getId() + "#");
                    Function inverseAtom = DATA_FACTORY.getFunction(target.getFunctionSymbol(), target.getTerm(1), target.getTerm(0));
                    return nativeQLFactory.create(newId, map.getSourceQuery(), ImmutableList.of(inverseAtom));
                })
                .collect(ImmutableCollectors.toList());

        mappings.addAll(newMappingsForInverseSameAs);

        return mappings;
    }
}
