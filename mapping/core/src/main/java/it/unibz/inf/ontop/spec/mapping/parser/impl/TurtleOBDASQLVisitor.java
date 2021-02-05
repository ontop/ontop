package it.unibz.inf.ontop.spec.mapping.parser.impl;

import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import org.antlr.v4.runtime.tree.RuleNode;

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
    private final TurtleOBDAVisitor<ImmutableTerm> turtleOBDASQLTermVisitor;

    protected TurtleOBDASQLVisitor(TargetAtomFactory targetAtomFactory, TurtleOBDAVisitor<ImmutableTerm> turtleOBDASQLTermVisitor) {
        this.targetAtomFactory = targetAtomFactory;
        this.turtleOBDASQLTermVisitor = turtleOBDASQLTermVisitor;
    }

    @Override
    public Stream<TargetAtom> visitChildren(RuleNode node) {
        return IntStream.range(0, node.getChildCount())
                .mapToObj(node::getChild)
                .flatMap(c -> c.accept(this));
    }

    @Override
    public Stream<TargetAtom> visitTriplesStatement(TurtleOBDAParser.TriplesStatementContext ctx) {
        currentGraph = null;
        return visitChildren(ctx);
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
    public Stream<TargetAtom> visitPredicateResource(TurtleOBDAParser.PredicateResourceContext ctx) {
        currentPredicate = ctx.accept(turtleOBDASQLTermVisitor);
        return null;
    }

    @Override
    public Stream<TargetAtom> visitPredicateRdfType(TurtleOBDAParser.PredicateRdfTypeContext ctx) {
        currentPredicate = ctx.accept(turtleOBDASQLTermVisitor);
        return null;
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
