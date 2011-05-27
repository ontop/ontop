package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.model.CQIE;

import java.util.List;
import java.util.concurrent.CountDownLatch;



class CQCWorkerThread implements Runnable {
	private final CountDownLatch	startSignal;
	private final CountDownLatch	doneSignal;

	private int						from;
	private int						to;
	private CQCUtilities			cqc;
	List<CQIE>						queries;
	boolean[]						stopflag;

	CQCWorkerThread(CountDownLatch startSignal, CountDownLatch doneSignal, int from, int to, List<CQIE> queries, CQCUtilities cqc,
			boolean[] stopflag) {
		this.startSignal = startSignal;
		this.doneSignal = doneSignal;
		this.from = from;
		this.to = to;
		this.queries = queries;
		this.cqc = cqc;
		this.stopflag = stopflag;
	}

	public void run() {
		try {
			startSignal.await();
			doWork();
			doneSignal.countDown();
		} catch (InterruptedException ex) {
		} // return;
	}

	void doWork() {
		for (int j = from - 1; j > to; j--) {
			if (cqc.isContainedIn(queries.get(j))) {
				/* We found a containment, registring it as the answer */
				stopflag[0] = true;
				break;
			}

			if (stopflag[0] == true) {
				/*
				 * Some other thread found a containment, no point in
				 * continuing, stopping work
				 */
				break;
			}

		}
	}
}