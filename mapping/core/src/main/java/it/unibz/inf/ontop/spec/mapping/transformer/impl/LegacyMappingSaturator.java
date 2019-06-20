package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.constraints.LinearInclusionDependencies;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.datalog.impl.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.DatabaseRelationDefinition;
import it.unibz.inf.ontop.dbschema.ForeignKeyConstraint;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.TMappingExclusionConfig;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingSaturator;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import it.unibz.inf.ontop.substitution.impl.UnifierUtilities;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.List;

/**
 * Uses the old Datalog-based mapping saturation code
 */
@Singleton
public class LegacyMappingSaturator implements MappingSaturator {

    private final TMappingExclusionConfig tMappingExclusionConfig;
    private final TermFactory termFactory;
    private final TMappingProcessor tMappingProcessor;
    private final DatalogFactory datalogFactory;
    private final SubstitutionFactory substitutionFactory;
    private final AtomFactory atomFactory;
    private final ImmutabilityTools immutabilityTools;
    private final CoreUtilsFactory coreUtilsFactory;
    private final ImmutableUnificationTools immutableUnificationTools;

    @Inject
    private LegacyMappingSaturator(TMappingExclusionConfig tMappingExclusionConfig,
                                   TermFactory termFactory,
                                   TMappingProcessor tMappingProcessor, DatalogFactory datalogFactory,
                                   SubstitutionFactory substitutionFactory, AtomFactory atomFactory,
                                   ImmutabilityTools immutabilityTools, CoreUtilsFactory coreUtilsFactory,
                                   ImmutableUnificationTools immutableUnificationTools) {
        this.tMappingExclusionConfig = tMappingExclusionConfig;
        this.termFactory = termFactory;
        this.tMappingProcessor = tMappingProcessor;
        this.datalogFactory = datalogFactory;
        this.substitutionFactory = substitutionFactory;
        this.atomFactory = atomFactory;
        this.immutabilityTools = immutabilityTools;
        this.coreUtilsFactory = coreUtilsFactory;
        this.immutableUnificationTools = immutableUnificationTools;
    }

    @Override
    public Mapping saturate(Mapping mapping, DBMetadata dbMetadata, ClassifiedTBox saturatedTBox) {

        LinearInclusionDependencies.Builder<AtomPredicate> b = LinearInclusionDependencies.builder(coreUtilsFactory, atomFactory);

        dbMetadata.getDatabaseRelations().stream()
                .map(DatabaseRelationDefinition::getForeignKeys)
                .flatMap(List::stream)
                .forEach(fk -> getLinearInclusionDependency(b, fk));

        CQContainmentCheckUnderLIDs foreignKeyCQC = new CQContainmentCheckUnderLIDs(b.build(), datalogFactory,
                atomFactory, termFactory, immutabilityTools);

        return tMappingProcessor.getTMappings(mapping, saturatedTBox, foreignKeyCQC, tMappingExclusionConfig);
    }


    private void getLinearInclusionDependency(LinearInclusionDependencies.Builder<AtomPredicate> b, ForeignKeyConstraint fk) {

        DatabaseRelationDefinition def1 = fk.getRelation();
        VariableOrGroundTerm[] terms1 = new VariableOrGroundTerm[def1.getAttributes().size()];
        for (int i = 0; i < terms1.length; i++)
            terms1[i] = termFactory.getVariable("t" + i);

        DatabaseRelationDefinition def2 = fk.getReferencedRelation();
        VariableOrGroundTerm[] terms2 = new VariableOrGroundTerm[def2.getAttributes().size()];
        for (int i = 0; i < terms2.length; i++)
            terms2[i] = termFactory.getVariable("p" + i);

        for (ForeignKeyConstraint.Component comp : fk.getComponents())
            terms1[comp.getAttribute().getIndex() - 1] = terms2[comp.getReference().getIndex() - 1]; // indexes start at 1

        DataAtom<AtomPredicate> head = atomFactory.getDataAtom(def2.getAtomPredicate(), ImmutableList.copyOf(terms2));
        DataAtom<AtomPredicate> body = atomFactory.getDataAtom(def1.getAtomPredicate(), ImmutableList.copyOf(terms1));

        b.add(head, body);
    }

}
