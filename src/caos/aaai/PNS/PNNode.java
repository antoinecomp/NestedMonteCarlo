package caos.aaai.PNS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import caos.aaai.OperatorLibrary;
import caos.aaai.OperatorLibrary.Operator.GroundedOperator;
import caos.aaai.Pair;
import caos.aaai.StartingMaterialsLibrary;
import caos.aaai.State;
import caos.aaai.OperatorLibrary.Operator;

public abstract class PNNode {
	public static long INFINITY      = Long.MAX_VALUE;   
	public static long UNINITIALIZED = Long.MIN_VALUE; 
	public static int NODE_COUNT = 0;
	private int childIndex;

	protected long proof;
	protected long disproof;
	protected List<PNNode> children;
	protected int bestChild;
	protected List<Pair<Operator, int[]>> actions;

	
	protected PNNode() {
		NODE_COUNT++;
		proof = UNINITIALIZED;
		disproof = UNINITIALIZED;
		bestChild = -1;
		children = null; 
	}


	public void initialize(long proof2, long disproof2) { //, Operator op) {
		this.proof      = proof2;
		this.disproof   = disproof2;
	}
	
	public long getProof() {
		return this.proof;
	}
	public long getDisproof() {
		return this.disproof;
	}
	public boolean isProven() {
		return getProof() == 0;
	}
	public boolean isDisproven() {
		return getDisproof() == 0;
	}
	public boolean isLeaf() {
		return this.children == null;
	}
	public boolean isUninitialized() {
		return this.proof == UNINITIALIZED && this.disproof == UNINITIALIZED;
	}
	public void setProven() {
		this.proof    = 0;
		this.disproof = INFINITY;
	}
	public void setDisproven() {
		this.proof    = INFINITY;
		this.disproof = 0;
	}

	public void expand(List<PNNode> kids, List<Pair<Operator, int[]>> actions2) {
		this.children = kids;
		this.actions  = actions2;
		updateProofDisproofNumbers();
	}
	
	public PNNode getBestChild(Collection<PNNode> ancestorNodes) {
		assert(children != null);
		PNNode kid = children.get(bestChild);
		
		if(ancestorNodes.contains(kid)) {
			// Don't try to prove via an ancestor.  Choose a different kid.
			kid = findBestChild(ancestorNodes);
		}
		return kid;
	}
	protected abstract PNNode findBestChild(Collection<PNNode> ancestorNodes);


	public int getBestChildIndex() {
		return bestChild;
	}

	public boolean updateProofDisproofNumbers() {
		return updateProofDisproofNumbers(new ArrayList<PNNode>());
	}
	public abstract boolean updateProofDisproofNumbers(Collection<PNNode> ancestorNodes);
	public abstract boolean stillConsistent();
	public abstract void dump();
	public abstract Collection<State> getEnclosedStates();

	public abstract Collection<List<GroundedOperator>> collectMinProofTree(String idString, StartingMaterialsLibrary sml);
	public abstract Collection<GroundedOperator> collectUnorderedMinProofTree(String idString);
	public abstract Collection<GroundedOperator> collectFullSearchTree(String idString);
}
