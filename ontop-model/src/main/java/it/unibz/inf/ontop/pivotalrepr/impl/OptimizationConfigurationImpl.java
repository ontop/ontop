package it.unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.executor.InternalProposalExecutor;
import it.unibz.inf.ontop.pivotalrepr.OptimizationConfiguration;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryOptimizationProposal;

import java.util.Optional;


public class OptimizationConfigurationImpl implements OptimizationConfiguration {

    private final ImmutableMap<Class<? extends QueryOptimizationProposal>, Class<? extends InternalProposalExecutor>> map;

    public OptimizationConfigurationImpl(
            ImmutableMap<Class<? extends QueryOptimizationProposal>, Class<? extends InternalProposalExecutor>> map) {
        this.map = map;
    }

    @Override
    public Optional<Class<? extends InternalProposalExecutor>> getProposalExecutorInterface(
            Class<? extends QueryOptimizationProposal> proposalClass) {

        Class<?>[] proposalClassHierarchy = proposalClass.getInterfaces();

        for (Class<?> klass : proposalClassHierarchy) {
            Optional<Class<? extends InternalProposalExecutor>> optionalExecutorClass
                    = Optional.ofNullable(map.get(klass));

            if (optionalExecutorClass.isPresent()) {
                return optionalExecutorClass;
            }
        }
        return Optional.empty();
    }
}
