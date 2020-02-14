package it.unibz.inf.ontop.protege.gui.treemodels;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;

public class MappingBasedTreeModelFilter extends TreeModelFilter<SQLPPTriplesMap> {

    public MappingBasedTreeModelFilter() {
        super.bNegation = false;
    }

    @Override
    public boolean match(SQLPPTriplesMap object) {
        ImmutableList<TargetAtom> atoms = object.getTargetAtoms();

        boolean isMatch = false;
        for (String keyword : vecKeyword) {
            for (TargetAtom atom : atoms) {
                isMatch = isMatch || TreeModelTools.match(keyword.trim(), atom);
            }
            if (isMatch) {
                break; // end loop if a match is found!
            }
        }
        return bNegation != isMatch;
    }

}
