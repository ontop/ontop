package inf.unibz.it.obda.dl.codec.constraints.xml;

import inf.unibz.it.dl.codec.xml.AssertionXMLCodec;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSUniquenessConstraint;
import inf.unibz.it.obda.constraints.parser.ConstraintsRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.obda.query.domain.Term;
import org.obda.query.domain.Variable;
import org.obda.query.domain.imp.TermFactoryImpl;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class RDBMSUniquenessConstraintXMLCodec extends AssertionXMLCodec<RDBMSUniquenessConstraint>{

	public RDBMSUniquenessConstraintXMLCodec() {
		super();
		// TODO Auto-generated constructor stub
	}

	private static final String	TAG	= "RDBMSUniquenessConstraint";
	private static final String	V1	= "variable";
	private static final String	MAPPING	= "mapping";

	private final TermFactoryImpl termFactory = TermFactoryImpl.getInstance();

	@Override
	public RDBMSUniquenessConstraint decode(Element input) {
		NodeList nl = input.getElementsByTagName(MAPPING);
		Element el = (Element) nl.item(0);
		String id = el.getAttribute("id");
		NodeList l1 = el.getElementsByTagName(V1);
		Vector<Variable> v = new Vector<Variable>();
		for(int i=0;i<l1.getLength();i++){
			Element e = (Element) l1.item(i);
			String name = e.getAttribute("name");
			v.add(termFactory.createVariable(name));
		}

		try {
			return ConstraintsRenderer.getInstance().createRDBMSUniquenessConstraint(id, v);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Element encode(RDBMSUniquenessConstraint input) {
		Element element = createElement(TAG);
		Element map = createElement(MAPPING);
		map.setAttribute("id", input.getMappingID());
		List<Variable> list = input.getVariables();
		Iterator<Variable> it = list.iterator();
		while(it.hasNext()){
			Term t = it.next();
			Element e = createElement(V1);
			e.setAttribute("name", t.getName());
			map.appendChild(e);
		}
		element.appendChild(map);
		return element;
	}

	@Override
	public Collection<String> getAttributes() {
		ArrayList<String> attributes = new ArrayList<String>();
		return attributes;
	}

	@Override
	public String getElementTag() {
		return TAG;
	}

}
