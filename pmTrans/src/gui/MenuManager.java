package gui;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import util.CacheList;

public class MenuManager {

	private Menu menuBar;
	private Shell shell;
	private PmTrans pmTrans;
	private Menu recentAudiosM;
	private Menu recentTextsM;
	private MenuItem miniMode;
	private List<MenuItem> miniModeDependent;
	private List<MenuItem> findDependent;

	public MenuManager(Shell shell, PmTrans pm) {
		this.shell = shell;
		this.pmTrans = pm;
		miniModeDependent = new LinkedList<MenuItem>();
		findDependent = new LinkedList<MenuItem>();
	}

	public void createMenu() {
		menuBar = new Menu(shell, SWT.BAR);
		createFileMenu();
		createEditMenu();
		createPlaybackMenu();
		createViewMenu();
		createHelpMenu();

		shell.setMenuBar(menuBar);
	}

	private void createEditMenu() {
		MenuItem editMI = new MenuItem(menuBar, SWT.CASCADE);
		editMI.setText("Edit");
		Menu editMenu = new Menu(menuBar);
		editMI.setMenu(editMenu);

		// Cut
		MenuItem mi = addMenuItem(editMenu, "Cut \t Ctrl+X",
				new SelectionAdapter() {
					public void widgetSelected(SelectionEvent arg0) {
						pmTrans.cut();
					}
				});
		mi.setImage(Config.getInstance().getImage(Config.ICON_PATH_CUT));
		miniModeDependent.add(mi);
		// Copy
		mi = addMenuItem(editMenu, "Copy \t Ctrl+C", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				pmTrans.copy();
			}
		});
		mi.setImage(Config.getInstance().getImage(Config.ICON_PATH_COPY));
		miniModeDependent.add(mi);
		// Paste
		mi = addMenuItem(editMenu, "Paste \t Ctrl+V", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				pmTrans.paste();
			}
		});
		mi.setImage(Config.getInstance().getImage(Config.ICON_PATH_PASTE));
		miniModeDependent.add(mi);
		// Select all
		mi = addMenuItem(editMenu, "Select all \t Ctrl+A", SWT.CTRL + 'a',
				new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						pmTrans.selectAll();
					}
				});

		// Find
		new MenuItem(editMenu, SWT.SEPARATOR);
		miniModeDependent.add(addMenuItem(editMenu, "Find \t Ctrl+F",
				SWT.MOD1 + 'f', new SelectionAdapter() {
					public void widgetSelected(SelectionEvent arg0) {
						pmTrans.find();
					}
				}));
		MenuItem findNext = addMenuItem(editMenu, "Find next \t F3", SWT.F3,
				new SelectionAdapter() {
					public void widgetSelected(SelectionEvent arg0) {
						pmTrans.quickFindNext();
					}
				});
		findNext.setEnabled(false);
		findDependent.add(findNext);

		// Find/replace
		miniModeDependent.add(addMenuItem(editMenu,
				"Find/replace\t Ctrl+Shift+F", SWT.MOD1 | SWT.MOD2 + 'F',
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						pmTrans.findReplace();
					}
				}));
	}

	private void createFileMenu() {
		MenuItem fileMI = new MenuItem(menuBar, SWT.CASCADE);
		fileMI.setText("File");
		Menu fileMenu = new Menu(menuBar);
		fileMI.setMenu(fileMenu);
		// Open audio
		addMenuItem(fileMenu, "Open interview audio \t Ctrl+Shift+O",
				SWT.MOD1 | SWT.MOD2 + 'o', new SelectionAdapter() {
					public void widgetSelected(SelectionEvent arg0) {
						pmTrans.openNewAudio();
					}
				}).setImage(
				Config.getInstance().getImage(Config.ICON_PATH_OPEN_AUDIO));
		;
		// Open/save transcriptions
		new MenuItem(fileMenu, SWT.SEPARATOR);
		miniModeDependent.add(addMenuItem(
				fileMenu,
				"Open transcription \t Ctrl+O",
				SWT.MOD1 + 'o',
				new SelectionAdapter() {
					public void widgetSelected(SelectionEvent arg0) {
						pmTrans.openTranscription();
					}
				},
				Config.getInstance().getImage(
						Config.ICON_PATH_OPEN_TRANSCRIPTION)));
		miniModeDependent.add(addMenuItem(
				fileMenu,
				"Save transcription \t Ctrl+S",
				SWT.MOD1 + 's',
				new SelectionAdapter() {
					public void widgetSelected(SelectionEvent arg0) {
						pmTrans.saveTranscription();
					}
				},
				Config.getInstance().getImage(
						Config.ICON_PATH_SAVE_TRANSCRIPTION)));

		// Import/export text
		new MenuItem(fileMenu, SWT.SEPARATOR);
		miniModeDependent.add(addMenuItem(fileMenu,
				"Import transcription text", SWT.NONE, new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						pmTrans.importText();
					}
				}));
		miniModeDependent.add(addMenuItem(fileMenu, "Export transciption text",
				SWT.NONE, new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						pmTrans.exportTextFile();
					}
				}));
		// Recent audios
		new MenuItem(fileMenu, SWT.SEPARATOR);
		MenuItem recentAudiosMI = new MenuItem(fileMenu, SWT.CASCADE);
		recentAudiosMI.setText("Recent audio files");
		recentAudiosM = new Menu(menuBar);
		recentAudiosMI.setMenu(recentAudiosM);
		createRecentAudiosMenu();

		// Recent texts
		MenuItem recentTextsMI = new MenuItem(fileMenu, SWT.CASCADE);
		recentTextsMI.setText("Recent text files");
		recentTextsM = new Menu(menuBar);
		recentTextsMI.setMenu(recentTextsM);
		miniModeDependent.add(recentTextsMI);
		createRecentTextsMenu();

		// Preferences
		new MenuItem(fileMenu, SWT.SEPARATOR);
		addMenuItem(fileMenu, "Preferences", SWT.NONE, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				pmTrans.preferences();
			}
		});

	}

	private void createHelpMenu() {
		MenuItem helpMI = new MenuItem(menuBar, SWT.CASCADE);
		helpMI.setText("Help");
		Menu helpM = new Menu(menuBar);
		helpMI.setMenu(helpM);

		// Help
		addMenuItem(helpM, "Help", SWT.F1, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch("https://pmtrans.codeplex.com/");
			}
		});

		// About
		addMenuItem(helpM, "About", SWT.NONE, new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				AboutDialog about = new AboutDialog(shell);
				about.open();
			}
		});
	}

	private void createViewMenu() {
		MenuItem viewMI = new MenuItem(menuBar, SWT.CASCADE);
		viewMI.setText("View");
		Menu viewM = new Menu(menuBar);
		viewMI.setMenu(viewM);

		miniMode = new MenuItem(viewM, SWT.CHECK);
		miniMode.setText("Mini-mode");
		miniMode.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (miniMode.getSelection()) {
					if (Config.getInstance().getBoolean(
							Config.SHOW_MINI_MODE_DIALOG)) {
						MessageDialogWithToggle diag = MessageDialogWithToggle
								.openOkCancelConfirm(
										shell,
										"Entering mini-mode",
										"You are entering mini-mode."
												+ System.getProperty("line.separator")
												+ System.getProperty("line.separator")
												+ "In this mode you can control the audio playback from external applications "
												+ "(e.g. MS Word, Libreoffice Writer, Notepad, etc) "
												+ "using the options in the Playback menu. Any text-related features like timestamps "
												+ "and exporting will be disabled while you are in mini-mode."
												+ System.getProperty("line.separator")
												+ System.getProperty("line.separator")
												+ "Please note that any changes to your current transcription will be lost, so save "
												+ "your work before continuing to mini-mode."
												+ System.getProperty("line.separator")
												+ System.getProperty("line.separator")
												+ "Do you want to continue?",
										"Got it. Don't ask this again.", false,
										Config.getInstance(),
										Config.SHOW_MINI_MODE_DIALOG);
						if (diag.getReturnCode() == 1) {
							miniMode.setSelection(false);
							return;
						}
					}
				}
				if (miniMode.getSelection())
					pmTrans.enterMiniMode();
				else
					pmTrans.exitMiniMode();
				for (MenuItem item : miniModeDependent)
					item.setEnabled(!miniMode.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	private void createPlaybackMenu() {
		MenuItem playbackMI = new MenuItem(menuBar, SWT.CASCADE);
		playbackMI.setText("Playback");
		Menu playbackM = new Menu(menuBar);
		playbackMI.setMenu(playbackM);

		// Pause/Resume
		addConfigurableMenuItem(playbackM, "Pause/Resume", Config.PAUSE_KEY,
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (!miniMode.getSelection())
							pmTrans.pauseAndRewind();
					}
				});

		// Short rewind
		addConfigurableMenuItem(playbackM, "Rewind "
				+ Config.getInstance().getInt(Config.SHORT_REWIND) + "seg",
				Config.SHORT_REWIND_KEY, new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (!miniMode.getSelection())
							pmTrans.rewind(Config.getInstance().getInt(
									Config.SHORT_REWIND));
					}
				});

		// Short rewind
		addConfigurableMenuItem(playbackM, "Rewind "
				+ Config.getInstance().getInt(Config.LONG_REWIND) + "seg",
				Config.LONG_REWIND_KEY, new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (!miniMode.getSelection())
							pmTrans.rewind(Config.getInstance().getInt(
									Config.LONG_REWIND));
					}
				});
		new MenuItem(playbackM, SWT.SEPARATOR);

		// Playback speed
		addConfigurableMenuItem(playbackM, "Slow down audio",
				Config.SLOW_DOWN_KEY, new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (!miniMode.getSelection())
							pmTrans.modifyAudioPlaybackRate(Config
									.getInstance().getInt(
											Config.SLOW_DOWN_PLAYBACK));
					}
				});
		addConfigurableMenuItem(playbackM, "Speed up audio",
				Config.SPEED_UP_KEY, new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (!miniMode.getSelection())
							pmTrans.modifyAudioPlaybackRate(Config
									.getInstance().getInt(
											Config.SPEED_UP_PLAYBACK));
					}
				});
		addConfigurableMenuItem(playbackM, "Enable/disable audio loops",
				Config.AUDIO_LOOPS_KEY, new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (!miniMode.getSelection())
							pmTrans.selectAudioLoops();
					}
				});

		// other operations
		new MenuItem(playbackM, SWT.SEPARATOR);
		miniModeDependent.add(addConfigurableMenuItem(playbackM,
				"Insert timestamp", Config.TIMESTAMP_KEY,
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (!miniMode.getSelection())
							pmTrans.insertTimeStamp();
					}
				}));
	}

	private void createRecentAudiosMenu() {
		for (MenuItem mi : recentAudiosM.getItems())
			mi.dispose();

		CacheList<File> audioFilesCache = pmTrans.getRecentAudios();
		for (int i = 0; i < audioFilesCache.size(); i++)
			addMenuItem(recentAudiosM, audioFilesCache.get(i).getName(),
					SWT.NONE, audioFilesCache.get(i), new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							pmTrans.openAudioFile((File) ((MenuItem) e
									.getSource()).getData());
						}
					});
	}

	private void createRecentTextsMenu() {
		for (MenuItem mi : recentTextsM.getItems())
			mi.dispose();

		CacheList<File> textFilesCache = pmTrans.getRecentTrasncriptions();
		for (int i = 0; i < textFilesCache.size(); i++)
			addMenuItem(recentTextsM, textFilesCache.get(i).getName(),
					SWT.NONE, textFilesCache.get(i), new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							pmTrans.openTranscriptionFile((File) ((MenuItem) e
									.getSource()).getData());
						}
					});
	}

	private MenuItem addConfigurableMenuItem(Menu menu, final String orgText,
			final String acceleratorKey, SelectionListener listener) {
		char accelerator = Config.getInstance().getString(acceleratorKey)
				.toUpperCase().charAt(0);
		int acc = SWT.MOD1 + (accelerator == ' ' ? SWT.SPACE : accelerator);
		String text = orgText + " \t Ctrl+"
				+ (accelerator == ' ' ? "[space]" : accelerator);

		final MenuItem item = addMenuItem(menu, text, acc, listener);

		Config.getInstance().addPropertyChangeListener(
				new IPropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent arg0) {
						if (arg0.getProperty().equals(acceleratorKey))
							updateAccelerator(item, orgText, Config
									.getInstance().getString(acceleratorKey)
									.toUpperCase().charAt(0));
					}
				});

		return item;
	}

	private MenuItem addMenuItem(Menu menu, String text,
			SelectionListener listener) {
		MenuItem item = new MenuItem(menu, SWT.NONE);
		item.setText(text);
		item.addSelectionListener(listener);
		return item;
	}

	private MenuItem addMenuItem(Menu menu, String text, int accelerator,
			SelectionListener listener) {
		MenuItem item = addMenuItem(menu, text, listener);
		if (accelerator != SWT.NONE)
			item.setAccelerator(accelerator);
		return item;
	}

	private MenuItem addMenuItem(Menu menu, String text, int accelerator,
			SelectionListener listener, Image icon) {
		MenuItem item = addMenuItem(menu, text, accelerator, listener);
		item.setImage(icon);

		return item;
	}

	private MenuItem addMenuItem(Menu menu, String text, int accelerator,
			Object data, SelectionListener listener) {
		MenuItem item = addMenuItem(menu, text, accelerator, listener);
		item.setData(data);
		return item;
	}

	public void setFindEnabled(boolean b) {
		for (MenuItem item : findDependent)
			if (!item.isDisposed())
				item.setEnabled(b);
	}

	public boolean getMiniModeSelection() {
		return miniMode.getSelection();
	}

	private void updateAccelerator(MenuItem item, String itemText,
			char newAccelerator) {
		itemText += " \t Ctrl+"
				+ (newAccelerator == ' ' ? "[space]" : newAccelerator);
		int acc = SWT.MOD1
				+ (newAccelerator == ' ' ? SWT.SPACE : newAccelerator);
		item.setText(itemText);
		item.setAccelerator(acc);
	}

	public void refreshRecentFilesCaches() {
		createRecentAudiosMenu();
		createRecentTextsMenu();
	}
}
