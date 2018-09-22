package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.datalog.impl.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.DatabaseRelationDefinition;
import it.unibz.inf.ontop.dbschema.ForeignKeyConstraint;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.TMappingExclusionConfig;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingSaturator;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import it.unibz.inf.ontop.substitution.impl.UnifierUtilities;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses the old Datalog-based mapping saturation code
 */
@Singleton
public class LegacyMappingSaturator implements MappingSaturator {

    private final TMappingExclusionConfig tMappingExclusionConfig;
    private final Mapping2DatalogConverter mapping2DatalogConverter;
    private final TermFactory termFactory;
    private final TMappingProcessor tMappingProcessor;
    private final DatalogFactory datalogFactory;
    private final UnifierUtilities unifierUtilities;
    private final SubstitutionUtilities substitutionUtilities;

    @Inject
    private LegacyMappingSaturator(TMappingExclusionConfig tMappingExclusionConfig,
                                   Mapping2DatalogConverter mapping2DatalogConverter,
                                   TermFactory termFactory,
                                   TMappingProcessor tMappingProcessor, DatalogFactory datalogFactory,
                                   UnifierUtilities unifierUtilities, SubstitutionUtilities substitutionUtilities) {
        this.tMappingExclusionConfig = tMappingExclusionConfig;
        this.mapping2DatalogConverter = mapping2DatalogConverter;
        this.termFactory = termFactory;
        this.tMappingProcessor = tMappingProcessor;
        this.datalogFactory = datalogFactory;
        this.unifierUtilities = unifierUtilities;
        this.substitutionUtilities = substitutionUtilities;
    }

    @Override
    public Mapping saturate(Mapping mapping, DBMetadata dbMetadata, ClassifiedTBox saturatedTBox) {

        ImmutableList<LinearInclusionDependency> foreignKeyRules =
                dbMetadata.getDatabaseRelations().stream()
                    .map(r -> r.getForeignKeys())
                    .flatMap(List::stream)
                    .map(fk -> getLinearInclusionDependency(fk))
                    .collect(ImmutableCollectors.toList());

        CQContainmentCheckUnderLIDs foreignKeyCQC = new CQContainmentCheckUnderLIDs(foreignKeyRules, datalogFactory,
                unifierUtilities, substitutionUtilities, termFactory);

        ImmutableList<CQIE> initialMappingRules = mapping2DatalogConverter.convert(mapping)
                .collect(ImmutableCollectors.toList());

        return tMappingProcessor.getTMappings(initialMappingRules, saturatedTBox, foreignKeyCQC, tMappingExclusionConfig, mapping.getMetadata());
    }


    private LinearInclusionDependency getLinearInclusionDependency(ForeignKeyConstraint fk) {
        DatabaseRelationDefinition def = fk.getRelation();
        DatabaseRelationDefinition def2 = fk.getReferencedRelation();

        // create variables for the current table
        int len1 = def.getAttributes().size();
        List<Term> terms1 = new ArrayList<>(len1);
        for (int i = 1; i <= len1; i++)
            terms1.add(termFactory.getVariable("t" + i));

        // create variables for the referenced table
        int len2 = def2.getAttributes().size();
        List<Term> terms2 = new ArrayList<>(len2);
        for (int i = 1; i <= len2; i++)
            terms2.add(termFactory.getVariable("p" + i));

        for (ForeignKeyConstraint.Component comp : fk.getComponents()) {
            // indexes start at 1
            int pos1 = comp.getAttribute().getIndex() - 1; // current column (1)
            int pos2 = comp.getReference().getIndex() - 1; // referenced column (2)

            terms1.set(pos1, terms2.get(pos2));
        }

        Function head = termFactory.getFunction(def2.getAtomPredicate(), terms2);
        Function body = termFactory.getFunction(def.getAtomPredicate(), terms1);

        return new LinearInclusionDependency(head, body);
    }

}
