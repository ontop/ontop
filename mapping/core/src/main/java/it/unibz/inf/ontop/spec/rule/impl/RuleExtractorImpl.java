package it.unibz.inf.ontop.spec.rule.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.moandjiezana.toml.Toml;
import it.unibz.inf.ontop.exception.OntopInvalidKGQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;
import it.unibz.inf.ontop.exception.SparqlRuleException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.query.KGQueryFactory;
import it.unibz.inf.ontop.query.translation.KGQueryTranslator;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.rule.RuleExtractor;

import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.List;
import java.util.Optional;

@Singleton
public class RuleExtractorImpl implements RuleExtractor {

    private static final String RULES_KEY = "rules";
    private final KGQueryFactory kgQueryFactory;
    private final KGQueryTranslator kgQueryTranslator;

    @Inject
    protected RuleExtractorImpl(KGQueryFactory kgQueryFactory, KGQueryTranslator kgQueryTranslator) {
        this.kgQueryFactory = kgQueryFactory;
        this.kgQueryTranslator = kgQueryTranslator;
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

        throw new RuntimeException("TODO: implement ordering rules");
    }

}
