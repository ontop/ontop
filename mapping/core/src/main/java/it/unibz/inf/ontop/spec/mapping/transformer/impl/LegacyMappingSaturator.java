package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.model.IriConstants;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.datalog.impl.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.datalog.LinearInclusionDependencies;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.spec.mapping.TMappingExclusionConfig;
import it.unibz.inf.ontop.spec.impl.LegacyIsNotNullDatalogMappingFiller;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingSaturator;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static it.unibz.inf.ontop.model.OntopModelSingletons.DATALOG_FACTORY;

/**
 * Uses the old Datalog-based mapping saturation code
 */
@Singleton
public class LegacyMappingSaturator implements MappingSaturator {

    private final TMappingExclusionConfig tMappingExclusionConfig;
    private final Mapping2DatalogConverter mapping2DatalogConverter;
    private final Datalog2QueryMappingConverter datalog2MappingConverter;
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final LegacyIsNotNullDatalogMappingFiller isNotNullDatalogMappingFiller;
    private final TMappingProcessor tMappingProcessor;

    @Inject
    private LegacyMappingSaturator(TMappingExclusionConfig tMappingExclusionConfig,
                                   Mapping2DatalogConverter mapping2DatalogConverter,
                                   Datalog2QueryMappingConverter datalog2MappingConverter,
                                   AtomFactory atomFactory, TermFactory termFactory,
                                   LegacyIsNotNullDatalogMappingFiller isNotNullDatalogMappingFiller,
                                   TMappingProcessor tMappingProcessor) {
        this.tMappingExclusionConfig = tMappingExclusionConfig;
        this.mapping2DatalogConverter = mapping2DatalogConverter;
        this.datalog2MappingConverter = datalog2MappingConverter;
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.isNotNullDatalogMappingFiller = isNotNullDatalogMappingFiller;
        this.tMappingProcessor = tMappingProcessor;
    }

    @Override
    public Mapping saturate(Mapping mapping, DBMetadata dbMetadata, TBoxReasoner saturatedTBox) {

        LinearInclusionDependencies foreignKeyRules = new LinearInclusionDependencies(dbMetadata.generateFKRules());
        CQContainmentCheckUnderLIDs foreignKeyCQC = new CQContainmentCheckUnderLIDs(foreignKeyRules);

        ImmutableList<CQIE> initialMappingRules = mapping2DatalogConverter.convert(mapping)
                .map(r -> isNotNullDatalogMappingFiller.addNotNull(r, dbMetadata))
                .collect(ImmutableCollectors.toList());

        ImmutableSet<CQIE> saturatedMappingRules = tMappingProcessor.getTMappings(initialMappingRules, saturatedTBox,
                true,
                foreignKeyCQC, tMappingExclusionConfig).stream()
                .map(r -> isNotNullDatalogMappingFiller.addNotNull(r, dbMetadata))
                .collect(ImmutableCollectors.toSet());

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
    private List<CQIE> generateTripleMappings(ImmutableSet<CQIE> saturatedRules) {
        List<CQIE> newmappings = new LinkedList<CQIE>();

        for (CQIE mapping : saturatedRules) {
            Function newhead;
            Function currenthead = mapping.getHead();
            if (currenthead.getArity() == 1) {
				/*
				 * head is Class(x) Forming head as triple(x,uri(rdf:type),
				 * uri(Class))
				 */
                Function rdfTypeConstant = termFactory.getUriTemplate(termFactory.getConstantLiteral(IriConstants.RDF_TYPE));

                String classname = currenthead.getFunctionSymbol().getName();
                Term classConstant = termFactory.getUriTemplate(termFactory.getConstantLiteral(classname));

                newhead = atomFactory.getTripleAtom(currenthead.getTerm(0), rdfTypeConstant, classConstant);
            }
            else if (currenthead.getArity() == 2) {
				/*
				 * head is Property(x,y) Forming head as triple(x,uri(Property),
				 * y)
				 */
                String propname = currenthead.getFunctionSymbol().getName();
                Function propConstant = termFactory.getUriTemplate(termFactory.getConstantLiteral(propname));

                newhead = atomFactory.getTripleAtom(currenthead.getTerm(0), propConstant, currenthead.getTerm(1));
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
