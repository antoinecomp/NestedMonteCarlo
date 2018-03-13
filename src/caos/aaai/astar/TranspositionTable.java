package caos.aaai.astar;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import caos.aaai.State;

// TODO: Is this class really necessary? 
public class TranspositionTable  {
	private HashMap<State, SearchNode> table;
	public TranspositionTable() {
		table = new HashMap();
	}
	public SearchNode getNode(State s) {
		SearchNode n = table.get(s);
		if (n==null) {
			n = new SearchNode(s);
			table.put(s, n);
		}
		return n;
	}

}
