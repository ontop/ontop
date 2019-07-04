package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.IQ2DatalogTranslator;
import it.unibz.inf.ontop.datalog.impl.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.datalog.impl.DatalogRule2QueryConverter;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.mapdb.Fun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/***
 * Splits a given mapping into builtin predicates (conditions)
 * and all other atoms (stripped), which are checked for containment
 * by the TMapping construction algorithm.
 */

public class TMappingRule {

	private final MappingTools.RDFPredicateInfo predicateInfo;
	private final ImmutableList<Term> headTerms;
	private final ImmutableList<DataAtom<AtomPredicate>> databaseAtoms;
	// an OR-connected list of AND-connected atomic filters
	private final ImmutableList<ImmutableList<ImmutableExpression>> filterAtoms;

	private final DatalogFactory datalogFactory;
	private final TermFactory termFactory;
	private final AtomFactory atomFactory;
	private final ImmutabilityTools immutabilityTools;
	private final IntermediateQueryFactory iqFactory;
	private final DatalogRule2QueryConverter datalogRuleConverter;

	/***
	 * Given a mappings in currentMapping, this method will
	 * return a new mappings in which no constants appear in the body of
	 * database predicates. This is done by replacing the constant occurrence
	 * with a fresh variable, and adding a new equality condition to the body of
	 * the mapping.
	 * <p/>
	 * 
	 * For example, let the mapping m be
	 * <p/>
	 * A(x) :- T(x,y,22)
	 * 
	 * <p>
	 * Then this method will replace m by the mapping m'
	 * <p>
	 * A(x) :- T(x,y,z), EQ(z,22)
	 * 
	 */
	
	public TMappingRule(IQ q, DatalogFactory datalogFactory, TermFactory termFactory, AtomFactory atomFactory, ImmutabilityTools immutabilityTools, IQ2DatalogTranslator iq2DatalogTranslator, IntermediateQueryFactory iqFactory, DatalogRule2QueryConverter datalogRuleConverter) {

        this.datalogFactory = datalogFactory;
        this.termFactory = termFactory;
        this.atomFactory = atomFactory;
        this.immutabilityTools = immutabilityTools;
        this.iqFactory = iqFactory;
        this.datalogRuleConverter = datalogRuleConverter;

		this.predicateInfo = MappingTools.extractRDFPredicate(q);

		List<CQIE> translation = iq2DatalogTranslator.translate(q).getRules();
		if (translation.size() != 1)
			System.out.println("TMAP PANIC");

		Function head = translation.get(0).getHead();
		List<Function> body = translation.get(0).getBody();

		ImmutableList.Builder<ImmutableExpression> filters = ImmutableList.builder();
		ImmutableList.Builder<DataAtom<AtomPredicate>> dbs = ImmutableList.builder();

        Map<Term, Variable> valueMap = new HashMap<>();

		for (Function atom : body) {
			if (atom.getFunctionSymbol() instanceof ExpressionOperation) {
				Function clone = (Function)atom.clone();
				filters.add(termFactory.getImmutableExpression(
						(ExpressionOperation)atom.getFunctionSymbol(),
						atom.getTerms().stream()
								.map(t -> immutabilityTools.convertIntoImmutableTerm(t))
								.collect(ImmutableCollectors.toList())));
			}
			else {
				// database atom, we need to replace all constants by filters
				dbs.add(replaceConstants2(atom, filters, valueMap));
			}
		}
        this.databaseAtoms = dbs.build();

		this.headTerms = predicateInfo.isClass()
				? ImmutableList.of(head.getTerms().get(0))
				: ImmutableList.of(head.getTerms().get(0), head.getTerms().get(2));

		ImmutableList<ImmutableExpression> f = filters.build();
		this.filterAtoms = f.isEmpty() ? ImmutableList.of() : ImmutableList.of(f);
	}

	

	private DataAtom<AtomPredicate> replaceConstants2(Function atom, ImmutableList.Builder<ImmutableExpression> filters, Map<Term, Variable> valueMap) {
		ImmutableList.Builder<VariableOrGroundTerm> builder = ImmutableList.builder();

		for (Term term : atom.getTerms()) {
			if (term instanceof Variable) {
				builder.add((Variable)term);
			}
			else {
				// Found a constant, replacing with a fresh variable
				// and adding the new equality atom
				Variable var = valueMap.get(term);
				if (var == null) {
					var = termFactory.getVariable("?FreshVar" + valueMap.keySet().size());
					valueMap.put(term, var);
					filters.add(termFactory.getImmutableExpression(ExpressionOperation.EQ, var, immutabilityTools.convertIntoImmutableTerm(term)));
				}
				builder.add(var);
			}
		}

		return atomFactory.getDataAtom((AtomPredicate)atom.getFunctionSymbol(), builder.build());
	}

	TMappingRule(TMappingRule baseRule, ImmutableList<ImmutableList<ImmutableExpression>> filterAtoms) {
        this.datalogFactory = baseRule.datalogFactory;
        this.termFactory = baseRule.termFactory;
        this.atomFactory = baseRule.atomFactory;
        this.immutabilityTools = baseRule.immutabilityTools;
        this.iqFactory = baseRule.iqFactory;
        this.datalogRuleConverter = baseRule.datalogRuleConverter;

        this.predicateInfo = baseRule.predicateInfo;

		this.databaseAtoms = baseRule.databaseAtoms;
		this.headTerms = baseRule.headTerms.stream().map(t -> t.clone()).collect(ImmutableCollectors.toList());

		this.filterAtoms = filterAtoms;
	}
	
	
	TMappingRule(ImmutableList<Term> headTerms, MappingTools.RDFPredicateInfo predicateInfo, TMappingRule baseRule) {
        this.datalogFactory = baseRule.datalogFactory;
        this.termFactory = baseRule.termFactory;
		this.atomFactory = baseRule.atomFactory;
		this.immutabilityTools = baseRule.immutabilityTools;
		this.iqFactory = baseRule.iqFactory;
		this.datalogRuleConverter = baseRule.datalogRuleConverter;

		this.predicateInfo = predicateInfo;

		this.databaseAtoms = baseRule.databaseAtoms;
		this.headTerms = headTerms.stream().map(t -> t.clone()).collect(ImmutableCollectors.toList());

        this.filterAtoms = baseRule.filterAtoms;
	}
	
	public IRI getIri() { return predicateInfo.getIri(); }

	public MappingTools.RDFPredicateInfo getPredicateInfo() { return predicateInfo; }

	public Substitution computeHomomorphsim(TMappingRule other, CQContainmentCheckUnderLIDs cqc) {

		//System.out.println("CH: " + head + " v " + other.head);

		return cqc.computeHomomorphsim(headTerms, databaseAtoms, other.headTerms, other.databaseAtoms);
	}
	
	public IQ asIQ() {
		List<Function> combinedBody;
		if (!filterAtoms.isEmpty()) {
			combinedBody = new ArrayList<>(databaseAtoms.size() + filterAtoms.size()); 
			combinedBody.addAll(getDatabaseAtoms());
			
			Iterator<ImmutableList<ImmutableExpression>> iterOR = filterAtoms.iterator();
			List<ImmutableExpression> list = iterOR.next(); // IMPORTANT: assume that conditions is non-empty
			ImmutableExpression mergedConditions = getMergedByAND(list);
			while (iterOR.hasNext()) {
				list = iterOR.next();
				ImmutableExpression e = getMergedByAND(list);
				mergedConditions = termFactory.getImmutableExpression(ExpressionOperation.OR, e, mergedConditions);
			}
			
			combinedBody.add(immutabilityTools.convertToMutableBooleanExpression(mergedConditions));
		}
		else
			combinedBody = getDatabaseAtoms();

		Function head = predicateInfo.isClass()
				? atomFactory.getMutableTripleHeadAtom(headTerms.get(0), predicateInfo.getIri())
				: atomFactory.getMutableTripleHeadAtom(headTerms.get(0), predicateInfo.getIri(), headTerms.get(1));

		return datalogRuleConverter.extractPredicatesAndConvertDatalogRule(
				datalogFactory.getCQIE(head, combinedBody), iqFactory);
	}
	
	/***
	 * Takes a list of boolean atoms and returns one single atom
	 * representing the conjunction 
	 * 
	 * ASSUMPTION: the list is non-empty
	 * 
	 * Example: A -> A
	 *          A, B -> AND(B,A)
	 *          A, B, C -> AND(C,AND(B,A))
	 * 
	 */
	
	private ImmutableExpression getMergedByAND(List<ImmutableExpression> list) {
		Iterator<ImmutableExpression> iterAND = list.iterator();
		ImmutableExpression mergedConditions = iterAND.next();
		while (iterAND.hasNext()) {
			ImmutableExpression e = iterAND.next();
			mergedConditions = termFactory.getImmutableExpression(ExpressionOperation.AND, e, mergedConditions);
		}		
		return mergedConditions;
	}
	
	
	public boolean isFact() {
		return databaseAtoms.isEmpty() && filterAtoms.isEmpty();
	}
	
    public ImmutableList<Term> getHeadTerms() {
        return headTerms;
    }

    public ImmutableList<Function> getDatabaseAtoms() {
		return databaseAtoms.stream()
				.map(immutabilityTools::convertToMutableFunction)
				.collect(ImmutableCollectors.toList());
	}
	
	public ImmutableList<ImmutableList<ImmutableExpression>> getConditions() { return filterAtoms; }
	
	@Override
	public int hashCode() {
		return predicateInfo.getIri().hashCode() ^ headTerms.hashCode() ^ databaseAtoms.hashCode() ^ filterAtoms.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof TMappingRule) {
			TMappingRule otherRule = (TMappingRule)other;
			return (headTerms.equals(otherRule.headTerms) &&
					databaseAtoms.equals(otherRule.databaseAtoms) && 
					filterAtoms.equals(otherRule.filterAtoms));
		}
		return false;
	}

	@Override 
	public String toString() {
		return predicateInfo.getIri() + "(" + headTerms + ") <- " + databaseAtoms + " AND " + filterAtoms;
	}
}
