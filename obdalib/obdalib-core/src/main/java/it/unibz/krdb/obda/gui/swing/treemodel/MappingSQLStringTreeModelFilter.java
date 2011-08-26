package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;

/*
 * @author
 * This filter receives a string in the constructor and returns true if any mapping contains the string in the body.
 *
 */
public class MappingSQLStringTreeModelFilter extends TreeModelFilter<OBDAMappingAxiom> {

  public MappingSQLStringTreeModelFilter() {
    super.bNegation = false;
  }

	@Override
	public boolean match(OBDAMappingAxiom object) {

    final OBDASQLQuery bodyquery = (OBDASQLQuery) object.getSourceQuery();

    boolean isMatch = false;

	  String[] vecKeyword = strFilter.split(KEYWORD_DELIM);
    for (String keyword : vecKeyword) {
      isMatch = match(keyword.trim(), bodyquery.toString());
      if(isMatch) {
        break;  // end loop if a match is found!
      }
    }
    // no match found!
		return (bNegation ? !isMatch : isMatch);
	}

  /** A helper method to check a match */
	public static boolean match(String keyword, String query) {

	  if (query.indexOf(keyword) != -1) {  // match found!
      return true;
    }
	  return false;
	}
}
