package it.unibz.krdb.obda.codec;

import java.io.Serializable;

import it.unibz.krdb.obda.model.OBDAModel;


public abstract class ObjectToTextCodec <ObjectClass extends Object> implements Serializable {

	protected OBDAModel apic = null;

	public ObjectToTextCodec(OBDAModel apic){
		this.apic = apic;
	}
	
	public abstract String encode(ObjectClass input);

	public abstract ObjectClass decode(String input);
}
