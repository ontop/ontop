package it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

/*
Used to retrieve the IRI involved in a same as mapping.
Consider only simple mappings. The IRIs are retrieved from the bindings of the root construction node
or from the children construction nodes of a union
*/

public class SameAsIRIsExtractor {


    private IntermediateQuery definition;
    private boolean isObjectProperty;

    public SameAsIRIsExtractor(IntermediateQuery definition) {

        this.definition = definition;


    }

    /*
    Get IRIs from all children (construction nodes) of the union
     */

    private ImmutableSet<ImmutableTerm> extractIRIsFromUnionNode(IntermediateQuery query, UnionNode currentNode) {

        Stream<ImmutableTerm> childrenBindings = query.getChildrenStream(currentNode)
                .flatMap(c -> extractIRIs(c));

        return childrenBindings.collect(ImmutableCollectors.toSet());


    }

    /*
    Returns a set with the IRIs extracted from the mapping definition
     */
    public ImmutableSet<ImmutableTerm> getIRIs() {
        return getIRIs(definition.getRootConstructionNode(), definition)
                .collect(ImmutableCollectors.toSet());
    }
//        ConstructionNode rootConstructionNode = definition.getRootConstructionNode();
//        if (rootConstructionNode.getSubstitution().isEmpty()) {
//            Optional<QueryNode> firstChild = definition.getFirstChild(rootConstructionNode);
//            if (firstChild.isPresent()) {
//                QueryNode node = firstChild.get();
//                if (node instanceof UnionNode) {
//                    //get iris in the union
//                    return Optional.of(extractIRIsFromUnionNode(definition, (UnionNode) node));
//                }
//            }
//
//            return Optional.empty();
//        } else {
//            return Optional.of(extractIRIs(rootConstructionNode)
//                    .collect(ImmutableCollectors.toSet()));
//        }
//
//
//    }

    /**
     * Recursive
     */
    private Stream<ImmutableTerm> getIRIs(QueryNode currentNode, IntermediateQuery query) {
        return Stream.concat(
                query.getChildren(currentNode).stream()
                        .flatMap(n -> getIRIs(
                                n,
                                query
                        )),
                extractIRIs(currentNode)
        );
    }

    /**
     * Extract the IRIs from the construction node, searching through its bindings and getting only the IRI
     */
    private Stream<ImmutableTerm> extractIRIs(QueryNode currentNode) {

        if (currentNode instanceof ConstructionNode) {
            ConstructionNode constructionNode = (ConstructionNode) currentNode;
            ImmutableCollection<ImmutableTerm> localBindings = constructionNode.getSubstitution()
                    .getImmutableMap().values();

            // if it has two IRIs it's an object Property
            isObjectProperty = localBindings.stream().allMatch(v -> ((ImmutableFunctionalTerm) v).isDataFunction()) && localBindings.size() == 2;

            return localBindings.stream().map(v -> ((ImmutableFunctionalTerm) v).getTerm(0))
                    //filter out the variables
                    .filter(v -> v instanceof ValueConstant);
        } else {
            throw new IllegalStateException("Construction node is missing ");
        }
    }


    /*
    Return true when the property is an object property (IRI in subject and object part)
     */

    public boolean isObjectProperty() {
        return isObjectProperty;
    }


}
