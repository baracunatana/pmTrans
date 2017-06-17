package gui;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import util.PmTransException;

public class Config extends PreferenceStore {

	private static Config instance = null;

	/**
	 * Non-configurable stuff
	 */
	// Config file path
	private static String CONFIG_PATH = "./config.properties";
	// State file path
	public static String STATE_PATH = "./state.transcriber";
	// Icon paths
	public static String ICON_PATH_PLAY = "/icon/start.png";
	public static String ICON_PATH_PAUSE = "/icon/pause.png";
	public static String ICON_PATH_RESTART = "/icon/restart.png";
	public static String ICON_PATH_OPEN_TRANSCRIPTION = "/icon/open.png";
	public static String ICON_PATH_OPEN_AUDIO = "/icon/openAudio.png";
	public static String ICON_PATH_SAVE_TRANSCRIPTION = "/icon/save.png";
	public static String ICON_PATH_LOOP = "/icon/loop.png";
	public static String ICON_PATH_ZOOM_IN = "/icon/zoom_in.png";
	public static String ICON_PATH_ZOOM_OUT = "/icon/zoom_out.png";
	public static String ICON_PATH_COPY = "/icon/copy.png";
	public static String ICON_PATH_CUT = "/icon/cut.png";
	public static String ICON_PATH_PASTE = "/icon/paste.png";
	public static String ICON_PATH_CROSS = "/icon/cross.png";
	public static String ICON_PATH_ADVANCED_SEARCH = "/icon/advancedSearch.png";
	public static String ICON_PATH_CHANGE_BACKGROUND_COLOR = "/icon/changeBackgroundColor.png";
	public static String ICON_PATH_CHANGE_FONT_COLOR = "/icon/changeFontColor.png";
	public static String ICON_PATH_SETTINGS = "/icon/settings.png";
	public static String ICON_PATH_CONTRIBUTE = "/icon/contribute.png";
	public static String DEFAULT_ACCELERATORS = "cxvfosa";

	// Main shell initial dimensions
	private int SHELL_HEIGHT_DEFAULT = 600;
	private int SHELL_LENGHT_DEFAULT = 600;
	public static String SHELL_HEIGHT = "window.height";
	public static String SHELL_LENGHT = "window.lenght";
	// Last directory paths for file dialogs
	private String LAST_OPEN_AUDIO_PATH_DEFAULT = "";
	public static String LAST_OPEN_AUDIO_PATH = "last.open.audio.path";
	private String LAST_OPEN_TEXT_PATH_DEFAULT = "";
	public static String LAST_OPEN_TEXT_PATH = "last.open.text.path";
	// Last directory path for the export dialog
	private String LAST_EXPORT_TRANSCRIPTION_PATH_DEFALUT = "";
	public static String LAST_EXPORT_TRANSCRIPTION_PATH = "last.export.transcription.path";

	// URLs
	public static String CONTRIBUTE_URL = "https://github.com/juanerasmoe/pmTrans/wiki/Contribute-to-pmTrans";
	
	/**
	 * Configurable stuff
	 */
	// Duration of the short rewind in seconds
	private int SHORT_REWIND_DEFAULT = 5;
	public static String SHORT_REWIND = "short.rewind.duration";
	// Duration of the long rewind in seconds
	private int LONG_REWIND_DEFAULT = 10;
	public static String LONG_REWIND = "long.rewind.duration";
	// Duration of the rewind-and-play
	private static int REWIND_AND_PLAY_DEFAULT = 2;
	public static String REWIND_AND_PLAY = "rewind.and.play.duration";
	// Max size of the previous-files list
	private static int AUDIO_FILE_CACHE_LENGHT_DEFAULT = 7;
	public static String AUDIO_FILE_CACHE_LENGHT = "audio.file.cache.lenght";
	private static int TEXT_FILE_CACHE_LENGHT_DEFAULT = 7;
	public static String TEXT_FILE_CACHE_LENGHT = "text.file.cache.lenght";
	private static int SLOW_DOWN_PLAYBACK_DEFAULT = -5;
	public static String SLOW_DOWN_PLAYBACK = "slow.down.playback";
	private static int SPEED_UP_PLAYBACK_DEFAULT = 5;
	public static String SPEED_UP_PLAYBACK = "speed.up.plaback";
	// Auto save
	private static boolean AUTO_SAVE_DEFAULT = true;
	public static String AUTO_SAVE = "auto.save";
	private static int AUTO_SAVE_TIME_DEFAULT = 2;
	public static String AUTO_SAVE_TIME = "auto.save.time";
	// Mini-mode dialog
	private static boolean SHOW_MINI_MODE_DIALOG_DEFAULT = true;
	public static String SHOW_MINI_MODE_DIALOG = "show.mini.mode.dialog";
	// Font and size
	private static String FONT_DEFAULT = "Courier New";
	public static String FONT = "font";
	private static int FONT_SIZE_DEFAULT = 10;
	public static String FONT_SIZE = "font.size";
	private static Color FONT_COLOR_DEFAULT = Display.getCurrent()
			.getSystemColor(SWT.COLOR_BLACK);
	public static String FONT_COLOR = "font.color";
	private static Color BACKGROUND_COLOR_DEFAULT = Display.getCurrent()
			.getSystemColor(SWT.COLOR_WHITE);
	public static String BACKGROUND_COLOR = "background.color";

	// CONFIGURABLE ACCELERATORS
	private String accelerators;
	// Pause
	private static String PAUSE_KEY_DEFAULT = " ";
	public static String PAUSE_KEY = "pause.key";
	// Short rewind
	private static String SHORT_REWIND_KEY_DEFAULT = "7";
	public static String SHORT_REWIND_KEY = "short.rewind.key";
	// Long rewind
	private static String LONG_REWIND_KEY_DEFAULT = "8";
	public static String LONG_REWIND_KEY = "long.rewind.key";
	// Speed up
	private static String SPEED_UP_KEY_DEFAULT = "4";
	public static String SPEED_UP_KEY = "speed.up.key";
	// Slow down
	private static String SLOW_DOWN_KEY_DEFAULT = "3";
	public static String SLOW_DOWN_KEY = "slow.down.key";
	// Audio loops
	private static String AUDIO_LOOPS_KEY_DEFAULT = "9";
	public static String AUDIO_LOOPS_KEY = "audio.loops.key";
	public static String LOOP_FRECUENCY = "loop.frecuency";
	private static int LOOP_FRECUENCY_DEFAULT = 5;
	public static String LOOP_LENGHT = "loop.lenght";
	private static int LOOP_LENGHT_DEFAULT = 2;
	// Timestamps
	private static String TIMESTAMP_KEY_DEFAULT = "t";
	public static String TIMESTAMP_KEY = "timestamp.key";

	private Config() {

		super(CONFIG_PATH);

		// Set up the defaults
		setDefault(SHORT_REWIND, SHORT_REWIND_DEFAULT);
		setDefault(LONG_REWIND, LONG_REWIND_DEFAULT);
		setDefault(REWIND_AND_PLAY, REWIND_AND_PLAY_DEFAULT);
		setDefault(SHELL_HEIGHT, SHELL_HEIGHT_DEFAULT);
		setDefault(SHELL_LENGHT, SHELL_LENGHT_DEFAULT);
		setDefault(TEXT_FILE_CACHE_LENGHT, TEXT_FILE_CACHE_LENGHT_DEFAULT);
		setDefault(AUDIO_FILE_CACHE_LENGHT, AUDIO_FILE_CACHE_LENGHT_DEFAULT);
		setDefault(SLOW_DOWN_PLAYBACK, SLOW_DOWN_PLAYBACK_DEFAULT);
		setDefault(SPEED_UP_PLAYBACK, SPEED_UP_PLAYBACK_DEFAULT);
		setDefault(AUTO_SAVE, AUTO_SAVE_DEFAULT);
		setDefault(AUTO_SAVE_TIME, AUTO_SAVE_TIME_DEFAULT);
		setDefault(SHOW_MINI_MODE_DIALOG, SHOW_MINI_MODE_DIALOG_DEFAULT);
		setDefault(FONT, FONT_DEFAULT);
		setDefault(FONT_SIZE, FONT_SIZE_DEFAULT);
		setDefault(FONT_COLOR, FONT_COLOR_DEFAULT);
		setDefault(BACKGROUND_COLOR, BACKGROUND_COLOR_DEFAULT);

		// Pause
		setDefault(PAUSE_KEY, PAUSE_KEY_DEFAULT);
		// Short rewind
		setDefault(SHORT_REWIND_KEY, SHORT_REWIND_KEY_DEFAULT);
		// Long rewind
		setDefault(LONG_REWIND_KEY, LONG_REWIND_KEY_DEFAULT);
		// Playback speed
		setDefault(SPEED_UP_KEY, SPEED_UP_KEY_DEFAULT);
		setDefault(SLOW_DOWN_KEY, SLOW_DOWN_KEY_DEFAULT);
		// Audio loops
		setDefault(AUDIO_LOOPS_KEY, AUDIO_LOOPS_KEY_DEFAULT);
		setDefault(LOOP_FRECUENCY, LOOP_FRECUENCY_DEFAULT);
		setDefault(LOOP_LENGHT, LOOP_LENGHT_DEFAULT);
		// Timestamp
		setDefault(TIMESTAMP_KEY, TIMESTAMP_KEY_DEFAULT);
		// Cache
		setDefault(LAST_OPEN_AUDIO_PATH, LAST_OPEN_AUDIO_PATH_DEFAULT);
		setDefault(LAST_OPEN_TEXT_PATH, LAST_OPEN_TEXT_PATH_DEFAULT);
		setDefault(LAST_EXPORT_TRANSCRIPTION_PATH,
				LAST_EXPORT_TRANSCRIPTION_PATH_DEFALUT);

		try {
			load();
		} catch (Exception e) {
			// The properties will start as default values
		}

		updateAccelerators();

		// Add the listeners
		addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				try {
					updateAccelerators();
					save();
				} catch (IOException e) {
					// ignore
				}
			}
		});
	}

	public void showConfigurationDialog(Shell parent) throws PmTransException {
		// Create the preference manager
		PreferenceManager mgr = new PreferenceManager();

		// Create the nodes
		PreferenceNode playbackNode = new PreferenceNode("playbackPreferences");
		PreferencePage playbackPage = new FieldEditorPreferencePage() {
			@Override
			protected void createFieldEditors() {
				addField(new IntegerFieldEditor(SHORT_REWIND,
						"Short rewind duration (in sec)",
						getFieldEditorParent()));
				addField(new IntegerFieldEditor(LONG_REWIND,
						"Long rewind duration (in sec)", getFieldEditorParent()));
				addField(new IntegerFieldEditor(REWIND_AND_PLAY,
						"Rewind-and-resume duartion duration (in sec)",
						getFieldEditorParent()));
				addField(new IntegerFieldEditor(LOOP_FRECUENCY,
						"Loops frecuency (in seconds)", getFieldEditorParent()));
				addField(new IntegerFieldEditor(LOOP_LENGHT,
						"Loop rewind lenght (in seconds)",
						getFieldEditorParent()));
			}
		};
		playbackPage.setTitle("Playback preferences");
		playbackNode.setPage(playbackPage);

		PreferenceNode shortcutsNode = new PreferenceNode(
				"shortcutsPreferences");
		PreferencePage shortcutsPage = new FieldEditorPreferencePage() {
			@Override
			protected void createFieldEditors() {
				addField(new ShortcutFieldEditor(SHORT_REWIND_KEY,
						"Short rewind", getFieldEditorParent()));
				addField(new ShortcutFieldEditor(LONG_REWIND_KEY,
						"Long rewind", getFieldEditorParent()));
				addField(new ShortcutFieldEditor(PAUSE_KEY, "Pause and resume",
						getFieldEditorParent()));
				addField(new ShortcutFieldEditor(AUDIO_LOOPS_KEY,
						"Enable audio loops", getFieldEditorParent()));
				addField(new ShortcutFieldEditor(SLOW_DOWN_KEY,
						"Slow down audio playback", getFieldEditorParent()));
				addField(new ShortcutFieldEditor(SPEED_UP_KEY,
						"Speed up audio playback", getFieldEditorParent()));
				addField(new ShortcutFieldEditor(TIMESTAMP_KEY,
						"Insert timestamp", getFieldEditorParent()));
			}
		};
		shortcutsPage.setTitle("Shortcuts preferences");
		shortcutsNode.setPage(shortcutsPage);

		PreferenceNode generalNode = new PreferenceNode("generalPreferences");
		PreferencePage generalPage = new FieldEditorPreferencePage() {
			@Override
			protected void createFieldEditors() {
				addField(new IntegerFieldEditor(AUDIO_FILE_CACHE_LENGHT,
						"Max size of the \"recent audio files\" list",
						getFieldEditorParent()));
				addField(new IntegerFieldEditor(TEXT_FILE_CACHE_LENGHT,
						"Max size of the \"recent text files\" list",
						getFieldEditorParent()));
				// TODO add a separator here
				addField(new BooleanFieldEditor(AUTO_SAVE, "Auto save",
						getFieldEditorParent()));
				addField(new IntegerFieldEditor(AUTO_SAVE_TIME,
						"Auto save frecuency (in minutes)",
						getFieldEditorParent()));
			}
		};
		generalPage.setTitle("General preferences");
		generalNode.setPage(generalPage);

		mgr.addToRoot(playbackNode);
		mgr.addToRoot(shortcutsNode);
		mgr.addToRoot(generalNode);
		PreferenceDialog dlg = new PreferenceDialog(parent, mgr);
		dlg.setPreferenceStore(this);

		if (dlg.open() == PreferenceDialog.OK)
			try {
				save();
			} catch (IOException e) {
				throw new PmTransException("Unable to save preferences", e);
			}
	}

	private void updateAccelerators() {
		accelerators = "" + DEFAULT_ACCELERATORS;
		accelerators += getAcceleratorChar(AUDIO_LOOPS_KEY);
		accelerators += getAcceleratorChar(LONG_REWIND_KEY);
		accelerators += getAcceleratorChar(SHORT_REWIND_KEY);
		accelerators += getAcceleratorChar(PAUSE_KEY);
		accelerators += getAcceleratorChar(SPEED_UP_KEY);
		accelerators += getAcceleratorChar(SLOW_DOWN_KEY);
		accelerators += getAcceleratorChar(TIMESTAMP_KEY);
	}

	private char getAcceleratorChar(String action) {
		return (getString(action).equals("[space]") ? ' ' : getString(action)
				.charAt(0));
	}

	public static Config getInstance() {
		if (instance == null)
			instance = new Config();
		return instance;
	}

	public class ShortcutFieldEditor extends FieldEditor {

		String keyConst;

		Composite top;
		Label command;
		Label ctrl;
		Text character;

		public ShortcutFieldEditor(String key, String labelText,
				Composite parent) {
			super(key, labelText, parent);
			keyConst = key;
		}

		@Override
		protected void adjustForNumColumns(int numColumns) {
			((GridData) top.getLayoutData()).horizontalSpan = numColumns;
		}

		@Override
		protected void doFillIntoGrid(Composite parent, int numColumns) {
			top = parent;
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = numColumns;
			top.setLayoutData(gd);

			command = new Label(top, SWT.NORMAL);
			command.setText(getLabelText() + ":");
			GridData g = new GridData();
			g.grabExcessHorizontalSpace = true;
			g.horizontalAlignment = SWT.FILL;
			command.setLayoutData(g);
			ctrl = new Label(top, SWT.NORMAL);
			ctrl.setText("ctrl + ");
			character = new Text(top, SWT.NORMAL);
			g = new GridData();
			GC gc = new GC(character);
			FontMetrics fm = gc.getFontMetrics();
			g.widthHint = 10 * fm.getAverageCharWidth();
			gc.dispose();
			character.setLayoutData(g);
		}

		@Override
		protected void doLoad() {
			IPreferenceStore ps = getPreferenceStore();
			character.setText(ps.getString(keyConst).equals(" ") ? "[space]"
					: ps.getString(keyConst));
		}

		@Override
		protected void doLoadDefault() {
			IPreferenceStore ps = getPreferenceStore();
			character
					.setText(ps.getDefaultString(keyConst).equals(" ") ? "[space]"
							: ps.getDefaultString(keyConst));
		}

		@Override
		protected void doStore() {
			if (!character.getText().equals("[space]")
					&& character.getText().length() > 1) {
				MessageDialog
						.openError(
								Display.getCurrent().getActiveShell(),
								"Error",
								"The key "
										+ character.getText()
										+ " is not supported as a shortcut. Please use another one.");
				doLoadDefault();
				return;
			}
			String c = character.getText().equals("[space]") ? " " : character
					.getText();

			IPreferenceStore ps = getPreferenceStore();
			updateAccelerators();
			accelerators = accelerators.replace(ps.getString(keyConst), "");
			if (accelerators.contains(c)) {
				MessageDialog
						.openError(
								Display.getCurrent().getActiveShell(),
								"Error",
								"The key "
										+ c
										+ " is already used as a shortcut. Please use another one.");
				doLoad();
				updateAccelerators();
				return;
			}
			ps.setValue(keyConst, c);
		}

		@Override
		public int getNumberOfControls() {
			return 3;
		}
	}

	public Image getImage(String resourceName) {
		InputStream in = getClass().getResourceAsStream(resourceName);
		return new Image(Display.getCurrent(), in);
	}

	public Color getColor(String prop) {
		String color = getString(prop);
		if (color.isEmpty())
			return Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
		String rgb[] = color.split(";");
		return new Color(Display.getCurrent(), new RGB(
				Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]),
				Integer.parseInt(rgb[2])));
	}

	public void setDefault(String prop, Color color) {
		setDefault(prop, "" + color.getRed() + ";" + color.getGreen() + ";"
				+ color.getBlue());
	}

	public void setValue(String prop, Color color) {
		setValue(prop, "" + color.getRed() + ";" + color.getGreen() + ";"
				+ color.getBlue());
	}
}
