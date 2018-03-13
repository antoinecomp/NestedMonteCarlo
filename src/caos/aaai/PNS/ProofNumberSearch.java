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

public class ProofNumberSearch implements SearchInterface {
	public static final boolean DEBUG = false;

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
	private static long START_TIME;
	public ProofNumberSearch() {
		tt = new TranspositionTable();
	}


	@Override
	public void initializeSearch(OperatorLibrary lib, StartingMaterialsLibrary sml, int bound) {
		library = lib;
		startingMaterials = sml;
		heuristic = new Heuristic();
		BOUND = bound;
		if (bound < 1) {
			BOUND = Integer.MAX_VALUE;
		}
	}

	public void search(State initialState) {
		System.err.println("And now, Proof Number Search...");
		START_TIME = System.currentTimeMillis();

		this.root = tt.getNode(initialState);
		Evaluation eval = heuristic.evaluate(initialState);
		root.initialize(eval.proof, eval.disproof);
		int i = 0;
		while(!root.isProven() && !root.isDisproven()) {
			System.err.println("-----Root expansion "+(i++)+".  Constructed "+PNNode.NODE_COUNT+" nodes.  Generated="+generated_states+" time="+(System.currentTimeMillis()-START_TIME)+"  Root.proof="+root.getProof()+" Root.disproof="+root.getDisproof());
			Collection<PNNode> ancestors = new ArrayList<PNNode>();
			expandBestTipNode(root, ancestors, 0);
			if (i>=BOUND) { 
				break;
			}
		}
		System.err.println("-----"+i+"th root expansion completed.  Constructed "+PNNode.NODE_COUNT+" nodes.  Root.proof="+root.getProof()+" Root.disproof="+root.getDisproof());
		System.err.println("-----Constructed "+PNNode.NODE_COUNT+" nodes. Generated="+generated_states+". Expanded="+expanded_states);
		System.err.println("-----Time="+(System.currentTimeMillis()-START_TIME));
	}

	public Collection<GroundedOperator> getAnswer() {
		Collection<List<GroundedOperator>> tree_answer = this.root.collectMinProofTree("", startingMaterials);
		Collection<GroundedOperator> flattened_answer = new ArrayList<GroundedOperator>();
		if(tree_answer.isEmpty()) {
			System.err.println("=======WE FAILED TO FIND AN ANSWER========");
		} else {
			for(List<GroundedOperator> possibleProducts : tree_answer) {
				GroundedOperator go = possibleProducts.get(0);
				Molecule goal = ((OrNode)root).getState().getMolecule();
				
				boolean foundGoal;
				try {
					foundGoal = MolecularUtils.molecularEquivalentToSomeChiralForm(goal, go.product);
					if(foundGoal) {
						System.err.println("ACCEPTING "+go.product.toFormat("cxsmarts"));
						collectAnswerFromGroundedOperators(flattened_answer, go);
						System.err.println("RETURNING "+flattened_answer.size()+" operators in our solution.");
						
						break;
					} else {
						System.err.println("rejecting "+go.product.toFormat("cxsmarts")+" as it's not "+goal.toFormat("cxsmarts"));
					}
				} catch (SearchException e) {
				}
			}
		}
		return flattened_answer;
	}
	private void collectAnswerFromGroundedOperators(
			Collection<GroundedOperator> flattened_answer, GroundedOperator go) {
		if (go != null) {
			flattened_answer.add(go);
			System.err.println("Just flattened "+go.idString+" "+(go.rxn==null?go.rxn:go.rxn.getName()));
			if (go.reagentOps != null) {
				for(GroundedOperator op : go.reagentOps) {
					collectAnswerFromGroundedOperators(flattened_answer, op);
				}
			}
		}
	}


	public boolean getSuccess() {
		return this.root.isProven();
	}

	@Override
	public void dumpSearchSpace()  throws Exception  {
		Collection<GroundedOperator> c = this.root.collectFullSearchTree("");
		OperatorLibrary.drawCollection(c);	
	}


	private void expandBestTipNode(PNNode node, Collection<PNNode> ancestorNodes, int depth) {
		String dots = "";
		for(int i=0; i<depth; i++)
			dots += "...";
		Collection<PNNode> newAncestors = new ArrayList<PNNode>(ancestorNodes);
		newAncestors.add(node);				

		if(DEBUG){
			System.err.println(dots+"old ancestorList.size="+ancestorNodes.size()+" new ancestorList.size="+newAncestors.size());
			System.err.print(dots+"[");
			for(PNNode ss : ancestorNodes) {
				System.err.print("  "+ss.hashCode());
			}
			System.err.println(" ]");
		}

		
		if (node.isLeaf()) {
			if(DEBUG){
				System.err.print(dots+"expanding leaf "+node.hashCode()+" at depth "+depth+" with state ");
				node.dump();
			}
			assert(node instanceof OrNode);

			expandLeaf((OrNode)node, ancestorNodes);
			if(DEBUG){
				System.err.println(dots+"that leaf now has proof="+node.getProof()+" and disproof="+node.getDisproof());	
			}
		} else {
			assert(!node.isLeaf()); 
			boolean changed = false;
			while(!changed) {
				if(DEBUG){
					System.err.println(dots+"traversing "+(node instanceof OrNode?"Or":"And")+"Node "+node.hashCode()+" at depth "+depth+" that's got proof="+node.getProof()+" and disproof="+node.getDisproof()+".  Selecting child "+node.getBestChildIndex()+((node.actions==null||node.getBestChildIndex()==-1)?"":(" via "+node.actions.get(node.getBestChildIndex()).a.toString()))+". We've found "+newAncestors.size()+" ancestors.");
					node.dump();
				}
				PNNode bestChild = node.getBestChild(ancestorNodes);
				if (bestChild != null && node.stillConsistent()) {
					// AndNodes should have the same disproof number as their best child.
					// OrNodes should have the same proof number as their best child.
					// However, since we're searching graphs and not trees, the values of the
					// children might have changed since we determined which child was "best".
					// So, if the numbers are inconsistent, then skip expanding and recompute
					// best.
					expandBestTipNode(bestChild, newAncestors, depth+1);					
				} else {
					// If the bestChild's proof or disproof numbers changed since we expanded this node,
					// then it's not safe to assume that it's still the best child.  Skip expanding it,
					// just update this node's proof and disproof numbers.
				}
				// If this node's proof/disproof numbers didn't change then its still it's parent's best node.
				// (assuming tree and not graph, of course (making this is a blatant but useful lie)).
				// (this will turn out to be false because we're searching AND/OR graphs rather than trees,
				// so some distant descendant may have changed without our knowledge.)
				changed = node.updateProofDisproofNumbers(ancestorNodes);
				if(DEBUG){
					System.err.println(dots+"done traversing "+(node instanceof OrNode?"Or":"And")+"Node "+node.hashCode()+" at depth "+depth+". It's now got proof="+node.getProof()+" and disproof="+node.getDisproof()+" and I think that means the numbers "+(changed?"changed":"didn't change")+".  Best child is now "+node.getBestChildIndex()+((node.actions==null||node.getBestChildIndex()==-1)?"":(" via "+node.actions.get(node.getBestChildIndex()).a.toString())));			
				}
			}
		}
	}

	private void expandLeaf(OrNode node, Collection<PNNode> ancestorNodes) {
		assert(node.isLeaf());
		expanded_states++;

		List<PNNode> kids     = new ArrayList<PNNode>();
		List<Pair<OperatorLibrary.Operator, int[]>> actions  = new ArrayList<Pair<OperatorLibrary.Operator, int[]>>();
		int op_count = 0;
		boolean done = false;
		if(DEBUG){
			System.err.print(" time="+(System.currentTimeMillis()-START_TIME)+".  Working on node:"+node.hashCode()+". Applying operator ");
		}
		for(OperatorLibrary.Operator op : library) {

			if(DEBUG){
				System.err.print((op_count++)+" ");
			}
			Collection<Pair<Collection<State>, int[]>> alternativeSuccessors = op.apply(node.getState(), startingMaterials, "..");
			for(Pair<Collection<State>, int[]> pair : alternativeSuccessors) {
				Collection<State> successorStates = pair.a;
				int[] ratio = pair.b;
				if(DEBUG){
					System.err.println("Successor States size="+successorStates.size());
				}
				generated_states += successorStates.size(); 

				List<PNNode> fragments = new ArrayList<PNNode>();
				for(State s : successorStates) {

						PNNode n = tt.getNode(s);
						if(DEBUG){
							System.err.println("Considering node="+n.hashCode()+" with state: "+s.toString());
						}
						if(n.isProven() || n.isDisproven()) {
							// don't fiddle with the evals.
							if(DEBUG){
								System.err.println("State is already "+(n.isProven()?"proven":"disproven"));
							}
						} else if (n.isLeaf() && n.isUninitialized()) {
							// heuristic initialization of p/d numbers
							Evaluation eval = heuristic.evaluate(s);
							n.initialize(eval.proof, eval.disproof);
							if(DEBUG){
								System.err.println("Initializing state to p="+n.getProof()+", d="+n.getDisproof());
							}
						} else if (n.isLeaf() && !n.isUninitialized()) {
							// previously initialized leaf 
							if(DEBUG){
								System.err.println("Previously initialized leaf node p="+n.getProof()+", d="+n.getDisproof());
							}
						} else {
							// internal node
							if(DEBUG){
								System.err.println("which is an internal node with p="+n.getProof()+", d="+n.getDisproof());
							}
						}
						fragments.add(n);
				}

				// TODO: real GHI.  This just a hack to prevent cycles.
				boolean cyclic_solution = false;

				// If the only way to make this state requires its own ancestors, then don't bother adding anything as a child!
				if(!cyclic_solution) {
					PNNode newNode;
					if(fragments.size() == 1) {
						// Don't bother building an AndNode to wrap one child.
						// e.g. If this is a FGI and there's only one reactant. 
						newNode = fragments.get(0);
					} else {
						newNode = new AndNode(fragments, op);
					}

					kids.add(newNode);
					actions.add(new Pair<Operator, int[]>(op, ratio));
				}
			}
		}
		if(DEBUG){
			System.err.println();
		}
		node.expand(kids, actions);
	}


}
