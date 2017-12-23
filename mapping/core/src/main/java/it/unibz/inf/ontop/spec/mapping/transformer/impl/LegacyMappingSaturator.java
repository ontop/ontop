package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.model.IriConstants;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.datalog.impl.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.datalog.LinearInclusionDependencies;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.mapping.TMappingExclusionConfig;
import it.unibz.inf.ontop.spec.impl.LegacyIsNotNullDatalogMappingFiller;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingSaturator;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
import java.util.List;

import static it.unibz.inf.ontop.model.OntopModelSingletons.ATOM_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.DATALOG_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;

/**
 * Uses the old Datalog-based mapping saturation code
 */
@Singleton
public class LegacyMappingSaturator implements MappingSaturator {

    private final TMappingExclusionConfig tMappingExclusionConfig;
    private final Mapping2DatalogConverter mapping2DatalogConverter;
    private final Datalog2QueryMappingConverter datalog2MappingConverter;

    @Inject
    private LegacyMappingSaturator(TMappingExclusionConfig tMappingExclusionConfig,
                                   Mapping2DatalogConverter mapping2DatalogConverter,
                                   Datalog2QueryMappingConverter datalog2MappingConverter) {
        this.tMappingExclusionConfig = tMappingExclusionConfig;
        this.mapping2DatalogConverter = mapping2DatalogConverter;
        this.datalog2MappingConverter = datalog2MappingConverter;
    }

    @Override
    public Mapping saturate(Mapping mapping, DBMetadata dbMetadata, ClassifiedTBox saturatedTBox) {

        ImmutableList<CQIE> initialMappingRules = mapping2DatalogConverter.convert(mapping)
                .map(r -> LegacyIsNotNullDatalogMappingFiller.addNotNull(r, dbMetadata))
                .collect(ImmutableCollectors.toList());

        LinearInclusionDependencies foreignKeyRules = new LinearInclusionDependencies(dbMetadata.generateFKRules());
        CQContainmentCheckUnderLIDs foreignKeyCQC = new CQContainmentCheckUnderLIDs(foreignKeyRules);
        ImmutableSet<CQIE> saturatedMappingRules = TMappingProcessor.getTMappings(initialMappingRules, saturatedTBox,
                foreignKeyCQC, tMappingExclusionConfig).stream()
                // NOT SURE WHY SECOND TIME IS NEEDED
                .map(r -> LegacyIsNotNullDatalogMappingFiller.addNotNull(r, dbMetadata))
                .collect(ImmutableCollectors.toSet());

        List<CQIE> allMappingRules = new ArrayList<>(saturatedMappingRules);
        allMappingRules.addAll(generateTripleMappings(saturatedMappingRules));

        return datalog2MappingConverter.convertMappingRules(ImmutableList.copyOf(allMappingRules),
                dbMetadata, mapping.getExecutorRegistry(), mapping.getMetadata());
    }

    @Override
    public Mapping saturate(Mapping mapping, DBMetadata dbMetadata) {

        ImmutableSet<CQIE> initialMappingRules = mapping2DatalogConverter.convert(mapping)
                .map(r -> LegacyIsNotNullDatalogMappingFiller.addNotNull(r, dbMetadata))
                .collect(ImmutableCollectors.toSet());

        List<CQIE> allMappingRules = new ArrayList<>(initialMappingRules);
        allMappingRules.addAll(generateTripleMappings(initialMappingRules));

        return datalog2MappingConverter.convertMappingRules(ImmutableList.copyOf(allMappingRules),
                dbMetadata, mapping.getExecutorRegistry(), mapping.getMetadata());
    }

    /***
     * Creates mappings with heads as "triple(x,y,z)" from mappings with binary
     * and unary atoms"
     *
     * TODO: clean it
     */
    private static ImmutableList<CQIE> generateTripleMappings(ImmutableSet<CQIE> saturatedRules) {
        return saturatedRules.stream()
                .filter(r -> !r.getHead().getFunctionSymbol().getName().startsWith(DATALOG_FACTORY.getSubqueryPredicatePrefix()))
                .map(r -> generateTripleMapping(r))
                .collect(ImmutableCollectors.toList());
    }

    private static CQIE generateTripleMapping(CQIE rule) {
        Function newhead;
        Function currenthead = rule.getHead();
        if (currenthead.getArity() == 1) {
                /*
                 * head is Class(x) Forming head as triple(x,uri(rdf:type),
				 * uri(Class))
				 */
            Function rdfTypeConstant = TERM_FACTORY.getUriTemplate(TERM_FACTORY.getConstantLiteral(IriConstants.RDF_TYPE));

            String classname = currenthead.getFunctionSymbol().getName();
            Term classConstant = TERM_FACTORY.getUriTemplate(TERM_FACTORY.getConstantLiteral(classname));

            newhead = ATOM_FACTORY.getTripleAtom(currenthead.getTerm(0), rdfTypeConstant, classConstant);
        } else if (currenthead.getArity() == 2) {
				/*
				 * head is Property(x,y) Forming head as triple(x,uri(Property),
				 * y)
				 */
            String propname = currenthead.getFunctionSymbol().getName();
            Function propConstant = TERM_FACTORY.getUriTemplate(TERM_FACTORY.getConstantLiteral(propname));

            newhead = ATOM_FACTORY.getTripleAtom(currenthead.getTerm(0), propConstant, currenthead.getTerm(1));
        } else {
				/*
				 * head is triple(x,uri(Property),y)
				 */
            newhead = (Function) currenthead.clone();
        }
        return DATALOG_FACTORY.getCQIE(newhead, rule.getBody());
    }
}
