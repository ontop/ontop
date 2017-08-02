package it.unibz.inf.ontop.iq.tools.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import it.unibz.inf.ontop.iq.executor.ProposalExecutor;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.iq.proposal.ProposalResults;
import it.unibz.inf.ontop.iq.proposal.QueryOptimizationProposal;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;

import java.util.Optional;

public class StandardExecutorRegistry implements ExecutorRegistry {

    private final ImmutableMap<Class<? extends QueryOptimizationProposal>, Class<? extends ProposalExecutor>> classMap;
    private final Injector injector;

    public StandardExecutorRegistry(Injector injector,
                                    ImmutableMap<Class<? extends QueryOptimizationProposal>, Class<? extends ProposalExecutor>> classMap) {
        this.classMap = classMap;
        this.injector = injector;
    }


    @Override
    public <R extends ProposalResults, P extends QueryOptimizationProposal<R>> ProposalExecutor<P, R> getExecutor(
            P proposal) {

        Optional<Class<? extends ProposalExecutor>> optionalExecutorClass =
                getProposalExecutorInterface(proposal.getClass());

        if (optionalExecutorClass.isPresent()) {
            return injector.getInstance(optionalExecutorClass.get());
        }
        else {
            throw new InvalidOntopConfigurationException("No executor found for a proposal of the type " + proposal.getClass());
        }
    }

    private Optional<Class<? extends ProposalExecutor>> getProposalExecutorInterface(
            Class<? extends QueryOptimizationProposal> proposalClass) {

        Class<?>[] proposalClassHierarchy = proposalClass.getInterfaces();

        for (Class<?> klass : proposalClassHierarchy) {
            Optional<Class<? extends ProposalExecutor>> optionalExecutorClass
                    = Optional.ofNullable(classMap.get(klass));

            if (optionalExecutorClass.isPresent()) {
                return optionalExecutorClass;
            }
        }
        return Optional.empty();
    }
}
