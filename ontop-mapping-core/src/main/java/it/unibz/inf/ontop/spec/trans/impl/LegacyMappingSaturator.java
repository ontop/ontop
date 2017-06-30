package it.unibz.inf.ontop.spec.trans.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.spec.impl.LegacyIsNotNullDatalogMappingFiller;
import it.unibz.inf.ontop.spec.trans.MappingSaturator;
import it.unibz.inf.ontop.mapping.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.mapping.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.model.impl.TermUtils;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.LinearInclusionDependencies;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingProcessor;
import it.unibz.inf.ontop.sql.DatabaseRelationDefinition;
import it.unibz.inf.ontop.sql.Relation2DatalogPredicate;
import it.unibz.inf.ontop.sql.RelationID;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

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
    public Mapping saturate(Mapping mapping, DBMetadata dbMetadata, TBoxReasoner saturatedTBox) {

        LinearInclusionDependencies foreignKeyRules = new LinearInclusionDependencies(dbMetadata.generateFKRules());
        CQContainmentCheckUnderLIDs foreignKeyCQC = new CQContainmentCheckUnderLIDs(foreignKeyRules);

        ImmutableList<CQIE> initialMappingRules = mapping2DatalogConverter.convert(mapping)
                .map(r -> LegacyIsNotNullDatalogMappingFiller.addNotNull(r, dbMetadata))
                .collect(ImmutableCollectors.toList());

        ImmutableList<CQIE> saturatedMappingRules = TMappingProcessor.getTMappings(initialMappingRules, saturatedTBox, true,
                foreignKeyCQC, tMappingExclusionConfig).stream()
                .map(r -> LegacyIsNotNullDatalogMappingFiller.addNotNull(r, dbMetadata))
                .collect(ImmutableCollectors.toList());

        List<CQIE> allMappingRules = new ArrayList<>(saturatedMappingRules);
        allMappingRules.addAll(generateTripleMappings(saturatedMappingRules));

        return datalog2MappingConverter.convertMappingRules(ImmutableList.copyOf(allMappingRules),
                dbMetadata, mapping.getExecutorRegistry(), mapping.getMetadata());
    }

    /***
     * Creates mappings with heads as "triple(x,y,z)" from mappings with binary
     * and unary atoms"
     *
     * TODO: clean it
     */
    private static List<CQIE> generateTripleMappings(List<CQIE> saturatedRules) {
        List<CQIE> newmappings = new LinkedList<CQIE>();

        for (CQIE mapping : saturatedRules) {
            Function newhead = null;
            Function currenthead = mapping.getHead();
            if (currenthead.getArity() == 1) {
				/*
				 * head is Class(x) Forming head as triple(x,uri(rdf:type),
				 * uri(Class))
				 */
                Function rdfTypeConstant = DATA_FACTORY.getUriTemplate(DATA_FACTORY.getConstantLiteral(OBDAVocabulary.RDF_TYPE));

                String classname = currenthead.getFunctionSymbol().getName();
                Term classConstant = DATA_FACTORY.getUriTemplate(DATA_FACTORY.getConstantLiteral(classname));

                newhead = DATA_FACTORY.getTripleAtom(currenthead.getTerm(0), rdfTypeConstant, classConstant);
            }
            else if (currenthead.getArity() == 2) {
				/*
				 * head is Property(x,y) Forming head as triple(x,uri(Property),
				 * y)
				 */
                String propname = currenthead.getFunctionSymbol().getName();
                Function propConstant = DATA_FACTORY.getUriTemplate(DATA_FACTORY.getConstantLiteral(propname));

                newhead = DATA_FACTORY.getTripleAtom(currenthead.getTerm(0), propConstant, currenthead.getTerm(1));
            }
            else {
				/*
				 * head is triple(x,uri(Property),y)
				 */
                newhead = (Function) currenthead.clone();
            }
            CQIE newmapping = DATA_FACTORY.getCQIE(newhead, mapping.getBody());
            newmappings.add(newmapping);
        }
        return newmappings;
    }
}
