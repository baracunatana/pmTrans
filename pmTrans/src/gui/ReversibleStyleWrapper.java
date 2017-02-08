package gui;

import org.eclipse.swt.custom.StyleRange;

public class ReversibleStyleWrapper {

	public final Object id;
	public final StyleRange[] ranges;

	public ReversibleStyleWrapper(Object id, StyleRange[] ranges) {
		super();
		this.id = id;
		this.ranges = ranges;
	}

}
