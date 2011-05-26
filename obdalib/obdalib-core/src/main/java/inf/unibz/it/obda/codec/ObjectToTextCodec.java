package inf.unibz.it.obda.codec;

import inf.unibz.it.obda.api.controller.APIController;


public abstract class ObjectToTextCodec <ObjectClass extends Object> {

	protected APIController apic = null;

	public ObjectToTextCodec(APIController apic){
		this.apic = apic;
	}
	
	public abstract String encode(ObjectClass input);

	public abstract ObjectClass decode(String input);
}
