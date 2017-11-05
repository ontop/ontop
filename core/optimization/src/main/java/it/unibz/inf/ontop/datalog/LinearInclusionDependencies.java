package it.unibz.inf.ontop.datalog;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;

public class LinearInclusionDependencies {
    
	private final Map<Predicate, List<CQIE>> rules;
	private final DatalogFactory datalogFactory;

	public LinearInclusionDependencies(DatalogFactory datalogFactory) {
		this.datalogFactory = datalogFactory;
		rules = new HashMap<>();
	}

	public LinearInclusionDependencies(ImmutableMultimap<AtomPredicate, CQIE> predicateRuleMap,
									   DatalogFactory datalogFactory) {
		rules = predicateRuleMap.asMap().entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						e -> new ArrayList<>(e.getValue())
				));
		this.datalogFactory = datalogFactory;
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
        CQIE rule = datalogFactory.getCQIE(head, body);
		
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
