package caos.aaai.astar;

import caos.aaai.OperatorLibrary;
import caos.aaai.State;
import caos.aaai.OperatorLibrary.Operator;

public class SearchNode {
	private enum Status { NEW, OPEN, CLOSED, DEADEND }
	private State s;
	private int h;
	private int g;
	private Status status;
	private SearchNode parent;
	private Operator creatingOp;
	private int childIndex;
	
	public SearchNode(State s) {
		status = Status.NEW;
		this.s = s;
		h = -1;
		g = -1;
	}


	public void open_initial(int h) {
		assert(this.status == Status.NEW);
		this.status = Status.OPEN;		
		this.h = h;
		this.g = 0;
		this.parent = null;
		this.creatingOp = null;
		this.childIndex = -1;
	}

	public void open(int h, SearchNode parent, Operator op, int i) {
		assert(this.status == Status.NEW);
		this.status = Status.OPEN;	
		this.h = h;
		this.parent = parent;
		this.creatingOp = op;
		this.childIndex = i;
	}

	public void reopen(SearchNode parent, Operator op, int i) {
		assert(this.status == Status.OPEN || this.status == Status.CLOSED);
		this.status = Status.OPEN;
		this.parent = parent;
		this.g = parent.get_g()+1;
		this.creatingOp = op;
		this.childIndex = i;
	}

	public void close() {
		assert(this.status == Status.OPEN);
		this.status = Status.CLOSED;	
	}

	public int get_g() {
		return this.g;
	}

	public int get_h() {
		return this.h;
	}

	public int get_f() {
		return get_g() + get_h();
	}

	public State getState() {
		return this.s;
	}

	public void setDeadEnd() {
		assert(this.status == Status.OPEN || this.status == Status.CLOSED);
		this.status = Status.DEADEND;
	}

	public boolean isDeadEnd() {
		return this.status == Status.DEADEND;
	}
	
	public boolean isNew() {

		return this.status == Status.NEW;	}
	
	public boolean isClosed() {
		return this.status == Status.CLOSED;
	}
	
	public boolean isGoal() {
		return this.h==0;
	}


	public void dump() {
		System.err.println("Node child["+this.childIndex+"] dumped.");
		System.err.println(this.getState());
	}
}
