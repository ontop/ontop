package it.unibz.inf.ontop.pivotalrepr.utils.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import it.unibz.inf.ontop.executor.InternalProposalExecutor;
import it.unibz.inf.ontop.injection.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.pivotalrepr.proposal.ProposalResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;

import java.util.Optional;

public class StandardExecutorRegistry implements ExecutorRegistry {

    private final ImmutableMap<Class<? extends QueryOptimizationProposal>, Class<? extends InternalProposalExecutor>> classMap;
    private final Injector injector;

    public StandardExecutorRegistry(Injector injector,
                                    ImmutableMap<Class<? extends QueryOptimizationProposal>, Class<? extends InternalProposalExecutor>> classMap) {
        this.classMap = classMap;
        this.injector = injector;
    }


    @Override
    public <R extends ProposalResults, P extends QueryOptimizationProposal<R>> InternalProposalExecutor<P, R> getExecutor(
            P proposal) {

        Optional<Class<? extends InternalProposalExecutor>> optionalExecutorClass =
                getProposalExecutorInterface(proposal.getClass());

        if (optionalExecutorClass.isPresent()) {
            return injector.getInstance(optionalExecutorClass.get());
        }
        else {
            throw new InvalidOntopConfigurationException("No executor found for a proposal of the type " + proposal.getClass());
        }
    }

    private Optional<Class<? extends InternalProposalExecutor>> getProposalExecutorInterface(
            Class<? extends QueryOptimizationProposal> proposalClass) {

        Class<?>[] proposalClassHierarchy = proposalClass.getInterfaces();

        for (Class<?> klass : proposalClassHierarchy) {
            Optional<Class<? extends InternalProposalExecutor>> optionalExecutorClass
                    = Optional.ofNullable(classMap.get(klass));

            if (optionalExecutorClass.isPresent()) {
                return optionalExecutorClass;
            }
        }
        return Optional.empty();
    }
}
