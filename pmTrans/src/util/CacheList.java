package util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CacheList<E extends Comparable<E>> implements Serializable {

	private static final long serialVersionUID = 2008755182857334161L;
	private ArrayList<E> objects;
	private int maxSize;
	private transient List<ListChangeListener> listeners;

	public CacheList(int maxSize) {
		this.maxSize = maxSize;
		objects = new ArrayList<E>();
	}

	public void addListChangedListener(ListChangeListener listener) {
		if (listeners == null)
			listeners = new LinkedList<ListChangeListener>();
		listeners.add(listener);
	}

	public void listChanged() {
		for (ListChangeListener l : listeners)
			l.listChanged();
	}

	public void add(E f) {
		// see if the file is already in the list
		for (Iterator<E> iterator = objects.iterator(); iterator.hasNext();) {
			E ff = iterator.next();
			if (ff.compareTo(f) == 0)
				iterator.remove();
		}
		objects.add(0, f);

		while (objects.size() > maxSize)
			objects.remove(objects.size() - 1);
		listChanged();
	}

	public void remove(E f) {
		for (Iterator<E> iterator = objects.iterator(); iterator.hasNext();) {
			E ff = iterator.next();
			if (ff.compareTo(f) == 0)
				iterator.remove();
		}
		listChanged();
	}

	public int size() {
		return objects.size();
	}

	public E get(int index) {
		return objects.get(index);
	}

}
