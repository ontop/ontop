package it.unibz.inf.ontop.spec.trans.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.mapping.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.model.IriConstants;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.LinearInclusionDependencies;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingProcessor;
import it.unibz.inf.ontop.spec.impl.LegacyIsNotNullDatalogMappingFiller;
import it.unibz.inf.ontop.spec.trans.MappingSaturator;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
import java.util.LinkedList;
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
                Function rdfTypeConstant = TERM_FACTORY.getUriTemplate(TERM_FACTORY.getConstantLiteral(IriConstants.RDF_TYPE));

                String classname = currenthead.getFunctionSymbol().getName();
                Term classConstant = TERM_FACTORY.getUriTemplate(TERM_FACTORY.getConstantLiteral(classname));

                newhead = ATOM_FACTORY.getTripleAtom(currenthead.getTerm(0), rdfTypeConstant, classConstant);
            }
            else if (currenthead.getArity() == 2) {
				/*
				 * head is Property(x,y) Forming head as triple(x,uri(Property),
				 * y)
				 */
                String propname = currenthead.getFunctionSymbol().getName();
                Function propConstant = TERM_FACTORY.getUriTemplate(TERM_FACTORY.getConstantLiteral(propname));

                newhead = ATOM_FACTORY.getTripleAtom(currenthead.getTerm(0), propConstant, currenthead.getTerm(1));
            }
            else {
				/*
				 * head is triple(x,uri(Property),y)
				 */
                newhead = (Function) currenthead.clone();
            }
            CQIE newmapping = DATALOG_FACTORY.getCQIE(newhead, mapping.getBody());
            newmappings.add(newmapping);
        }
        return newmappings;
    }
}
