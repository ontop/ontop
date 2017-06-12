package it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.pivotalrepr.ConstructionNode;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.UnionNode;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

/*
Used to retrieve the IRI involved in a same as mapping.
 */
public class SameAsIRIsExtractor   {


    private IntermediateQuery definition;
    private boolean objectProperty;

    public SameAsIRIsExtractor(IntermediateQuery definition) {

        this.definition = definition;


    }


    private ImmutableSet<ImmutableTerm> extractIRIsFromUnionNode(IntermediateQuery query, UnionNode currentNode) {

        Stream<ImmutableTerm> childrenBindings = query.getChildrenStream(currentNode)
                .flatMap(c -> extractIRIs(c));

        return childrenBindings.collect(ImmutableCollectors.toSet());


    }

    public Optional<ImmutableSet<ImmutableTerm>> getIRIs() {

        ConstructionNode rootConstructionNode = definition.getRootConstructionNode();
        if(rootConstructionNode.getSubstitution().isEmpty())
        {
            Optional<QueryNode> firstChild = definition.getFirstChild(rootConstructionNode);
            if (firstChild.isPresent()){
                QueryNode node =firstChild.get();
                if (node instanceof UnionNode){
                    //get iris in the union
                    return Optional.of(extractIRIsFromUnionNode(definition, (UnionNode) node));
                }
//                else{
//                    if (node instanceof ConstructionNode)
//                        return Optional.of(extractIRIs(node)
//                                .collect(ImmutableCollectors.toSet()));
//                }
            }

            return Optional.empty();
        }
        else{
            return Optional.of(extractIRIs(rootConstructionNode)
                    .collect(ImmutableCollectors.toSet()));
        }



    }

    /**
     * Extract the iris from the construction node, searching for its bindings and getting only the iri
     */
    private Stream<ImmutableTerm> extractIRIs(QueryNode currentNode) {

        if(currentNode instanceof ConstructionNode) {
            ConstructionNode constructionNode = (ConstructionNode) currentNode;
            ImmutableCollection<ImmutableTerm> localBindings = constructionNode.getSubstitution()
                    .getImmutableMap().values();

            // if it has two IRIs it's an object Property
            objectProperty = localBindings.stream().allMatch(v -> ((ImmutableFunctionalTerm) v).isDataFunction()) && localBindings.size() == 2;

            return localBindings.stream().map(v -> ((ImmutableFunctionalTerm) v).getTerm(0));
        }
        else {
            throw new IllegalStateException("Construction node is missing ");
        }
    }


    public boolean isObjectProperty(){
        return objectProperty;
    }




}
