package pcl.lc.irc.job;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;

import pcl.lc.irc.IRCBot;

public class TaskScheduler extends Thread {

	public static void queueTask() {

	}

	private final LinkedBlockingDeque<Task> tasks = new LinkedBlockingDeque<Task>();
	private boolean abort = false;

	public TaskScheduler() {
		setName("BotTaskScheduler");
		setDaemon(true);
	}

	public void abort() {
		this.abort = true;
	}

	@Override
	public void run() {
		try {
			while (!abort) {
				synchronized (tasks) {
					try {
						Task t = tasks.take();
						t.run(t.varargs);
					} catch (Throwable err) {
						if (err instanceof InterruptedException)
							throw err;
						IRCBot.getLog().log(Level.WARNING, "Task failed!", err);
					}
				}
			}
		} catch (InterruptedException interrupt) {
			// Do nothing
		}
	}

}
