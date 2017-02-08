package gui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import util.CacheList;

public class BarManager {

	private PmTrans pmTrans;
	private Shell shell;
	private CoolBar bar;

	public BarManager(Shell s, PmTrans pm) {
		this.pmTrans = pm;
		this.shell = s;
	}

	public void createToolBar() {
		bar = new CoolBar(shell, SWT.FLAT | SWT.TOP);
		// bars
		bar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		createFileSection();
		createEditBar();
		createFontSection();
		createSettingsBar();

		bar.pack();
		bar.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				pmTrans.adjustLayout();
			}
		});
	}

	private void createSettingsBar() {
		ToolBar tb = new ToolBar(bar, SWT.FLAT);

		addItemToToolBar(tb, null, "Preferences", SWT.PUSH, new Listener() {
			public void handleEvent(Event event) {
				pmTrans.preferences();
			}
		}, Config.getInstance().getImage(Config.ICON_PATH_SETTINGS));

		tb.pack();
		CoolItem fileCI = new CoolItem(bar, SWT.NONE);
		Point s = tb.getSize();
		fileCI.setControl(tb);
		fileCI.setSize(fileCI.computeSize(s.x, s.y));
	}

	private void createFileSection() {
		ToolBar tb = new ToolBar(bar, SWT.FLAT);

		// Open
		addItemToToolBar(
				tb,
				null,
				"Open transcription",
				SWT.DROP_DOWN,
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						if (event.detail != SWT.ARROW)
							pmTrans.openTranscription();
						else {
							Menu dropMenu = new Menu(Display.getCurrent()
									.getActiveShell());
							CacheList<File> transcriptionCache = pmTrans
									.getRecentTrasncriptions();
							for (int i = 0; i < transcriptionCache.size(); i++)
								addMenuItem(dropMenu, transcriptionCache.get(i)
										.getName(), SWT.NONE,
										transcriptionCache.get(i),
										new SelectionAdapter() {
											@Override
											public void widgetSelected(
													SelectionEvent e) {
												pmTrans.openTranscriptionFile((File) ((MenuItem) e
														.getSource()).getData());
											}
										});
							ToolItem item = (ToolItem) event.widget;
							Rectangle rect = item.getBounds();
							Point pt = item.getParent().toDisplay(
									new Point(rect.x, rect.y));
							dropMenu.setLocation(pt.x, pt.y + rect.height);
							dropMenu.setVisible(true);
						}
					}
				},
				Config.getInstance().getImage(
						Config.ICON_PATH_OPEN_TRANSCRIPTION));
		// Save
		addItemToToolBar(
				tb,
				null,
				"Save transcription",
				SWT.PUSH,
				new Listener() {
					@Override
					public void handleEvent(Event event) {
						pmTrans.saveTranscription();
					}
				},
				Config.getInstance().getImage(
						Config.ICON_PATH_SAVE_TRANSCRIPTION));
		// Open Audio
		addItemToToolBar(tb, null, "Open audio file", SWT.DROP_DOWN,
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						if (event.detail != SWT.ARROW)
							pmTrans.openNewAudio();
						else {
							Menu dropMenu = new Menu(Display.getCurrent()
									.getActiveShell());
							CacheList<File> audiosCache = pmTrans
									.getRecentAudios();
							for (int i = 0; i < audiosCache.size(); i++)
								addMenuItem(dropMenu, audiosCache.get(i)
										.getName(), SWT.NONE, audiosCache
										.get(i), new SelectionAdapter() {
									@Override
									public void widgetSelected(SelectionEvent e) {
										pmTrans.openAudioFile((File) ((MenuItem) e
												.getSource()).getData());
									}
								});
							ToolItem item = (ToolItem) event.widget;
							Rectangle rect = item.getBounds();
							Point pt = item.getParent().toDisplay(
									new Point(rect.x, rect.y));
							dropMenu.setLocation(pt.x, pt.y + rect.height);
							dropMenu.setVisible(true);
						}
					}
				}, Config.getInstance().getImage(Config.ICON_PATH_OPEN_AUDIO));

		tb.pack();

		CoolItem fileCI = new CoolItem(bar, SWT.NONE);
		Point s = tb.getSize();
		fileCI.setControl(tb);
		fileCI.setSize(fileCI.computeSize(s.x + 4, s.y));
	}

	private void createEditBar() {
		ToolBar tb = new ToolBar(bar, SWT.FLAT);

		// Cut
		addItemToToolBar(tb, null, "Cut", SWT.PUSH, new Listener() {

			@Override
			public void handleEvent(Event event) {
				pmTrans.cut();
			}
		}, Config.getInstance().getImage(Config.ICON_PATH_CUT));
		// Copy
		addItemToToolBar(tb, null, "Copy", SWT.PUSH, new Listener() {

			@Override
			public void handleEvent(Event event) {
				pmTrans.copy();
			}
		}, Config.getInstance().getImage(Config.ICON_PATH_COPY));
		// Paste
		addItemToToolBar(tb, null, "Paste", SWT.PUSH, new Listener() {

			@Override
			public void handleEvent(Event event) {
				pmTrans.paste();
			}
		}, Config.getInstance().getImage(Config.ICON_PATH_PASTE));

		addItemToToolBar(tb, null, null, SWT.SEPARATOR);
		// Find

		// Replace
		addItemToToolBar(tb, null, "Advanced search", SWT.PUSH, new Listener() {
			public void handleEvent(Event event) {
				pmTrans.findReplace();
			}
		}, Config.getInstance().getImage(Config.ICON_PATH_ADVANCED_SEARCH));

		// Undo
		// Redo

		tb.pack();
		CoolItem fileCI = new CoolItem(bar, SWT.NONE);
		Point s = tb.getSize();
		fileCI.setControl(tb);
		fileCI.setSize(fileCI.computeSize(s.x + 4, s.y));
	}

	private void createFontSection() {
		ToolBar tb = new ToolBar(bar, SWT.FLAT);

		// Zoom
		ToolItem zoomIn = addItemToToolBar(tb, null, "Zoom in", SWT.PUSH);
		zoomIn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				pmTrans.zoomIn();
			}
		});
		zoomIn.setImage(Config.getInstance().getImage(Config.ICON_PATH_ZOOM_IN));
		ToolItem zoomOut = addItemToToolBar(tb, null, "Zoom out", SWT.PUSH);
		zoomOut.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				pmTrans.zoomOut();
			}
		});
		zoomOut.setImage(Config.getInstance().getImage(
				Config.ICON_PATH_ZOOM_OUT));

		// Background Color
		addItemToToolBar(
				tb,
				null,
				"Change background color",
				SWT.PUSH,
				new Listener() {

					@Override
					public void handleEvent(Event event) {
						pmTrans.changeBackgroundColor();
					}
				},
				Config.getInstance().getImage(
						Config.ICON_PATH_CHANGE_BACKGROUND_COLOR));

		// Font Color
		addItemToToolBar(
				tb,
				null,
				"Change font color",
				SWT.PUSH,
				new Listener() {
					public void handleEvent(Event event) {
						pmTrans.changeFontColor();
					}
				},
				Config.getInstance().getImage(
						Config.ICON_PATH_CHANGE_FONT_COLOR));

		// Font
		ToolItem fontSelector = addItemToToolBar(tb, null, "Select font",
				SWT.SEPARATOR);
		final Combo combo = new Combo(tb, SWT.READ_ONLY);
		combo.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				pmTrans.setFontName(combo.getItem(combo.getSelectionIndex()));
			}
		});
		combo.setItems(new String[] { "Arial", "Times New Roman", "Sans Serif",
				"Comic Sans MS", "Courier New", "Verdana", "Cursive", "Tahoma",
				"MS Sans Serif", "Lucida Sans Unicode", "Serif" });
		String font = Config.getInstance().getString(Config.FONT);
		int i = 0;
		while (i < combo.getItemCount()) {
			if (combo.getItem(i).equals(font))
				break;
			i++;
		}
		if (i < combo.getItemCount())
			combo.select(i);
		else {
			combo.select(0);
			Config.getInstance().setValue(Config.FONT, combo.getItem(0));
		}
		combo.pack();
		fontSelector.setWidth(combo.getSize().x);
		fontSelector.setControl(combo);

		// Font Color

		tb.pack();

		CoolItem fileCI = new CoolItem(bar, SWT.NONE);
		Point s = tb.getSize();
		fileCI.setControl(tb);
		fileCI.setSize(fileCI.computeSize(s.x + 4, s.y + 1));
	}

	private ToolItem addItemToToolBar(ToolBar bar, String text, String toolTip,
			int type) {
		ToolItem cit = new ToolItem(bar, type);
		if (text != null)
			cit.setText(text);
		if (toolTip != null)
			cit.setToolTipText(toolTip);

		return cit;
	}

	private ToolItem addItemToToolBar(ToolBar bar, String text, String toolTip,
			int type, Listener listener, Image icon) {
		ToolItem item = addItemToToolBar(bar, text, toolTip, type);

		if (listener != null)
			item.addListener(SWT.Selection, listener);
		if (icon != null)
			item.setImage(icon);

		return item;
	}

	private ToolItem addItemToToolBar(ToolBar bar, String text, String toolTip,
			int type, SelectionAdapter listener, Image icon) {
		ToolItem item = addItemToToolBar(bar, text, toolTip, type);

		if (listener != null)
			item.addSelectionListener(listener);
		if (icon != null)
			item.setImage(icon);

		return item;
	}

	public void setBarVisible(boolean b) {
		if (b)
			createToolBar();
		else
			bar.dispose();
	}

	private MenuItem addMenuItem(Menu menu, String text, int accelerator,
			Object data, SelectionListener listener) {
		MenuItem item = new MenuItem(menu, SWT.NONE);
		item.setText(text);
		item.addSelectionListener(listener);
		if (accelerator != SWT.NONE)
			item.setAccelerator(accelerator);
		item.setData(data);
		return item;
	}
}
