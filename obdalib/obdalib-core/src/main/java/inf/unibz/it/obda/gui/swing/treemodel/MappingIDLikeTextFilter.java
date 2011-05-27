package inf.unibz.it.obda.gui.swing.treemodel;

import inf.unibz.it.obda.model.OBDAMappingAxiom;

/**
 * A filter that receives a String str during construction and returns
 * a match if the input mapping contains str in the id field.
 *
 * @author mariano
 *
 */
public class MappingIDLikeTextFilter implements TreeModelFilter<OBDAMappingAxiom> {

	String str = "";

	public MappingIDLikeTextFilter(String str) {
		this.str = str;
	}

	/* (non-Javadoc)
	 * @see inf.unibz.it.obda.gui.swing.treemodel.filter.TreeModelFilter#match(java.lang.Object)
	 */
	@Override
	public boolean match(OBDAMappingAxiom object) {
		if (object.getId().indexOf(str) != -1) {
			return true;
		}
		return false;
	}


//	public static void main(String args[]) {
//		TermFactoryImpl termFactory = (TermFactoryImpl) TermFactory.getInstance();
//
//		try {
//			RDBMSOBDAMappingAxiom m = new RDBMSOBDAMappingAxiom("test id");
//			ConjunctiveQuery t = new ConjunctiveQuery();
//			t.addQueryAtom(new ConceptQueryAtom(new NamedConcept(URI.create("test")), termFactory.createVariable("x")));
//			m.setTargetQuery(t);
//			RDBMSSQLQuery q = new RDBMSSQLQuery();
//			q.setInputQuery("SELECT ..", null);
//			m.setSourceQuery(q);
//
//			MappingIDLikeTextFilter filter = new MappingIDLikeTextFilter("id2");
//			System.out.println(filter.match(m));
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

}

//Receive

