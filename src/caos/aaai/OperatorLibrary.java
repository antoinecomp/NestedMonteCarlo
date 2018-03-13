package caos.aaai;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.derby.iapi.services.io.ArrayOutputStream;

import test.ViewJTable;

import caos.aaai.MolecularUtils.MolecularListMap.Pair;
import caos.aaai.MolecularUtils.MolecularMap;
import caos.aaai.OperatorLibrary.Operator;
import caos.aaai.OperatorLibrary.Operator.GroundedOperator;
import caos.aaai.PNS.ProofNumberSearch;
import chemaxon.reaction.*;
import chemaxon.checkers.RingStrainErrorChecker;
import chemaxon.checkers.StructureChecker;
import chemaxon.checkers.ValenceErrorChecker;
import chemaxon.checkers.result.StructureCheckerResult;
import chemaxon.fixers.StructureFixer;
import chemaxon.formats.*;
import chemaxon.sss.search.MolSearchOptions;
import chemaxon.sss.search.SearchException;
import chemaxon.sss.search.StandardizedMolSearch;
import chemaxon.struc.*;
import chemaxon.struc.prop.MStringProp;

public class OperatorLibrary implements Iterable<OperatorLibrary.Operator> {
	private List<Operator> library;

	private static final boolean DEBUG = ProofNumberSearch.DEBUG; //true;
	public static final boolean DEBUG_FORWARDIZATION = ProofNumberSearch.DEBUG; //true;

	public static void drawCollection(Collection<GroundedOperator> c) throws Exception {
		List<Molecule> mols = new ArrayList<Molecule>();
		List<Molecule> rxns = new ArrayList<Molecule>();

		final Object[][] mol_array = new Object[c.size()][3];
		int i=0;
		for(GroundedOperator s : c ) {
			mol_array[i][0] = s.idString;
			mol_array[i][1] = s.rxn==null?new RxnMolecule():s.rxn;
			mol_array[i][2] = s.product;
			if(s.idString!=null && s.product!=null)
				System.err.println(s.idString+"   ~/bin/view.sh \'"+s.product.toFormat("cxsmarts")+"\'");
			i++;
		}
		System.err.println("----------------------");
		i=0;
		String smiles = "";
		try {
			for(GroundedOperator s : c ) {
				smiles+=s.product.toFormat("smiles")+".";
				i++;
			}
			smiles.substring(0, smiles.length()-1);
		} catch (Exception e) {}
		System.err.println(smiles);
		System.err.println("----------------------");
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ViewJTable.createAndShowGUI(mol_array);
			}
		});
	}

	public static class Operator {

		// Trade-off.  The higher this number, the more independent reaction
		// sites can be reacted but the slower the forward check becomes.
		private static final int EXCESS_REAGENT_LIMIT = 50;  // just guessing...
		private static final int EXCESS_FORWARD_REAGENT_LIMIT = 4;  // This is for ChemAxon's ratio, which uses a different definition than REAGENT_LIMIT, so the numbers are different.
		private static final boolean RUN_FORWARD_CHECK = true;

		public static class GroundedOperator {
			public RxnMolecule rxn;
			public Molecule product;
			public List<Molecule> reactants;
			public List<Molecule> groundedReactants;
			public String idString;
			public boolean forwardized;
			public List<GroundedOperator> reagentOps;
			public GroundedOperator(String idString, Operator creatingOp, State state,
					List<State> kidStates) {
				this.idString = idString;
				if(creatingOp == null) {
					rxn = null;
				} else {
					rxn = creatingOp.rxn;
				}
				product = state.getMolecule();
				reactants = new ArrayList<Molecule>();
				for(State s : kidStates) {
					reactants.add(s.getMolecule());
				}
				forwardized=false;
			}
			public GroundedOperator(String idString, Operator creatingOp, Molecule state,
					List<Molecule> kidStates, List<GroundedOperator> reagentOps) {
				this.idString = idString;
				if(creatingOp == null) {
					rxn = null;
				} else {
					rxn = creatingOp.rxn;
				}
				product = state;
				if(kidStates != null) {
					reactants = new ArrayList<Molecule>();
					reactants.addAll(kidStates);
					groundedReactants = new ArrayList<Molecule>();
					groundedReactants.addAll(kidStates);
					this.reagentOps = reagentOps;
				}
				forwardized=true;
			}
		}

		public RxnMolecule rxn;
		public Operator(RxnMolecule rxn2) {
			rxn = rxn2;
		}
		public void dump() {
			System.err.println("toString:"+rxn.toString());
			System.err.println("comment:"+rxn.getComment());
			for(String s : rxn.properties().getKeys())
				System.err.println("properties: "+s+"="+rxn.properties().get(s));
			System.err.println("Name:"+rxn.getName());
		}
		public String toString() {
			if(rxn == null) {
				return "NULL_rxn";
			} 
			if(rxn.getName() != null && rxn.getName() != "") {
				return rxn.getName();
			}
			MPropertyContainer msp = rxn.properties();
			if (msp == null) {
				return "NULL_noprop";
			}
			MStringProp msp_name = (MStringProp)rxn.properties().get("NAME");
			if (msp_name == null) {
				return "NULL_nonameprop";
			}
			String name = msp_name.stringValue();
			if (name == null) {
				return "NULL_name";
			} else {
				return name;
			}
		}


		private static Collection<Molecule> forwardApplication(RxnMolecule rxn, List<Molecule> requiredReactants, int[] ratio) throws Exception {
			Collection<Molecule> generatedProducts = new ArrayList<Molecule>();

			// run reaction at current ratio
			if(DEBUG_FORWARDIZATION) {
				System.err.println("******** about to run forward:"+Arrays.toString(ratio));

				System.err.print("******** :");
				for(int x: ratio){
					System.err.print(x+" ");	
				}
				System.err.println();

				System.err.print("******** About to forward apply: ./run_reactor.sh ");

				for(Molecule m : requiredReactants) {
					System.err.print("'"+m.toFormat("smarts")+"' ");
				}
				System.err.println(" -r '"+rxn.toFormat("smarts")+"' ");

				System.err.print("******* pre generatedProducts.size="+generatedProducts.size());	
			}
			forwardApplicationInner(requiredReactants, generatedProducts, rxn, ratio);
			if(DEBUG_FORWARDIZATION) {
				System.err.println("******* post generatedProducts.size="+generatedProducts.size());
			}

			return generatedProducts;
		}
		private static void forwardApplicationInner(
				List<Molecule> requiredReactants,
				Collection<Molecule> generatedProducts, RxnMolecule rxn,
				int[] ratio) throws ReactionException, SearchException {

			// create Reactor
			Reactor reactor = new Reactor();
			reactor.setReaction(rxn);
			if(ratio != null)
				reactor.setRatio(ratio);

			Molecule[] reactants = new Molecule[requiredReactants.size()];
			Molecule[] product  = null;
			int i=0;
			for(Molecule m : requiredReactants) {
				if(DEBUG_FORWARDIZATION) {
					System.err.println("******* reactant: "+m.toFormat("cxsmarts"));
				}
				reactants[i++] = m; 
			}
			reactor.setReactants(reactants); // because it's synthetic

			try {
				while ((product= reactor.react()) != null) {
					for (int k=0; k < product.length; ++k) {
						if(DEBUG_FORWARDIZATION) {
							System.err.println("******* pre molecularSetAdd generatedProducts.size="+generatedProducts.size());	
							System.err.println("******* adding "+product[k].toFormat("cxsmarts"));
						}
						MolecularUtils.molecularSetAdd(generatedProducts, product[k]);
						if(DEBUG_FORWARDIZATION) {
							System.err.println("******* post molecularSetAdd generatedProducts.size="+generatedProducts.size());	
						}
					}
				}
				//				if(!MolecularUtils.molecularContainsSomeChiralMatch2(generatedProducts,s.getMolecule())) {
				//					return null;
				//				}
			} catch (ReactionException re) {
			}
		}

		public Collection<Molecule> applyForward(List<Molecule> requiredReactants, int[] ratio) throws Exception {
			return forwardApplication(this.rxn, requiredReactants, ratio);
		}

		private boolean forwardCheck(Molecule mol, Collection<Molecule> requiredReactants, String indent, int[] ignored) throws Exception{
			if(!RUN_FORWARD_CHECK){
				return true;
			}

			// ACTUALLY WE DON'T HAVE TO TEST DIFFERENT RATIOS BECAUSE EACH STEP OF THE APPLYREACTION
			// APPLICATION ASSUMES WE'RE TRYING TO BUILD THE MOLECULE PASSED IN IN THAT ONE CALL.
			int[] ratio = new int[rxn.getReactantCount()];

			// run one additional totally even ratio
			for (int ii=0; ii<rxn.getReactantCount(); ii++) {
				ratio[ii] = 1;
			}
			// run reaction at current ratio
			boolean b = runForward(mol, requiredReactants, indent, ratio);
			if (b) {
				// If we succeeded in creating the product by running the reaction forward at some ratio,
				// then claim success.
				return true;
			} else {
				// If no ratio we try ended up making the product, well, we've failed.
				return false;
			}
		}
		private boolean runForward(Molecule mol, Collection<Molecule> requiredReactants, String indent, int[] ratio) throws SearchException,
		ReactionException {

			// TODO: it may be safe to cache the reactor after setup and just change reactants/product with each application.
			// create Reactor
			Reactor reactor = new Reactor();
			reactor.setReaction(rxn);

			Molecule[] reactants = new Molecule[requiredReactants.size()];
			Molecule[] product  = null;
			int i=0;
			for(Molecule m : requiredReactants) {
				reactants[i++] = m;				
			}
			reactor.setReactants(reactants); // because it's RETROsynthetic
			reactor.setRatio(ratio);
			if (DEBUG) {
				System.err.println(indent+"ForwardReaction:"+toString());
				System.err.println(indent+"Forward ~/bin/view.sh \'"+rxn.toFormat("cxsmarts")+"\'");
				System.err.println(indent+"ForwardrxnoString:"+rxn.toString()+" Name:"+rxn.getName()+" Comment:"+rxn.getComment());
				System.err.println(indent+"ForwardReactants:");
				for(Molecule m : reactants)
					System.err.println(indent+"Forward ~/bin/view.sh \'"+m.toFormat("cxsmarts")+"\'");
				System.err.println(indent+"ForwardExpectedProduct:");
				System.err.println(indent+"Forward ~/bin/view.sh \'"+mol.toFormat("cxsmarts")+"\'");
				System.err.print(indent+"ForwardRatio: [");
				for(int ii: ratio) {
					System.err.print((String.valueOf(ii))+" ");
				}
				System.err.println("]");

			}

			try {
				Collection<Molecule> generatedProducts = new ArrayList<Molecule>();
				if (DEBUG) {
					System.err.println(indent+"About to react, forwardly.");
				}
				while ((product= reactor.react()) != null) {
					if (DEBUG) {
						System.err.println(indent+"Forward Products:");
					}
					for (int k=0; k < product.length; ++k) {
						if (DEBUG) {
							System.err.println(indent+".."+"~/bin/view.sh \'"+product[k].toFormat("cxsmarts")+"\'");
						}
						generatedProducts.add(product[k]);
					}

				}
				if(!MolecularUtils.molecularContainsSomeChiralMatch(generatedProducts,mol)) {
					if (DEBUG) {
						System.err.println(indent+"Got " +generatedProducts.size()+ " products.  They don't include what you were looking for!");
						for(Molecule m : generatedProducts) {
							System.err.println(indent+MolecularUtils.molecularSatisfiedBySomeChiralForm(m, mol));
							System.err.println(indent+MolecularUtils.molecularSatisfiedBySomeChiralForm(mol, m));
							System.err.println(indent+m.toFormat("cxsmarts") + " vs. " + mol.toFormat("cxsmarts"));
						}
					}
					return false;
				}
			} catch (ReactionException re) {
				throw re;
			}
			return true;
		}


		/**
		 * Since a reaction core could match multiple locations in a molecule,
		 * applying a reaction retrosynthetically could yield multiple disconnections.
		 * Therefore, this returns a set of sets of States.
		 * @param s
		 * @param sml 
		 * @return
		 */
		public Collection<caos.aaai.Pair<Collection<State>, int[]>> apply(State s, StartingMaterialsLibrary sml, String indent) {
			Collection<caos.aaai.Pair<Collection<State>, int[]>> possibleDisconnections = new ArrayList<caos.aaai.Pair<Collection<State>, int[]>>();

			List<List<Molecule>> moleculeSets;
			MolecularMap<List<List<Molecule>>> reactionResultsCache = new MolecularUtils.MolecularMap<List<List<Molecule>>>(); 

			// Rather than taking the transitive closures to get reaction happening at multiple centers,
			// (which, for example, causes deep chains in the solution tree and throws off the branching factor)
			// the 5.7.0 version of Reactor permits specification of ratios that capture what happens
			// when you mix reagents in different ratios.  Now, since we're running reactions BACKWARDS, 
			// it doesn't help us directly.  So, just try a variety of excess reagent conditions, and
			// take the union of the generated precursors...
			//			// take the transitive closure by applying this reaction multiple times
			moleculeSets = applyReactionWithCaching(s.getMolecule(), sml, indent, null, reactionResultsCache);

			MolecularUtils.MolecularListMap<int[]> computedRatios = new MolecularUtils.MolecularListMap<int[]>();
			for(List<Molecule>lm : moleculeSets) {
				int[] ratioDefault = new int[rxn.getReactantCount()];
				for (int ii=0; ii<rxn.getReactantCount(); ii++) {
					ratioDefault[ii] = 1;
				}

				computedRatios.put(lm, ratioDefault);
			}



			// TODO: ChemAxon got back to me.  Apparently, in retrosynthetic direction the way to use ratio is
			// an int array with one element for the product.  so, [2] will run it twice.  
			// TODO: Experiment with replacing the below code by using ChemAxon's ratio.
			boolean fixpointFound;
			int iteration = 0;
			List<List<Molecule>> moleculeSetsToReact = new ArrayList<List<Molecule>>();
			moleculeSetsToReact.addAll(moleculeSets);
			List<List<Molecule>> moleculeSetsAlreadyReacted = new ArrayList<List<Molecule>>();
			String pre_indent = indent;
			indent = ",,"+indent;
			while(!moleculeSetsToReact.isEmpty()) {
				// Three lines to implement pop!  Grrr at Java...
				int lastIndex = moleculeSetsToReact.size()-1;
				List<Molecule> currentReagents = moleculeSetsToReact.get(lastIndex);
				moleculeSetsToReact.remove(lastIndex);

				// Don't try reacting this set of reagents anymore
				try {
					MolecularUtils.molecularSetsMergeOneIn(moleculeSetsAlreadyReacted, currentReagents);
				} catch (SearchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// And make sure that we return it as one of the possible precursor sets
				moleculeSets.add(currentReagents);

				for(int currentMoleculeIndex=0; currentMoleculeIndex<currentReagents.size(); currentMoleculeIndex++) {
					Molecule m = currentReagents.get(currentMoleculeIndex);
					List<List<Molecule>> transitiveMoleculeSets = applyReactionWithCaching(m, sml,indent+"--", null, reactionResultsCache);

					for(List<Molecule> oneMoleculesDecomposition : transitiveMoleculeSets) {

						// To really be the same reaction run at a different ratio, all the reagents (that aren't
						// the molecule we're trying to build) must be the same as the currentReagent list.
						if(DEBUG) {
							System.err.println(indent+"comparing two molecule lists.  The current reagents:");
							MolecularUtils.printMoleculeList(currentReagents);
							System.err.println(indent+"And the current decomposition:");
							MolecularUtils.printMoleculeList(oneMoleculesDecomposition);
						}
						boolean reallyRatio = true;
						for(int decompositionIndex=0; decompositionIndex<oneMoleculesDecomposition.size(); decompositionIndex++) {
							try {
								reallyRatio &= (decompositionIndex == currentMoleculeIndex) || 
								(MolecularUtils.molecularEquivalence(currentReagents.get(decompositionIndex), 
										oneMoleculesDecomposition.get(decompositionIndex)));
								if(!reallyRatio)
									break;
							} catch (SearchException e) {
								reallyRatio = false;
							}
						}
						if(!reallyRatio){
							if(DEBUG){
								System.err.println(indent+"Looks like we think the two molecule lists are different!");
							}
							continue;
						}

						int[] currentRatio = computedRatios.get(currentReagents);
						int[] computedRatio = new int[oneMoleculesDecomposition.size()];
						for(int ii=0; ii<computedRatio.length; ii++) {
							if (ii == currentMoleculeIndex) {
								computedRatio[ii] = 1;  // current molecule is the only one we're allowed to change, so this new molecule is well new.
							} else {
								computedRatio[ii] = currentRatio[ii]+1; // other molecules got used one more time than before.
							} 
						}

						List<Molecule> newReagentSet = new ArrayList<Molecule>();
						try {
							newReagentSet.addAll(oneMoleculesDecomposition);
							if (!MolecularUtils.molecularSetsContains(moleculeSetsAlreadyReacted, newReagentSet)) {
								MolecularUtils.molecularSetsMergeOneIn(moleculeSetsToReact, newReagentSet);
								computedRatios.put(newReagentSet, computedRatio);
							}
						} catch (SearchException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}


				// safety check to prevent infinite looping
				iteration++;
				if(iteration > EXCESS_REAGENT_LIMIT) {
					break;
				}
			}
			if(DEBUG) {
				System.err.println(indent+" ok.  Here's the ratios we came up with:");
				for(Pair<int[]> lp : computedRatios.items()) {
					MolecularUtils.printMoleculeList(lp.lm);
					System.err.println(indent+" at a ratio of: "+Arrays.toString(lp.v));
				}
			}
			indent = pre_indent;

			// It looks like we're getting multiple copies of reagents back, so uniquify the moleculeSets to remove duplicates
			List<List<Molecule>> uniquePrecursors = new ArrayList<List<Molecule>>();
			int iter = 0;
			for(List<Molecule> cm : moleculeSets) {
				iter++;
				try {
					int presize = uniquePrecursors.size();
					MolecularUtils.molecularSetsMergeOneIn(uniquePrecursors, cm);
				} catch (SearchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// convert sets of molecules into sets of states
			for(List<Molecule> cm : uniquePrecursors) {
				List<State> requiredReactantsStates = new ArrayList<State>();
				for(Molecule m : cm) {
					try {
						requiredReactantsStates.add(new State(m, sml.contains(m)));
					} catch (SearchException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw new Error(e);
					}
				}
				int[] ratio = computedRatios.get(cm);
				assert(ratio != null);
				possibleDisconnections.add(new caos.aaai.Pair<Collection<State>, int[]>(requiredReactantsStates, ratio));
			}
			return possibleDisconnections;
		}

		private List<List<Molecule>> applyReactionWithCaching(Molecule mol, StartingMaterialsLibrary sml, String indent, int[] ratio, MolecularUtils.MolecularMap<List<List<Molecule>>> map) {
			List<List<Molecule>> ccm;
			ccm = map.get(mol);
			if(ccm == null){
				ccm = applyReactionWithoutCaching(mol, sml, indent, ratio);
				map.put(mol, ccm);
			}
			return ccm;
		}

		private List<List<Molecule>> applyReactionWithoutCaching(Molecule mol, StartingMaterialsLibrary sml, String indent, int[] ratio) {
			try { 
				List<List<Molecule>> possibleDisconnections = new ArrayList<List<Molecule>>();

				// TODO: it may be safe to cache the reactor after setup and just change reactants/product with each application.
				// create Reactor
				Reactor reactor = new Reactor();
				// going retrosynthetic style
				reactor.setReverse(true);
				reactor.setReaction(rxn);

				Molecule[] reactants = null;
				Molecule[] product  = new Molecule[1];
				product[0] = mol;
				reactor.setReactants(product); // because it's RETROsynthetic
				if (DEBUG) {
					System.err.println(indent+"Reaction:"+toString());
					System.err.println(indent+"#reactants:"+rxn.getReactantCount()); //+" ratio.length:"+ratio.length+" ratio="+Arrays.toString(ratio));


					System.err.println(indent+"~/bin/view.sh \'"+rxn.toFormat("cxsmarts")+"\'");
					System.err.println(indent+"rxnoString:"+rxn.toString()+" Name:"+rxn.getName()+" Comment:"+rxn.getComment());
					System.err.println(indent+"Product:");
					System.err.println(indent+"~/bin/view.sh \'"+product[0].toFormat("cxsmarts")+"\'");
				}
				try {
					// TODO: if there exist selectivity rules, the products will be given in decreasing order
					// of selectivity.  Possibly it would be better just to take the major product, rather than
					// accept all of these...
					// See file ChemAxon/JChem/doc/user/Reactor.html
					while ((reactants= reactor.react()) != null) {
						List<Molecule> requiredReactants = new ArrayList<Molecule>();
						if (DEBUG) {
							System.err.println(indent+"Reactants ("+reactants.length+"):");
						}
						for (int k=0; k < reactants.length; ++k) {
							if (DEBUG) {
								System.err.println(indent+".."+"Reactant "+k+":");
								System.err.println(indent+".."+"Before cleaning:");
								System.err.println(indent+"...."+"~/bin/view.sh \'"+reactants[k].toFormat("cxsmarts")+"\'");
								System.err.println(indent+"...."+(sml.contains(reactants[k])?"Found in SML":"Not found in SML"));
							}
							MolecularUtils.cleanMolecule(reactants[k]);
							requiredReactants.add(reactants[k]);
							//							System.err.println(reactants[k].toFormat("cxsmarts"));
							if (DEBUG) {
								System.err.println(indent+".."+"After cleaning:");
								System.err.println(indent+"...."+"~/bin/view.sh \'"+reactants[k].toFormat("cxsmarts")+"\'");
								System.err.println(indent+"...."+(sml.contains(reactants[k])?"Found in SML":"Not found in SML"));
							}
						}
						if (DEBUG) {
							System.err.println(indent+(structuresSafe(requiredReactants, indent, sml)?"Reactants are structure safe":"Reactants are NOT structure safe"));
							System.err.println(indent+(forwardCheck(mol, requiredReactants, indent, ratio)?"Reactants pass forwardCheck":"Reactants FAIL forwardCheck"));
						}
						if (structuresSafe(requiredReactants, indent, sml) && forwardCheck(mol, requiredReactants, indent, ratio)) {
							//						if (forwardCheck(s, requiredReactants)) {

							possibleDisconnections.add(requiredReactants);	
						}
					}
				} catch (ReactionException re) {
					if (DEBUG) {
						System.err.print(re.getMessage());
						re.printStackTrace();
					}
					// Previously, we saw a problem w/ the reaction having query molecules and barfing
					// I can't tell how to fix that so, for now, test whether that's the case and swallow the exception

					boolean rxnHasQueryPieces = rxnHasQueryPieces(rxn);
				} catch (ArrayIndexOutOfBoundsException aioobe) {
					if (DEBUG) {
						System.err.print(indent+"LIKELY BUG IN CHEMAXON: "+aioobe.getMessage());
						aioobe.printStackTrace();
					}
				}
				return possibleDisconnections;
			} catch (Exception e){
				if (DEBUG) {
					System.err.println(indent+"Reaction:");
					System.err.println(indent+"~/bin/view.sh \'"+rxn.toFormat("cxsmarts")+"\'");
					System.err.println(indent+"rxnoString:"+rxn.toString()+" Name:"+rxn.getName()+" Comment:"+rxn.getComment());
					System.err.println(indent+"rxnIsQuery:"+rxn.isQuery()+" ");
					System.err.println(indent+"Agents:");
					for(int i=0; i<rxn.getAgentCount(); i++) {
						Molecule a = rxn.getAgent(i);
						System.err.println(indent+"  Class:"+a.getClass());
						System.err.println(indent+"  Reaction Component:"+a.canBeReactionComponent());
						System.err.println(indent+"  isQuery:"+a.isQuery());
						System.err.println(indent+"  ~/bin/view.sh \'"+a.toFormat("cxsmarts")+"\'");
					}
					System.err.println(indent+"Products:");
					for(int i=0; i<rxn.getProductCount(); i++) {
						Molecule a = rxn.getProduct(i);
						System.err.println(indent+"  Class:"+a.getClass());
						System.err.println(indent+"  Reaction Component:"+a.canBeReactionComponent());
						System.err.println(indent+"  isQuery:"+a.isQuery());
						System.err.println(indent+"  ~/bin/view.sh \'"+a.toFormat("cxsmarts")+"\'");
					}
					System.err.println(indent+"Reactants:");
					for(int i=0; i<rxn.getReactantCount(); i++) {
						Molecule a = rxn.getReactant(i);
						System.err.println(indent+"  Class:"+a.getClass());
						System.err.println(indent+"  Reaction Component:"+a.canBeReactionComponent());
						System.err.println(indent+"  isQuery:"+a.isQuery());
						System.err.println(indent+"  ~/bin/view.sh \'"+a.toFormat("cxsmarts")+"\'");
					}
					System.err.println(indent+"Class:"+mol.getClass());
					System.err.println(indent+"Reaction Component:"+mol.canBeReactionComponent());
					System.err.println(indent+"isQuery:"+mol.isQuery());
					System.err.println(indent+"Product:");
					System.err.println(indent+"~/bin/view.sh \'"+mol.toFormat("cxsmarts")+"\'");
					System.err.println(indent+"Simplified Product:");
					System.err.println(indent+"~/bin/view.sh \'"+mol.getSimplifiedMolecule().toFormat("cxsmarts")+"\'");
					System.err.println(indent+"Sgroups:");
					for(Sgroup sgrp : mol.getSgroupArray()) {
						System.err.println(indent+"~/bin/view.sh \'"+sgrp.createMolecule().toFormat("cxsmarts")+"\'");
					}
					System.err.println(indent+"MolAtom:");
					for(MolAtom ma: mol.getAtomArray()) {
						System.err.println(indent+"AtNo:"+ma.getAtno()+" AtSym:"+ma.getSymbol());
						System.err.println(indent+"IsQuery:"+ma.isQuery()+" Has QProps:"+ma.hasQProps()+" QProps:");
						for(String pname: ma.getQPropNames()) {
							System.err.println(indent+pname);	
						}
						System.err.println(indent+"QueryLabel:"+ma.getQueryLabel()+" ");
						System.err.println(indent+"QueryStr:"+ma.getQuerystr()+" ");
					}
					System.err.println(indent+"MolBond:");
					for(MolBond ma: mol.getBondArray()) {
						System.err.println(indent+"IsQuery:"+ma.isQuery());
						System.err.println(indent+"QueryStr:"+ma.getQuerystr()+" ");
						System.err.println(indent+"Is QueryBond:"+(ma instanceof QueryBond)+" ");
						if(ma instanceof QueryBond) 
							System.err.println(indent+"QueryBondQueryStr:"+((QueryBond)ma).getQuerystr()+" ");
						System.err.println(indent+"Class:"+ma.getClass()+" ");				
					}					
					RgMolecule rgm = (RgMolecule)mol.getParent();
					if(rgm != null) {
						System.err.println(indent+"Rgroup count:"+rgm.getRgroupCount());
						System.err.println(indent+"Root molecule:"+rgm.getRoot().toFormat("cxsmarts"));
						for(int i=0; i<rgm.getRgroupCount(); i++) {
							System.err.println(indent+"Rgroup "+i+":");
							for(int j=0; j<rgm.getRgroupMemberCount(i); j++){
								System.err.println(indent+"Rgroup member"+j+":");
								Molecule rgm2 = rgm.getRgroupMember(i, j);
								System.err.println(indent+"~/bin/view.sh \'"+rgm2.toFormat("cxsmarts")+"\'");

							}
						}
					}
				}
				throw new Error(e);
			}
		}
		private boolean structuresSafe(Collection<Molecule> requiredReactants, String indent, StartingMaterialsLibrary sml) {
			for(Molecule m : requiredReactants) {
				boolean b;
				try {
					b = sml.contains(m);
				} catch (SearchException e) {
					b = false;
				}
				if (!b && !structureSafe(m, indent)) 
					return false;
			}
			return true;
		}
		private boolean structureSafe(Molecule m, String indent) {
			// TODO: add more checks than this valence thing.

			m.valenceCheck();
			m.checkConsistency();
			for (MolAtom a :m.getAtomArray()) {
				if(a.hasValenceError()) {
					return false;
				}
			}
			if (m.hasValenceError()) {
				return false;
			}

			// Add calls to StructureCheckers for aromaticity? explicit hydrogen? molecular charge? ring strain? wedge error?
			// see http://www.chemaxon.com/marvin/help/structurechecker/checker.html
			StructureChecker sc = new ValenceErrorChecker();
			if (sc.check(m) != null) {
				return false;
			}
			StructureChecker rsec = new RingStrainErrorChecker();
			if (rsec.check(m) != null) {
				return false;
			}

			return true;
		}
	}

	public OperatorLibrary(String libraryPath) throws Exception {
		library = loadReactionLibrary(libraryPath);
	}

	private static RxnMolecule loadReaction(String fn) throws Exception {
		// read reaction
		MolImporter importer = new MolImporter(fn);
		Molecule rmol;
		RxnMolecule rxn = null;
		while((rmol = importer.read()) != null) {
			rxn = (RxnMolecule)rmol;
			ArrayList<RxnMolecule> steps = rxn.getReactionSteps();
			if(steps != null && steps.size() > 0) {
				System.err.println("Come implement multi-step reactions.");
				throw new Exception("Come implement multi-step reactions.");
			}
		}
		importer.close();
		rxn.toString();
		return rxn;
	}

	public List<Operator> loadReactionLibrary(String libraryPath) throws Exception {
		ArrayList<Operator> library = new ArrayList<Operator>();
		int pruned_count = 0;
		System.err.println(libraryPath);
		java.io.File dir = new java.io.File(libraryPath);
		String[] contents = dir.list();
		int i=0;
		if (contents == null) {
			// We're pointing at a missing directory.  I do this sometimes to run short tests, so don't die on it.
			System.err.println("The reaction library "+libraryPath+" seems to be missing!");
			return library;
		}
		for (String file : contents) {
			System.err.println(file);          
			if (file.endsWith(".mrv") || file.endsWith(".smarts") || file.endsWith(".smiles") || file.endsWith(".smi")) {
				System.err.println("Loading rxn "+(i++));
				RxnMolecule rxn = loadReaction(libraryPath+"/"+file);
				if (rxn.getName() == null || rxn.getName() == "") {
					rxn.setName(file);
				}

				Operator o = new Operator(rxn);
				o.dump();
				library.add(o);
			}
		}
		return library;
	}

	public int size() {
		return library.size();
	}

	public void add(Operator o) {
		library.add(o);
	}

	public Iterator<Operator> iterator() {
		return library.iterator();
	}

	private static boolean rxnHasQueryPieces(RxnMolecule rxn) {
		// Test if the *reaction* is the query or if it's the reagent
		boolean rxnHasQueryPieces = rxn.isQuery();
		for(int i=0; i<rxn.getAgentCount(); i++) {
			Molecule a = rxn.getAgent(i);
			rxnHasQueryPieces |= a.isQuery();
		}
		for(int i=0; i<rxn.getProductCount(); i++) {
			Molecule a = rxn.getProduct(i);
			rxnHasQueryPieces |= a.isQuery();
		}
		for(int i=0; i<rxn.getReactantCount(); i++) {
			Molecule a = rxn.getReactant(i);
			rxnHasQueryPieces |= a.isQuery();
		}
		return rxnHasQueryPieces;
	}


	// TODO: This functionality should be updated once PNNode.collectMinProofTree ACTUALLY returns a tree.
	public static void forwardizeAnswer(Collection<GroundedOperator> c,
			StartingMaterialsLibrary originalSML) throws Exception {
		StartingMaterialsLibrary mySML = new StartingMaterialsLibrary(originalSML);
		boolean allOpsforwardized;
		int stillNeedForwardizing = Integer.MAX_VALUE;
		int lastForwardizeCount;
		do {
			lastForwardizeCount = stillNeedForwardizing;

			System.err.println("... still forwardizing.");
			for(GroundedOperator go : c) {
				if (!go.forwardized) {
					// Try to forwardize it.
					// Java is needlessly verbose...  Would it kill them to have lambdas and list comprehensions?
					if(go.rxn == null) { 
						Molecule groundedMolecule = mySML.getMolecularMatch(go.product);
						if(groundedMolecule != null)
							go.product = groundedMolecule;
						if (go.product == null) 
							go.product = mySML.getPlaceholder();
						go.forwardized = true;
					} else {

						boolean allReactantsPresent = true;
						for(Molecule m : go.reactants) {
							allReactantsPresent &= mySML.contains(m);
						}
						if (allReactantsPresent) {
							List<Molecule> requiredReactants = new ArrayList<Molecule>();
							go.groundedReactants = new ArrayList<Molecule>();
							for(Molecule m : go.reactants) {
								Molecule groundedMolecule = mySML.getMolecularMatch(m); 
								requiredReactants.add(groundedMolecule);
								go.groundedReactants.add(groundedMolecule);
							}
							Collection<Molecule> products = Operator.forwardApplication(go.rxn, requiredReactants, null);
							for(Molecule m : products) {
								mySML.add(m);
								go.product = m;
							}
							if (products.size() != 1) {
								System.err.println("Uh oh!  Produced "+products.size()+" products for "+go.toString()+" "+go.rxn.getName()+" "+go.rxn.toFormat("cxsmarts"));
								System.err.println("They are:");
								for(Molecule m : products) {
									System.err.println("  "+m.toFormat("cxsmarts"));
								}
							}
							go.forwardized = true;
						}
					}
				}
			}			

			stillNeedForwardizing = 0;
			for(GroundedOperator go : c) {
				if(!go.forwardized) {
					stillNeedForwardizing++;
				}
			}
		}
		while (stillNeedForwardizing < lastForwardizeCount);

		// OK, we hit a fixpoint.  But that doesn't mean that we succeeded in forwardizing everything.
		// If we stopped the search (e.g. hit the expansion bound) before it found a solution,
		// then we'll get to this point, where there exist molecules that are neither starting
		// materials nor have all of their Reactants synthesized.  
		//
		// To avoid an infinite loop in this case, we pretend to forwardize them.  Because the 
		// display needs a go.product set, we set it to whatever we have lying around.
		for(GroundedOperator go : c) {
			if (!go.forwardized) {
				Molecule groundedMolecule = mySML.getMolecularMatch(go.product);
				if (groundedMolecule != null)
					go.product = groundedMolecule;
				if (go.product == null) 
					go.product = mySML.getPlaceholder();

				if(go.groundedReactants == null)
					go.groundedReactants = go.reactants;

				go.forwardized = true;
			}
		}		
	}

	public static void printAnswer(Molecule goal, Collection<GroundedOperator> c) {
		List<String> steps = new ArrayList<String>();
		steps.add(goal.toFormat("smarts")+" is the goal");

		for(GroundedOperator o: c){
			if(DEBUG){
			System.err.println("ZZZZ:"+o.idString);
			}
			if (o.groundedReactants != null) {
				List<String> reactantStrings = new ArrayList<String>();
				for(Molecule m:o.groundedReactants) {
					reactantStrings.add(m.toFormat("smarts"));
				}

				Collections.sort(reactantStrings);
				String reactants = join(reactantStrings, ".");
				String name = o.rxn.getName();
				String product = o.product.toFormat("smarts");

				steps.add(reactants+">>"+product+" via "+name);
			} else {
				if(DEBUG){
				System.err.println("reactants is null");
				}
				if(o.product != null) {
					String product = o.product.toFormat("smarts");
					steps.add(product+" is a starting material");
				} else {
					if(DEBUG){
						System.err.println("product is null");
					}
				}
			}

		}
		Collections.sort(steps);
		for(String s : steps) {
			System.out.println(s);
		}
	}

	static private String join(List<String> list, String conjunction)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String item : list)
		{
			if (first)
				first = false;
			else
				sb.append(conjunction);
			sb.append(item);
		}
		return sb.toString();
	}

}
