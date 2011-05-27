package it.unibz.krdb.obda.codec;

import it.unibz.krdb.obda.model.OBDAModel;


public abstract class ObjectToTextCodec <ObjectClass extends Object> {

	protected OBDAModel apic = null;

	public ObjectToTextCodec(OBDAModel apic){
		this.apic = apic;
	}
	
	public abstract String encode(ObjectClass input);

	public abstract ObjectClass decode(String input);
}
