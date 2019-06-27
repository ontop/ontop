package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.impl.DatalogProgram2QueryConverterImpl;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.transformer.ABoxFactIntoMappingConverter;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.impl.GroundTermTools.castIntoGroundTerm;
import static it.unibz.inf.ontop.model.term.impl.GroundTermTools.isGroundTerm;


public class LegacyABoxFactIntoMappingConverter implements ABoxFactIntoMappingConverter {


    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyABoxFactIntoMappingConverter.class);

    private final SpecificationFactory mappingFactory;
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final ImmutabilityTools immutabilityTools;
    private final IntermediateQueryFactory iqFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final CoreUtilsFactory coreUtilsFactory;
    private final SubstitutionFactory substitutionFactory;


    @Inject
    public LegacyABoxFactIntoMappingConverter(SpecificationFactory mappingFactory, AtomFactory atomFactory,
                                              TermFactory termFactory,
                                              ImmutabilityTools immutabilityTools, IntermediateQueryFactory iqFactory,
                                              UnionBasedQueryMerger queryMerger, CoreUtilsFactory coreUtilsFactory, SubstitutionFactory substitutionFactory) {
        this.mappingFactory = mappingFactory;
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.immutabilityTools = immutabilityTools;
        this.iqFactory = iqFactory;
        this.queryMerger = queryMerger;
        this.coreUtilsFactory = coreUtilsFactory;
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public Mapping convert(OntologyABox ontology, boolean isOntologyAnnotationQueryingEnabled,
                           UriTemplateMatcher uriTemplateMatcher) {

        ImmutableList<IQ> classes = ontology.getClassAssertions().stream()
                .map(ca -> convertFact(
                        getTerm(ca.getIndividual(), uriTemplateMatcher),
                        getIRI(RDF.TYPE),
                        getIRI(ca.getConcept().getIRI())))
                .collect(ImmutableCollectors.toList());

        ImmutableList<IQ> properties = Stream.concat(Stream.concat(
                ontology.getObjectPropertyAssertions().stream()
                        .map(pa -> convertFact(
                                getTerm(pa.getSubject(), uriTemplateMatcher),
                                getIRI(pa.getProperty().getIRI()),
                                getTerm(pa.getObject(), uriTemplateMatcher))),

                ontology.getDataPropertyAssertions().stream()
                        .map(da -> convertFact(
                                getTerm(da.getSubject(), uriTemplateMatcher),
                                getIRI(da.getProperty().getIRI()),
                                getValueConstant(da.getValue())))),

                isOntologyAnnotationQueryingEnabled
                        ? ontology.getAnnotationAssertions().stream()
                        .map(aa -> convertFact(
                                getTerm(aa.getSubject(), uriTemplateMatcher),
                                getIRI(aa.getProperty().getIRI()),
                                (aa.getValue() instanceof ValueConstant)
                                        ? getValueConstant((ValueConstant) aa.getValue())
                                        : getTerm((ObjectConstant) aa.getValue(), uriTemplateMatcher)))
                        : Stream.of())
                .collect(ImmutableCollectors.toList());

        LOGGER.debug("Appended {} object property assertions as fact rules", ontology.getObjectPropertyAssertions().size());
        LOGGER.debug("Appended {} data property assertions as fact rules", ontology.getDataPropertyAssertions().size());
        LOGGER.debug("Appended {} annotation assertions as fact rules", ontology.getAnnotationAssertions().size());
        LOGGER.debug("Appended {} class assertions from ontology as fact rules", ontology.getClassAssertions().size());

        ImmutableTable<RDFAtomPredicate, IRI, IQ> classTable = table(classes);
        ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyTable = table(properties);

        if (!classTable.isEmpty())
            System.out.println("CLASS TABLE " + classTable);

        Mapping a = mappingFactory.createMapping(
                mappingFactory.createMetadata(
                        //TODO: parse the ontology prefixes ??
                        mappingFactory.createPrefixManager(ImmutableMap.of()),
                        uriTemplateMatcher),
                propertyTable,
                classTable);

        return a;
    }

    private ImmutableTable<RDFAtomPredicate, IRI, IQ> table(ImmutableList<IQ> iqs) {

        ImmutableMultimap<IRI, IQ> heads = iqs.stream()
                .collect(ImmutableCollectors.toMultimap(iq -> MappingTools.extractRDFPredicate(iq).getIri(), iq -> iq));

        return heads.asMap().entrySet().stream()
                .map(e -> queryMerger.mergeDefinitions(e.getValue()).get()
                        .liftBinding())
                .map(iq -> Tables.immutableCell(
                        (RDFAtomPredicate) iq.getProjectionAtom().getPredicate(),
                        MappingTools.extractRDFPredicate(iq).getIri(),
                        iq))
                .collect(ImmutableCollectors.toTable());
    }


    public IQ convertFact(Term subject, Term property, Term object)
            throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException {

        Function head = atomFactory.getMutableTripleAtom(subject, property, object);

        Predicate datalogAtomPredicate = head.getFunctionSymbol();
        AtomPredicate atomPredicate = (AtomPredicate) datalogAtomPredicate;

        ImmutableList.Builder<Variable> argListBuilder = ImmutableList.builder();
        ImmutableMap.Builder<Variable, ImmutableTerm> bindingBuilder = ImmutableMap.builder();

        VariableGenerator projectedVariableGenerator = coreUtilsFactory.createVariableGenerator(ImmutableSet.of());
        for (Term term : head.getTerms()) {
            Variable newArgument;

            /*
             * If a projected variable occurs multiple times as an head argument,
             * rename it and keep track of the equivalence.
             *
             */
            if (term instanceof Variable) {
                Variable originalVariable = (Variable) term;
                newArgument = projectedVariableGenerator.generateNewVariableIfConflicting(originalVariable);
                if (!newArgument.equals(originalVariable)) {
                    bindingBuilder.put(newArgument, originalVariable);
                }
            }
            /*
             * Ground-term: replace by a variable and add a binding.
             * (easier to merge than putting the ground term in the data atom).
             */
            else if (isGroundTerm(term)) {
                Variable newVariable = projectedVariableGenerator.generateNewVariable();
                newArgument = newVariable;
                bindingBuilder.put(newVariable, castIntoGroundTerm(term));
            }
            /*
             * Non-ground functional term
             */
            else {
                ImmutableTerm nonVariableTerm = immutabilityTools.convertIntoImmutableTerm(term);
                Variable newVariable = projectedVariableGenerator.generateNewVariable();
                newArgument = newVariable;
                bindingBuilder.put(newVariable, nonVariableTerm);
            }
            argListBuilder.add(newArgument);
        }

        DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(atomPredicate, argListBuilder.build());
        ImmutableSubstitution<ImmutableTerm> substitution = substitutionFactory.getSubstitution(bindingBuilder.build());

        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(projectionAtom.getVariables(),
                substitution);

        IQTree constructionTree = iqFactory.createUnaryIQTree(topConstructionNode, iqFactory.createTrueNode());
        return iqFactory.createIQ(projectionAtom, constructionTree);
    }


    // BNODES are not supported here
    private Term getTerm(ObjectConstant o, UriTemplateMatcher uriTemplateMatcher) {
        IRIConstant iri = (IRIConstant) o;
        return immutabilityTools.convertToMutableFunction(uriTemplateMatcher.generateURIFunction(iri.getIRI().getIRIString()));
    }

    private Term getIRI(IRI iri) {
        return termFactory.getUriTemplate(termFactory.getConstantLiteral(iri.getIRIString()));
    }

    private Term getValueConstant(ValueConstant o) {
        return o.getType().getLanguageTag()
                .map(lang ->
                        termFactory.getTypedTerm(termFactory.getConstantLiteral(o.getValue()), lang.getFullString()))
                .orElseGet(() ->
                        termFactory.getTypedTerm(o, o.getType()));
    }


}
