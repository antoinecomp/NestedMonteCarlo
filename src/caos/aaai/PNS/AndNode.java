package caos.aaai.PNS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import caos.aaai.OperatorLibrary.Operator;
import caos.aaai.OperatorLibrary.Operator.GroundedOperator;
import caos.aaai.OperatorLibrary;
import caos.aaai.StartingMaterialsLibrary;
import caos.aaai.State;

public class AndNode extends PNNode {
	public AndNode(List<PNNode> fragments, Operator op) {
		super();
		this.expand(fragments, null);
	}

	public boolean updateProofDisproofNumbers(Collection<PNNode> ancestorNodes) {
		long preProof    = this.getProof();
		long preDisproof = this.getDisproof();
		int preBestChild = this.getBestChildIndex();

		long newProof    = 0;
		long newMaxProof = 0;
		long newDisproof = INFINITY;
		int bestChild = -1;
		for(int i=0; i<this.children.size(); i++) {
			PNNode c = this.children.get(i);
			if(ancestorNodes.contains(c)) {
				newProof = INFINITY;
				newDisproof = 0;
				bestChild = -1;
				break;
			}
			// AND must prove all children.  Therefore sum proof numbers
			if (newProof == INFINITY) {
				// already disproven, don't do anything
			} else if (c.isDisproven()) {
				// can't prove an AND node with a disproven child!
				newProof = INFINITY;
			} else {
				newProof += c.getProof();							
			}

			if(newMaxProof < c.getProof()) {
				// AND must prove all children.  Therefore max over child proofs is a lower bound.
				newMaxProof = c.getProof();
			}
			if(!c.isProven() && newDisproof > c.getDisproof()) {
				// AND can be disproved by any child.  Therefore min disproofs.
				newDisproof = c.getDisproof();
				bestChild = i;
			}
		}

		this.proof     = newProof;
		this.disproof  = newDisproof;
		this.bestChild = bestChild;
		// Did the counts change?
		if(ProofNumberSearch.DEBUG){
			System.err.println("AndNode "+this.hashCode()+" has p="+this.proof+", d="+this.disproof+".  Used to be p="+preProof+", d="+preDisproof+".  bestChild is "+this.bestChild+" and was "+preBestChild);
		}
		return newProof != preProof || newDisproof != preDisproof;
	}

	@Override
	protected PNNode findBestChild(Collection<PNNode> ancestorNodes) {
		long newProof    = 0;
		long newDisproof = INFINITY;
		int bestChild = -1;
		for(int i=0; i<this.children.size(); i++) {
			PNNode c = this.children.get(i);
			if(ancestorNodes.contains(c)) {
				continue;
			}
			// AND must prove all children.  Therefore sum proof numbers
			if (newProof == INFINITY) {
				// already disproven, don't do anything
			} else if (c.isDisproven()) {
				// can't prove an AND node with a disproven child!
				newProof = INFINITY;
			} else {
				newProof += c.getProof();							
			}

			if(!c.isProven() && newDisproof > c.getDisproof()) {
				// AND can be disproved by any child.  Therefore min disproofs.
				newDisproof = c.getDisproof();
				bestChild = i;
			}
		}

		if(bestChild == -1)
			return null;
		else
			return this.children.get(bestChild);
	}

	@Override
	public boolean stillConsistent() {
		// AndNodes have the same disproof number as their bestChild
		PNNode c = this.getBestChild(new ArrayList<PNNode>());
		return this.getDisproof() == c.getDisproof();
	}

	@Override
	public void dump() {
		String s = "";
		s +="[";
		for(PNNode c : children) {
			//			s += ((OrNode)c).toString()+".";
			s += ((OrNode)c).hashCode()+", ";
		}
		s +="]";
		System.err.println("AndNode "+hashCode()+": #children="+(children==null?"null":children.size())+" "+s);
	}

	@Override
	public Collection<GroundedOperator> collectUnorderedMinProofTree(String idString) {
		Collection<GroundedOperator> c = new ArrayList<GroundedOperator>();
		int i=0;
		for(PNNode kid : this.children) {
			System.err.println("... adding an AndNode child:"+kid.toString()+" proof="+kid.getProof()+" disproof="+kid.getDisproof());
			c.addAll(kid.collectUnorderedMinProofTree(idString+"["+(i++)+"]"));
		}
		return c;
	}
	@Override
	public Collection<GroundedOperator> collectFullSearchTree(String idString) {
		Collection<GroundedOperator> c = new ArrayList<GroundedOperator>();
		if (this.children != null) {
			int i=0;
			for(PNNode kid : this.children) {
				c.addAll(kid.collectFullSearchTree(idString+"["+(i++)+"]"));
			}
		}
		return c;
	}

	public Collection<State> getEnclosedStates() {
		Collection<State> c = new ArrayList<State>();
		for(PNNode kid : this.children) {
			c.add(((OrNode) kid).getState());
		}
		return c;
	}

	@Override
	public Collection<List<GroundedOperator>> collectMinProofTree(String idString,
			StartingMaterialsLibrary sml) {
		Collection<List<GroundedOperator>> answer = new ArrayList<List<GroundedOperator>>();
		answer.add(new ArrayList<GroundedOperator>());
		if(OperatorLibrary.DEBUG_FORWARDIZATION) {
			System.err.println("... entering an AndNode: "+idString+" hash="+this.hashCode());
		}

		// Compute Cartesian Product of children's products.
		// L is a list of lists.  
		// answer is initialized to {{}}.
		// for each list X in L,
		//   create a new_answer list, which is empty 
		//   for each element Y in X,
		//      create a duplicate D of answer.
		//      append Y to each element of D
		//      union D into new_answer
		//   set answer equal to new_answer

		int i=0;
		// Here, L is the list of children's min proof tree results 
		for(PNNode kid : this.children) {
			if(OperatorLibrary.DEBUG_FORWARDIZATION) {
				System.err.println("... adding an AndNode child:"+kid.toString()+" hash="+kid.hashCode()+", the "+i+"th child, out of "+this.children.size()+", with proof="+kid.getProof()+" disproof="+kid.getDisproof()+" hash="+this.hashCode());
			}
			if(OperatorLibrary.DEBUG_FORWARDIZATION) {
				System.err.println("...  PRE  answer.size="+answer.size()+" ops.  Size of the first one is "+(answer.iterator().hasNext()?answer.iterator().next().size():"empty!"));
			}
			Collection<List<GroundedOperator>> new_answer = new ArrayList<List<GroundedOperator>>();

			Collection<List<GroundedOperator>> X = kid.collectMinProofTree(idString+"["+(i++)+"]", sml);
			if(OperatorLibrary.DEBUG_FORWARDIZATION) {
				System.err.print("... X.size="+X.size()+" X's sizes= ");
				for(List<GroundedOperator> Y_wrapper : X) {
					System.err.print(Y_wrapper.size()+" ");
				}
				System.err.println();
			}
			for(List<GroundedOperator> Y_wrapper : X) {
				GroundedOperator Y;
				if(Y_wrapper.isEmpty()) {
					System.err.println("... the AndNode's "+(i-1)+"th child:"+kid.toString()+" had an empty grounded operator!");
					Y = null;
					throw new RuntimeException("Y_wrapper should never be empty!");
				} else {
					Y = Y_wrapper.get(0);
				}
				Collection<List<GroundedOperator>> D =  new ArrayList<List<GroundedOperator>>();
				for(List<GroundedOperator> lgo : answer) {
					List<GroundedOperator> newList = new ArrayList<GroundedOperator>();
					newList.addAll(lgo); // copy the list, so we don't add things into the same backing array
					D.add(newList);
				}
				for(List<GroundedOperator> d : D) {
					d.add(Y);
				}
				new_answer.addAll(D);
			}
			answer = new_answer;
			if(OperatorLibrary.DEBUG_FORWARDIZATION) {
				System.err.println("...  POST answer.size="+answer.size()+" ops.  Size of the first one is "+(answer.iterator().hasNext()?answer.iterator().next().size():"empty!"));
			}
		}

		if(OperatorLibrary.DEBUG_FORWARDIZATION) {
			System.err.println("... exiting an AndNode: "+idString+" "+" hash="+this.hashCode()+" and returning "+answer.size()+" ops.  Size of the first one is "+(answer.iterator().hasNext()?answer.iterator().next().size():"empty!"));
		}
		return answer;
	}

}
