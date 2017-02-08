package gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class EditingPane extends Composite {

	private static final String TIMESTAMP_DATA = "timestamp";
	private static final int TIMESTAMP_FONT_STYLE = SWT.BOLD;
	private static final Color TIMESTAPM_FONT_COLOR = Display.getCurrent()
			.getSystemColor(SWT.COLOR_BLUE);
	private static final Cursor ARROW_CURSOR = new Cursor(Display.getCurrent(),
			SWT.CURSOR_ARROW);
	private static final Cursor CARRET_CURSOR = new Cursor(
			Display.getCurrent(), SWT.CURSOR_IBEAM);

	private StyledText text;
	private boolean changed = false;

	PmTrans evidenceBucket;

	public EditingPane(Composite parent, PmTrans pm) {
		super(parent, SWT.NONE);
		setLayout(new GridLayout(1, false));

		evidenceBucket = pm;

		// Create the text pane
		text = new StyledText(this, SWT.V_SCROLL | SWT.BORDER);
		text.setEditable(true);
		updateFont();

		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = SWT.FILL;

		text.setWordWrap(true);
		text.setLayoutData(gd);

		// Event handling for autosave and save before closing
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!changed)
					changed = true;
			}
		});

		// Timestamp selection and deleting handling
		text.addCaretListener(new CaretListener() {
			@Override
			public void caretMoved(CaretEvent event) {
				int currentOffset = text.getSelection().x;
				int newOffset = event.caretOffset;
				if (currentOffset == newOffset)
					return;
				StyleRange[] timestamps = text.getStyleRanges();
				if (text.getSelection().x == text.getSelection().y)
					for (int i = 0; i < timestamps.length; i++) {
						StyleRange tsp = timestamps[i];
						if (tsp.data.toString().startsWith(TIMESTAMP_DATA)
								&& newOffset >= tsp.start
								&& newOffset < (tsp.start + tsp.length))
							text.setSelection(currentOffset > newOffset ? tsp.start
									: tsp.start + tsp.length);
					}
			}
		});
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				StyleRange[] timestamps = text.getStyleRanges();
				for (int i = 0; i < timestamps.length; i++) {
					StyleRange tsp = timestamps[i];
					if (tsp.data.toString().startsWith(TIMESTAMP_DATA)) {
						int originalLenght = Integer.parseInt(tsp.data
								.toString().replace(TIMESTAMP_DATA, ""));
						if (originalLenght != tsp.length)
							text.replaceTextRange(tsp.start, tsp.length, "");
					}
				}
			}
		});
		text.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
				try {
					int clickOffset = text.getOffsetAtLocation(new Point(e.x,
							e.y));
					for (int i = 0; i < text.getStyleRanges().length; i++) {
						StyleRange range = text.getStyleRanges()[i];
						if (range.data.toString().startsWith(TIMESTAMP_DATA)
								&& range.start < clickOffset
								&& range.start + range.length > clickOffset) {
							String[] time = text.getTextRange(range.start,
									range.length).split(":");
							evidenceBucket.goTo(Integer.parseInt(time[0]) * 60
									+ Double.parseDouble(time[1]));
						}
					}
				} catch (IllegalArgumentException e2) {
					// ignore
				}

			}

			@Override
			public void mouseDown(MouseEvent e) {

			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {

			}
		});
		text.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				try {
					int mouseOffset = text.getOffsetAtLocation(new Point(e.x,
							e.y));
					if (!text.getCursor().equals(ARROW_CURSOR))
						for (int i = 0; i < text.getStyleRanges().length; i++) {
							StyleRange range = text.getStyleRanges()[i];
							if (range.data.toString()
									.startsWith(TIMESTAMP_DATA)
									&& range.start < mouseOffset
									&& range.start + range.length > mouseOffset)
								text.setCursor(ARROW_CURSOR);
						}
					else {
						boolean noTimestamp = true;
						for (int i = 0; i < text.getStyleRanges().length; i++) {
							StyleRange range = text.getStyleRanges()[i];
							if (range.data.toString()
									.startsWith(TIMESTAMP_DATA)
									&& range.start < mouseOffset
									&& range.start + range.length > mouseOffset)
								noTimestamp = false;
						}
						if (noTimestamp)
							text.setCursor(CARRET_CURSOR);
					}

				} catch (IllegalArgumentException e2) {
					if (text.getCursor().equals(ARROW_CURSOR))
						text.setCursor(CARRET_CURSOR);
				}
			}
		});
	}

	public Control getVisualization() {
		return text;
	}

	public boolean isChanged() {
		return changed;
	}

	public void insertTimestamp(double secs) {
		int minutes = ((int) secs) / 60;
		int seconds = ((int) secs) % 60;
		int milis = (int) ((secs % 1) * 1000);
		String timestamp = String.format("%02d:%02d.%04d", minutes, seconds,
				milis);
		text.insert(timestamp);
		printTimestamp(text.getSelection().x, timestamp.length());
		text.setSelection(text.getSelection().x + timestamp.length());
	}

	public void printTimestamp(int start, int lenght) {
		StyleRange styleRange = new StyleRange();
		styleRange.start = start;
		styleRange.length = lenght;
		styleRange.fontStyle = TIMESTAMP_FONT_STYLE;
		styleRange.foreground = TIMESTAPM_FONT_COLOR;
		styleRange.data = TIMESTAMP_DATA + lenght;
		text.setStyleRange(styleRange);
	}

	public void loadTranscription(File transcriptionFile)
			throws ParserConfigurationException, SAXException, IOException {
		text.setText("");

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(transcriptionFile);

		// Text
		text.setText(doc.getElementsByTagName("text").item(0).getTextContent());

		// Timestamps
		NodeList nList = doc.getElementsByTagName("timeStamp");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Element timestamp = (Element) nList.item(temp);
			printTimestamp(Integer.parseInt(timestamp.getAttribute("start")),
					Integer.parseInt(timestamp.getAttribute("lenght")));
		}

		changed = false;
	}

	public void importText(File f) throws IOException {
		text.setText("");
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = br.readLine();
		while (line != null) {
			text.append(line + System.getProperty("line.separator"));
			line = br.readLine();
		}
		br.close();
		changed = false;
	}

	public void saveTranscription(File transcriptionFile)
			throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("transcription");
		doc.appendChild(rootElement);
		// Text Element
		Element textE = doc.createElement("text");
		textE.appendChild(doc.createTextNode(text.getText()));
		rootElement.appendChild(textE);

		// Timestamps
		Element timeStamps = doc.createElement("time_stamps");
		rootElement.appendChild(timeStamps);
		for (int i = 0; i < text.getStyleRanges().length; i++) {
			StyleRange range = text.getStyleRanges()[i];
			Element timeStampE = doc.createElement("timeStamp");
			Attr start = doc.createAttribute("start");
			Attr lenght = doc.createAttribute("lenght");
			start.setValue("" + range.start);
			lenght.setValue("" + range.length);
			timeStampE.setAttributeNode(start);
			timeStampE.setAttributeNode(lenght);
			timeStamps.appendChild(timeStampE);
		}

		// Save the file
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(transcriptionFile);

		transformer.transform(source, result);
		changed = false;
	}

	public void exportText(File exportFile) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(exportFile));
		bw.write(text.getText());
		bw.close();
		changed = false;
	}

	public void clear() {
		text.setText("");
		changed = false;
	}

	public void cut() {
		text.cut();
	}

	public void paste() {
		text.paste();
	}

	public void copy() {
		text.copy();
	}

	public void updateFont() {
		text.setFont(new Font(Display.getCurrent(), Config.getInstance()
				.getString(Config.FONT), Config.getInstance().getInt(
				Config.FONT_SIZE), SWT.NORMAL));
		text.setBackground(Config.getInstance().getColor(
				Config.BACKGROUND_COLOR));
		text.setForeground(Config.getInstance().getColor(Config.FONT_COLOR));
	}

	public void zoomIn() {
		int currentSize = Config.getInstance().getInt(Config.FONT_SIZE);
		Config.getInstance().setValue(Config.FONT_SIZE,
				currentSize > 32 ? currentSize : currentSize + 1);
		updateFont();
	}

	public void zoomOut() {
		int currentSize = Config.getInstance().getInt(Config.FONT_SIZE);
		Config.getInstance().setValue(Config.FONT_SIZE,
				currentSize <= 6 ? currentSize : currentSize - 1);
		updateFont();
	}

	public void setFontName(String fName) {
		Config.getInstance().setValue(Config.FONT, fName);
		updateFont();
	}

	public FindReplaceDialog getFindReplaceDialog() {
		return new FindReplaceDialog(getShell(), text);
	}

	public void changeBackgroundColor() {
		ColorDialog cd = new ColorDialog(getShell());
		cd.setRGB(text.getBackground().getRGB());
		cd.setText("Choose a color");

		RGB newColor = cd.open();
		if (newColor != null)
			Config.getInstance().setValue(Config.BACKGROUND_COLOR,
					new Color(Display.getCurrent(), newColor));
		updateFont();
	}

	public void changeFontColor() {
		ColorDialog cd = new ColorDialog(getShell());
		cd.setRGB(text.getBackground().getRGB());
		cd.setText("Choose a color");

		RGB newColor = cd.open();
		if (newColor != null)
			Config.getInstance().setValue(Config.FONT_COLOR,
					new Color(Display.getCurrent(), newColor));
		updateFont();
	}

	public boolean quickFindPrevious(String keyWord) {
		int index = text.getText().substring(0, text.getSelection().x)
				.lastIndexOf(keyWord);
		if (index == -1)
			return false;
		text.setSelection(index, index + keyWord.length());
		return true;
	}

	public boolean quickFindNext(String searchString) {
		if (searchString.isEmpty())
			return false;
		int index = text.getText().indexOf(searchString,
				text.getSelection().x + 1);
		if (index == -1)
			return false;
		text.setSelection(index, index + searchString.length());
		return true;
	}

	public void selectAll() {
		text.selectAll();
	}
}
