package it.unibz.inf.ontop.query.translation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.OntopInvalidKGQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.term.GroundTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.query.translation.impl.RDF4JTupleExprTranslator;
import it.unibz.inf.ontop.query.translation.impl.RDF4JValueExprTranslator;
import it.unibz.inf.ontop.query.translation.impl.RDF4JValueTranslator;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.RDF;
import org.eclipse.rdf4j.model.Triple;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.algebra.TripleRef;
import org.eclipse.rdf4j.query.algebra.ValueExprTripleRef;
import org.eclipse.rdf4j.query.algebra.Var;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Objects;

public class RDF4JRDFStarSupportTest {

        private static final Injector INJECTOR = createInjector();

        @SuppressWarnings("null")
        private static Injector createInjector() {
                OntopModelConfiguration configuration = OntopModelConfiguration.defaultBuilder()
                                .enableTestMode()
                                .build();
                return configuration.getInjector();
        }
    private static final CoreSingletons CORE_SINGLETONS = INJECTOR.getInstance(CoreSingletons.class);
    private static final TermFactory TERM_FACTORY = INJECTOR.getInstance(TermFactory.class);
    private static final TypeFactory TYPE_FACTORY = INJECTOR.getInstance(TypeFactory.class);
    private static final RDF RDF_FACTORY = INJECTOR.getInstance(RDF.class);
    private static final FunctionSymbolFactory FUNCTION_SYMBOL_FACTORY = INJECTOR.getInstance(FunctionSymbolFactory.class);
    private static final CoreUtilsFactory CORE_UTILS_FACTORY = INJECTOR.getInstance(CoreUtilsFactory.class);

    @Test
    public void valueTranslatorConvertsEmbeddedTriple() {
        RDF4JValueTranslator translator = new RDF4JValueTranslator(TERM_FACTORY, RDF_FACTORY, TYPE_FACTORY);
        ValueFactory vf = SimpleValueFactory.getInstance();

        Triple tripleValue = vf.createTriple(
                vf.createIRI("http://example.org/s"),
                vf.createIRI("http://example.org/p"),
                vf.createLiteral("hello", "en"));

        GroundTerm term = translator.getTermForLiteralOrIri(tripleValue);

        GroundTerm expectedSubject = TERM_FACTORY.getConstantIRI(RDF_FACTORY.createIRI("http://example.org/s"));
        GroundTerm expectedPredicate = TERM_FACTORY.getConstantIRI(RDF_FACTORY.createIRI("http://example.org/p"));
        GroundTerm expectedObject = TERM_FACTORY.getRDFLiteralConstant("hello", "en");
        ImmutableTerm expected = TERM_FACTORY.getRDFStarTripleFunctionalTerm(
                expectedSubject,
                expectedPredicate,
                expectedObject).simplify();

        assertEquals(expected, term);
    }

    @Test
    public void valueExprTripleRefBuildsFunctionalTerm() {
        Variable s = TERM_FACTORY.getVariable("s");
        Variable p = TERM_FACTORY.getVariable("p");
        Variable o = TERM_FACTORY.getVariable("o");
        VariableGenerator variableGenerator = CORE_UTILS_FACTORY.createVariableGenerator(ImmutableSet.of());

        RDF4JValueExprTranslator translator = new RDF4JValueExprTranslator(
                ImmutableSet.of(s, p, o),
                ImmutableMap.of(),
                false,
                TERM_FACTORY,
                RDF_FACTORY,
                TYPE_FACTORY,
                FUNCTION_SYMBOL_FACTORY,
                variableGenerator);

        ValueExprTripleRef tripleExpr = new ValueExprTripleRef("t", new Var("s"), new Var("p"), new Var("o"));

        RDF4JValueExprTranslator.ExistsMapAnnotatedObject<ImmutableTerm> result = translator.getTerm(tripleExpr);
        ImmutableTerm expected = TERM_FACTORY.getRDFStarTripleFunctionalTerm(s, p, o);

        assertEquals(expected, result.get());
    }

    @Test
        public void tupleTranslatorBindsTripleVariable() throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException {
        RDF4JTupleExprTranslator translator = new RDF4JTupleExprTranslator(
                ImmutableMap.of(),
                null,
                false,
                CORE_SINGLETONS,
                RDF_FACTORY,
                CORE_SINGLETONS.getIQTreeTools());

        TripleRef tripleRef = new TripleRef();
        tripleRef.setSubjectVar(new Var("s"));
        tripleRef.setPredicateVar(new Var("p"));
        tripleRef.setObjectVar(new Var("o"));
        tripleRef.setExprVar(new Var("t"));

        IQTree tree = translator.getTree(tripleRef);
        assertTrue(tree.getRootNode() instanceof ConstructionNode);

        ConstructionNode constructionNode = (ConstructionNode) tree.getRootNode();
        Variable tripleVar = Objects.requireNonNull(TERM_FACTORY.getVariable("t"));
        assertEquals(
                TERM_FACTORY.getRDFStarTripleFunctionalTerm(
                        TERM_FACTORY.getVariable("s"),
                        TERM_FACTORY.getVariable("p"),
                        TERM_FACTORY.getVariable("o")),
                constructionNode.getSubstitution().get(tripleVar));
        ImmutableSet<Variable> projectedVariables = tree.getVariables();
        assertTrue(projectedVariables.contains(tripleVar));
    }
}
