package gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;

public class CollapsibleScale extends Composite {

	public void setToolTipText(String string) {
		button.setToolTipText(string);
	}

	private Button button;
	private Shell popup;
	private Scale scale;
	private Label selectionLabel;
	public String selectionPrefix = "";
	public String selectionPosfix = "";

	public CollapsibleScale(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, true));
		button = new Button(this, SWT.TOGGLE);
		button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				Rectangle parentRect = Display.getCurrent().map(button, null,
						button.getBounds());
				scale.pack();
				updateSelectionLabel();
				selectionLabel.pack();
				popup.pack();
				popup.setBounds(parentRect.x + button.getSize().x * 9 / 10,
						parentRect.y - popup.getSize().y * 9 / 10,
						popup.getSize().x, popup.getSize().y * 2);
				popup.setVisible(!popup.isVisible());
				scale.setFocus();
			}
		});

		popup = new Shell(Display.getCurrent().getActiveShell(), SWT.NO_TRIM
				| SWT.ON_TOP);
		popup.setLayout(new GridLayout(1, true));

		scale = new Scale(popup, SWT.VERTICAL);
		scale.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				popup.setVisible(false);
				button.setSelection(false);
			}

			@Override
			public void focusGained(FocusEvent e) {
				// Do nothing
			}
		});
		scale.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		selectionLabel = new Label(popup, SWT.NONE);
		FontData[] fd = selectionLabel.getFont().getFontData();
		fd[0].height = fd[0].height * 8 / 10;
		selectionLabel.setFont(new Font(Display.getCurrent(), fd));
		scale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSelectionLabel();
			}
		});
		selectionLabel.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true,
				false));

		button.pack();
	}

	public int getSelection() {
		return scale.getSelection();
	}

	public void setText(String text) {
		button.setText(text);
	}

	public void setImage(Image img) {
		button.setImage(img);
	}

	public void addSelectionListener(SelectionListener listener) {
		scale.addSelectionListener(listener);
	}

	public void setIncrement(int increment) {
		scale.setIncrement(increment);
	}

	public void setMaximum(int value) {
		scale.setMaximum(value);
	}

	public void setMinimum(int value) {
		scale.setMinimum(value);
	}

	public void setPageIncrement(int pageIncrement) {
		scale.setPageIncrement(pageIncrement);
	}

	public void setSelection(int value) {
		scale.setSelection(value);
		// selectionLabel.setText("" + scale.getSelection());
	}

	private void updateSelectionLabel() {
		selectionLabel.setText(selectionPrefix + scale.getSelection()
				+ selectionPosfix);
	}
}
