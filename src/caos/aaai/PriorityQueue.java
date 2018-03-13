package caos.aaai;

import java.util.ArrayList;
import java.util.List;

public class PriorityQueue {
	List<State> searchspace;
	
	public PriorityQueue() {
		this.searchspace = new ArrayList<State>();
	}

	// TODO: make this more intelligent!
	public State pop_best_node() {
		return searchspace.remove(0);
	}

	// TODO: make this more intelligent!
	public void insert(int get_f, int succ_h, State state) {
		searchspace.add(state);
	}

	public boolean isEmpty() {
		return searchspace.isEmpty();
	}
	
	public void dump() {
		String str="";
		for(State s:searchspace) {
			str += s.toString()+".";
		}
		System.err.println("~/bin/view.sh \""+str+"\"");
	}

}
