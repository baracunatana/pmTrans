package gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class AboutDialog extends Dialog {

	private static final String VERSION = "1.2 beta 3";

	public AboutDialog(Shell arg0) {
		super(arg0, SWT.DIALOG_TRIM | SWT.RESIZE);
	}

	public void open() {
		// Create the dialog window
		Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		createContents(shell);
		shell.pack();
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void createContents(final Shell shell) {
		shell.setLayout(new GridLayout(3, true));
		GridData data = new GridData();

		// Build the string
		String msg = "";
		msg += "pmTrans (The Poor's Man Transcriber) is an open source software brought to you by:";
		msg += (System.getProperty("line.separator") + "       Juan Erasmo Gómez");
		msg += System.getProperty("line.separator");
		msg += (System.getProperty("line.separator") + "Licence: GPL V3");
		msg += (System.getProperty("line.separator") + "Get some help: Project home page");
		msg += (System.getProperty("line.separator") + "Support the developer: Show your love");
		msg += System.getProperty("line.separator");
		msg += (System.getProperty("line.separator") + "Version: " + VERSION);

		// Show the text
		final StyledText text = new StyledText(shell, SWT.V_SCROLL);
		text.setEditable(false);
		text.setText(msg);
		data.horizontalSpan = 3;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		data.grabExcessVerticalSpace = true;
		data.verticalAlignment = SWT.FILL;
		text.setWordWrap(true);
		text.setLayoutData(data);

		// Create the links for the text
		StyleRange[] styles = {
				createLinkStyle(msg, "GPL V3",
						"http://www.gnu.org/copyleft/gpl.html"),
				createLinkStyle(msg, "Project home page",
						"https://pmtrans.codeplex.com/"), };
		text.setStyleRanges(styles);

		text.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				try {
					int offset = text.getOffsetAtLocation(new Point(event.x,
							event.y));
					StyleRange style = text.getStyleRangeAtOffset(offset);
					if (style != null && style.underline
							&& style.underlineStyle == SWT.UNDERLINE_LINK) {
						System.out.println(style.data);
						Program.launch(style.data.toString());
					}
				} catch (IllegalArgumentException e) {
					// Ignore
				}
			}
		});

		// Create ok button
		new Label(shell, SWT.NONE).setText("");
		new Label(shell, SWT.NONE).setText("");
		Button ok = new Button(shell, SWT.PUSH);
		ok.setText("OK");
		data = new GridData(GridData.FILL_HORIZONTAL);
		ok.setLayoutData(data);
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				shell.close();
			}
		});
		shell.setDefaultButton(ok);
	}

	private StyleRange createLinkStyle(String fullText, String word, String link) {
		StyleRange style = new StyleRange();
		style.underline = true;
		style.underlineStyle = SWT.UNDERLINE_LINK;
		style.data = link;
		style.start = fullText.indexOf(word);
		style.length = word.length();
		return style;
	}
}
