package caos.aaai.astar;

import caos.aaai.State;
import caos.aaai.astar.Heuristic.Evaluation;

public class Heuristic {
	public class Evaluation {
		public boolean isDeadEnd;
		public int h;
		public Evaluation(boolean b, int i) {
			isDeadEnd = b;
			h = i;
		}		
	}

	/**
	 * BFS heuristic.
	 */
	public Evaluation evaluate(State initialState) {
		// TODO: Make real heuristic
		int h;
		if (initialState.isGoal()) {
			h = 0;
		} else {
			h = 1;
		}
		return new Evaluation(false,h);
	}

}
