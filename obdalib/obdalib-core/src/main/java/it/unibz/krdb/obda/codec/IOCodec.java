package it.unibz.krdb.obda.codec;


/*******************************************************************************
 * An interface for IO Codec (Coder-Decoder), a set of classes that can
 * translate from objects of type InputClass to objects of type OutputClass, and
 * viceversa using the methods code and decode.
 *
 *
 *
 * @author Mariano Rodriguez Muro
 *
 */
public interface IOCodec  <OutputClass, InputClass> {

	public abstract OutputClass encode(InputClass input) throws Exception;

	public abstract InputClass decode(OutputClass input) throws Exception;
}
