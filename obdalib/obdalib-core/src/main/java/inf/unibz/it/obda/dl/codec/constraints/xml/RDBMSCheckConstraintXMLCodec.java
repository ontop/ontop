package inf.unibz.it.obda.dl.codec.constraints.xml;

import inf.unibz.it.dl.codec.xml.AssertionXMLCodec;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSCheckConstraint;
import inf.unibz.it.obda.constraints.parser.ConstraintsRenderer;
import inf.unibz.it.ucq.typing.CheckOperationTerm;
import inf.unibz.it.ucq.typing.UnknownXSDTypeException;
import inf.unibz.it.ucq.typing.XSDTypingController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.obda.query.domain.Term;
import org.obda.query.domain.imp.TermFactoryImpl;
import org.obda.query.domain.imp.ValueConstantImpl;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.datatype.xsd.XSDatatype;

public class RDBMSCheckConstraintXMLCodec extends AssertionXMLCodec<RDBMSCheckConstraint>{

	public RDBMSCheckConstraintXMLCodec() {
		super();
	}

	private static final String	TAG	= "RDBMSCheckConstraint";
	private static final String	CHECK	= "Check";
	private static final String	V1	= "variable1";
	private static final String	V2	= "variable2";
	private static final String CONSTANT = "constant";
	private static final String TYPE = "type";
	private static final String	OP	= "operator";
	private static final String	MAPPING	= "mapping";

	private final TermFactoryImpl termFactory = TermFactoryImpl.getInstance();

	@Override
	public RDBMSCheckConstraint decode(Element input) {
		NodeList nl = input.getElementsByTagName(MAPPING);
		Element el = (Element) nl.item(0);
		String id = el.getAttribute("id");
		NodeList check = el.getElementsByTagName(CHECK);
		List<CheckOperationTerm> list = new Vector<CheckOperationTerm>();
		for(int i=0;i<check.getLength();i++){
			Element c = (Element) nl.item(i);
			NodeList v1 = c.getElementsByTagName(V1);
			String name_v1 = ((Element)v1.item(0)).getAttribute("name");
			Term qt1 = termFactory.createVariable(name_v1);
			NodeList v2 = c.getElementsByTagName(V2);
			Term qt2 = null;
			if(v2.getLength() > 0){
				String name_v2 = ((Element)v2.item(0)).getAttribute("name");
				qt2 = termFactory.createVariable(name_v2);
			}else{
				v2 = c.getElementsByTagName(CONSTANT);
				if(v2.getLength() > 0){
					String name_v2 = ((Element)v2.item(0)).getAttribute("name");
					String type_v2 = ((Element)v2.item(0)).getAttribute(TYPE);
					XSDatatype type = StringType.theInstance;
					try {
						type = XSDTypingController.getInstance().getType(type_v2);
					} catch (UnknownXSDTypeException e) {
						e.printStackTrace();
					}
					qt2 = termFactory.createVariable(name_v2,type);
				}
			}
			NodeList op = c.getElementsByTagName(OP);
			String name_op = ((Element)op.item(0)).getAttribute("op");
			list.add(new CheckOperationTerm(qt1, name_op, qt2));
		}

		try {
			return ConstraintsRenderer.getInstance(apic).createRDBMSCheckConstraint(id, list);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Element encode(RDBMSCheckConstraint input) {
		Element element = createElement(TAG);
		Element map = createElement(MAPPING);
		map.setAttribute("id", input.getMappingID());
		List<CheckOperationTerm> list =input.getChecks();
		Iterator<CheckOperationTerm> it = list.iterator();
		while(it.hasNext()){
			CheckOperationTerm cot = it.next();
			Element check = createElement(CHECK);
			Element v1 = createElement(V1);
			v1.setAttribute("name", cot.getTerm1().getName());
			check.appendChild(v1);
			Term t2 = cot.getTerm2();
			if(t2 instanceof ValueConstantImpl){
				ValueConstantImpl tct = (ValueConstantImpl) t2;
				Element constant = createElement(CONSTANT);
				constant.setAttribute("name", t2.getName());
				constant.setAttribute(TYPE, tct.getType().toString());
				check.appendChild(constant);
			}else{
				Element v2 = createElement(V2);
				v2.setAttribute("name",t2.getName());
				check.appendChild(v2);
			}
			Element op = createElement(OP);
			op.setAttribute("op", cot.getOperator());
			check.appendChild(op);

			map.appendChild(check);
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
