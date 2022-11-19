package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLFunctionSymbol;
import it.unibz.inf.ontop.model.vocabulary.GEO;
import it.unibz.inf.ontop.model.vocabulary.GEOF;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.IRI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * The class recursively expands the geosparql query rewrite intensional nodes with the respective feature and
 * geometry rdf triples.
 */
public abstract class GeoQueryRewriterTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

    protected final AtomFactory atomFactory;
    protected final VariableGenerator variableGenerator;
    protected final TermFactory termFactory;
    protected final FunctionSymbolFactory functionSymbolFactory;

    @Inject
    protected GeoQueryRewriterTransformer(IntermediateQueryFactory iqFactory,
                                          AtomFactory atomFactory,
                                          VariableGenerator variableGenerator,
                                          TermFactory termFactory,
                                          FunctionSymbolFactory functionSymbolFactory) {
        super(iqFactory);
        this.atomFactory = atomFactory;
        this.variableGenerator = variableGenerator;
        this.termFactory = termFactory;
        this.functionSymbolFactory = functionSymbolFactory;
    }

    @Override
    public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {


        // STEP 1 - Add the new children
        ImmutableList<IntensionalDataNode> intchildren = children.stream()
                .map(c -> (IntensionalDataNode) c)
                .collect(ImmutableCollectors.toList());
        ImmutableList<IntensionalDataNode> newchildren = addGeoIntensionalTriples(intchildren, variableGenerator);
        ImmutableList.Builder<IntensionalDataNode> builderBGP = ImmutableList.builder();
        ImmutableList.Builder<IQTree> builderChildren = ImmutableList.builder();
        for (IQTree child : newchildren) {
            if (child.getRootNode() instanceof IntensionalDataNode) {
                builderBGP.add((IntensionalDataNode)child);
            }
            else {
                addTransformedBGP(builderChildren, builderBGP.build());
                builderBGP = ImmutableList.builder();
                builderChildren.add(child.acceptTransformer(this));
            }
        }
        addTransformedBGP(builderChildren, builderBGP.build());

        // STEP 2 - Retrieve the geospatial variables
        ImmutableList<? extends VariableOrGroundTerm> geoVariables = children.stream()
                .filter(n -> n instanceof IntensionalDataNode)
                .map(a -> ((IntensionalDataNode) a).getProjectionAtom().getArguments())
                .filter(n -> (GEO.QUERY_REWRITE_FUNCTIONS.toString()).contains(n.get(1).toString()))
                .flatMap(Collection::stream)
                .collect(ImmutableCollectors.toList());

        Optional<? extends VariableOrGroundTerm> geoVar0 = getSpatialVariable(intchildren, newchildren, 0, geoVariables);
        Optional<? extends VariableOrGroundTerm> geoVar1 = getSpatialVariable(intchildren, newchildren, 2, geoVariables);

        // STEP 3 - Update the root node with the new geospatial filter

        // Create RDF_2_DB_BOOL functor
        BooleanFunctionSymbol booleanFunctor = functionSymbolFactory.getRDF2DBBooleanFunctionSymbol();

        // Create GEOF_# functor
        // We need to use getIRIString() without >, <
        String geofFunctionSymbolString = geoVariables.get(1).toString().replace(GEO.PREFIX, GEOF.PREFIX)
                .replace("<","")
                .replace(">","");
        Optional<SPARQLFunctionSymbol> geofunctor = functionSymbolFactory.getSPARQLFunctionSymbol(geofFunctionSymbolString, 2);

        // Construct new geospatial filter
        ImmutableFunctionalTerm geofFilter = termFactory.getImmutableFunctionalTerm(geofunctor.get(),
                geoVar0.get(),
                geoVar1.get());
        ImmutableExpression geofBooleanFilter = termFactory.getImmutableExpression(booleanFunctor, geofFilter);

        // Combine geospatial filter with any existing filters (if such a filter exists)
        FunctionSymbol defaultAndFunctor = functionSymbolFactory.getDBFunctionSymbolFactory().getDBAnd(2);
        ImmutableExpression newFilter = rootNode.getOptionalFilterCondition().isPresent()
                ? (ImmutableExpression) termFactory.getNonGroundFunctionalTerm(defaultAndFunctor,
                rootNode.getOptionalFilterCondition().get(),
                geofBooleanFilter)
                : geofBooleanFilter;

        // Modify inner join node filter
        InnerJoinNode newRootNode = rootNode.changeOptionalFilterCondition(Optional.ofNullable(newFilter));

        return formInnerJoin(builderChildren.build(), newRootNode.getOptionalFilterCondition());
    }

    @Override
    public IQTree transformIntensionalData(IntensionalDataNode intensionalDataNode) {
        return formInnerJoin(transformBGP(ImmutableList.of(intensionalDataNode)), Optional.empty());
    }

    private IQTree formInnerJoin(ImmutableList<IQTree> list, Optional<ImmutableExpression> filter) {
        switch (list.size()) {
            case 0:
                throw new IllegalStateException("All triple patterns of BGP have been eliminated by the transformation");
            case 1:
                if (filter.isPresent())
                    return iqFactory.createUnaryIQTree(
                            iqFactory.createFilterNode(filter.get()),
                            list.get(0));
                else return list.get(0);
            default:
                return iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(filter), list);
        }
    }

    /**
     * Add one intensional node per geometry
     * Add one intensional node if applicable per feature
     */
    private ImmutableList<IntensionalDataNode> addGeoIntensionalTriples(ImmutableList<IntensionalDataNode> bgp,
                                                                        VariableGenerator variableGenerator) {
        ArrayList<IntensionalDataNode> list = new ArrayList<>(bgp); // mutable copy

        // Retrieve the geosparql query rewrite node
        ImmutableList<DataAtom> geoRewriteStatement = list.stream()
                .filter(n -> (isGeospatial(n.getProjectionAtom().getArguments().get(1).toString())))
                .map(n -> (DataAtom) n.getProjectionAtom())
                .collect(ImmutableCollectors.toList());
        ImmutableList<IntensionalDataNode> geoRewriteNode = list.stream()
                .filter(n -> (isGeospatial(n.getProjectionAtom().getArguments().get(1).toString())))
                .collect(ImmutableCollectors.toList());

        for (DataAtom<RDFAtomPredicate> atom : geoRewriteStatement) {

            // LOGIC
            // 1. Check arg 1, hasdefaultgeometry
            // 2. If geometry add one intensional node
            // 3. If feature add two intensional nodes
            // NOTE: No check on whether subject is suclass of Feature

            // Geometry check case. Only HASDEFAULTGEOMETRY considered as predicate
            // If HASDEFAULTGEOMETRY is not used, we assume the geosparql query rewrite extension applies to a Feature
            boolean firstgeom = checkGeoDatatypes(list, atom, GEO.HASDEFAULTGEOMETRY, 0);
            boolean secondgeom = checkGeoDatatypes(list, atom, GEO.HASDEFAULTGEOMETRY, 2);

            Variable newExistentialVar0 = variableGenerator.generateNewVariable();
            Variable newExistentialVar1 = variableGenerator.generateNewVariable();

            if (firstgeom) { //else subject of georewrite node is a feature not geometry
                // CASE 1 - Geometry
                IntensionalDataNode newGeometryNode = generateGeometryNode(atom, 0, newExistentialVar0);
                list.add(newGeometryNode);
            } else {
                // CASE 2 - Feature
                Variable newGeomExistentialVar0 = variableGenerator.generateNewVariable();
                ImmutableList<IntensionalDataNode> newFeatureNodes = generateFeatureNodes(atom, 0,
                        newGeomExistentialVar0,
                        newExistentialVar0);
                list.addAll(newFeatureNodes);
            }

            if (secondgeom) { //else object of georewrite node is a feature not geometry
                // CASE 1 - Geometry
                IntensionalDataNode newGeometryNode = generateGeometryNode(atom, 2, newExistentialVar1);
                list.add(newGeometryNode);
            } else {
                // CASE 2 - Feature
                Variable newGeomExistentialVar1 = variableGenerator.generateNewVariable();
                ImmutableList<IntensionalDataNode> newFeatureNodes = generateFeatureNodes(atom, 2,
                        newGeomExistentialVar1,
                        newExistentialVar1);
                list.addAll(newFeatureNodes);
            }
        }
        list.removeAll(geoRewriteNode);
        return ImmutableList.copyOf(list);
    }

    /**
     * Check whether geospatial translation is necessary - GeoSPARQL query rewrite extension
     * Condition : Any geo:{topological_function} used
     */

    protected boolean isGeospatial(String query) {

        return GEO.QUERY_REWRITE_FUNCTIONS.stream()
                .map(g -> g.getIRIString().toUpperCase())
                .anyMatch(query.toUpperCase()::contains);
    }

    /**
     * Method checks whether HASDEFAULTGEOMETRY is present thus, the subject or object are geometries or features
     */
    private boolean checkGeoDatatypes(ArrayList<IntensionalDataNode> listIntensionalNodes,
                                      DataAtom<RDFAtomPredicate> atom,
                                      IRI iri,
                                      Integer georewritenodeposition) {

        // If feature, we check for the triple object; if hasDefaultGeometry, we check for the predicate
        Integer spatialobjpositionintriple = iri.equals(GEO.HASDEFAULTGEOMETRY)
                ? 1
                : 2;

        // If feature, the subject value is the argument, if hasDefaultGeomtry, the object value matters
        Integer geovariableargumentposition = iri.equals(GEO.HASDEFAULTGEOMETRY)
                ? 2
                : 0;

        // georewritenodeposition = 0 --> first argument, = 2 --> second argument
        return (listIntensionalNodes.stream()
                .map(n -> (DataAtom) n.getProjectionAtom())
                .filter(a -> a.getArguments().get(spatialobjpositionintriple).toString().replace("<","")
                        .replace(">","").equals(iri.getIRIString()) &&
                        a.getArguments().get(geovariableargumentposition).equals(atom.getArguments().get(georewritenodeposition)))
                .count()) == 1;
    }

    /**
     * Method returns the geometry or feature variables part of the geospatial boolean expression
     * @return Geometry or Feature variables
     */

    private Optional<? extends VariableOrGroundTerm> getSpatialVariable(ImmutableList<IntensionalDataNode> children,
                                                                        ImmutableList<IntensionalDataNode> newchildren,
                                                                        Integer argumentnumber,
                                                                        ImmutableList<? extends VariableOrGroundTerm> geoVariables) {

        ImmutableList<? extends ImmutableList<? extends VariableOrGroundTerm>> originalNodes = children.stream()
                .filter(n -> n instanceof IntensionalDataNode)
                .map(a -> ((IntensionalDataNode) a).getProjectionAtom().getArguments())
                .collect(ImmutableCollectors.toList());

        ImmutableList<? extends ImmutableList<? extends VariableOrGroundTerm>> updatedNodes = newchildren.stream()
                .filter(n -> n instanceof IntensionalDataNode)
                .map(a -> ((IntensionalDataNode) a).getProjectionAtom().getArguments())
                .collect(ImmutableCollectors.toList());

        Optional<Boolean> hasGeometryPresent = originalNodes.stream()
                .filter(n -> (geoVariables.get(argumentnumber)).equals(n.get(2)))
                .map(n -> n.get(1).toString().equals("<"+GEO.HASDEFAULTGEOMETRY.getIRIString()+">"))
                .findFirst();

        if (hasGeometryPresent.isPresent() && hasGeometryPresent.get()) {
            // CASE 1 - Geometry
            return updatedNodes.stream()
                    .filter(n -> (geoVariables.get(argumentnumber)).equals(n.get(0)))
                    .map(n -> n.get(2))
                    .findFirst();
        } else {
            // CASE 2 - Feature
            Optional<? extends VariableOrGroundTerm> geometryVar = updatedNodes.stream()
                    .filter(n -> (geoVariables.get(argumentnumber)).equals(n.get(0)) &&
                            n.get(1).toString().equals("<"+GEO.HASDEFAULTGEOMETRY.getIRIString()+">"))
                    .map(n -> n.get(2))
                    .findFirst();

            return updatedNodes.stream()
                    .filter(n -> geometryVar.get().equals(n.get(0)) &&
                            n.get(1).toString().equals("<"+GEO.GEO_AS_WKT.getIRIString()+">"))
                    .map(n -> n.get(2))
                    .findFirst();
        }
    }

    // Adds one intensional node in the case of a Geometry
    private IntensionalDataNode generateGeometryNode(DataAtom<RDFAtomPredicate> atom,
                                                     Integer position,
                                                     Variable existentialVarWktLiteral) {

        DataAtom<AtomPredicate> WKTLiteralAtom = atomFactory.getIntensionalTripleAtom(
                atom.getArguments().get(position),
                GEO.GEO_AS_WKT,
                existentialVarWktLiteral);
        return iqFactory.createIntensionalDataNode(WKTLiteralAtom);
    }

    // Adds two intensional nodes in the case of a Feature
    private ImmutableList<IntensionalDataNode> generateFeatureNodes(DataAtom<RDFAtomPredicate> atom,
                                                                    Integer position,
                                                                    Variable existentialVarGeometry,
                                                                    Variable existentialVarWktLiteral) {

        DataAtom<AtomPredicate> geomAtom = atomFactory.getIntensionalTripleAtom(
                atom.getArguments().get(position),
                GEO.HASDEFAULTGEOMETRY,
                existentialVarGeometry);
        DataAtom<AtomPredicate> WKTLiteralAtom = atomFactory.getIntensionalTripleAtom(
                existentialVarGeometry,
                GEO.GEO_AS_WKT,
                existentialVarWktLiteral);
        return ImmutableList.of(iqFactory.createIntensionalDataNode(geomAtom),
                iqFactory.createIntensionalDataNode(WKTLiteralAtom));
    }

    private void addTransformedBGP(ImmutableList.Builder<IQTree> builderChildren, ImmutableList<IntensionalDataNode> currentBGP) {
        if (!currentBGP.isEmpty())
            builderChildren.addAll(transformBGP(currentBGP));
    }

    protected abstract ImmutableList<IQTree> transformBGP(ImmutableList<IntensionalDataNode> bgp);
}
