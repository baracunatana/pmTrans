package AudioPlayback;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import be.hogent.tarsos.dsp.AudioDispatcher;
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioPlayer;
import be.hogent.tarsos.dsp.AudioProcessor;
import be.hogent.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.hogent.tarsos.dsp.WaveformSimilarityBasedOverlapAdd.Parameters;
import be.hogent.tarsos.transcoder.DefaultAttributes;
import be.hogent.tarsos.transcoder.Transcoder;
import be.hogent.tarsos.transcoder.ffmpeg.EncoderException;

public class AudioPlayerTarsosDSP extends Player {

	AudioFormat format;
	AudioPlayer audioPlayer;
	AudioDispatcher dispatcher;
	WaveformSimilarityBasedOverlapAdd wsola;
	Thread audioT;
	double currentTime;
	File f;
	PlayerState state;
	double rate;
	boolean transcoded = false;

	static enum PlayerState {
		PLAYING, PAUZED,
	}

	public AudioPlayerTarsosDSP(final File file)
			throws UnsupportedAudioFileException, IOException {
		if (Transcoder.transcodingRequired(file.getAbsolutePath(),
				DefaultAttributes.WAV_PCM_S16LE_MONO_44KHZ.getAttributes())) {
			final File monoFile = new File(".temp_" + file.getName() + ".wav");
			final Shell[] shell = Display.getCurrent().getShells();

			ProgressMonitorDialog pmg = new ProgressMonitorDialog(shell[0]);
			try {
				pmg.run(true, false, new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException,
							InterruptedException {
						monitor.beginTask(
								"Processing audio file. Please wait.",
								IProgressMonitor.UNKNOWN);
						convertToMonoWav(file, monoFile);
						transcoded = true;
					}
				});
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			f = monoFile;
		} else
			f = file;
		format = AudioSystem.getAudioFileFormat(f).getFormat();
		currentTime = 0;
		rate = 1;
		start();
	}

	@Override
	protected void disposePlayer() {
		stop();
		if (transcoded) {
			System.out.println("Deleting temp file " + f.getAbsolutePath());
			f.delete();
		}
	}

	@Override
	public void stop() {
		dispatcher.stop();
		state = PlayerState.PAUZED;
	}

	@Override
	public double getMediaTime() {
		return currentTime;
	}

	@Override
	public void setMediaTime(double seconds) {
		if (state == PlayerState.PLAYING) {
			startFrom(seconds);
		} else
			currentTime = seconds;
	}

	@Override
	public double getDuration() {
		return dispatcher.durationInSeconds();
	}

	@Override
	public void start() {
		startFrom(currentTime);
		state = PlayerState.PLAYING;
	}

	private void startFrom(double seconds) {
		try {
			if (isPlaying()) {
				dispatcher.stop();
				audioT.interrupt();
			}

			audioPlayer = new AudioPlayer(format);
			wsola = new WaveformSimilarityBasedOverlapAdd(
					Parameters.slowdownDefaults(rate, format.getSampleRate()));
			dispatcher = AudioDispatcher.fromFile(f,
					wsola.getInputBufferSize(), wsola.getOverlap());

			dispatcher.addAudioProcessor(wsola);
			dispatcher.addAudioProcessor(audioPlayer);
			dispatcher.addAudioProcessor(new AudioProcessor() {

				@Override
				public void processingFinished() {
					// TODO Auto-generated method stub

				}

				@Override
				public boolean process(AudioEvent arg0) {
					currentTime = arg0.getTimeStamp();
					return true;
				}
			});
			dispatcher.skip(seconds);
			audioT = new Thread(dispatcher);
			audioT.start();
			state = PlayerState.PLAYING;
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean isPlaying() {
		return state == PlayerState.PLAYING;
	}

	@Override
	public void setRate(int nRate) {
		if (nRate < 20)
			nRate = 20;
		if (nRate > 200)
			nRate = 200;
		speedScale.setSelection(nRate);
		rate = nRate / 100.0;
		if (isPlaying())
			startFrom(currentTime);
		updateRateText();
	}

	@Override
	public int getRate() {
		return (int) (rate * 100);
	}

	private void convertToMonoWav(File sourceFileName, File destFileName) {
		try {
			Transcoder.transcode(sourceFileName, destFileName,
					DefaultAttributes.WAV_PCM_S16LE_MONO_44KHZ);
		} catch (EncoderException e) {
			e.printStackTrace();
		}
	}

}
