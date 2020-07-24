package it.unibz.inf.ontop.answering.logging.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.GroundTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * TODO: move it away from the query logging package if also used for other purposes
 */
@Singleton
public class QueryTemplateExtractor {

    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected QueryTemplateExtractor(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
        iqFactory = coreSingletons.getIQFactory();
    }

    Optional<QueryTemplateExtraction> extract(IQ iq) {

        IQTree initialIQTree = iq.getTree();
        QueryTemplateTransformer transformer = new QueryTemplateTransformer(coreSingletons,
                initialIQTree.getKnownVariables());

        IQTree newTree = transformer.transform(initialIQTree);
        ImmutableMap<GroundTerm, Variable> parameterMap = transformer.getParameterMap();

        if (parameterMap.isEmpty())
            return Optional.empty();

        return Optional.of(new QueryTemplateExtraction(
                iqFactory.createIQ(iq.getProjectionAtom(), newTree),
                parameterMap));
    }


    /**
     * Specific to an IQ
     *
     * TODO: consider FILTER and CONSTRUCT ?
     *
     */
    protected static class QueryTemplateTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final CoreSingletons coreSingletons;
        private final VariableGenerator variableGenerator;

        // Mutable
        private final Map<GroundTerm, Variable> parameterMap;
        private final AtomFactory atomFactory;

        protected QueryTemplateTransformer(CoreSingletons coreSingletons, ImmutableSet<Variable> knownVariables) {
            super(coreSingletons);
            this.coreSingletons = coreSingletons;
            atomFactory = coreSingletons.getAtomFactory();
            this.variableGenerator = coreSingletons.getCoreUtilsFactory()
                    .createVariableGenerator(knownVariables);
            this.parameterMap = Maps.newHashMap();
        }

        public ImmutableMap<GroundTerm, Variable> getParameterMap() {
            return ImmutableMap.copyOf(parameterMap);
        }

        @Override
        public IQTree transformIntensionalData(IntensionalDataNode dataNode) {
            DataAtom<AtomPredicate> atom = dataNode.getProjectionAtom();

            if (atom.getPredicate() instanceof RDFAtomPredicate) {
                RDFAtomPredicate rdfAtomPredicate = (RDFAtomPredicate) atom.getPredicate();

                ImmutableList<? extends VariableOrGroundTerm> arguments = atom.getArguments();

                ImmutableList<Integer> indexes = rdfAtomPredicate.getClassIRI(arguments)
                        .map(iri -> ImmutableList.of(0))
                        .orElseGet(() -> ImmutableList.of(0, 2));

                ImmutableMap<Integer, GroundTerm> groundTermIndex = indexes.stream()
                        .flatMap(i -> Optional.of(arguments.get(i))
                                .filter(ImmutableTerm::isGround)
                                .map(t -> (GroundTerm) t)
                                .map(t -> Maps.immutableEntry(i, t))
                                .map(Stream::of)
                                .orElseGet(Stream::empty))
                        .collect(ImmutableCollectors.toMap());

                if (groundTermIndex.isEmpty())
                    return dataNode;

                // Adds new ground terms to the parameter map
                groundTermIndex.values()
                        .forEach(t -> parameterMap.computeIfAbsent(t, g -> variableGenerator.generateNewVariable()));

                ImmutableList<VariableOrGroundTerm> newArguments = IntStream.range(0, arguments.size())
                        .boxed()
                        .map(i -> Optional.ofNullable(groundTermIndex.get(i))
                                .map(parameterMap::get)
                                .map(t -> (VariableOrGroundTerm)t)
                                .orElseGet(() -> arguments.get(i)))
                        .collect(ImmutableCollectors.toList());
                DataAtom<AtomPredicate> newAtom = atomFactory.getDataAtom(atom.getPredicate(), newArguments);

                return iqFactory.createIntensionalDataNode(newAtom);
            }

            return dataNode;
        }
    }



    public static class QueryTemplateExtraction {

        private final IQ iq;
        private final ImmutableMap<GroundTerm, Variable> parameterMap;

        public QueryTemplateExtraction(IQ iq, ImmutableMap<GroundTerm, Variable> parameterMap) {
            this.iq = iq;
            this.parameterMap = parameterMap;
        }

        public IQ getIq() {
            return iq;
        }

        public ImmutableMap<GroundTerm, Variable> getParameterMap() {
            return parameterMap;
        }
    }
}
