package it.unibz.inf.ontop.answering.logging.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.visit.impl.AbstractPredicateExtractor;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.stream.Stream;

@Singleton
public class ClassAndPropertyExtractor extends AbstractPredicateExtractor<IntensionalDataNode> {

    @Inject
    protected ClassAndPropertyExtractor() {
    }

    public ClassesAndProperties extractClassesAndProperties(IQ iq) {
        ImmutableList<DataAtom<RDFAtomPredicate>> atoms = iq.getTree().acceptVisitor(this)
                .map(IntensionalDataNode::getProjectionAtom)
                .filter(a -> a.getPredicate() instanceof RDFAtomPredicate)
                .map(a -> (DataAtom<RDFAtomPredicate>)(DataAtom<?>) a)
                .collect(ImmutableCollectors.toList());

        ImmutableSet<IRI> classes = atoms.stream()
                .map(a -> a.getPredicate().getClassIRI(a.getArguments()))
                .flatMap(o -> o.map(Stream::of)
                        .orElseGet(Stream::empty))
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<IRI> properties = atoms.stream()
                .map(a -> a.getPredicate().getPropertyIRI(a.getArguments()))
                .flatMap(o -> o.map(Stream::of)
                        .orElseGet(Stream::empty))
                .filter(p -> !p.equals(RDF.TYPE))
                .collect(ImmutableCollectors.toSet());

        return new ClassesAndProperties(classes, properties);
    }

    @Override
    public Stream<IntensionalDataNode> visitIntensionalData(IntensionalDataNode dataNode) {
        return Stream.of(dataNode);
    }

    @Override
    public Stream<IntensionalDataNode> visitExtensionalData(ExtensionalDataNode dataNode) {
        return Stream.empty();
    }

    public static class ClassesAndProperties {
        private final ImmutableSet<IRI> classes;
        private final ImmutableSet<IRI> properties;

        protected ClassesAndProperties(ImmutableSet<IRI> classes, ImmutableSet<IRI> properties) {
            this.classes = classes;
            this.properties = properties;
        }

        public ImmutableSet<IRI> getClasses() {
            return classes;
        }

        public ImmutableSet<IRI> getProperties() {
            return properties;
        }
    }


}
