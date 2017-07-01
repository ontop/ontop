package it.unibz.inf.ontop.owlrefplatform.core.basicoperations;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.model.predicate.AtomPredicate;
import it.unibz.inf.ontop.model.predicate.Predicate;
import it.unibz.inf.ontop.model.term.Function;

import static it.unibz.inf.ontop.model.OntopModelSingletons.DATALOG_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.DATA_FACTORY;

public class LinearInclusionDependencies {
    
	private final Map<Predicate, List<CQIE>> rules;

	public LinearInclusionDependencies() {
		rules = new HashMap<>();
	}

	public LinearInclusionDependencies(ImmutableMultimap<AtomPredicate, CQIE> predicateRuleMap) {
		rules = predicateRuleMap.asMap().entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						e -> new ArrayList<>(e.getValue())
				));
	}

	public List<CQIE> getRules(Predicate pred) {
		List<CQIE> rrs = rules.get(pred);
		if (rrs == null)
			return Collections.emptyList();
		
		return rrs;
	}
		
	/*
	 * adds a rule to the indexed linear dependencies
	 * 
	 * @param head: atom
	 * @param body: atom
	 */
	public void addRule(Function head, Function body) {
        CQIE rule = DATALOG_FACTORY.getCQIE(head, body);
		
        List<CQIE> list = rules.get(body.getFunctionSymbol());
        if (list == null) {
        	list = new LinkedList<>();
        	rules.put(body.getFunctionSymbol(), list);
        }
		
        list.add(rule);		
	}

    @Override
    public String toString() {
    	return rules.toString();
    }
}
