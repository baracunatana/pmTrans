package gui;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import util.CacheList;
import util.ListChangeListener;
import util.PmTransException;
import AudioPlayback.AudioPlayerTarsosDSP;
import AudioPlayback.DummyPlayer;
import AudioPlayback.Player;

public class PmTrans {

	private Shell shell;
	private Display d;
	private Player player;
	private MenuManager menuManager;
	private EditingPane textEditor;
	private File transcriptionFile;
	private GlobalKeyListener gKL;
	private FindBar findBar;
	private BarManager barManager;
	private CacheList<File> audioFilesCache;
	private CacheList<File> textFilesCache;

	private PmTrans(GlobalKeyListener gkl) {
		// Initialize the interface
		d = new Display();
		shell = new Shell(d);
		menuManager = new MenuManager(shell, this);
		barManager = new BarManager(shell, this);

		// Global keys
		this.gKL = gkl;

		initState();
		initGui();
		startAutoSave();

		shell.open();

		while (!shell.isDisposed())
			if (!d.readAndDispatch())
				d.sleep();

		if (player != null) {
			player.close();
			player = null;
		}
		GlobalScreen.unregisterNativeHook();
		saveState();
		try {
			Config.getInstance().save();
		} catch (IOException e) {
			// ignore
		}
	}

	private void initGui() {
		shell.setLayout(new GridLayout(1, true));

		shell.setText("pmTrans");

		menuManager.createMenu();
		barManager.createToolBar();

		textEditor = new EditingPane(shell, this);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessVerticalSpace = true;
		textEditor.setLayoutData(gd);

		createNewDummyPlayer();

		shell.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				if (!menuManager.getMiniModeSelection()) {
					Config.getInstance().setValue(Config.SHELL_HEIGHT, shell.getSize().x);
					Config.getInstance().setValue(Config.SHELL_LENGHT, shell.getSize().y);
				}
			}
		});

		// Add the listeners for the recent files caches
		audioFilesCache.addListChangedListener(new ListChangeListener() {

			@Override
			public void listChanged() {
				menuManager.refreshRecentFilesCaches();
			}
		});
		textFilesCache.addListChangedListener(new ListChangeListener() {

			@Override
			public void listChanged() {
				menuManager.refreshRecentFilesCaches();
			}
		});

		// Set the shell size
		shell.setSize(Config.getInstance().getInt(Config.SHELL_HEIGHT),
				Config.getInstance().getInt(Config.SHELL_LENGHT));

		// Ask if the user want to save changes before closing and close the
		// player
		shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				event.doit = closeTranscription();
				if (event.doit && player != null) {
					player.stop();
					player.close();
				}
			}
		});
	}

	private void startAutoSave() {
		if (Config.getInstance().getBoolean(Config.AUTO_SAVE)) {
			final int frecuency = Config.getInstance().getInt(Config.AUTO_SAVE_TIME) * 60 * 1000;
			if (frecuency > 0) {
				Display.getCurrent().timerExec(frecuency, new Runnable() {
					public void run() {
						if (!textEditor.isDisposed() && transcriptionFile != null) {
							try {
								textEditor.saveTranscription(transcriptionFile);
							} catch (Exception e) {
								// ignore
							}
						}
						Display.getCurrent().timerExec(frecuency, this);
					}
				});
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void initState() {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(Config.STATE_PATH));
			CacheList<File> cl = (CacheList<File>) in.readObject();
			audioFilesCache = cl != null ? cl
					: new CacheList<File>(Config.getInstance().getInt(Config.AUDIO_FILE_CACHE_LENGHT));
			cl = (CacheList<File>) in.readObject();
			textFilesCache = cl != null ? cl
					: new CacheList<File>(Config.getInstance().getInt(Config.TEXT_FILE_CACHE_LENGHT));
			in.close();
		} catch (FileNotFoundException e) {
			audioFilesCache = new CacheList<File>(Config.getInstance().getInt(Config.AUDIO_FILE_CACHE_LENGHT));
			textFilesCache = new CacheList<File>(Config.getInstance().getInt(Config.TEXT_FILE_CACHE_LENGHT));
		} catch (Exception e) {
			MessageBox diag = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
			diag.setMessage("Unable to initialize previous state. Strating from scratch");
			diag.open();
			audioFilesCache = new CacheList<File>(Config.getInstance().getInt(Config.AUDIO_FILE_CACHE_LENGHT));
			textFilesCache = new CacheList<File>(Config.getInstance().getInt(Config.TEXT_FILE_CACHE_LENGHT));
		}
	}

	private void saveState() {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(Config.STATE_PATH));
			out.writeObject(audioFilesCache);
			out.writeObject(textFilesCache);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	protected void openNewAudio() {
		FileDialog fd = new FileDialog(shell, SWT.OPEN);
		fd.setText("Select the audio file");
		String[] filterExt = { "*.wav;*.WAV;*.mp3;*.MP3", "*.*" };
		String[] filterNames = { "WAV and MP3 files", "All files" };
		fd.setFilterExtensions(filterExt);
		fd.setFilterNames(filterNames);
		String lastPath = Config.getInstance().getString(Config.LAST_OPEN_AUDIO_PATH);
		if (lastPath != null && lastPath.isEmpty())
			fd.setFileName(lastPath);
		String selected = fd.open();
		if (selected != null) {
			closePlayer();
			openAudioFile(new File(selected));
			Config.getInstance().putValue(Config.LAST_OPEN_AUDIO_PATH, selected);
			try {
				Config.getInstance().save();
			} catch (IOException e) {
				// The user do not NEED to know about this...
			}
		}
	}

	protected void openAudioFile(File file) {
		// Add new file to cache and refresh the list
		closePlayer();

		// Create the player
		try {
			if (file != null && file.exists()) {

				player = new AudioPlayerTarsosDSP(file);
				GridData gd = new GridData();
				gd.grabExcessHorizontalSpace = true;
				gd.horizontalAlignment = SWT.FILL;
				gd.verticalAlignment = SWT.FILL;
				player.initGUI(shell, gd);

				audioFilesCache.add(file);
				shell.layout();
			} else {
				createNewDummyPlayer();
				audioFilesCache.remove(file);
				MessageBox diag = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
				diag.setMessage("Unable to open file " + file.getPath());
				diag.open();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void closePlayer() {
		if (player != null) {
			player.close();
			player = null;
		}
	}

	protected void importText() {
		if (!textEditor.isDisposed()) {
			FileDialog fd = new FileDialog(shell, SWT.OPEN);
			fd.setText("Open");
			String[] filterExt = { "*.txt;*.TXT" };
			String[] filterNames = { "TXT files" };
			fd.setFilterExtensions(filterExt);
			fd.setFilterNames(filterNames);
			String lastPath = Config.getInstance().getString(Config.LAST_OPEN_TEXT_PATH);
			if (lastPath != null && !lastPath.isEmpty())
				fd.setFileName(lastPath);
			String selected = fd.open();
			if (selected != null) {
				importTextFile(new File(selected));
				Config.getInstance().putValue(Config.LAST_OPEN_TEXT_PATH, selected);
				try {
					Config.getInstance().save();
				} catch (IOException e) {
					// The user do not NEED to know about this...
				}
			}
		}
	}

	public void openTranscription() {
		if (!textEditor.isDisposed()) {
			FileDialog fd = new FileDialog(shell, SWT.OPEN);
			fd.setText("Select the transcription file");
			String[] filterExt = { "*.xpmt;*.XPMT" };
			String[] filterNames = { "pmTrans transcription files" };
			fd.setFilterExtensions(filterExt);
			fd.setFilterNames(filterNames);
			String lastPath = Config.getInstance().getString(Config.LAST_OPEN_TEXT_PATH);
			if (lastPath != null && !lastPath.isEmpty())
				fd.setFileName(lastPath);
			String selected = fd.open();
			if (selected != null) {
				openTranscriptionFile(new File(selected));
				Config.getInstance().putValue(Config.LAST_OPEN_TEXT_PATH, selected);
				try {
					Config.getInstance().save();
				} catch (IOException e) {
					// The user do not NEED to know about this...
				}
			}
		}
	}

	protected void openTranscriptionFile(File f) {
		if (!textEditor.isDisposed()) {
			try {
				closeTranscription();
				transcriptionFile = f;
				textEditor.loadTranscription(transcriptionFile);
				textFilesCache.add(transcriptionFile);
				shell.setText(f.getName());
			} catch (Exception e) {
				textEditor.clear();
				textFilesCache.remove(transcriptionFile);
				MessageBox diag = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
				diag.setMessage("Unable to open file " + f.getPath());
				diag.open();
				transcriptionFile = null;
			}
		}
	}

	/**
	 * @return true if the transcription was closed, false if the operation was
	 *         cancelled
	 */
	protected boolean closeTranscription() {
		if (!textEditor.isDisposed()) {
			if (textEditor.isChanged()) {
				MessageBox diag = new MessageBox(shell,
						SWT.APPLICATION_MODAL | SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
				diag.setMessage("You have unsaved changes, would you like to save them?");
				int opt = diag.open();
				if (opt == SWT.YES)
					saveTranscription();
				if (opt == SWT.CANCEL)
					return false;
			}
			textEditor.clear();
			transcriptionFile = null;
		}
		return true;
	}

	public static void main(String[] args) {
		// Global keys
		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException ex) {
			System.err.println(ex);
		}
		GlobalKeyListener GKL = new GlobalKeyListener();
		GlobalScreen.getInstance().addNativeKeyListener(GKL);

		new PmTrans(GKL);
	}

	protected void pauseAndRewind() {
		d.syncExec(new Runnable() {
			@Override
			public void run() {
				if (player != null)
					player.pauseAndRewind();
			}
		});
	}

	public void rewind(final int seconds) {
		d.syncExec(new Runnable() {
			@Override
			public void run() {
				if (player != null)
					player.rewind(seconds);
			}
		});
	}

	public void modifyAudioPlaybackRate(final int factor) {
		d.syncExec(new Runnable() {
			@Override
			public void run() {
				player.setRate(player.getRate() + factor);
			}
		});
	}

	public void selectAudioLoops() {
		d.syncExec(new Runnable() {
			@Override
			public void run() {
				player.selectAudioLoops();
			}
		});
	}

	public void insertTimeStamp() {
		if (!textEditor.isDisposed()) {
			if (player == null)
				return;
			textEditor.insertTimestamp(player.getMediaTime());
		}
	}

	public void saveTranscription() {
		if (!textEditor.isDisposed()) {
			if (transcriptionFile != null)
				try {
					textEditor.saveTranscription(transcriptionFile);
				} catch (Exception e) {
					e.printStackTrace();
					MessageBox diag = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					diag.setMessage("Unable to write file " + transcriptionFile.getPath());
					diag.open();
				}
			else {
				try {
					boolean done = false;
					while (!done) {
						FileDialog fd = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
						fd.setFilterNames(new String[] { "pmTrans transcription file", "All Files (*.*)" });
						fd.setFilterExtensions(new String[] { "*.xpmt", "*.*" });
						String file = fd.open();
						if (file != null) {
							transcriptionFile = new File(file);
							boolean overwrite = true;
							if (transcriptionFile.exists())
								overwrite = MessageDialog.openConfirm(shell, "Overwrite current file?",
										"Would you like to overwrite " + transcriptionFile.getName() + "?");
							if (overwrite) {
								shell.setText(transcriptionFile.getName());
								textEditor.saveTranscription(transcriptionFile);
								textFilesCache.add(transcriptionFile);
								done = true;
							}
						} else
							done = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
					MessageBox diag = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					diag.setMessage("Unable to write file " + transcriptionFile.getPath());
					diag.open();
				}
			}
		}
	}

	public void goTo(double seconds) {
		player.setMediaTime(seconds);
	}

	protected void exportTextFile() {
		boolean done = false;
		while (!done)
			if (!textEditor.isDisposed()) {
				FileDialog fd = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
				fd.setFilterNames(new String[] { "Plain text file (*.txt)", "All Files (*.*)" });
				fd.setFilterExtensions(new String[] { "*.txt", "*.*" });
				String lastPath = Config.getInstance().getString(Config.LAST_EXPORT_TRANSCRIPTION_PATH);
				if (lastPath != null && !lastPath.isEmpty())
					fd.setFileName(lastPath);
				String file = fd.open();
				try {
					if (file != null) {
						Config.getInstance().putValue(Config.LAST_EXPORT_TRANSCRIPTION_PATH, file);
						File destFile = new File(file);
						boolean overwrite = true;
						if (destFile.exists())
							overwrite = MessageDialog.openConfirm(shell, "Overwrite current file?",
									"Would you like to overwrite " + destFile.getName() + "?");
						if (overwrite) {
							textEditor.exportText(new File(file));
							done = true;
						}
					} else
						done = true;
				} catch (Exception e) {
					e.printStackTrace();
					MessageBox diag = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					diag.setMessage("Unable to export to file " + transcriptionFile.getPath());
					diag.open();
				}
			}
	}

	protected void importTextFile(File f) {
		if (!textEditor.isDisposed()) {
			FileDialog fd = new FileDialog(shell, SWT.OPEN);
			fd.setText("Import text");
			fd.setFilterExtensions(new String[] { "*.txt;*.TXT" });
			fd.setFilterNames(new String[] { "Plain text files (*.txt)" });
			String selected = fd.open();
			if (selected != null) {
				try {
					textEditor.importText(new File(selected));
				} catch (IOException e) {
					e.printStackTrace();
					MessageBox diag = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					diag.setMessage("Unable to open file " + transcriptionFile.getPath());
					diag.open();
				}
			}
		}
	}

	public void enterMiniMode() {
		gKL.registerMainWindow(this);
		if (findBar != null)
			findBar.dispose();
		textEditor.dispose();
		barManager.setBarVisible(false);
		shell.layout();
		shell.pack();
	}

	public void exitMiniMode() {
		gKL.unregisterMainWindow();
		barManager.setBarVisible(true);
		textEditor = new EditingPane(shell, this);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessVerticalSpace = true;
		textEditor.setLayoutData(gd);
		player.getVisualization().moveBelow(textEditor);

		shell.setSize(Config.getInstance().getInt(Config.SHELL_HEIGHT),
				Config.getInstance().getInt(Config.SHELL_LENGHT));
		shell.layout();
	}

	public void cut() {
		if (!textEditor.isDisposed())
			textEditor.cut();
	}

	public void copy() {
		if (!textEditor.isDisposed())
			textEditor.copy();
	}

	public void paste() {
		if (!textEditor.isDisposed())
			textEditor.paste();
	}

	public void find() {
		if (menuManager.getMiniModeSelection())
			return;

		if (findBar != null) {
			findBar.dispose();
		} else {
			findBar = new FindBar(shell, this);
			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.FILL;
			gd.verticalAlignment = SWT.FILL;
			findBar.setLayoutData(gd);
			menuManager.setFindEnabled(true);
			findBar.addListener(SWT.Dispose, new Listener() {
				public void handleEvent(Event event) {
					menuManager.setFindEnabled(false);
					findBar = null;
				}
			});
		}
		shell.layout();
	}

	public boolean quickFindNext() {
		return textEditor.quickFindNext(findBar.getSearchString());
	}

	public boolean quickFindPrevious() {
		return textEditor.quickFindPrevious(findBar.getSearchString());
	}

	public void zoomIn() {
		textEditor.zoomIn();
	}

	public void zoomOut() {
		textEditor.zoomOut();
	}

	public void setFontName(String item) {
		textEditor.setFontName(item);
	}

	public void adjustLayout() {
		shell.layout();
	}

	public void findReplace() {
		FindReplaceDialog diag = textEditor.getFindReplaceDialog();
		diag.open();
	}

	public void changeBackgroundColor() {
		textEditor.changeBackgroundColor();
	}

	public void changeFontColor() {
		textEditor.changeFontColor();
	}

	public void preferences() {
		try {
			Config.getInstance().showConfigurationDialog(shell);
		} catch (PmTransException e) {
			MessageBox diag = new MessageBox(Display.getCurrent().getActiveShell(),
					SWT.APPLICATION_MODAL | SWT.ICON_ERROR | SWT.OK);
			diag.setMessage("Unable to save preferences");
			diag.open();
		}
	}

	public void selectAll() {
		textEditor.selectAll();
	}

	public CacheList<File> getRecentTrasncriptions() {
		return textFilesCache;
	}

	public CacheList<File> getRecentAudios() {
		return audioFilesCache;
	}

	private void createNewDummyPlayer() {
		closePlayer();
		player = new DummyPlayer();
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		player.initGUI(shell, gd);
		shell.setSize(shell.getSize());
		shell.layout();
	}

	/**
	 * Opens the contribute page in a web browser
	 */
	public void contribute() {
		org.eclipse.swt.program.Program.launch(Config.CONTRIBUTE_URL);
	}
}
