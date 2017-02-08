package AudioPlayback;

public class DummyPlayer extends Player {

	private int rate = 100;

	@Override
	protected void disposePlayer() {
		// Do nothing
	}

	@Override
	public void stop() {
		// Do nothing
	}

	@Override
	public double getMediaTime() {
		// Do nothing
		return 0;
	}

	@Override
	public void setMediaTime(double seconds) {
		// Do nothing
	}

	@Override
	public double getDuration() {
		// Do nothing
		return 0;
	}

	@Override
	public void start() {
		// Do nothing
	}

	@Override
	public boolean isPlaying() {
		// Do nothing
		return false;
	}

	@Override
	public void setRate(int rate) {
		this.rate = rate;
	}

	@Override
	public int getRate() {
		return rate;
	}

}
