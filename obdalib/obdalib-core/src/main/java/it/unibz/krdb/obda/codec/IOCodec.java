package it.unibz.krdb.obda.codec;

/**
 * An interface for IO Codec (Coder-Decoder), a set of classes that can
 * translate from objects of type InputClass to objects of type OutputClass, and
 * viceversa using the methods code and decode.
 * 
 * Note: This is a legacy code. Do not use instances of this class. This code
 * is used by the old test cases which needed to be updated.
 */
public interface IOCodec<OutputClass, InputClass> {

	public OutputClass encode(InputClass input) throws Exception;

	public InputClass decode(OutputClass input) throws Exception;
}
