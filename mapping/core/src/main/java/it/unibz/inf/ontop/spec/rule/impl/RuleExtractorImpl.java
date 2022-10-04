package it.unibz.inf.ontop.spec.rule.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.rule.RuleExtractor;

@Singleton
public class RuleExtractorImpl implements RuleExtractor {

    @Inject
    protected RuleExtractorImpl() {
    }

    @Override
    public ImmutableList<IQ> extract(OBDASpecInput specInput) throws OBDASpecificationException {
        // TODO: implement it seriously!
        return ImmutableList.of();
    }
}
