package it.unibz.inf.ontop.spec.rule.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.moandjiezana.toml.Toml;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInvalidKGQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;
import it.unibz.inf.ontop.exception.SparqlRuleException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.query.KGQueryFactory;
import it.unibz.inf.ontop.query.translation.KGQueryTranslator;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.rule.RuleExtractor;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class RuleExtractorImpl implements RuleExtractor {

    private static final String RULES_KEY = "rules";
    private final KGQueryFactory kgQueryFactory;
    private final KGQueryTranslator kgQueryTranslator;
    private final IntensionalNodeExtractor intensionalNodeExtractor;

    @Inject
    protected RuleExtractorImpl(KGQueryFactory kgQueryFactory, KGQueryTranslator kgQueryTranslator,
                                IntensionalNodeExtractor intensionalNodeExtractor) {
        this.kgQueryFactory = kgQueryFactory;
        this.kgQueryTranslator = kgQueryTranslator;
        this.intensionalNodeExtractor = intensionalNodeExtractor;
    }

    @Override
    public ImmutableList<IQ> extract(OBDASpecInput specInput) throws SparqlRuleException {
        try {
            Optional<Reader> reader = specInput.getSparqlRulesReader();
            if (!reader.isPresent())
                return ImmutableList.of();

            return order(parse(reader.get()));

        } catch (FileNotFoundException e) {
            throw new SparqlRuleException(e);
        }
    }

    protected ImmutableSet<IQ> parse(Reader sparqlRulesReader) throws SparqlRuleException {
        Toml toml;
        try {
            toml = new Toml().read(sparqlRulesReader);
        }
        // The TOML library throws IllegalStateException or re-throws IOException-s as RuntimeException-s.
        catch (RuntimeException e) {
            throw new SparqlRuleException(e);
        }
        if (!toml.contains(RULES_KEY))
            throw new SparqlRuleException("Invalid SPARQL rules: was expecting the key \"rules\"");

        List<String> ruleStrings;
        try {
            ruleStrings = toml.getList(RULES_KEY);
        }
        // Reverse-engineering the implementation of the library
        catch (ClassCastException e) {
            throw new SparqlRuleException("Invalid SPARQL rules: Was expecting a list of strings for the key \"rules\"");
        }

        ImmutableSet.Builder<IQ> ruleSetBuilder = ImmutableSet.builder();

        for(String ruleString : ruleStrings) {
            ruleSetBuilder.addAll(parseSparqlRule(ruleString));
        }
        return ruleSetBuilder.build();
    }

    private ImmutableSet<IQ> parseSparqlRule(String ruleString) throws SparqlRuleException {
        try {
            return kgQueryFactory.createInsertQuery(ruleString)
                    .translate(kgQueryTranslator);
        } catch (OntopInvalidKGQueryException | OntopUnsupportedKGQueryException e) {
            throw new SparqlRuleException(e);
        }
    }

    protected ImmutableList<IQ> order(ImmutableSet<IQ> rules) throws SparqlRuleException {
        if (rules.size() <= 1)
            return ImmutableList.copyOf(rules);

        ImmutableMultimap<IQ, IQ> directDependencyMultimap = extractDirectDependencyMultimap(rules);
        if (directDependencyMultimap.isEmpty())
            return ImmutableList.copyOf(rules);

        Map<IQ, Set<IQ>> saturatedDependencyMap = saturate(directDependencyMultimap);
        checkForCycles(saturatedDependencyMap);

        ImmutableMap<IQ, Integer> dependencyCount = rules.stream()
                .collect(ImmutableCollectors.toMap(
                        r -> r,
                        r -> saturatedDependencyMap.getOrDefault(r, ImmutableSet.of()).size()));

        return rules.stream().sorted(Comparator.comparing(dependencyCount::get))
                .collect(ImmutableCollectors.toList());
    }

    private ImmutableMultimap<IQ, IQ> extractDirectDependencyMultimap(ImmutableSet<IQ> rules) throws SparqlRuleException {
        ImmutableMultimap<IRI, IQ> definitions = extractedIndexByOutputPredicate(rules);

        ImmutableMultimap.Builder<IQ, IQ> multimapBuilder = ImmutableMultimap.builder();
        for (IQ rule : rules) {
            for (IRI iri : extractDependencyPredicates(rule)) {
                for (IQ dependency: definitions.get(iri))
                    multimapBuilder.put(rule, dependency);
            }
        }
        return multimapBuilder.build();
    }

    private ImmutableMultimap<IRI, IQ> extractedIndexByOutputPredicate(ImmutableSet<IQ> rules) throws SparqlRuleException {
        ImmutableMultimap.Builder<IRI, IQ> multimapBuilder = ImmutableMultimap.builder();
        for (IQ rule : rules) {
            // Slightly abusing the notion of mapping assertion, for not re-implementing the same logic for extracting the IRI
            try {
                IRI iri = new MappingAssertion(rule, null).getIndex().getIri();
                multimapBuilder.put(iri, rule);
            } catch (MappingAssertion.NoGroundPredicateOntopInternalBugException e) {
                throw new SparqlRuleException("Unsupported rule: must use a constant class or a constant non-rdf:type " +
                        "property in the INSERT clause.\n" + rule);
            }
        }
        return multimapBuilder.build();
    }

    private ImmutableSet<IRI> extractDependencyPredicates(IQ rule) throws SparqlRuleException {
        ImmutableList<DataAtom<AtomPredicate>> dependencyAtoms = rule.getTree().acceptVisitor(intensionalNodeExtractor)
                .map(IntensionalDataNode::getProjectionAtom)
                .collect(ImmutableCollectors.toList());

        ImmutableSet.Builder<IRI> predicateBuilder = ImmutableSet.builder();
        for (DataAtom<AtomPredicate> atom : dependencyAtoms) {
            predicateBuilder.add(extractPredicate(atom, rule));
        }
        return predicateBuilder.build();
    }

    private IRI extractPredicate(DataAtom<AtomPredicate> atom, IQ rule) throws SparqlRuleException {
        AtomPredicate predicate = atom.getPredicate();
        if (predicate instanceof RDFAtomPredicate)
            return ((RDFAtomPredicate) predicate).getPredicateIRI(atom.getArguments())
                    .orElseThrow(() -> new SparqlRuleException("All the triple/quad patterns must have a constant class " +
                            "or a constant non-rdf:type property.\n" +
                            "Unsupported rule: " + rule));
        throw new MinorOntopInternalBugException("Non RDFAtomPredicate found for an IQ rule");
    }

    private Map<IQ, Set<IQ>> saturate(ImmutableMultimap<IQ, IQ> directDependencyMultimap) {
        Map<IQ, Set<IQ>> map = directDependencyMultimap.asMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Sets.newHashSet(e.getValue())));
        boolean hasNotConverged = true;
        while (hasNotConverged) {
            hasNotConverged = false;

            for (Map.Entry<IQ, Set<IQ>> entry : map.entrySet()) {
                Set<IQ> mutableDependencySet = entry.getValue();
                ImmutableSet<IQ> currentDependencies = ImmutableSet.copyOf(entry.getValue());
                for (IQ dependency : currentDependencies) {
                    Set<IQ> inheritedDependencies = map.get(dependency);
                    if (inheritedDependencies != null) {
                        boolean hasChanged = mutableDependencySet.addAll(inheritedDependencies);
                        hasNotConverged = hasNotConverged || hasChanged;
                    }
                }
            }
        }
        return map;
    }

    private void checkForCycles(Map<IQ, Set<IQ>> saturatedDependencyMap) throws SparqlRuleException {
        ImmutableSet<String> rulesInCycles = saturatedDependencyMap.entrySet().stream()
                .filter(e -> e.getValue().contains(e.getKey()))
                .map(Map.Entry::getKey)
                .map(Object::toString)
                .collect(ImmutableCollectors.toSet());

        if (!rulesInCycles.isEmpty())
            throw new SparqlRuleException("Dependency cycles detected for the following rules: \n" +
                    String.join("\n", rulesInCycles));
    }

}
