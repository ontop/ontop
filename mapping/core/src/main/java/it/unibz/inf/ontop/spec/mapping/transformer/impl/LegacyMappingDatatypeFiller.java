package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.exception.UnknownDatatypeException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.ProvenanceMappingFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.iq.transform.impl.LazyRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.type.impl.TermTypeInferenceTools;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingDatatypeFiller;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractMap;
import java.util.stream.Stream;

/**
 * Legacy code to infer datatypes not declared in the targets of mapping assertions.
 * Types are inferred from the DB metadata.
 * TODO: rewrite in a Datalog independent fashion
 */
public class LegacyMappingDatatypeFiller implements MappingDatatypeFiller {


    private final OntopMappingSettings settings;
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final TermTypeInferenceTools termTypeInferenceTools;
    private final QueryUnionSplitter unionSplitter;
    private final UnionFlattener unionNormalizer;
    private final IntermediateQueryFactory iqFactory;
    private final ProvenanceMappingFactory provMappingFactory;
    private final NoNullValueEnforcer noNullValueEnforcer;
    private final SubstitutionFactory substitutionFactory;
    private final AtomFactory atomFactory;

    @Inject
    private LegacyMappingDatatypeFiller(OntopMappingSettings settings,
                                        TermFactory termFactory, TypeFactory typeFactory,
                                        TermTypeInferenceTools termTypeInferenceTools,
                                        QueryUnionSplitter unionSplitter,
                                        UnionFlattener unionNormalizer,
                                        IntermediateQueryFactory iqFactory,
                                        ProvenanceMappingFactory provMappingFactory,
                                        NoNullValueEnforcer noNullValueEnforcer,
                                        SubstitutionFactory substitutionFactory,
                                        AtomFactory atomFactory) {
        this.settings = settings;
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.termTypeInferenceTools = termTypeInferenceTools;
        this.unionSplitter = unionSplitter;
        this.unionNormalizer = unionNormalizer;
        this.iqFactory = iqFactory;
        this.provMappingFactory = provMappingFactory;
        this.noNullValueEnforcer = noNullValueEnforcer;
        this.substitutionFactory = substitutionFactory;
        this.atomFactory = atomFactory;
    }

    /***
     * Infers missing data types.
     * For each rule, gets the type from the rule head, and if absent, retrieves the type from the metadata.
     *
     * The behavior of type retrieval is the following for each rule:
     * . build a "termOccurrenceIndex", which is a map from variables to body atoms + position.
     * For ex, consider the rule: C(x, y) <- A(x, y) \wedge B(y)
     * The "termOccurrenceIndex" map is {
     *  x \mapsTo [<A(x, y), 1>],
     *  y \mapsTo [<A(x, y), 2>, <B(y), 1>]
     *  }
     *  . then take the first occurrence each variable (e.g. take <A(x, y), 2> for variable y),
     *  and assign to the variable the corresponding column type in the DB
     *  (e.g. for y, the type of column 1 of table A).
     *  . then inductively infer the types of functions (e.g. concat, ...) from the variable types.
     *  Only the outermost expression is assigned a type.
     *
     *  Assumptions:
     *  .rule body atoms are extensional
     *  .the corresponding column types are compatible (e.g the types for column 1 of A and column 1 of B)
     */

    @Override
    public MappingWithProvenance inferMissingDatatypes(MappingWithProvenance mapping) throws UnknownDatatypeException {
        MappingDataTypeCompletion typeCompletion = new MappingDataTypeCompletion(
                settings.isDefaultDatatypeInferred(), termFactory, typeFactory, termTypeInferenceTools);

        try {
            ImmutableMap<IQ, PPMappingAssertionProvenance> iqMap = mapping.getProvenanceMap().entrySet().stream()
                    .filter(e -> !e.getKey().getTree().isDeclaredAsEmpty())
                    .flatMap(e -> (MappingTools.extractRDFPredicate(e.getKey()).isClass()
                            ? Stream.of(e.getKey())
                            : inferMissingDatatypes(e.getKey(), typeCompletion))
                                .map(iq -> new AbstractMap.SimpleEntry<>(iq, e.getValue())))
                    .collect(ImmutableCollectors.toMap());

            return provMappingFactory.create(iqMap, mapping.getMetadata());
        }
        catch (MappingDataTypeCompletion.UnknownDatatypeRuntimeException e) {
            throw new UnknownDatatypeException(e.getMessage());
        }
    }

    private Stream<IQ> inferMissingDatatypes(IQ iq0, MappingDataTypeCompletion typeCompletion) {
        //case of data and object property
        return unionSplitter.splitUnion(unionNormalizer.optimize(iq0))
                .filter(iq -> !iq.getTree().isDeclaredAsEmpty())
                .map(iq -> {
                    DistinctVariableOnlyDataAtom pa = iq.getProjectionAtom();
                    ConstructionNode constructionNode = ((ConstructionNode)iq.getTree().getRootNode());
                    ImmutableSubstitution<ImmutableTerm> sub = constructionNode.getSubstitution();
                    ImmutableMultimap<Variable, Attribute> termOccurenceIndex = createIndex(iq.getTree());
                    ImmutableTerm object = sub.applyToVariable(pa.getTerm(2)); // third argument only
                    // Infer variable datatypes
                    ImmutableTerm object2p = typeCompletion.insertVariableDataTyping(object, termOccurenceIndex);
                    // Infer operation datatypes from variable datatypes
                    ImmutableTerm object2 = typeCompletion.insertOperationDatatyping(object2p);
                    Variable var2 = iq.getVariableGenerator().generateNewVariable();
                    DistinctVariableOnlyDataAtom pa2 = atomFactory.getDistinctVariableOnlyDataAtom(
                            pa.getPredicate(), pa.getTerm(0), pa.getTerm(1), var2);
                    ImmutableSubstitution<ImmutableTerm> sub2 = substitutionFactory.getSubstitution(
                            pa.getTerm(0), sub.applyToVariable(pa.getTerm(0)),
                            pa.getTerm(1), sub.applyToVariable(pa.getTerm(1)),
                            var2, object2);
                    return  iqFactory.createIQ(pa2, iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(pa2.getVariables(), sub2),
                            ((UnaryIQTree)iq.getTree()).getChild()));
                })
                .map(rule -> noNullValueEnforcer.transform(rule).liftBinding()).distinct();

    }


    private ImmutableMultimap<Variable, Attribute> createIndex(IQTree tree) {
        ImmutableMultimap.Builder<Variable, Attribute> termOccurenceIndex = ImmutableMultimap.builder();
        // ASSUMPTION: there are no CONSTRUCT nodes apart from the top level
        // TODO: a more light-weight version is needed (that does not change the tree)
        tree.acceptTransformer(new LazyRecursiveIQTreeVisitingTransformer(iqFactory) {
           @Override
           public IQTree transformExtensionalData(ExtensionalDataNode dataNode) {
               RelationDefinition td = dataNode.getProjectionAtom().getPredicate().getRelationDefinition();
               ImmutableList<? extends VariableOrGroundTerm> terms = dataNode.getProjectionAtom().getArguments();
               int i = 1; // position index
               for (VariableOrGroundTerm t : terms) {
                   if (t instanceof Variable) {
                       termOccurenceIndex.put((Variable) t, td.getAttribute(i));
                   }
                   i++; // increase the position index for the next variable
               }
               return dataNode;
           }
        });
        return termOccurenceIndex.build();
    }



}
