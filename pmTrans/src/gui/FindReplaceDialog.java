package gui;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import util.WordIndexer;
import util.WordIndexerWrapper;

/**
 * @author Juan Erasmo Gómez
 */
public class FindReplaceDialog extends Dialog {

	private static final String FIND_HIGHLIGHT_DATA = "find";
	private static final Color FIND_HIGHLIGHT_COLOR = Display.getCurrent()
			.getSystemColor(SWT.COLOR_YELLOW);

	private StyledText text;
	private Button wholeWords;
	private Button matchCase;
	private Button highlightAll;
	private Button regex;
	private Button directionForward;
	private Button directionBackward;
	private Text findText;
	private Text replaceText;
	private Label find;
	private ListIterator<WordIndexerWrapper> resultsIterator;

	public FindReplaceDialog(Shell parent, StyledText text) {
		super(parent, SWT.DIALOG_TRIM);
		this.text = text;
	}

	public void open() {
		final Shell shell = new Shell(getParent(), getStyle());
		shell.setText("Find/replace");
		createContents(shell);
		shell.pack();
		shell.open();

		Listener researcherListener = new Listener() {
			public void handleEvent(Event event) {
				if (resultsIterator != null)
					resultsIterator = null;
			}
		};
		text.addListener(SWT.Modify, researcherListener);

		Display display = getParent().getDisplay();
		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();

		text.removeListener(SWT.Modify, researcherListener);
		clearSearchResults();
	}

	private void createContents(final Shell shell) {
		shell.setLayout(new GridLayout(6, false));

		find = new Label(shell, SWT.NONE);
		find.setText("Find:");
		find.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1,
				1));

		findText = new Text(shell, SWT.BORDER);
		findText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3,
				1));
		findText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				clearSearchResults();
			}
		});

		Button findNextButton = new Button(shell, SWT.PUSH);
		findNextButton.setText("Find next");
		findNextButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 2, 1));
		shell.setDefaultButton(findNextButton);
		findNextButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				findNext();
			}
		});

		Label replace = new Label(shell, SWT.NONE);
		replace.setText("Replace:");
		replace.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		replaceText = new Text(shell, SWT.BORDER);
		replaceText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 3, 1));

		Button replaceButton = new Button(shell, SWT.PUSH);
		replaceButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 2, 1));
		replaceButton.setText("Replace");
		replaceButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				replace();
			}
		});

		new Label(shell, SWT.NONE).setLayoutData(new GridData(SWT.FILL,
				SWT.FILL, false, false, 4, 1));
		Button replaceFind = new Button(shell, SWT.PUSH);
		replaceFind.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 2, 1));
		replaceFind.setText("Replace/Find");
		replaceFind.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				replaceFind();
			}
		});

		new Label(shell, SWT.NONE).setLayoutData(new GridData(SWT.FILL,
				SWT.FILL, false, false, 4, 1));
		Button replaceAllButton = new Button(shell, SWT.PUSH);
		replaceAllButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 2, 1));
		replaceAllButton.setText("Replace All");
		replaceAllButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				replaceAll();
			}
		});

		// Options
		renderOptions(shell);
		renderDirection(shell);
		renderTransparency(shell);

		// Close button
		new Label(shell, SWT.NONE).setLayoutData(new GridData(SWT.FILL,
				SWT.FILL, false, false, 4, 1));
		Button close = new Button(shell, SWT.PUSH);
		close.setText("Close");
		close.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2,
				1));
		close.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				shell.dispose();
			}
		});

		// Add the new search listeners
		Listener newSearchListener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				restartSearch();
			}
		};
		findText.addListener(SWT.Modify, newSearchListener);
		highlightAll.addListener(SWT.Selection, newSearchListener);
		wholeWords.addListener(SWT.Selection, newSearchListener);
	}

	private boolean replaceFind() {
		replace();
		return findNext();
	}

	private void renderTransparency(final Shell shell) {
		Group group = new Group(shell, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 6, 1));
		group.setLayout(new GridLayout(1, false));
		group.setText("Transparency");
		final Scale transparencySlider = new Scale(group, SWT.HORIZONTAL);
		transparencySlider.setMinimum(20);
		transparencySlider.setMaximum(100);
		transparencySlider.setPageIncrement(90);
		transparencySlider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));
		transparencySlider.setSelection(100);
		transparencySlider.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				shell.setAlpha(255 * transparencySlider.getSelection() / 100);
			}
		});
	}

	private void renderDirection(Shell shell) {
		Group group = new Group(shell, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
		group.setLayout(new GridLayout(1, false));
		group.setText("Direction");

		directionForward = new Button(group, SWT.RADIO);
		directionForward.setText("Fordward");
		directionBackward = new Button(group, SWT.RADIO);
		directionBackward.setText("Backwards");
		directionForward.setSelection(true);
	}

	private void renderOptions(Shell shell) {
		Group group = new Group(shell, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
		group.setLayout(new GridLayout(1, false));
		group.setText("Options");

		wholeWords = createOption("Whole word", group);
		matchCase = createOption("Case sensitive", group);
		highlightAll = createOption("Highlight all", group);
		regex = createOption("Regular expression", group);
		highlightAll.setSelection(true);
	}

	private Button createOption(String option, Composite parent) {
		GridData optionsLD = new GridData(SWT.FILL, SWT.CENTER, false, false);
		Button opt = new Button(parent, SWT.CHECK);
		opt.setText(option);
		opt.setLayoutData(optionsLD);
		return opt;
	}

	private List<WordIndexerWrapper> findAll(String keyWord, boolean matchCase,
			boolean wholeWord, boolean regex, Point searchBounds) {

		String wholeText = text.getText();
		if (!matchCase) {
			keyWord = keyWord.toLowerCase();
			wholeText = wholeText.toLowerCase();
		}
		if (!regex) {
			String temp = "";
			for (int i = 0; i < keyWord.length(); i++)
				temp += ("[" + keyWord.charAt(i) + "]");
			keyWord = temp;
		}
		if (wholeWord)
			keyWord = "\\b" + keyWord + "\\b";
		System.out.println("looking for: " + keyWord);
		WordIndexer finder = new WordIndexer(wholeText);
		List<WordIndexerWrapper> indexes = new LinkedList<WordIndexerWrapper>();
		try {
			indexes = finder.findIndexesForKeyword(keyWord, searchBounds.x,
					searchBounds.y);
		} catch (PatternSyntaxException e) {
			MessageBox diag = new MessageBox(Display.getCurrent()
					.getActiveShell(), SWT.APPLICATION_MODAL | SWT.ICON_ERROR
					| SWT.OK);
			diag.setMessage("Regular expression error.\n\n" + e.getMessage());
			diag.open();
		}
		return indexes;
	}

	private boolean findNext() {
		if (text.getCharCount() == 0)
			return false;

		if (resultsIterator == null && !initSearch())
			return false;

		if (directionForward.getSelection() && resultsIterator.hasNext()) {
			WordIndexerWrapper next = resultsIterator.next();
			text.setSelection(next.start, next.end);
			return true;
		} else if (directionBackward.getSelection()
				&& resultsIterator.hasPrevious()) {
			WordIndexerWrapper previous = resultsIterator.previous();
			text.setSelection(previous.start, previous.end);
			return true;
		} else {
			Display.getCurrent().beep();
			return false;
		}
	}

	private boolean initSearch() {
		text.setSelection(text.getSelection().x);
		Point searchBounds = new Point(0, text.getCharCount() - 1);
		if (searchBounds.x >= searchBounds.y)
			return false;
		List<WordIndexerWrapper> indexes = findAll(findText.getText(),
				matchCase.getSelection(), wholeWords.getSelection(),
				regex.getSelection(), searchBounds);
		resultsIterator = indexes.listIterator();
		// Highlight the findings
		while (highlightAll.getSelection() && resultsIterator.hasNext()) {
			WordIndexerWrapper i = resultsIterator.next();
			StyleRange highlight = new StyleRange(i.start, i.end - i.start,
					null, FIND_HIGHLIGHT_COLOR);
			highlight.data = new ReversibleStyleWrapper(FIND_HIGHLIGHT_DATA,
					text.getStyleRanges(i.start, i.end - i.start));
			text.setStyleRange(highlight);
		}
		// Reset the iterator position
		resultsIterator = indexes.listIterator();
		while (resultsIterator.hasNext()) {
			WordIndexerWrapper i = resultsIterator.next();
			if (i.start >= text.getSelection().x) {
				resultsIterator.previous();
				break;
			}
		}
		return true;
	}

	private int replaceAll() {
		int num = 0;
		while (replaceFind())
			num++;
		return num;
	}

	private boolean replace() {
		Point x = text.getSelection();
		if (!text.getSelectionText().isEmpty()) {
			text.replaceTextRange(text.getSelection().x, text.getSelection().y
					- text.getSelection().x, replaceText.getText());
			text.setSelection(x.x);
			return true;
		}
		return false;
	}

	private void clearSearchResults() {
		StyleRange[] styles = text.getStyleRanges();
		for (int i = 0; i < styles.length; i++)
			if (styles[i].data instanceof ReversibleStyleWrapper
					&& ((ReversibleStyleWrapper) styles[i].data).id
							.equals(FIND_HIGHLIGHT_DATA)) {
				text.replaceStyleRanges(styles[i].start, styles[i].length,
						((ReversibleStyleWrapper) styles[i].data).ranges);
				i = 0;
				styles = text.getStyleRanges();
			}
	}

	private void restartSearch() {
		resultsIterator = null;
		clearSearchResults();
	}
}
