package inf.unibz.it.obda.codec;

import inf.unibz.it.obda.model.OBDAModel;


public abstract class ObjectToTextCodec <ObjectClass extends Object> {

	protected OBDAModel apic = null;

	public ObjectToTextCodec(OBDAModel apic){
		this.apic = apic;
	}
	
	public abstract String encode(ObjectClass input);

	public abstract ObjectClass decode(String input);
}
