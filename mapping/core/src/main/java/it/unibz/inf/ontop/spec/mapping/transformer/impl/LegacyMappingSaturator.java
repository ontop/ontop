package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.constraints.LinearInclusionDependencies;
import it.unibz.inf.ontop.constraints.impl.ImmutableCQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.DatabaseRelationDefinition;
import it.unibz.inf.ontop.dbschema.ForeignKeyConstraint;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.spec.mapping.*;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingSaturator;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;

import java.util.List;

/**
 * Uses the old Datalog-based mapping saturation code
 */
@Singleton
public class LegacyMappingSaturator implements MappingSaturator {

    private final TMappingExclusionConfig tMappingExclusionConfig;
    private final TermFactory termFactory;
    private final TMappingProcessor tMappingProcessor;
    private final AtomFactory atomFactory;
    private final CoreUtilsFactory coreUtilsFactory;

    @Inject
    private LegacyMappingSaturator(TMappingExclusionConfig tMappingExclusionConfig,
                                   TermFactory termFactory,
                                   TMappingProcessor tMappingProcessor,
                                   AtomFactory atomFactory,
                                   CoreUtilsFactory coreUtilsFactory) {
        this.tMappingExclusionConfig = tMappingExclusionConfig;
        this.termFactory = termFactory;
        this.tMappingProcessor = tMappingProcessor;
        this.atomFactory = atomFactory;
        this.coreUtilsFactory = coreUtilsFactory;
    }

    @Override
    public ImmutableList<MappingAssertion> saturate(ImmutableList<MappingAssertion> mapping, DBMetadata dbMetadata, ClassifiedTBox saturatedTBox) {

        LinearInclusionDependencies.Builder<RelationPredicate> b = LinearInclusionDependencies.builder(coreUtilsFactory, atomFactory);

        dbMetadata.getDatabaseRelations().stream()
                .map(DatabaseRelationDefinition::getForeignKeys)
                .flatMap(List::stream)
                .forEach(fk -> getLinearInclusionDependency(b, fk));

        LinearInclusionDependencies<RelationPredicate> lids = b.build();

        ImmutableCQContainmentCheckUnderLIDs<RelationPredicate> cqContainmentCheck = new ImmutableCQContainmentCheckUnderLIDs(lids);

        return tMappingProcessor.getTMappings(mapping, saturatedTBox, tMappingExclusionConfig, cqContainmentCheck);
    }


    private void getLinearInclusionDependency(LinearInclusionDependencies.Builder<RelationPredicate> b, ForeignKeyConstraint fk) {

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

        DataAtom<RelationPredicate> head = atomFactory.getDataAtom(def2.getAtomPredicate(), ImmutableList.copyOf(terms2));
        DataAtom<RelationPredicate> body = atomFactory.getDataAtom(def1.getAtomPredicate(), ImmutableList.copyOf(terms1));

        b.add(head, body);
    }

}
