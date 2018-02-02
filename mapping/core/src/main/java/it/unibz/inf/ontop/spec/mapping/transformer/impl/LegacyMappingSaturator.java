package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.model.IriConstants;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.datalog.impl.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.datalog.LinearInclusionDependencies;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.mapping.TMappingExclusionConfig;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingSaturator;
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
    private final Datalog2QueryMappingConverter datalog2MappingConverter;
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final TMappingProcessor tMappingProcessor;
    private final DatalogFactory datalogFactory;
    private final UnifierUtilities unifierUtilities;
    private final SubstitutionUtilities substitutionUtilities;

    @Inject
    private LegacyMappingSaturator(TMappingExclusionConfig tMappingExclusionConfig,
                                   Mapping2DatalogConverter mapping2DatalogConverter,
                                   Datalog2QueryMappingConverter datalog2MappingConverter,
                                   AtomFactory atomFactory, TermFactory termFactory,
                                   TMappingProcessor tMappingProcessor, DatalogFactory datalogFactory,
                                   UnifierUtilities unifierUtilities, SubstitutionUtilities substitutionUtilities) {
        this.tMappingExclusionConfig = tMappingExclusionConfig;
        this.mapping2DatalogConverter = mapping2DatalogConverter;
        this.datalog2MappingConverter = datalog2MappingConverter;
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.tMappingProcessor = tMappingProcessor;
        this.datalogFactory = datalogFactory;
        this.unifierUtilities = unifierUtilities;
        this.substitutionUtilities = substitutionUtilities;
    }

    @Override
    public Mapping saturate(Mapping mapping, DBMetadata dbMetadata, ClassifiedTBox saturatedTBox) {

        LinearInclusionDependencies foreignKeyRules = new LinearInclusionDependencies(dbMetadata.generateFKRules(), datalogFactory);
        CQContainmentCheckUnderLIDs foreignKeyCQC = new CQContainmentCheckUnderLIDs(foreignKeyRules, datalogFactory,
                unifierUtilities, substitutionUtilities, termFactory);

        ImmutableList<CQIE> initialMappingRules = mapping2DatalogConverter.convert(mapping)
                .collect(ImmutableCollectors.toList());

        ImmutableSet<CQIE> saturatedMappingRules = ImmutableSet.copyOf(
                tMappingProcessor.getTMappings(initialMappingRules, saturatedTBox, foreignKeyCQC, tMappingExclusionConfig));

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
    private ImmutableList<CQIE> generateTripleMappings(ImmutableSet<CQIE> saturatedRules) {
        return saturatedRules.stream()
                .filter(r -> !r.getHead().getFunctionSymbol().getName().startsWith(datalogFactory.getSubqueryPredicatePrefix()))
                .map(this::generateTripleMapping)
                .collect(ImmutableCollectors.toList());
    }

    private CQIE generateTripleMapping(CQIE rule) {
        Function newhead;
        Function currenthead = rule.getHead();
        if (currenthead.getArity() == 1) {
            // head is Class(x) Forming head as triple(x,uri(rdf:type), uri(Class))
            Function rdfTypeConstant = termFactory.getUriTemplate(termFactory.getConstantLiteral(IriConstants.RDF_TYPE));

            String classname = currenthead.getFunctionSymbol().getName();
            Term classConstant = termFactory.getUriTemplate(termFactory.getConstantLiteral(classname));

            newhead = atomFactory.getTripleAtom(currenthead.getTerm(0), rdfTypeConstant, classConstant);
        }
        else if (currenthead.getArity() == 2) {
            // head is Property(x,y) Forming head as triple(x,uri(Property), y)
            String propname = currenthead.getFunctionSymbol().getName();
            Function propConstant = termFactory.getUriTemplate(termFactory.getConstantLiteral(propname));

            newhead = atomFactory.getTripleAtom(currenthead.getTerm(0), propConstant, currenthead.getTerm(1));
        }
        else {
            // head is triple(x,uri(Property),y)
            newhead = (Function) currenthead.clone();
        }
        return datalogFactory.getCQIE(newhead, rule.getBody());
    }
}
