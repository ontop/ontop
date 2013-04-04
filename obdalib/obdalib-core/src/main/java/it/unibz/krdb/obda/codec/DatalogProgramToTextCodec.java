package it.unibz.krdb.obda.codec;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDAModel;

import java.util.Iterator;
import java.util.List;



/**
 * A class that transforms a datalog program into a string
 * Note: class was implemented for debugging should not be used and still 
 * contains several errors
 * 
 * @author Manfred Gerstgrasser
 *
 */
public class DatalogProgramToTextCodec extends ObjectToTextCodec<DatalogProgram> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3709145508488320676L;

	public DatalogProgramToTextCodec(OBDAModel apic) {
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
			sb.append(q);
		}
		return sb.toString();
	}

}
