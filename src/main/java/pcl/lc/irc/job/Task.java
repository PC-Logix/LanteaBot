package pcl.lc.irc.job;

public abstract class Task {

	protected final Object[] varargs;

	public Task(Object... args) {
		this.varargs = args;
	}

	public abstract void run(Object[] varargs);

}
