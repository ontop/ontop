package inf.unibz.it.obda.dl.codec.constraints.xml;

import inf.unibz.it.dl.codec.xml.AssertionXMLCodec;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSForeignKeyConstraint;
import inf.unibz.it.obda.constraints.parser.ConstraintsRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.obda.query.domain.Variable;
import org.obda.query.domain.imp.TermFactoryImpl;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class RDBMSForeignKeyConstraintXMLCodec extends AssertionXMLCodec<RDBMSForeignKeyConstraint>{

	public RDBMSForeignKeyConstraintXMLCodec() {
		super();
	}

	private static final String	TAG	= "RDBMSForeignKeyConstraint";
	private static final String	V1	= "variable";
	private static final String	MAPPING	= "mapping";

	private final TermFactoryImpl termFactory = TermFactoryImpl.getInstance();

	@Override
	public RDBMSForeignKeyConstraint decode(Element input) {
		NodeList nl = input.getElementsByTagName(MAPPING);
		if(nl.getLength() == 2){
			Element map1 = (Element) nl.item(0);
			Element map2 = (Element) nl.item(1);
			String id1 = map1.getAttribute("id");
			String id2 = map2.getAttribute("id");
			NodeList l1 = map1.getElementsByTagName(V1);
			NodeList l2 = map2.getElementsByTagName(V1);
			Vector<Variable> v1 = new Vector<Variable>();
			for(int i=0;i<l1.getLength();i++){
				Element e = (Element) l1.item(i);
				String name = e.getAttribute("name");
				v1.add(termFactory.createVariable(name));
			}
			Vector<Variable> v2 = new Vector<Variable>();
			for(int i=0;i<l2.getLength();i++){
				Element e = (Element) l2.item(i);
				String name = e.getAttribute("name");
				v2.add(termFactory.createVariable(name));
			}

			try {
				return ConstraintsRenderer.getInstance(apic).createRDBMSForeignKeyConstraint(id1, id2, v1, v2);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		}else{
			return null;
		}
	}

	@Override
	public Element encode(RDBMSForeignKeyConstraint input) {

		Element element = createElement(TAG);
		Element map1 = createElement(MAPPING);
		Element map2 = createElement(MAPPING);
		map1.setAttribute("id", input.getIDForMappingOne());
		map2.setAttribute("id", input.getIDForMappingTwo());

		List<Variable> l1 = input.getVariablesOfQueryOne();
		Iterator<Variable> it1 = l1.iterator();
		while(it1.hasNext()){
			Variable var = it1.next();
			Element e = createElement(V1);
			e.setAttribute("name", var.getName());
			map1.appendChild(e);
		}
		List<Variable> l2 =input.getVariablesOfQueryTwo();
		Iterator<Variable> it2 = l2.iterator();
		while(it2.hasNext()){
			Variable var = it2.next();
			Element e = createElement(V1);
			e.setAttribute("name", var.getName());
			map2.appendChild(e);
		}
		element.appendChild(map1);
		element.appendChild(map2);
		return element;

	}

	@Override
	public Collection<String> getAttributes() {
		ArrayList<String> attributes = new ArrayList<String>();
		return attributes;
	}

	@Override
	public String getElementTag() {
		// TODO Auto-generated method stub
		return TAG;
	}

}
