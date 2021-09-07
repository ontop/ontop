package it.unibz.inf.ontop.spec.mapping.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import eu.optique.r2rml.api.MappingFactory;
import eu.optique.r2rml.api.model.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.template.TemplateFactory;
import it.unibz.inf.ontop.model.template.impl.BnodeTemplateFactory;
import it.unibz.inf.ontop.model.template.impl.IRITemplateFactory;
import it.unibz.inf.ontop.model.template.impl.LiteralTemplateFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.LanguageTag;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.spec.mapping.parser.impl.R2RMLVocabulary;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;


/**
 * Transform OBDA mappings in R2RML mappings
 * @author Sarah, Mindas, Timi, Guohui, Martin
 *
 */
public class SQLPPTriplesMapToR2RMLConverter {

	private final RDF rdfFactory;
	private final MappingFactory mappingFactory;

	private static final String baseIRIString = "urn:";

	private final Function<RDFTermType, TermMapFactory<GraphMap>> graphTermMapFactorySupplier;
	private final Function<RDFTermType, TermMapFactory<SubjectMap>> subjectTermMapFactorySupplier;
	private final Function<RDFTermType, TermMapFactory<PredicateMap>> predicateTermMapFactorySupplier;
	private final Function<RDFTermType, TermMapFactory<ObjectMap>> objectTermMapFactorySupplier;

	public SQLPPTriplesMapToR2RMLConverter(RDF rdfFactory, MappingFactory mappingFactory) {
		this.rdfFactory = rdfFactory;
		this.mappingFactory = mappingFactory;

		this.graphTermMapFactorySupplier =  new TermMapFactorySupplier<>(mappingFactory::createGraphMap,
				mappingFactory::createGraphMap, mappingFactory::createGraphMap);
		this.subjectTermMapFactorySupplier =  new TermMapFactorySupplier<>(mappingFactory::createSubjectMap,
				mappingFactory::createSubjectMap, mappingFactory::createSubjectMap);
		this.predicateTermMapFactorySupplier = new TermMapFactorySupplier<>(mappingFactory::createPredicateMap,
				mappingFactory::createPredicateMap, mappingFactory::createPredicateMap);
		this.objectTermMapFactorySupplier = new ObjectTermMapFactorySupplier<>(mappingFactory::createObjectMap,
				mappingFactory::createObjectMap, mappingFactory::createObjectMap);
	}

    /**
	 * Get R2RML TriplesMaps from OBDA mapping axiom
	 */
	public Stream<TriplesMap> convert(SQLPPTriplesMap triplesMap) {

		// check if mapping id is an iri
		String mapping_id = triplesMap.getId();
		String mainNodeIriPrefix = !mapping_id.contains(":")
				? baseIRIString + mapping_id
				: mapping_id;

		ImmutableMap<ImmutableTerm, Collection<Map.Entry<RDFAtomPredicate, TargetAtom>>> subjectMap = triplesMap.getTargetAtoms().stream()
				.filter(t -> t.getProjectionAtom().getPredicate() instanceof RDFAtomPredicate)
				.map(t -> Maps.immutableEntry((RDFAtomPredicate)t.getProjectionAtom().getPredicate(), t))
				.collect(ImmutableCollectors.toMultimap(
						e -> e.getKey().getSubject(e.getValue().getSubstitutedTerms()),
						e -> e))
				.asMap();

		String sql = triplesMap.getSourceQuery().getSQL();

		return subjectMap.entrySet().stream()
				.flatMap(e -> processSameSubjectGroup(mappingFactory.createR2RMLView(sql), e.getKey(), e.getValue(),
						// do not create triples map with the same name in case of multiple subjects
						subjectMap.size() == 1
								? mainNodeIriPrefix
								: mainNodeIriPrefix + "-" + UUID.randomUUID()));
	}

	private Stream<TriplesMap> processSameSubjectGroup(LogicalTable logicalTable,
													   ImmutableTerm subject,
													   Collection<Map.Entry<RDFAtomPredicate, TargetAtom>> targetAtoms,
													   String mainNodeIriPrefix) {

		ImmutableMap<Optional<ImmutableTerm>, Collection<Map.Entry<RDFAtomPredicate, TargetAtom>>> graphMap = targetAtoms.stream()
				.collect(ImmutableCollectors.toMultimap(
						e -> e.getKey().getGraph(e.getValue().getSubstitutedTerms()),
						e -> e))
				.asMap();

		return graphMap.entrySet().stream()
				.map(e -> processSameSubjectGraphGroup(logicalTable, subject, e.getKey(), e.getValue(), mainNodeIriPrefix));
	}

	private TriplesMap processSameSubjectGraphGroup(LogicalTable logicalTable,
													ImmutableTerm subject,
													Optional<ImmutableTerm> graph,
													Collection<Map.Entry<RDFAtomPredicate, TargetAtom>> targetAtoms,
													String mainNodeIriPrefix) {

		// do not create triples map with the same name in case of multiple named graphs
		IRI iri = rdfFactory.createIRI(
				graph.map(t -> mainNodeIriPrefix + "-" + UUID.randomUUID())
						.orElse(mainNodeIriPrefix));

		TriplesMap tm = mappingFactory.createTriplesMap(logicalTable, getTermMap(subject, subjectTermMapFactorySupplier), iri);

		graph.ifPresent(t -> tm.getSubjectMap().addGraphMap(getTermMap(t, graphTermMapFactorySupplier)));

		for (Map.Entry<RDFAtomPredicate, TargetAtom> e : targetAtoms)  {
			RDFAtomPredicate predicate = e.getKey();
			ImmutableList<ImmutableTerm> terms = e.getValue().getSubstitutedTerms();

			Optional<IRI> classIri = predicate.getClassIRI(terms);
			if (classIri.isPresent()) {
				tm.getSubjectMap().addClass(classIri.get());
			}
			else {
				tm.addPredicateObjectMap(mappingFactory.createPredicateObjectMap(
						getTermMap(predicate.getProperty(terms), predicateTermMapFactorySupplier),
						getTermMap(predicate.getObject(terms), objectTermMapFactorySupplier)));
			}
		}

		return tm;
	}

	private class TermMapFactorySupplier<T extends TermMap> implements Function<RDFTermType, TermMapFactory<T>> {
		protected final TermMapFactory<T> bnode, iri;

		TermMapFactorySupplier(Function<Template, T> templateFct, Function<String, T> columnFct, Function<IRI, T> iriFct) {
			bnode = new BnodeTermMapFactory<>(columnFct, templateFct);
			iri = new IriTermMapFactory<>(columnFct, templateFct, iriFct);
		}

		@Override
		public TermMapFactory<T> apply(RDFTermType type) {
			if (type instanceof ObjectRDFType)
				return (((ObjectRDFType) type).isBlankNode()) ? bnode : iri;

			throw new MinorOntopInternalBugException("Unexpected term type: " + type);
		}
	}

	private class ObjectTermMapFactorySupplier<T extends ObjectMap> extends TermMapFactorySupplier<T> {
		private final Map<RDFDatatype, LiteralTermMapFactory<T>> map = new HashMap<>();
		private final Function<Literal, T> rdfTermFct;

		ObjectTermMapFactorySupplier(Function<Template, T> templateFct, Function<String, T> columnFct, Function<RDFTerm, T> rdfTermFct) {
			super(templateFct, columnFct, rdfTermFct::apply);
			this.rdfTermFct = rdfTermFct::apply;
		}

		@Override
		public TermMapFactory<T> apply(RDFTermType type) {
			if (type instanceof RDFDatatype) {
				RDFDatatype datatype = (RDFDatatype) type;
				return map.computeIfAbsent(datatype,
						d -> new LiteralTermMapFactory<>(datatype, iri.columnFct, iri.templateFct, rdfTermFct));
			}
			return super.apply(type);
		}
	}

	private <T extends TermMap> T getTermMap(ImmutableTerm term,
											 Function<RDFTermType, TermMapFactory<T>> termMapFactorySupplier) {

		if (term instanceof RDFConstant) {
			RDFConstant constant = (RDFConstant) term;
			RDFTermType termType = constant.getType();
			return termMapFactorySupplier.apply(termType).forConstant(constant.getValue());
		}
		if (term instanceof ImmutableFunctionalTerm) {
			ImmutableFunctionalTerm rdfFunctionalTerm = (ImmutableFunctionalTerm) term;
			ImmutableTerm lexicalTerm = DBTypeConversionFunctionSymbol.uncast(rdfFunctionalTerm.getTerm(0));

			// Might be abstract (e.g. partially defined literal map)
			RDFTermType termType = Optional.of(rdfFunctionalTerm.getTerm(1))
					.filter(t -> t instanceof RDFTermTypeConstant)
					.map(t -> (RDFTermTypeConstant) t)
					.map(RDFTermTypeConstant::getRDFTermType)
					.orElseThrow(() -> new R2RMLSerializationException(
							"Was expecting a RDFTermTypeConstant in the mapping assertion, not "
									+ rdfFunctionalTerm.getTerm(1)));

			return termMapFactorySupplier.apply(termType).create(lexicalTerm);
		}
		throw new MinorOntopInternalBugException("Unexpected term: " + term);
	}


	private abstract class TermMapFactory<T extends TermMap> {
		protected final IRI termType;
		protected final TemplateFactory templateFactory;
		protected final Function<String, T> columnFct;
		protected final Function<Template, T> templateFct;

		TermMapFactory(IRI termType, TemplateFactory templateFactory, Function<String, T> columnFct, Function<Template, T> templateFct) {
			this.termType = termType;
			this.templateFactory = templateFactory;
			this.columnFct = columnFct;
			this.templateFct = templateFct;
		}

		public abstract T forConstant(String value);

		protected T forColumn(Variable term) {
			T termMap = columnFct.apply(term.getName());
			termMap.setTermType(termType);
			return termMap;
		}
		protected T forTemplate(ImmutableFunctionalTerm term) {
			String templateString = templateFactory.serializeTemplateTerm(term);
			T termMap = templateFct.apply(mappingFactory.createTemplate(templateString));
			termMap.setTermType(termType);
			return termMap;
		}

		public T create(ImmutableTerm term) {
			if (term instanceof DBConstant)
				return forConstant(((DBConstant)term).getValue());
			if (term instanceof Variable)
				return forColumn((Variable)term);
			if (term instanceof ImmutableFunctionalTerm)
				return forTemplate((ImmutableFunctionalTerm)term);

			throw new MinorOntopInternalBugException("Unexpected lexical term for an termMap: " + term);
		}
	}

	private class BnodeTermMapFactory<T extends TermMap> extends TermMapFactory<T> {

		BnodeTermMapFactory(Function<String, T> columnFct, Function<Template, T> templateFct) {
			super(R2RMLVocabulary.blankNode, new BnodeTemplateFactory(null), columnFct, templateFct);
		}

		/**
			constant Bnodes do not exist (https://www.w3.org/TR/r2rml/#constant)
		    use a column-free template instead
		 */
		@Override
		public T forConstant(String value) {
			T termMap = templateFct.apply(mappingFactory.createTemplate(value));
			termMap.setTermType(termType);
			return termMap;
		}
	}

	private class IriTermMapFactory<T extends TermMap> extends TermMapFactory<T> {
		private final Function<IRI, T> constantFct;

		IriTermMapFactory(Function<String, T> columnFct, Function<Template, T> templateFct, Function<IRI, T> constantFct) {
			super(R2RMLVocabulary.iri, new IRITemplateFactory(null), columnFct, templateFct);
			this.constantFct = constantFct;
		}

		@Override
		public T forConstant(String value) {
			return constantFct.apply(rdfFactory.createIRI(value));
		}
	}

	private class LiteralTermMapFactory<T extends ObjectMap> extends TermMapFactory<T> {
		private final RDFDatatype datatype;
		private final Function<Literal, T> constantFct;

		LiteralTermMapFactory(RDFDatatype datatype, Function<String, T> columnFct, Function<Template, T> templateFct, Function<Literal, T> constantFct) {
			super(R2RMLVocabulary.literal, new LiteralTemplateFactory(null, null), columnFct, templateFct);
			this.datatype = datatype;
			this.constantFct = constantFct;
		}

		@Override
		public T forConstant(String value) {
			Literal literal = datatype.getLanguageTag()
					.map(lang -> rdfFactory.createLiteral(value, lang.getFullString()))
					.orElseGet(() -> rdfFactory.createLiteral(value, datatype.getIRI()));

			return constantFct.apply(literal);
		}
		@Override
		protected T forColumn(Variable term) {
			return setDatatype(super.forColumn(term), datatype);
		}
		@Override
		protected T forTemplate(ImmutableFunctionalTerm term) {
			return setDatatype(super.forTemplate(term), datatype);
		}

		private T setDatatype(T objectMap, RDFDatatype datatype) {
			Optional<LanguageTag> optionalLangTag = datatype.getLanguageTag();
			if (optionalLangTag.isPresent())
				objectMap.setLanguageTag(optionalLangTag.get().getFullString());
			else if (!datatype.getIRI().equals(RDFS.LITERAL))
				objectMap.setDatatype(datatype.getIRI());
			return objectMap;
		}
	}

	/**
	 * TODO: shall we consider as an internal bug or differently?
	 */
	static class R2RMLSerializationException extends OntopInternalBugException {
		private R2RMLSerializationException(String message) {
			super(message);
		}
	}
}
