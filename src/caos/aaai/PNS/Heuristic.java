package caos.aaai.PNS;

import caos.aaai.State;
import caos.aaai.PNS.Heuristic.Evaluation;

public class Heuristic {
	public class Evaluation {
		public long proof;
		public long disproof;
		public Evaluation(long p, long d) {
			proof = p;
			disproof = d;
		}		
	}

	/**
	 * BFS heuristic.
	 */
	public Evaluation evaluate(State s) {
		// TODO: Make real heuristic
		int h;
		if (s.isGoal()) {
			return new Evaluation(0,PNNode.INFINITY);
		} else {
			int size = s.size();
			return new Evaluation(1,1);
		}
	}

}
