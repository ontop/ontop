package org.obda.owlrefplatform.codecs;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.utils.codec.ObjectToTextCodec;

import java.util.Iterator;
import java.util.List;

import org.obda.query.domain.CQIE;
import org.obda.query.domain.DatalogProgram;

/**
 * A class that transforms a datalog program into a string
 * Note: class was implemented for debugging should not be used and still 
 * contains several errors
 * 
 * @author Manfred Gerstgrasser
 *
 */
public class DatalogProgramToTextCodec extends ObjectToTextCodec<DatalogProgram> {

	public DatalogProgramToTextCodec(APIController apic) {
		super(apic);
	}

	@Override
	@Deprecated
	public DatalogProgram decode(String input) {
		return null;
	}

	/**
	 * transforms the given datalog program into a string
	 */
	@Override
	public String encode(DatalogProgram input) {

		List<CQIE> list = input.getRules();
		Iterator<CQIE> it =list.iterator();
		StringBuffer sb = new StringBuffer();
		while(it.hasNext()){
			CQIE q = it.next();
			if(sb.length()>0){
				sb.append("\n");
			}
			CQIEToTextCodec codec = new CQIEToTextCodec(apic);
			sb.append(codec.encode(q));
		}
		return sb.toString();
	}

}
