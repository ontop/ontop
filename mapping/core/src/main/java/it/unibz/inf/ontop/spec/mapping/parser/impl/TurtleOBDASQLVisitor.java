package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import org.antlr.v4.runtime.tree.RuleNode;
import org.apache.commons.rdf.api.RDF;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Stateful!
 */
public class TurtleOBDASQLVisitor extends TurtleOBDABaseVisitor<Stream<TargetAtom>> implements TurtleOBDAVisitor<Stream<TargetAtom>> {

    private ImmutableTerm currentGraph;
    private ImmutableTerm currentSubject;
    private ImmutableTerm currentPredicate;

    private final TargetAtomFactory targetAtomFactory;
    private final TurtleOBDASQLTermVisitor turtleOBDASQLTermVisitor;

    protected TurtleOBDASQLVisitor(TermFactory termFactory, TypeFactory typeFactory,
                                        TargetAtomFactory targetAtomFactory, RDF rdfFactory,
                                        OntopMappingSettings settings,
                                   ImmutableMap<String, String> prefixes) {
        this.targetAtomFactory = targetAtomFactory;
        this.turtleOBDASQLTermVisitor = new TurtleOBDASQLTermVisitor(termFactory, rdfFactory, typeFactory, settings, prefixes);
    }

    @Override
    public Stream<TargetAtom> visitChildren(RuleNode node) {
        return IntStream.range(0, node.getChildCount())
                .mapToObj(node::getChild)
                .flatMap(c -> c.accept(this));
    }

    @Override
    public Stream<TargetAtom> visitParse(TurtleOBDAParser.ParseContext ctx) {
        return Stream.concat(// first process triples with currentGraph = null
                ctx.triplesStatement().stream().flatMap(this::visitTriplesStatement),
                ctx.quadsStatement().stream().flatMap(this::visitQuadsStatement));
    }

    @Override
    public Stream<TargetAtom> visitQuadsStatement(TurtleOBDAParser.QuadsStatementContext ctx) {
        currentGraph = ctx.graph().accept(turtleOBDASQLTermVisitor);
        return visitChildren(ctx);
    }

    @Override
    public Stream<TargetAtom> visitTriples(TurtleOBDAParser.TriplesContext ctx) {
        currentSubject = ctx.subject().accept(turtleOBDASQLTermVisitor);
        return visitChildren(ctx);
    }

    @Override
    public Stream<TargetAtom> visitPredicateObject(TurtleOBDAParser.PredicateObjectContext ctx) {
        currentPredicate = ctx.verb().accept(turtleOBDASQLTermVisitor);
        return visitChildren(ctx);
    }

    @Override
    public Stream<TargetAtom> visitObject(TurtleOBDAParser.ObjectContext ctx) {
        ImmutableTerm object = ctx.accept(turtleOBDASQLTermVisitor);
        TargetAtom atom = currentGraph == null
                ? targetAtomFactory.getTripleTargetAtom(currentSubject, currentPredicate, object)
                : targetAtomFactory.getQuadTargetAtom(currentSubject, currentPredicate, object, currentGraph);
        return Stream.of(atom);
    }
}
