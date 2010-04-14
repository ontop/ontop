package inf.unibz.it.utils.codec;


public abstract class ObjectToTextCodec <ObjectClass extends Object> {

	public abstract String encode(ObjectClass input);

	public abstract ObjectClass decode(String input);
}
