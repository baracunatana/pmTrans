package AudioPlayback;

import gui.Config;

import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Scale;

public abstract class Player {

	private static final int PROGRESS_RESOLUTION = 100000;
	private static final int PROGRESS_REFRESH_RATE = 100;

	private Group visualization;
	private ProgressBar progress;
	private Button btnPlay;
	protected Scale speedScale;
	private Button btnLoops;
	protected boolean skipLoop = false;
	private AudioLoopsRunnable loopsTask;
	private Label speedLbl;

	public void rewind(int s) {
		if (getMediaTime() > s)
			setMediaTime(getMediaTime() - s);
		else
			setMediaTime(0);
		if (btnLoops.getSelection())
			restartLoops();
	}

	private void restartLoops() {
		if (loopsTask != null) {
			toogleLoops();
			toogleLoops();
		}
	}

	protected abstract void disposePlayer();

	public abstract void stop();

	/**
	 * @return The actual media time in seconds
	 */
	public abstract double getMediaTime();

	public abstract void setMediaTime(double seconds);

	public abstract double getDuration();

	public abstract void start();

	public abstract boolean isPlaying();

	/**
	 * Sets the play back rate
	 * 
	 * @param rate
	 *            New play back rate in the range 20-200
	 */
	public abstract void setRate(int rate);

	/**
	 * @return The play back rate in a range of 20-200
	 */
	public abstract int getRate();

	public void pauseAndRewind() {
		if (isPlaying()) {
			stop();
			btnPlay.setImage(Config.getInstance().getImage(
					Config.ICON_PATH_PLAY));
			visualization.layout();
		} else {
			rewind(Config.getInstance().getInt(Config.REWIND_AND_PLAY));
			start();
			btnPlay.setImage(Config.getInstance().getImage(
					Config.ICON_PATH_PAUSE));
			if (btnLoops.getSelection())
				restartLoops();
		}
	}

	public void close() {
		visualization.dispose();
		disposePlayer();
	}

	public void initGUI(Composite comp, Object layoutData) {
		visualization = new Group(comp, SWT.NONE);
		visualization.setLayout(new GridLayout(6, false));

		btnPlay = new Button(visualization, SWT.PUSH);
		btnPlay.setImage(Config.getInstance().getImage(Config.ICON_PATH_PAUSE));
		btnPlay.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				pauseAndRewind();
			}
		});

		Button btnRestart = new Button(visualization, SWT.PUSH);
		btnRestart.setImage(Config.getInstance().getImage(
				Config.ICON_PATH_RESTART));
		btnRestart.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setMediaTime(0);
			}
		});

		progress = new ProgressBar(visualization, SWT.SMOOTH);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.heightHint = 40;
		progress.setLayoutData(gd);
		progress.setMaximum(PROGRESS_RESOLUTION);
		progress.setMinimum(0);
		progress.setSelection(50);
		Display.getCurrent().timerExec(PROGRESS_REFRESH_RATE, new Runnable() {
			public void run() {
				if (!progress.isDisposed()) {
					updateProgressBar();
					Display.getCurrent().timerExec(PROGRESS_REFRESH_RATE, this);
				}
			}
		});
		progress.setTouchEnabled(true);
		progress.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				double selection = (double) arg0.x
						/ (double) progress.getBounds().width;
				setMediaTime((long) (selection * getDuration()));
				progress.setSelection((int) (progress.getMaximum() * selection));
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {

			}
		});
		progress.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				String time = getElapsedTimeString();
				Point point = progress.getSize();

				FontMetrics fontMetrics = e.gc.getFontMetrics();
				int width = fontMetrics.getAverageCharWidth() * time.length();
				int height = fontMetrics.getHeight();
				e.gc.setForeground(Display.getCurrent().getSystemColor(
						SWT.COLOR_BLACK));
				e.gc.drawString(time, (point.x - width) / 2,
						(point.y - height) / 2, true);
			}
		});

		final DecimalFormat twoDigit = new DecimalFormat("#0.00");
		speedLbl = new Label(visualization, SWT.NONE);
		speedLbl.setText(twoDigit.format(((double) getRate() / 100.0)) + "x");

		speedScale = new Scale(visualization, SWT.HORIZONTAL);
		speedScale.setMinimum(20);
		speedScale.setMaximum(200);
		speedScale.setSelection(100);
		speedScale.setIncrement(1);
		speedScale.setPageIncrement(20);
		gd = new GridData();
		gd.widthHint = 100;
		speedScale.setLayoutData(gd);

		speedScale.setSize(50, 50);
		speedScale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (getRate() != speedScale.getSelection()) {
					setRate(speedScale.getSelection());
				}
			}
		});

		btnLoops = new Button(visualization, SWT.TOGGLE);
		btnLoops.setImage(Config.getInstance().getImage(Config.ICON_PATH_LOOP));
		btnLoops.setToolTipText("Enable/disable audio loops");
		btnLoops.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				toogleLoops();
			}
		});

		visualization.pack();
		visualization.setLayoutData(layoutData);
	}

	public void updateRateText() {
		final DecimalFormat twoDigit = new DecimalFormat("#0.00");
		speedLbl.setText(twoDigit.format(((double) getRate() / 100.0)) + "x");
	}

	private void updateProgressBar() {
		progress.setSelection((int) (PROGRESS_RESOLUTION * getMediaTime() / getDuration()));

	}

	private String secondsToString(double seconds) {
		return String.format("%02d:%02d", ((int) seconds) / 60,
				((int) seconds) % 60);
	}

	private String getElapsedTimeString() {
		DecimalFormat twoDigit = new DecimalFormat("#0.0");
		return secondsToString(getMediaTime())
				+ "/"
				+ secondsToString(getDuration())
				+ " ("
				+ twoDigit.format(100 * getMediaTime()
						/ (getDuration() == 0 ? 1 : getDuration())) + ")% ";
	}

	private void toogleLoops() {
		if (loopsTask == null) {
			loopsTask = new AudioLoopsRunnable(this);
			Display.getCurrent().timerExec(
					Config.getInstance().getInt(Config.LOOP_FRECUENCY) * 1000,
					loopsTask);
		} else {
			loopsTask.stop();
			loopsTask = null;
		}
	}

	protected void updateSpeedDisplay() {
		speedScale.setSelection(getRate());
	}

	public void selectAudioLoops() {
		btnLoops.setSelection(!btnLoops.getSelection());
		toogleLoops();
	}

	public Composite getVisualization() {
		return visualization;
	}
}
