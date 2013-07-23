package it.unibz.krdb.obda.owlrefplatform.core.abox;

import java.util.EventObject;

public interface RepositoryChangedListener {

	//Clear cache if repository changed - after addition or removal of data
	public void repositoryChanged();
}
