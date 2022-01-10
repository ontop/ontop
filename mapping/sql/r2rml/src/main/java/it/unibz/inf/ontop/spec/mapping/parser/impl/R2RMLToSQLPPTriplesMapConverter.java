package it.unibz.inf.ontop.spec.mapping.parser.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import eu.optique.r2rml.api.model.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.template.TemplateFactory;
import it.unibz.inf.ontop.model.template.impl.BnodeTemplateFactory;
import it.unibz.inf.ontop.model.template.impl.IRITemplateFactory;
import it.unibz.inf.ontop.model.template.impl.LiteralTemplateFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.R2RMLSQLPPtriplesMap;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.Var2VarSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class R2RMLToSQLPPTriplesMapConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(R2RMLToSQLPPTriplesMapConverter.class);

	private final TermFactory termFactory;
	private final TargetAtomFactory targetAtomFactory;
	private final SQLPPSourceQueryFactory sourceQueryFactory;
	private final SubstitutionFactory substitutionFactory;
	private final IRIConstant rdfType;

	private final String baseIri = "http://example.com/base/";

	private final ImmutableMap<IRI, TermMapFactory<TermMap, ? extends TemplateFactory>> iriTerm;
	private final ImmutableMap<IRI, TermMapFactory<TermMap, ? extends TemplateFactory>> iriOrBnodeTerm;
	private final ImmutableMap<IRI, TermMapFactory<ObjectMap, ? extends TemplateFactory>> iriOrBnodeOrLiteralTerm;

	@Inject
	private R2RMLToSQLPPTriplesMapConverter(TermFactory termFactory,
											TypeFactory typeFactory,
											TargetAtomFactory targetAtomFactory,
											SQLPPSourceQueryFactory sourceQueryFactory,
											SubstitutionFactory substitutionFactory) {
		this.termFactory = termFactory;
		this.targetAtomFactory = targetAtomFactory;
		this.sourceQueryFactory = sourceQueryFactory;
		this.substitutionFactory = substitutionFactory;

		this.rdfType = termFactory.getConstantIRI(RDF.TYPE);

		IRITemplateFactory iriTemplateFactory = new IRITemplateFactory(termFactory);
		BnodeTemplateFactory bnodeTemplateFactory = new BnodeTemplateFactory(termFactory);
		LiteralTemplateFactory literalTemplateFactory = new LiteralTemplateFactory(termFactory, typeFactory);

		this.iriTerm = ImmutableMap.of(
				R2RMLVocabulary.iri, new IriTermMapFactory<>(iriTemplateFactory));
		this.iriOrBnodeTerm = ImmutableMap.of(
				R2RMLVocabulary.iri, new IriTermMapFactory<>(iriTemplateFactory),
				R2RMLVocabulary.blankNode, new BnodeTermMapFactory<>(bnodeTemplateFactory));
		this.iriOrBnodeOrLiteralTerm = ImmutableMap.of(
				R2RMLVocabulary.iri, new IriTermMapFactory<>(iriTemplateFactory),
				R2RMLVocabulary.blankNode, new BnodeTermMapFactory<>(bnodeTemplateFactory),
				R2RMLVocabulary.literal, new LiteralTermMapFactory<>(literalTemplateFactory));
	}


	public ImmutableList<SQLPPTriplesMap> convert(Collection<TriplesMap> tripleMaps) {

		// Pass 1: creates "regular" PP triples maps using the original SQL queries
		ImmutableMap<TriplesMap, SQLPPTriplesMap> regularMap = tripleMaps.stream()
				.map(tm -> getSQLPPTriplesMap(tm)
						.map(pp -> Maps.immutableEntry(tm, pp)))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(ImmutableCollectors.toMap());

		return Stream.concat(
				regularMap.values().stream(),
				// Pass 2 - Creates new PP triples maps for object ref maps
				// NB: these triples maps are novel because the SQL queries are different
				tripleMaps.stream()
						// It is important to create subject terms only once because of blank nodes.
						.flatMap(tm -> tm.getPredicateObjectMaps().stream()
								.flatMap(pom -> getRefSQLPPTriplesMaps(tm, pom, regularMap))))
				.collect(ImmutableCollectors.toList());
	}

	private Optional<SQLPPTriplesMap> getSQLPPTriplesMap(TriplesMap tm)  {
		ImmutableList<TargetAtom> targetAtoms = getTargetAtoms(tm)
				.collect(ImmutableCollectors.toList());

		if (targetAtoms.isEmpty()) {
			LOGGER.warn("WARNING a triples map without target query will not be introduced : "+ tm);
			return Optional.empty();
		}

		String sourceQuery = tm.getLogicalTable().getSQLQuery().trim();
		return Optional.of(new R2RMLSQLPPtriplesMap("mapping-" + tm.hashCode(),
				sourceQueryFactory.createSourceQuery(sourceQuery),  targetAtoms));
	}

    /*
         see https://www.w3.org/TR/r2rml/#generated-triples for the definitions of all variables
     */

	private Stream<TargetAtom> getTargetAtoms(TriplesMap tm)  {
		SubjectMap subjectMap = tm.getSubjectMap();

		ImmutableTerm subject = extract(iriOrBnodeTerm, subjectMap);
		ImmutableList<NonVariableTerm> subject_graphs = subjectMap.getGraphMaps().stream()
				.map(m -> extract(iriTerm, m))
				.collect(ImmutableCollectors.toList());

		return Stream.concat(
				subjectMap.getClasses().stream()
						.map(termFactory::getConstantIRI)
						.flatMap(iri -> getTargetAtoms(subject, rdfType, iri, subject_graphs)),
				tm.getPredicateObjectMaps().stream()
						.flatMap(pom -> getTargetAtoms(subject, pom, subject_graphs)));
	}

	private Stream<TargetAtom> getTargetAtoms(ImmutableTerm subject, PredicateObjectMap pom, ImmutableList<NonVariableTerm> subject_graphs) {
		Stream<NonVariableTerm> predicates = pom.getPredicateMaps().stream()
				.map(m -> extract(iriTerm, m));

		List<NonVariableTerm> objects = pom.getObjectMaps().stream()
				.map(m -> extract(iriOrBnodeOrLiteralTerm, m))
				.collect(ImmutableCollectors.toList());

		ImmutableList<NonVariableTerm> subject_graphs_and_predicate_object_graphs = Stream.concat(
				subject_graphs.stream(),
				pom.getGraphMaps().stream().map(m -> extract(iriTerm, m)))
				.distinct()
				.collect(ImmutableCollectors.toList());

		return predicates.flatMap(p -> objects.stream()
					.flatMap(o -> getTargetAtoms(subject, p, o, subject_graphs_and_predicate_object_graphs)));
	}

	private Stream<TargetAtom> getTargetAtoms(ImmutableTerm subject, ImmutableTerm predicate, ImmutableTerm object, ImmutableList<? extends ImmutableTerm> graphs) {
		return (graphs.isEmpty())
				? Stream.of(targetAtomFactory.getTripleTargetAtom(subject, predicate, object))
				: graphs.stream()
					.map(g -> isDefaultGraph(g)
						? targetAtomFactory.getTripleTargetAtom(subject, predicate, object)
						: targetAtomFactory.getQuadTargetAtom(subject, predicate, object, g));
	}

	private static boolean isDefaultGraph(ImmutableTerm graphTerm) {
		return (graphTerm instanceof IRIConstant)
				&& ((IRIConstant) graphTerm).getIRI().equals(R2RMLVocabulary.defaultGraph);
	}

	private static final String TMP_PREFIX = "TMP";
	private static final String CHILD_PREFIX = "CHILD";
	private static final String PARENT_PREFIX = "PARENT";

	private Stream<SQLPPTriplesMap> getRefSQLPPTriplesMaps(TriplesMap tm, PredicateObjectMap pobm, ImmutableMap<TriplesMap, SQLPPTriplesMap> regularTriplesMap)  {
		if (pobm.getRefObjectMaps().isEmpty())
			return Stream.of();

		ImmutableList<NonVariableTerm> extractedPredicates = pobm.getPredicateMaps().stream()
				.map(m -> extract(iriTerm, m))
				.collect(ImmutableCollectors.toList());

		ImmutableList<NonVariableTerm> subject_graphs_and_predicate_object_graphs = Stream.concat(
					tm.getSubjectMap().getGraphMaps().stream(),
					pobm.getGraphMaps().stream())
				.map(m -> extract(iriTerm, m))
				.distinct()
				.collect(ImmutableCollectors.toList());

		return pobm.getRefObjectMaps().stream()
				.map(robm -> getRefSQLPPTriplesMaps(tm, robm, extractedPredicates, subject_graphs_and_predicate_object_graphs, regularTriplesMap));
	}


	private SQLPPTriplesMap getRefSQLPPTriplesMaps(TriplesMap tm, RefObjectMap robm, ImmutableList<NonVariableTerm> extractedPredicates, ImmutableList<NonVariableTerm> extractedGraphs, ImmutableMap<TriplesMap, SQLPPTriplesMap> regularTriplesMap) {
		TriplesMap parent = robm.getParentMap();

		ImmutableTerm extractedSubject = getSubjectTerm(tm, regularTriplesMap);
		ImmutableTerm extractedObject = getSubjectTerm(parent, regularTriplesMap);

		ImmutableMap<Variable, Variable> childMap, parentMap;
		String sourceQuery;

		if (robm.getJoinConditions().isEmpty()) {
			if (!parent.getLogicalTable().getSQLQuery().trim().equals(tm.getLogicalTable().getSQLQuery().trim()))
				throw new IllegalArgumentException("No rr:joinCondition, but the two SQL queries are distinct: " +
						tm.getLogicalTable().getSQLQuery() + " and " + parent.getLogicalTable().getSQLQuery());

			childMap = parentMap = Stream.concat(
					getVariableStreamOf(extractedSubject, extractedPredicates, extractedGraphs),
					extractedObject.getVariableStream())
					.collect(toVariableRenamingMap(TMP_PREFIX));

			sourceQuery = getSQL(childMap.entrySet().stream()
							.map(e -> Maps.immutableEntry(TMP_PREFIX + "." + e.getKey(), e.getValue())),
					Stream.of(Maps.immutableEntry(tm.getLogicalTable(), TMP_PREFIX)),
					Stream.of());
		}
		else {
			childMap = getVariableStreamOf(extractedSubject, extractedPredicates, extractedGraphs)
					.collect(toVariableRenamingMap(CHILD_PREFIX));

			parentMap = extractedObject.getVariableStream()
					.collect(toVariableRenamingMap(PARENT_PREFIX));

			sourceQuery = getSQL(Stream.concat(
					childMap.entrySet().stream()
							.map(e -> Maps.immutableEntry(CHILD_PREFIX + "." + e.getKey(), e.getValue())),
					parentMap.entrySet().stream()
							.map(e -> Maps.immutableEntry(PARENT_PREFIX + "." + e.getKey(), e.getValue()))),
					Stream.of(Maps.immutableEntry(tm.getLogicalTable(), CHILD_PREFIX),
							Maps.immutableEntry(parent.getLogicalTable(), PARENT_PREFIX)),
					robm.getJoinConditions().stream()
							.map(j -> Maps.immutableEntry(CHILD_PREFIX + "." + j.getChild(), PARENT_PREFIX + "." + j.getParent())));
		}

		Var2VarSubstitution sub = substitutionFactory.getInjectiveVar2VarSubstitution(childMap);
		ImmutableTerm subject = sub.apply(extractedSubject);
		ImmutableList<ImmutableTerm>  graphs = extractedGraphs.stream()
				.map(sub::apply)
				.collect(ImmutableCollectors.toList());
		Var2VarSubstitution ob = substitutionFactory.getInjectiveVar2VarSubstitution(parentMap);
		ImmutableTerm object = ob.apply(extractedObject);

		ImmutableList<TargetAtom> targetAtoms = extractedPredicates.stream().map(sub::apply)
				.flatMap(p -> getTargetAtoms(subject, p, object, graphs))
				.collect(ImmutableCollectors.toList());

		// use referenceObjectMap robm as id, because there could be multiple joinCondition in the same triple map
		SQLPPTriplesMap ppTriplesMap = new R2RMLSQLPPtriplesMap("tm-join-" + robm.hashCode(),
				sourceQueryFactory.createSourceQuery(sourceQuery), targetAtoms);
		LOGGER.info("Join \"triples map\" introduced: " + ppTriplesMap);
		return ppTriplesMap;
	}

	private static String getSQL(Stream<Map.Entry<String, Variable>> selectItems,
								 Stream<Map.Entry<LogicalTable, String>> fromItems,
								 Stream<Map.Entry<String, String>> whereClause) {
		String condition = whereClause.map(e -> e.getKey() + " = " + e.getValue())
				.collect(Collectors.joining(" AND "));
		return "SELECT " + selectItems.map(e -> e.getKey() + " AS " + e.getValue().getName())
				.collect(Collectors.joining(", ")) +
				" FROM " + fromItems.map(e -> "(" + e.getKey().getSQLQuery().trim() + ") " + e.getValue())
				.collect(Collectors.joining(", ")) +
				(condition.isEmpty() ? "" : " WHERE " + condition);
	}

	private ImmutableTerm getSubjectTerm(TriplesMap tm, ImmutableMap<TriplesMap, SQLPPTriplesMap> regularTriplesMap) {
		// Re-uses the already created subject term. Important when dealing with blank nodes.
		return Optional.ofNullable(regularTriplesMap.get(tm))
				.map(pp -> pp.getTargetAtoms().stream()
						.map(a -> a.getSubstitutedTerm(0))
						.findAny()
						.orElseThrow(() -> new MinorOntopInternalBugException("All created SQLPPTriplesMaps must have at least one target atom")))
				.orElseGet(() -> extract(iriOrBnodeTerm, tm.getSubjectMap()));
	}

	private static Stream<Variable> getVariableStreamOf(ImmutableTerm t, ImmutableList<? extends ImmutableTerm> l1, ImmutableList<? extends ImmutableTerm> l2) {
		return Stream.concat(t.getVariableStream(),
				Stream.concat(l1.stream(), l2.stream()).flatMap(ImmutableTerm::getVariableStream));
	}

	private Collector<Variable, ?, ImmutableMap<Variable, Variable>> toVariableRenamingMap(String prefix) {
		return ImmutableCollectors.toMap(v -> v, v -> prefixAttributeName(prefix + "_", v));
	}

	private Variable prefixAttributeName(String prefix, Variable var) {
		String attributeName = var.getName();

		String newAttributeName;
		if (attributeName.startsWith("\"") && attributeName.endsWith("\"")
				|| attributeName.startsWith("`") && attributeName.endsWith("`"))
			newAttributeName = attributeName.substring(0,1) + prefix + attributeName.substring(1);
		else
			newAttributeName = prefix + attributeName;

		return termFactory.getVariable(newAttributeName);
	}






	/*
	  Terms
	 */


	private <T extends TermMap> NonVariableTerm extract(ImmutableMap<IRI, TermMapFactory<T, ? extends TemplateFactory>> map, T termMap) {
		TermMapFactory<T, ? extends TemplateFactory> termMapFactory = map.get(termMap.getTermType());
		if (termMapFactory == null) {
			throw new R2RMLParsingBugException("Was expecting one of " + map.keySet() +
					" when encountered " + termMap);
		}
		return termMapFactory.extract(termMap);
	}

	/*
	   @see it.unibz.inf.ontop.spec.mapping.serializer.impl.SQLPPTriplesMapToR2RMLConverter
	 */

	private abstract static class TermMapFactory<T extends TermMap, F extends TemplateFactory> {
		protected final F templateFactory;
		public TermMapFactory(F templateFactory) {
			this.templateFactory = templateFactory;
		}

		protected abstract NonVariableTerm onConstant(RDFTerm constant);

		protected NonVariableTerm onColumn(String column) {
			return templateFactory.getColumn(column);
		}

		protected NonVariableTerm onTemplate(String template) {
			return templateFactory.getTemplateTerm(templateFactory.getComponents(template));
		}

		public NonVariableTerm extract(T termMap) {
			if (termMap.getConstant() != null)
				return onConstant(termMap.getConstant());

			if (termMap.getTemplate() != null)
				return onTemplate(termMap.getTemplate().toString());

			if (termMap.getColumn() != null)
				return onColumn(termMap.getColumn());

			throw new R2RMLParsingBugException("A term map is either constant-valued, column-valued or template-valued.");
		}
	}

	private class IriTermMapFactory<T extends TermMap> extends TermMapFactory<T, IRITemplateFactory> {
		public IriTermMapFactory(IRITemplateFactory templateFactory) {
			super(templateFactory);
		}

		@Override
		protected NonVariableTerm onConstant(RDFTerm constant) {
			return templateFactory.getConstant(
					R2RMLVocabulary.resolveIri(((IRI)constant).getIRIString(), baseIri));
		}
		@Override
		protected NonVariableTerm onTemplate(String template) {
			return super.onTemplate(R2RMLVocabulary.resolveIri(template, baseIri));
		}
	}

	private static class BnodeTermMapFactory<T extends TermMap> extends TermMapFactory<T, BnodeTemplateFactory> {
		public BnodeTermMapFactory(BnodeTemplateFactory templateFactory) {
			super(templateFactory);
		}

		@Override
		protected NonVariableTerm onConstant(RDFTerm constant) {
			// https://www.w3.org/TR/r2rml/#constant says none can be an Bnode
			throw new R2RMLParsingBugException("Constant blank nodes are not accepted in R2RML (should have been detected earlier)");
		}
	}

	private class LiteralTermMapFactory<T extends ObjectMap> extends TermMapFactory<T, LiteralTemplateFactory> {
		public LiteralTermMapFactory(LiteralTemplateFactory templateFactory) {
			super(templateFactory);
		}
		@Override
		protected NonVariableTerm onConstant(RDFTerm constant) {
			if (constant instanceof Literal) {
				return templateFactory.getConstant(((Literal)constant).getLexicalForm());
			}
			throw new R2RMLParsingBugException("Was expecting a Literal as constant, not a " + constant.getClass());
		}

		@Override
		public NonVariableTerm extract(T om) {
			RDFDatatype datatype = templateFactory.extractDatatype(
						Optional.ofNullable(om.getLanguageTag()),
						Optional.ofNullable(om.getDatatype()))
					// Third try: datatype of the constant
					.orElseGet(() -> Optional.ofNullable(om.getConstant())
							.map(c -> (Literal) c)
							.map(Literal::getDatatype)
							.map(templateFactory::getDatatype)
							// Default case: RDFS.LITERAL (abstract, to be inferred later)
							.orElseGet(templateFactory::getAbstractRDFSLiteral));

			return termFactory.getRDFLiteralFunctionalTerm(super.extract(om), datatype);
		}
	}

	/**
	 * Bug most likely coming from the R2RML library, but we classify as an "internal" bug
	 */
	private static class R2RMLParsingBugException extends OntopInternalBugException {
		protected R2RMLParsingBugException(String message) {
			super(message);
		}
	}
}
