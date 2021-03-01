package pcl.lc.utils;

public class CommandChainStateObject {
	public CommandChainState state;
	public String msg;

	public CommandChainStateObject(CommandChainState state, String msg) {
		this.state = state;
		this.msg = msg;
	}

	public CommandChainStateObject(CommandChainState state) {
		this.state = state;
		this.msg = null;
	}

	public CommandChainStateObject() {
		this.state = CommandChainState.FINISHED;
		this.msg = null;
	}
}
