package it.unibz.krdb.obda.utils;

import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * A linked listed that rises "listChanged" events any time the list is
 * modified. This is a simple extension of LinkedList. All the actual
 * implementations of the methods are the original LinkedList methods. We just
 * wrap them to be able to rise the event.
 */
public class EventGeneratingLinkedList<E> extends LinkedList<E> implements EventGeneratingList<E> {

	private static final long serialVersionUID = 893780365770320662L;
	
	LinkedList<ListListener> listeners = new LinkedList<ListListener>();

	private static Logger log = LoggerFactory.getLogger(EventGeneratingLinkedList.class);

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.utils.EventGeneratingList#addListener(it.unibz.krdb.obda.utils.ListListener)
	 */
	@Override
	public void addListener(ListListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.utils.EventGeneratingList#removeListener(it.unibz.krdb.obda.utils.ListListener)
	 */
	@Override
	public void removeListener(ListListener listener) {
		listeners.remove(listener);
	}

	@Override
	public E set(int index, E element) {
		E oldobject = super.set(index, element);
		riseListChanged();
		return oldobject;
	}

	@Override
	public void add(int index, E element) {
		super.add(index, element);
		riseListChanged();
	}

	@Override
	public void clear() {
		super.clear();
		riseListChanged();
	}

	@Override
	public boolean add(E o) {
		boolean r = super.add(o);
		riseListChanged();
		return r;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean r = super.addAll(c);
		riseListChanged();
		return r;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		boolean r = super.addAll(index, c);
		riseListChanged();
		return r;
	}

	@Override
	public boolean remove(Object o) {
		boolean r = super.remove(o);
		riseListChanged();
		return r;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean r = super.removeAll(c);
		riseListChanged();
		return r;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean r = super.retainAll(c);
		riseListChanged();
		return r;
	}

	/* (non-Javadoc)
	 * @see it.unibz.krdb.obda.utils.EventGeneratingList#riseListChanged()
	 */
	@Override
	public void riseListChanged() {
		for (ListListener listener : listeners) {
			try {
				listener.listChanged();
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
		}
	}
}
