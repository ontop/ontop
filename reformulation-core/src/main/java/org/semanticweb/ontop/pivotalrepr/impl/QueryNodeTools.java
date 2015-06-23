package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableMap;
import fj.P;
import fj.P2;
import org.semanticweb.ontop.model.GroundTerm;
import org.semanticweb.ontop.model.ImmutableFunctionalTerm;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.model.impl.GroundTermTools;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;

import java.util.Map;

/**
 * TODO: describe
 */
public class QueryNodeTools {

    @Deprecated
    public static P2<ImmutableSubstitution<GroundTerm>, ImmutableSubstitution<ImmutableFunctionalTerm>> splitBindings(
            ImmutableMap<VariableImpl, ImmutableTerm> allBindings) {

        ImmutableMap.Builder<VariableImpl, GroundTerm> groundBindingBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<VariableImpl, ImmutableFunctionalTerm> functionalBindingBuilder = ImmutableMap.builder();

        for (Map.Entry<VariableImpl, ImmutableTerm> entry : allBindings.entrySet()) {
            ImmutableTerm value = entry.getValue();

            /**
             * Ground term
             */
            if (GroundTermTools.isGroundTerm(value)) {
                groundBindingBuilder.put(entry.getKey(), GroundTermTools.castIntoGroundTerm(value));
            }
            /**
             * Non-ground functional term
             */
            else if (value instanceof ImmutableFunctionalTerm) {
                functionalBindingBuilder.put(entry.getKey(), (ImmutableFunctionalTerm) value);
            }
            else {
                throw new IllegalArgumentException("Illegal term (probably a variable) found in a binding: " + value
                        + "\n Note that var-to-var are not bindings but renamings");
            }
        }
        ImmutableSubstitution<GroundTerm> groundTermBindings = new ImmutableSubstitutionImpl<>(
                groundBindingBuilder.build());
        ImmutableSubstitution<ImmutableFunctionalTerm> functionalBindings = new ImmutableSubstitutionImpl<>(
                functionalBindingBuilder.build());

        return P.p(groundTermBindings, functionalBindings);
    }
}
