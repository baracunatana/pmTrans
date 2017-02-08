package gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class FindBar extends Composite {

	private Text text;
	private Button findNext;
	private Button findPrevious;
	private PmTrans pmTrans;

	public FindBar(Composite parent, PmTrans pm) {
		super(parent, SWT.NONE);
		pmTrans = pm;
		setLayout(new GridLayout(5, false));
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = false;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessVerticalSpace = false;
		gd.verticalAlignment = SWT.FILL;

		Button close = new Button(this, SWT.FLAT);
		Image icon = Config.getInstance().getImage(Config.ICON_PATH_CROSS);
		close.setImage(icon);
		close.setSize(icon.getBounds().width + 2, icon.getBounds().height + 2);
		close.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				pmTrans.find();
			}
		});

		new Label(this, SWT.NONE).setText("Find:");
		text = new Text(this, SWT.BORDER);
		// text.setLayoutData(gd);
		text.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				text.setBackground(Display.getCurrent().getSystemColor(
						SWT.COLOR_WHITE));
			}
		});
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		text.forceFocus();
		text.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN)
					pmTrans.quickFindNext();
			}
		});

		findNext = new Button(this, SWT.PUSH);
		findNext.setText("Next");
		findNext.setLayoutData(gd);
		findNext.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				text.setBackground(Display.getCurrent().getSystemColor(
						SWT.COLOR_WHITE));
				if (text.getText().length() > 0)
					if (!pmTrans.quickFindNext())
						text.setBackground(Display.getCurrent().getSystemColor(
								SWT.COLOR_YELLOW));
				;
			}
		});

		findPrevious = new Button(this, SWT.PUSH);
		findPrevious.setText("Previous");
		findPrevious.setLayoutData(gd);
		findPrevious.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				text.setBackground(Display.getCurrent().getSystemColor(
						SWT.COLOR_WHITE));
				if (text.getText().length() > 0)
					if (!pmTrans.quickFindPrevious())
						text.setBackground(Display.getCurrent().getSystemColor(
								SWT.COLOR_YELLOW));
				;
			}
		});
		pack();
	}

	public String getSearchString() {
		return text.getText();
	}
}
