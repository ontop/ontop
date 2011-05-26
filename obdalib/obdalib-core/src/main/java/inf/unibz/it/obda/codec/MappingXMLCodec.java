package inf.unibz.it.obda.codec;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.model.CQIE;
import inf.unibz.it.obda.model.impl.CQIEImpl;
import inf.unibz.it.obda.model.impl.RDBMSOBDAMappingAxiom;
import inf.unibz.it.obda.model.impl.RDBMSSQLQuery;

import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The mapping xml codec can be use to encode OBDA mapping axioms
 * into XML respectively decode them from XML
 *
 * @author Manfred Gerstgrasser
 *
 */

public class MappingXMLCodec extends ObjectXMLCodec<OBDAMappingAxiom>{

	/**
	 * The xml tag used for representing obda mapping axioms
	 */
	private static final String	TAG	= "mapping";
	/**
	 * the current api controller
	 */
	APIController apic = null;
	/**
	 * the datalog conjunctive quey codec used to encode respectively decode the
	 * head of the mappings.
	 */
	DatalogConjunctiveQueryXMLCodec cqcodec = null;

	/**
	 * The contructor. Creates a new instance of the Codec
	 * @param apic
	 */
	public MappingXMLCodec(APIController apic){
		this.apic = apic;
		cqcodec = new DatalogConjunctiveQueryXMLCodec(apic);
	}

	/**
	 * Decodes the given XML element into an obda mapping axiom
	 */
	@Override
	public OBDAMappingAxiom decode(Element mapping) {

		String id = mapping.getAttribute("id");
		Element head = null;
		Element body = null;
		NodeList mappingchilds = mapping.getChildNodes();
		// Retrieving the child nodes avoiding empty nodes
		for (int j = 0; j < mappingchilds.getLength(); j++) {
			Node mappingchild = mappingchilds.item(j);
			if (!(mappingchild instanceof Element)) {
				continue;
			}
			if (head == null) {
				head = (Element) mappingchild;
				continue;
			}

			if (body == null) {
				body = (Element) mappingchild;
				continue;
			}
		}
		String SQLstring = body.getAttribute("string");

		CQIE headquery = cqcodec.decode(head);
		if(headquery == null){
			return null;
		}
		RDBMSSQLQuery bodyquery=null;
		RDBMSOBDAMappingAxiom newmapping=null;
		bodyquery = new RDBMSSQLQuery(SQLstring);
		newmapping = new RDBMSOBDAMappingAxiom(id);
		newmapping.setSourceQuery(bodyquery);
		newmapping.setTargetQuery(headquery);
		return newmapping;
	}

	/**
	 * Encodes the given obda mapping axiom into XML
	 */
	@Override
	public Element encode(OBDAMappingAxiom input) {

		Element mappingelement = createElement(TAG);
		// the new XML mapping
		mappingelement.setAttribute("id", input.getId());
		// the head XML child
		CQIE hq = (CQIEImpl) input.getTargetQuery();
		Element mappingheadelement = cqcodec.encode(hq);
		// the body XML child
		Element mappingbodyelement = createElement("SQLQuery");
		RDBMSSQLQuery bq = (RDBMSSQLQuery) input.getSourceQuery();
		if(bq != null){
			mappingbodyelement.setAttribute("string", bq.toString());
		}else{
			mappingbodyelement.setAttribute("string", "");
		}
		mappingelement.getOwnerDocument().adoptNode(mappingheadelement);
		mappingelement.appendChild(mappingheadelement);
		mappingelement.appendChild(mappingbodyelement);

		return mappingelement;
	}

	/**
	 * return all attributes used in an obda mapping axiom XML element
	 */
	@Override
	public Collection<String> getAttributes() {
		ArrayList<String> fixedAttributes = new ArrayList<String>();
		fixedAttributes.add("id");
		return fixedAttributes;
	}

	/**
	 * Returns the tag used to represent an obda mapping axiom in XML
	 */
	@Override
	public String getElementTag() {
		return TAG;
	}

}
