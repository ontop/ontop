package inf.unibz.it.obda.codec.xml;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.utils.codec.ObjectXMLCodec;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DatasourceXMLCodec extends ObjectXMLCodec<DataSource> {

	private static final String	TAG	= "dataSource";

	@Override
	public DataSource decode(Element input) {

		DataSource new_src = null;

		String name = input.getAttribute("name");
		String uri = input.getAttribute("ontologyURI");

		new_src = new DataSource(name);

		/***
		 * This if is only done because before, URI and name were used
		 * interchangable. Since now URI stands for the ontology URI we check if
		 * they are the same, if the are, it means its an old file and the URI
		 * is set to the current ontlogy's URI
		 */
		if (!name.equals(uri)) {
			new_src.setUri(uri);
		} else {
			throw new IllegalArgumentException("WARNING: A data source read form the .obda file has name=ontologyURI, this .obda file might be a deprecated file. Replace ontologyURI with the real URI of the ontology.");
//			APIController controller = APIController.getController();
//			URI currentOntologyURI = controller.getCurrentOntologyURI();
//			new_src.setUri(currentOntologyURI.toString());

		}
		NamedNodeMap attributes = input.getAttributes();
		int size = attributes.getLength();
		for (int i = 0; i < size; i++) {
			Node attribute = attributes.item(i);
			if (attribute.getNodeName().equals("name") || attribute.getNodeName().equals("ontologyURI")) {
				continue;
			} else {
				new_src.setParameter(attribute.getNodeName(), attribute.getNodeValue());
			}

		}

		return new_src;

	}

	@Override
	public Element encode(DataSource input) {
		Element element = createElement(TAG);
		element.setAttribute("name", input.getName());
		element.setAttribute("ontologyURI", input.getUri());

		Set<Object> keys = input.getParameters();
		for (Object key : keys) {
			String value = input.getParameter((String) key);
			element.setAttribute((String) key, value);
		}
		return element;
	}

	public Collection<String> getAttributes() {
		ArrayList<String> fixedAttributes = new ArrayList<String>();
		fixedAttributes.add("name");
		fixedAttributes.add("ontologyURI");
		return fixedAttributes;
	}

	public String getElementTag() {
		return TAG;
	}

}
