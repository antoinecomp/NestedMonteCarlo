package caos.aaai.PNS;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import caos.aaai.MolecularUtils;
import caos.aaai.OperatorLibrary;
import caos.aaai.Pair;
import caos.aaai.StartingMaterialsLibrary;
import caos.aaai.OperatorLibrary.Operator.GroundedOperator;
import caos.aaai.SearchInterface;
import caos.aaai.State;
import caos.aaai.OperatorLibrary.Operator;
import caos.aaai.PNS.Heuristic.Evaluation;
import chemaxon.reaction.*;
import chemaxon.formats.*;
import chemaxon.sss.search.SearchException;
import chemaxon.struc.*;

public class ExhaustiveSearch implements SearchInterface {
	private OperatorLibrary library;
	private Heuristic heuristic = null;
	private TranspositionTable tt = null;
	private PNNode root = null;
	private int expanded_states;
	private int generated_states;
	private int evaluated_states;
	private int reopened_states;
	public static StartingMaterialsLibrary startingMaterials;
	public static boolean SOLVED = true;
	public static boolean IN_PROGRESS= false;
	private static int BOUND = -1;
	private static int DEPTH = 5;
	private static long START_TIME;	

	public ExhaustiveSearch() {
		tt = new TranspositionTable();
	}
	

	@Override
	public void initializeSearch(OperatorLibrary lib, StartingMaterialsLibrary sml, int bound) {
		library = lib; 
		startingMaterials = sml;
		heuristic = new Heuristic();
		BOUND = bound;
		DEPTH = bound;
	}

	public void search(State initialState) {
		System.err.println("And now, Exhaustive Search...");
		START_TIME=System.currentTimeMillis();
		this.root = tt.getNode(initialState);
		Evaluation eval = heuristic.evaluate(initialState);
		root.initialize(eval.proof, eval.disproof);
		Collection<State> ancestors = new HashSet<State>();

		// IDA*
		for (int i=1; i<=DEPTH; i++) {
			expandAllChildren(initialState,i,ancestors,new StringBuffer());
		}

		System.err.println("-----Constructed "+PNNode.NODE_COUNT+" nodes. Generated="+generated_states+". Expanded="+expanded_states);

	}
	
	private void expandAllChildren(State state, int depth2, Collection<State> exploredStates, StringBuffer sb) {
		if (depth2 <= 0) 
			return;
		try {
			if(startingMaterials.contains(state.getMolecule()))
				return;
		} catch (SearchException e) {
		}
		expanded_states++;
		exploredStates.add(state);

		
		int op_count = 0;
		
		for(OperatorLibrary.Operator op : library) {
			sb.append((op_count++)+" ");
			Collection<Pair<Collection<State>, int[]>> alternativeSuccessors = op.apply(state, startingMaterials, "..");
			for(Pair<Collection<State>, int[]> p : alternativeSuccessors) {
				Collection<State> successorStates = p.a;
				generated_states += successorStates.size(); 
				for(State s : successorStates) {
						expandAllChildren(s, depth2-1, exploredStates,sb);
					}
				}
		}
		sb.setLength(0);
	}


	public Collection<GroundedOperator> getAnswer() {
		return this.root.collectUnorderedMinProofTree("");
	}
	public boolean getSuccess() {
		return this.root.isProven();
	}

	@Override
	public void dumpSearchSpace()  throws Exception  {
	    Collection<GroundedOperator> c = this.root.collectFullSearchTree("");
	    OperatorLibrary.drawCollection(c);	
	}
}
