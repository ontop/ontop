package inf.unibz.it.obda.owlapi;

import java.util.Iterator;
import java.util.List;

import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeListener;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;

public class SynchronizedMappingController extends MappingController implements OWLOntologyChangeListener{

	public SynchronizedMappingController(DatasourcesController dscontroller,
			APIController apic) {
		super(dscontroller, apic);
	}

	@Override
	public void ontologiesChanged(List<? extends OWLOntologyChange> changes)
			throws OWLException {
		
		Iterator<? extends OWLOntologyChange> it = changes.iterator();
		while(it.hasNext()){
			OWLOntologyChange ch = it.next();
		}
	}

}
