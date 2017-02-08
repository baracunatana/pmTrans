package gui;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class GlobalKeyListener implements NativeKeyListener {

	private PmTrans evidenceBucket;

	public GlobalKeyListener() {
		LogManager.getLogManager().reset();

		// Get the logger for "org.jnativehook" and set the level to off.
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage()
				.getName());
		logger.setLevel(Level.OFF);
	}

	public void nativeKeyPressed(NativeKeyEvent arg0) {
		// ignore
	}

	@Override
	public void nativeKeyReleased(final NativeKeyEvent event) {
		if (evidenceBucket != null)
			if (evaluateKeyEvent(event,
					Config.getInstance().getString(Config.PAUSE_KEY)
							.toLowerCase().charAt(0)))
				evidenceBucket.pauseAndRewind();
			else if (evaluateKeyEvent(event,
					Config.getInstance().getString(Config.SHORT_REWIND_KEY)
							.toLowerCase().charAt(0)))
				evidenceBucket.rewind(Config.getInstance().getInt(
						Config.SHORT_REWIND));
			else if (evaluateKeyEvent(event,
					Config.getInstance().getString(Config.LONG_REWIND_KEY)
							.toLowerCase().charAt(0)))
				evidenceBucket.rewind(Config.getInstance().getInt(
						Config.LONG_REWIND));
			else if (evaluateKeyEvent(event,
					Config.getInstance().getString(Config.SPEED_UP_KEY)
							.toLowerCase().charAt(0)))
				evidenceBucket.modifyAudioPlaybackRate(Config.getInstance()
						.getInt(Config.SPEED_UP_PLAYBACK));
			else if (evaluateKeyEvent(event,
					Config.getInstance().getString(Config.SLOW_DOWN_KEY)
							.toLowerCase().charAt(0)))
				evidenceBucket.modifyAudioPlaybackRate(Config.getInstance()
						.getInt(Config.SLOW_DOWN_PLAYBACK));
			else if (evaluateKeyEvent(event,
					Config.getInstance().getString(Config.AUDIO_LOOPS_KEY)
							.toLowerCase().charAt(0)))
				evidenceBucket.selectAudioLoops();
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent arg0) {
		// ignore
	}

	public void registerMainWindow(PmTrans evidenceBucket) {
		this.evidenceBucket = evidenceBucket;
	}

	private boolean evaluateKeyEvent(NativeKeyEvent event, char key) {
		String keyText = NativeKeyEvent.getKeyText(event.getKeyCode());
		keyText = keyText.equals("Space") ? " " : keyText;
		Character pressed = keyText.length() == 1 ? keyText.charAt(0) : null;
		return event.getModifiers() == NativeKeyEvent.CTRL_MASK
				&& pressed != null && pressed == key;
	}

	public void unregisterMainWindow() {
		evidenceBucket = null;
	}
}
