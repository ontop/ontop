package inf.unibz.it.obda.dependencies.miner;

import inf.unibz.it.obda.dependencies.miner.exception.MiningException;

/**
 * Interface, which all Dependency Miner should implement
 * 
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy 
 *
 */

public interface IMiner {

	/**
	 * Starts the mining process
	 */
	public void startMining();
	/**
	 * Interrupts the mining process
	 */
	public void cancelMining();
	
	public boolean hasErrorOccurred();
	
	public MiningException getException();
}
