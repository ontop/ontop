package it.unibz.inf.ontop.pivotalrepr.utils.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import it.unibz.inf.ontop.executor.InternalProposalExecutor;
import it.unibz.inf.ontop.injection.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.pivotalrepr.proposal.ProposalResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CachingExecutorRegistry implements ExecutorRegistry {

    /**
     * Mutable (append-only)
     */
    private final Map<Class<? extends QueryOptimizationProposal>, InternalProposalExecutor> executorCache;
    private final ImmutableMap<Class<? extends QueryOptimizationProposal>, Class<? extends InternalProposalExecutor>> classMap;
    private final Injector injector;

    public CachingExecutorRegistry(Injector injector,
                                   ImmutableMap<Class<? extends QueryOptimizationProposal>, Class<? extends InternalProposalExecutor>> classMap) {
        this.classMap = classMap;
        this.injector = injector;
        this.executorCache = new HashMap<>();
    }


    @Override
    public <R extends ProposalResults, P extends QueryOptimizationProposal<R>> InternalProposalExecutor<P, R> getExecutor(
            P proposal) {
        return Optional.ofNullable(executorCache.get(proposal.getClass()))
                .map(e -> (InternalProposalExecutor<P, R>) e)
                .orElseGet(() -> createExecutor(proposal));
    }

    private <P extends QueryOptimizationProposal<R>, R extends ProposalResults> InternalProposalExecutor<P, R> createExecutor(
            P proposal) {
        /**
         * It assumes that the concrete proposal classes DIRECTLY
         * implements a registered interface (extending QueryOptimizationProposal).
         */
        Class<? extends QueryOptimizationProposal> proposalClass = proposal.getClass();

        Optional<Class<? extends InternalProposalExecutor>> optionalExecutorClass =
                getProposalExecutorInterface(proposal.getClass());

        if (optionalExecutorClass.isPresent()) {
            InternalProposalExecutor<P, R> executor = injector.getInstance(optionalExecutorClass.get());
            if (executor.isThreadSafe())
                executorCache.put(proposalClass, executor);

            return executor;
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
