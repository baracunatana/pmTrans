package util;

import java.util.ArrayList;
import java.util.List;

public class SortedList<E extends Comparable<E>> extends ArrayList<E> implements
		List<E> {

	private static final long serialVersionUID = 8773049215952570096L;

	@Override
	public boolean add(E e) {
		if (contains(e))
			return false;
		for (E ee : this) {
			if (ee.compareTo(e) > 0) {
				add(indexOf(ee), e);
				return true;
			}
		}
		add(size(), e);
		return true;
	}

}
