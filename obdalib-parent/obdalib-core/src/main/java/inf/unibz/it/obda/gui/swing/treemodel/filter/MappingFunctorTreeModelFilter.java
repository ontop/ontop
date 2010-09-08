package inf.unibz.it.obda.gui.swing.treemodel.filter;

import java.util.ArrayList;

import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.FunctionTerm;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.domain.VariableTerm;

/**
 * @author This filter receives a string and returns true if any mapping
 *         contains the functor in some of the atoms in the head
 * 
 */

public class MappingFunctorTreeModelFilter implements
		TreeModelFilter<OBDAMappingAxiom> {

	private String strMappingFunctor = "";

	/**
	 * @param strMappingFunctor
	 *            Constructor of the function
	 */
	public MappingFunctorTreeModelFilter(String strMappingFunctor) {
		this.strMappingFunctor = strMappingFunctor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * inf.unibz.it.obda.gui.swing.treemodel.filter.TreeModelFilter#match(java
	 * .lang.Object)
	 */
	@Override
	public boolean match(OBDAMappingAxiom object) {
		boolean filterValue = false;
		OBDAMappingAxiom mapping = (OBDAMappingAxiom) object;
		ConjunctiveQuery headquery = (ConjunctiveQuery) mapping
				.getTargetQuery();
		ArrayList<QueryAtom> atoms = headquery.getAtoms();

		for (int i = 0; i < atoms.size(); i++) {
			QueryAtom atom = atoms.get(i);
			
			ArrayList<QueryTerm> queryTerms = atom.getTerms();

			for (int j = 0; j < queryTerms.size(); j++) {
				QueryTerm term = queryTerms.get(j);
				
				if (term instanceof FunctionTerm) {
					FunctionTerm functionTerm = (FunctionTerm) term;
					if(functionTerm.toString().indexOf(strMappingFunctor)!= -1)
					{
						filterValue = true;	
					}
					
				}
				
				if (term instanceof VariableTerm) {
					VariableTerm variableTerm = (VariableTerm) term;
					if(variableTerm.toString().indexOf(strMappingFunctor)!= -1)
					{
						filterValue = true;	
					}
					
				}

				/*
				 * if(term.getName().indexOf(strMappingFunctor) != -1)
				 * filterValue = true; if (term instanceof VariableTerm) { if
				 * (term.toString().indexOf(strMappingFunctor) != -1)
				 * filterValue = true; } else
				 */

			}
		}

		return filterValue;
	}

}
