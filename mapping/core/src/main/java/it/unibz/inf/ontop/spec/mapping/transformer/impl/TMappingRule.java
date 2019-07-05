package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.IQ2DatalogTranslator;
import it.unibz.inf.ontop.datalog.impl.DatalogRule2QueryConverter;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/***
 * Splits a given mapping into builtin predicates (conditions)
 * and all other atoms (stripped), which are checked for containment
 * by the TMapping construction algorithm.
 */

public class TMappingRule {

	private final MappingTools.RDFPredicateInfo predicateInfo;
	private final ImmutableList<ImmutableTerm> headTerms;
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

		List<Function> body = translation.get(0).getBody();

		ImmutableList.Builder<ImmutableExpression> filters = ImmutableList.builder();
		ImmutableList.Builder<DataAtom<AtomPredicate>> dbs = ImmutableList.builder();

        Map<Term, Variable> valueMap = new HashMap<>();

		for (Function atom : body) {
			if (atom.getFunctionSymbol() instanceof ExpressionOperation) {
				filters.add(termFactory.getImmutableExpression(
						(ExpressionOperation)atom.getFunctionSymbol(),
						atom.getTerms().stream()
								.map(t -> immutabilityTools.convertIntoImmutableTerm(t))
								.collect(ImmutableCollectors.toList())));
			}
			else {
				// database atom, we need to replace all constants by filters
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
				dbs.add(atomFactory.getDataAtom((AtomPredicate)atom.getFunctionSymbol(), builder.build()));
			}
		}
        this.databaseAtoms = dbs.build();


		DistinctVariableOnlyDataAtom h = q.getProjectionAtom();
		ConstructionNode cn = (ConstructionNode)q.getTree().getRootNode();
		ImmutableSubstitution sub = cn.getSubstitution();
		ImmutableList<ImmutableTerm> headT = sub.apply(h.getArguments());

		//Function head = translation.get(0).getHead();
		//this.headTerms = predicateInfo.isClass()
		//		? ImmutableList.of(immutabilityTools.convertIntoImmutableTerm(head.getTerms().get(0)))
		//		: ImmutableList.of(immutabilityTools.convertIntoImmutableTerm(head.getTerms().get(0)), immutabilityTools.convertIntoImmutableTerm(head.getTerms().get(2)));
		this.headTerms = predicateInfo.isClass()
				? ImmutableList.of(headT.get(0))
				: ImmutableList.of(headT.get(0), headT.get(2));

		ImmutableList<ImmutableExpression> f = filters.build();
		this.filterAtoms = f.isEmpty() ? ImmutableList.of() : ImmutableList.of(f);
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
		this.headTerms = baseRule.headTerms;

		this.filterAtoms = filterAtoms;
	}
	
	
	TMappingRule(ImmutableList<ImmutableTerm> headTerms, MappingTools.RDFPredicateInfo predicateInfo, TMappingRule baseRule) {
        this.datalogFactory = baseRule.datalogFactory;
        this.termFactory = baseRule.termFactory;
		this.atomFactory = baseRule.atomFactory;
		this.immutabilityTools = baseRule.immutabilityTools;
		this.iqFactory = baseRule.iqFactory;
		this.datalogRuleConverter = baseRule.datalogRuleConverter;

		this.predicateInfo = predicateInfo;

		this.databaseAtoms = baseRule.databaseAtoms;
		this.headTerms = headTerms;

        this.filterAtoms = baseRule.filterAtoms;
	}
	
	public IRI getIri() { return predicateInfo.getIri(); }

	public MappingTools.RDFPredicateInfo getPredicateInfo() { return predicateInfo; }


	public IQ asIQ() {

		// assumes that filterAtoms is a possibly empty list of non-empty lists
		Optional<ImmutableExpression> mergedConditions = filterAtoms.stream()
				.map(list -> list.stream()
						.reduce((r, e) -> termFactory.getImmutableExpression(ExpressionOperation.AND, e, r)).get())
				.reduce((r, e) -> termFactory.getImmutableExpression(ExpressionOperation.OR, e, r));

		ImmutableList<Function> combinedBody = Stream.concat(
				databaseAtoms.stream()
					.map(immutabilityTools::convertToMutableFunction),
				mergedConditions.isPresent()
						? Stream.of(immutabilityTools.convertToMutableBooleanExpression(mergedConditions.get()))
						: Stream.empty())
				.collect(ImmutableCollectors.toList());

		Function head = predicateInfo.isClass()
				? atomFactory.getMutableTripleHeadAtom(immutabilityTools.convertToMutableTerm(headTerms.get(0)), predicateInfo.getIri())
				: atomFactory.getMutableTripleHeadAtom(immutabilityTools.convertToMutableTerm(headTerms.get(0)), predicateInfo.getIri(), immutabilityTools.convertToMutableTerm(headTerms.get(1)));

		return datalogRuleConverter.extractPredicatesAndConvertDatalogRule(
				datalogFactory.getCQIE(head, combinedBody), iqFactory);
	}

	
    public ImmutableList<ImmutableTerm> getHeadTerms() {
        return headTerms;
    }

	public ImmutableList<DataAtom<AtomPredicate>> getDatabaseAtoms() {
		return databaseAtoms;
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
