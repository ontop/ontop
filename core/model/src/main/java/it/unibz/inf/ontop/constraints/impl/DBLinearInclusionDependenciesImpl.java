package it.unibz.inf.ontop.constraints.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.ForeignKeyConstraint;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

public class DBLinearInclusionDependenciesImpl extends BasicLinearInclusionDependenciesImpl<RelationPredicate> {

    private final AtomFactory atomFactory;
    private final VariableGenerator variableGenerator;

    public DBLinearInclusionDependenciesImpl(CoreUtilsFactory coreUtilsFactory,
                                              AtomFactory atomFactory) {
        this.atomFactory = atomFactory;
        this.variableGenerator = coreUtilsFactory.createVariableGenerator(ImmutableSet.of());
    }

    @Override
    protected Stream<DataAtom<RelationPredicate>> chase(DataAtom<RelationPredicate> atom) {
        return atom.getPredicate().getRelationDefinition().getForeignKeys().stream()
                .map(fk -> chase(fk, atom))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Optional<DataAtom<RelationPredicate>> chase(ForeignKeyConstraint fk, DataAtom<RelationPredicate> atom) {

        // TODO: better handling required
        if (fk.getComponents().stream().anyMatch(c -> c.getAttribute().isNullable()))
            return Optional.empty();

        ImmutableMap<Attribute, VariableOrGroundTerm> inversion = fk.getComponents().stream()
                .collect(ImmutableCollectors.toMap(
                        ForeignKeyConstraint.Component::getReferencedAttribute,
                        c -> atom.getArguments().get(c.getAttribute().getIndex() - 1)));

        ImmutableList<VariableOrGroundTerm> newArguments = fk.getReferencedRelation().getAttributes().stream()
                .map(a -> inversion.getOrDefault(a, variableGenerator.generateNewVariable(a.getID().getName())))
                .collect(ImmutableCollectors.toList());

        return Optional.of(atomFactory.getDataAtom(fk.getReferencedRelation().getAtomPredicate(), newArguments));
    }

    @Override
    protected void registerVariables(DataAtom<RelationPredicate> atom) {
        variableGenerator.registerAdditionalVariables(atom.getVariables());
    }

    @Override
    public String toString() {
        return "DB LIDs";
    }
}
