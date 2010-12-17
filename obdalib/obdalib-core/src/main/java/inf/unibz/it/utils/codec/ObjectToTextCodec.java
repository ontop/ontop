package inf.unibz.it.utils.codec;

import inf.unibz.it.obda.api.controller.APIController;

import org.obda.query.domain.Query;


public abstract class ObjectToTextCodec <ObjectClass extends Object> {

	protected APIController apic = null;

	public ObjectToTextCodec(APIController apic){
		this.apic = apic;
	}
	
	public abstract String encode(ObjectClass input);

	public abstract ObjectClass decode(String input);
}
