package caos.aaai.PNS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import chemaxon.sss.search.SearchException;
import chemaxon.struc.Molecule;

import caos.aaai.OperatorLibrary;
import caos.aaai.OperatorLibrary.Operator;
import caos.aaai.OperatorLibrary.Operator.GroundedOperator;
import caos.aaai.MolecularUtils;
import caos.aaai.Pair;
import caos.aaai.StartingMaterialsLibrary;
import caos.aaai.State;

public class OrNode extends PNNode {
	private State s;
	// Charge each reaction a cost of 1 to encourage shallow-and-wide search trees.
	private static final int REACTION_COST = 1;


	public OrNode(State s) {
		super();
		this.s = s;
	}

	public State getState() {
		return this.s;
	}



	@Override
	protected PNNode findBestChild(Collection<PNNode> ancestorNodes) {
		long newProof    = INFINITY;
		long newDisproof = 0; 
		int bestChild = -1;
		for(int i=0; i<this.children.size(); i++) {
			PNNode c = this.children.get(i);
			// OR must disprove all children.  Therefore sum disproof numbers.
			if(ancestorNodes.contains(c)) {
				continue;
			}
			if (newDisproof == INFINITY) {
				// already proven, don't do anything
			} else if (c.isProven()) {
				// can't disprove an OR node with a proven child!
				newDisproof = INFINITY;
			} else {
				newDisproof += c.getDisproof();							
			}

			long childProofNumber = getChildProofNumber(c);
			if(!c.isDisproven() && newProof > childProofNumber) {
				// OR can be proved by any child.  Therefore min proofs.
				newProof = childProofNumber;
				bestChild = i;
			}
		}
		if(bestChild == -1)
			return null;
		else
			return this.children.get(bestChild);
	}


	public boolean updateProofDisproofNumbers(Collection<PNNode> ancestorNodes) {
		long preProof    = this.getProof();
		long preDisproof = this.getDisproof();

		long newProof    = INFINITY;
		long newDisproof = 0; 
		int bestChild = -1;
		for(int i=0; i<this.children.size(); i++) {
			PNNode c = this.children.get(i);
			if(ancestorNodes.contains(c)) {
				if(ProofNumberSearch.DEBUG){
					System.err.println("Found "+c.hashCode()+" in our ancestor list! so skipping it.  bestChild better not equal "+i+"! (hopefully some of the other "+this.children.size()+" kids will work out.");
				}
				continue;
			}
			// OR must disprove all children.  Therefore sum disproof numbers.
			if (newDisproof == INFINITY) {
				// already proven, don't do anything
			} else if (c.isProven()) {
				// can't disprove an OR node with a proven child!
				newDisproof = INFINITY;
			} else {
				newDisproof += c.getDisproof();							
			}

			long childProofNumber = getChildProofNumber(c);
			if(!c.isDisproven() && newProof > childProofNumber) {
				// OR can be proved by any child.  Therefore min proofs.
				newProof = childProofNumber;
				bestChild = i;
			}
		}

		this.proof     = newProof;
		this.disproof  = newDisproof;
		this.bestChild = bestChild;
		// Did the counts change?
		if(ProofNumberSearch.DEBUG){
			System.err.println("OrNode "+this.hashCode()+" has p="+this.proof+", d="+this.disproof+".  Used to be p="+preProof+", d="+preDisproof);
		}
		return newProof != preProof || newDisproof != preDisproof;
	}

	private long getChildProofNumber(PNNode c) {
		return (c.isProven()?c.getProof():(c.getProof()+REACTION_COST));
	}

	@Override
	public boolean stillConsistent() {
		// OrNodes have the same proof number as their bestChild
		PNNode c = this.getBestChild(new ArrayList<PNNode>());
		return this.getProof() == getChildProofNumber(c);
	}

	@Override
	public void dump() {
		System.err.println("OrNode "+hashCode()+": #children="+(children==null?"null":children.size())+" state:"+getState().toString());
	}

	@Override
	public Collection<GroundedOperator> collectUnorderedMinProofTree(String idString) {
		Collection<GroundedOperator> c = new ArrayList<GroundedOperator>();
		if(this.bestChild != -1) {
			String newIDString = idString+"."+this.bestChild;
			PNNode favoriteKid = this.children.get(this.bestChild);
			List<State> kidStates = new ArrayList<State>();
			kidStates.addAll(favoriteKid.getEnclosedStates());

			GroundedOperator go = new GroundedOperator(newIDString+" p="+favoriteKid.getProof()+" d="+favoriteKid.getDisproof(), null, this.getState(), kidStates);
			c.add(go);
			System.err.println("... adding an OrNode favorite child:"+favoriteKid.toString()+" proof="+favoriteKid.getProof()+" disproof="+favoriteKid.getDisproof());
			c.addAll(favoriteKid.collectUnorderedMinProofTree(newIDString));
		} else {
			String newIDString = idString+".leaf"+" p="+this.getProof()+" d="+this.getDisproof();
			GroundedOperator go = new GroundedOperator(newIDString, null, this.getState(), new ArrayList<State>());
			c.add(go);
		}
		return c;
	}

	@Override
	public Collection<GroundedOperator> collectFullSearchTree(String idString) {
		Collection<GroundedOperator> c = new ArrayList<GroundedOperator>();
		if(this.children == null) {
			GroundedOperator go = new GroundedOperator(idString+".leaf"+" p="+this.getProof()+" d="+this.getDisproof(), null, this.getState(), new ArrayList<State>());
			c.add(go);			
		} else {
			int i=0;
			for(PNNode favoriteKid : this.children) {
				List<State> kidStates = new ArrayList<State>();
				kidStates.addAll(favoriteKid.getEnclosedStates());
				String newIDString = idString+"."+(i++);

				GroundedOperator go = new GroundedOperator(newIDString+" p="+favoriteKid.getProof()+" d="+favoriteKid.getDisproof(), null, this.getState(), kidStates);
				c.add(go);
				c.addAll(favoriteKid.collectFullSearchTree(newIDString));
			}
		} 
		return c;
	}


	public Collection<State> getEnclosedStates() {
		Collection<State> c = new ArrayList<State>();
		c.add(this.getState());	
		return c;
	}

	@Override
	public Collection<List<GroundedOperator>> collectMinProofTree(String idString,
			StartingMaterialsLibrary sml) {
		Collection<List<GroundedOperator>> c = new ArrayList<List<GroundedOperator>>();
		if(OperatorLibrary.DEBUG_FORWARDIZATION) {
			System.err.println("... entering an OrNode: "+idString+" hash="+this.hashCode()+" looking to make a "+this.getState().getMolecule().toFormat("smarts"));
		}

		// bestChild could be a -1 if this were a disproof...
		if(!this.isLeaf() && this.bestChild != -1) {
			String newIDString = idString+"."+this.bestChild;

			PNNode favoriteKid = this.children.get(this.bestChild);
			Pair<Operator, int[]> p = this.actions.get(this.bestChild);
			OperatorLibrary.Operator creatingOp = p.a;
			int[] ratio = p.b;
			if(OperatorLibrary.DEBUG_FORWARDIZATION) {
				System.err.println("... this ain't a leaf so we're trying to add an OrNode favorite child:"+favoriteKid.toString()+" hash="+favoriteKid.hashCode()+" proof="+favoriteKid.getProof()+" disproof="+favoriteKid.getDisproof());
				System.err.println("... it looks like we used the "+creatingOp.toString()+" reaction: "+creatingOp.rxn.toFormat("smarts"));
			}

			Collection<Molecule> generatedProducts = new ArrayList<Molecule>();
			Collection<List<GroundedOperator>> possibleReagents = favoriteKid.collectMinProofTree(newIDString, sml);
			for(List<GroundedOperator> reagentOps_Original : possibleReagents) {
				Collection<List<GroundedOperator>> permutations = new ArrayList<List<GroundedOperator>>(); permutations.add(reagentOps_Original);
				if(OperatorLibrary.DEBUG_FORWARDIZATION) {
					System.err.println("... Returned "+permutations.size()+" permutations");
				}
				for(List<GroundedOperator> reagentOps : permutations) {
					// Pull the products out of the grounded operators.  This would be a simple map call in a modern language.
					List<Molecule> requiredReactants = new ArrayList<Molecule>();
					for(GroundedOperator go : reagentOps) {
						requiredReactants.add(go.product);
						if(OperatorLibrary.DEBUG_FORWARDIZATION) {
							System.err.println("... just added to the requiredReactants: "+go.product.toFormat("smarts"));
						}
					}

					Collection<Molecule> products;
					try {
						if(OperatorLibrary.DEBUG_FORWARDIZATION) {
							System.err.println("... the reaction: "+creatingOp.toString());

							System.err.print("... About to forward apply: ./run_reactor.sh ");
							for(Molecule m : requiredReactants) {
								System.err.print("'"+m.toFormat("smarts")+"' ");
							}
							System.err.println(" -r '"+creatingOp.rxn.toFormat("smarts")+"' ");
						}
						products = creatingOp.applyForward(requiredReactants, ratio);
						if(OperatorLibrary.DEBUG_FORWARDIZATION) {
							System.err.println("... just did a forward application and got "+products.size()+" products:");

							for(Molecule m : products) {
								System.err.println("...    "+m.toFormat("smarts"));
							}
						}

					} catch (Exception e1) {
						products = new ArrayList<Molecule>();
					}
					for(Molecule m : products) {
						// if the product matches our state
						Molecule raw_molecule = this.getState().getMolecule();
						boolean matchesOurState;
						try {
							matchesOurState = MolecularUtils.molecularSatisfiedBySomeChiralForm(m, raw_molecule);
							boolean matchesOurStateBackwards = MolecularUtils.molecularSatisfiedBySomeChiralForm(raw_molecule, m);
							if(OperatorLibrary.DEBUG_FORWARDIZATION) {
								System.err.println("... "+m.toFormat("smarts")+(matchesOurState?" matches our state":" fails to match our state")+" which was "+raw_molecule.toFormat("smarts")+" and, of course, if we tried things backwards, it would "+(matchesOurStateBackwards?"match!":"still fail."));
							}
							if(matchesOurState) {
								// and it hasn't yet been produced
								if(!MolecularUtils.molecularContains(generatedProducts, m)) {
									// then add it to our answers.
									generatedProducts.add(m);
									List<GroundedOperator> ops = new ArrayList<GroundedOperator>();
									GroundedOperator go = new GroundedOperator(newIDString+" p="+favoriteKid.getProof()+" d="+favoriteKid.getDisproof(), creatingOp, m, requiredReactants, reagentOps);
									ops.add(go);
									c.add(ops);
									if(OperatorLibrary.DEBUG_FORWARDIZATION) {
										System.err.println("... adding an OrNode favorite child:"+favoriteKid.toString()+" hash="+favoriteKid.hashCode()+"proof="+favoriteKid.getProof()+" disproof="+favoriteKid.getDisproof());
									}
								}							
							}
						} catch (SearchException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		} else {
			String newIDString = idString+".leaf"+" p="+this.getProof()+" d="+this.getDisproof();

			// We got here either because (1) it's a starting material leaf 
			// or (2) the search terminated prematurely and it's an unexpanded leaf.  
			// or (3) it's a disproved leaf! (probably won't happen if there's EVER an alternative...)
			Molecule raw_molecule = this.getState().getMolecule();
			if(OperatorLibrary.DEBUG_FORWARDIZATION) {
				System.err.println("... this is a leaf so we're trying to add :"+newIDString+" which was molecule "+raw_molecule.toFormat("smarts"));
			}
			Collection<Molecule> matches;
			try {
				matches = sml.getAllMolecularMatches(raw_molecule);
				if(OperatorLibrary.DEBUG_FORWARDIZATION) {
					System.err.println("...... we found "+matches.size()+" matches in the SML.");
				}
			} catch (SearchException e) {
				matches = new ArrayList<Molecule>();
			}
			if(matches.isEmpty()) {
				List<GroundedOperator> ops = new ArrayList<GroundedOperator>();
				GroundedOperator go = new GroundedOperator(newIDString, null, raw_molecule, null, null);
				ops.add(go);
				c.add(ops);
			} else {
				for(Molecule m: matches) {
					List<GroundedOperator> ops = new ArrayList<GroundedOperator>();
					GroundedOperator go = new GroundedOperator(newIDString, null, m, null, null);
					ops.add(go);
					c.add(ops);
				}
			}
		}

		if(OperatorLibrary.DEBUG_FORWARDIZATION) {
			System.err.println("... exiting an OrNode: "+idString+" and returning "+c.size()+" ops.");
		}
		return c;
	}

	private Collection<List<GroundedOperator>> permute(List<GroundedOperator> reagentOps) {
		Collection<List<GroundedOperator>> c = new ArrayList<List<GroundedOperator>>();

		// permutation of an empty list is the empty list
		if(reagentOps.isEmpty()) {
			c.add(new ArrayList<GroundedOperator>());
		} else {
			// add each element to the accumulator's lists.
			// permute the remaining elements.
			for(int i=0; i<reagentOps.size(); i++)	 {
				GroundedOperator go = reagentOps.get(i);
				List<GroundedOperator> remainingOps = new ArrayList<GroundedOperator>();
				remainingOps.addAll(reagentOps);
				remainingOps.remove(i);
				Collection<List<GroundedOperator>> tailPermutations = permute(remainingOps);
				for(List<GroundedOperator> stubPermute : tailPermutations) {
					stubPermute.add(go);
					c.add(stubPermute);
				}
			}
		}

		return c;
	}


}
