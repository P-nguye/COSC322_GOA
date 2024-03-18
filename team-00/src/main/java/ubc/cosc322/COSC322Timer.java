package ubc.cosc322;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Timer;
import java.util.TimerTask;

import ubc.cosc322.engine.core.Color;

public class COSC322Timer {

	public final static int TURN_MILLIS = 30_000;

	private Timer timer = null;
	private TimerTask task = null;
	private Color color;
	private long startMillis;

	public void start(Color color) {
		if (timer != null) {
			throw new IllegalStateException("Time already started");
		}
		
		this.color = color;
		System.out.println(color + "'s timer has been started");
		timer = new Timer();
		task = new Task();
		timer.schedule(task, TURN_MILLIS);
		startMillis = System.currentTimeMillis();
	}

	public void stop() {
		if (timer == null) {
			return;
		}
		timer.cancel();
		timer = null;
		long elapsedMillis = System.currentTimeMillis() - startMillis;
		System.out.println(color + "'s timer has been stopped, " + elapsedMillis + "ms elapsed");
		
	}

	

	private class Task extends TimerTask {

		@Override
		public void run() {
			timer = null;
			System.out.println(color + " HAS RUN OUT OF TIME !");
		}

	}
	
}
