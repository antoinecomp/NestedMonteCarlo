package test;
import java.util.ArrayList;

import chemaxon.reaction.*;
import chemaxon.formats.*;
import chemaxon.struc.*;

public class ReactorDemo2 {
	//	static public void main(String[] args) throws Exception {
	//		if (args.length < 3) {
	//			System.err.println("Usage: ");
	//			System.err.println("  java ReactorDemo <reaction> <reactant1> <reactant2>");
	//			return;
	//		}
	//
	//		// read reaction
	//		MolImporter importer = new MolImporter(args[0]);
	//		Molecule rmol = importer.read();
	//		importer.close();
	//
	//		// read reactants
	//		Molecule mol = null;
	//		MolImporter importer1 = new MolImporter(args[1]);
	//		ArrayList r1 = new ArrayList();
	//		while ((mol = importer1.read()) != null) {
	//			r1.add(mol);
	//		}
	//		importer1.close();
	//		Molecule[] reactants1 = new Molecule[r1.size()];
	//		r1.toArray(reactants1);
	//		MolImporter importer2 = new MolImporter(args[2]);
	//		ArrayList r2 = new ArrayList();
	//		while ((mol = importer2.read()) != null) {
	//			r2.add(mol);
	//		}
	//		importer2.close();
	//		Molecule[] reactants2 = new Molecule[r2.size()];
	//		r2.toArray(reactants2);
	//
	//		System.err.println(r1.get(0).equals(r1.get(1)));
	//		// rules - now not needed because included in reaction definition
	//		//String reactivity = "!match(ratom(3), \"[#6][N,O,S:1][N,O,S:2]\", 1) && !match(ratom(3), \"[N,O,S:1][C,P,S]=[N,O,S]\", 1)";
	//
	//
	//		// main part
	//		// process reaction in combinatorial mode
	//
	//		// create Reactor
	//		Reactor reactor = new Reactor();
	//
	//		// set reaction, 
	//		// using rules in tags if rules are not specified
	//		//reactor.setReaction(rmol, reactivity);
	//
	//		// now rules are included in reaction definition
	//		reactor.setReaction(rmol);
	//
	//		Molecule[] reactants = new Molecule[2];
	//		Molecule[] products = null;
	//		for (int i=0; i < reactants1.length; ++i) {
	//
	//			// the first reactant
	//			reactants[0] = reactants1[i];
	//			for (int j=0; j < reactants2.length; ++j) {
	//
	//				// the second reactant
	//				reactants[1] = reactants2[j];
	//
	//				// set reactants
	//				reactor.setReactants(reactants);
	//
	//				// process reaction
	//				while ((products = reactor.react()) != null) {
	//					System.err.println("Reactants:");
	//					System.err.println(reactants[0].toFormat("smiles"));
	//					System.err.println(reactants[1].toFormat("smiles"));
	//					System.err.println("Products:");
	//					for (int k=0; k < products.length; ++k) {
	//						System.err.println(products[k].toFormat("smiles"));
	//					}
	//					RxnMolecule result = reactor.createReaction(products);
	//					System.err.println("Reaction form:");
	//					System.out.println(result.toFormat("smiles"));
	//					System.err.println();
	//				}
	//			}
	//		}
	//	}
	static public void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.err.println("Usage: ");
			System.err.println("  java ReactorDemo <reaction> <reactant1> <reactant2>");
			return;
		}

		// read rxn
		Molecule rxn = MolImporter.importMol(args[0]);

		// read mol 1
		Molecule a = MolImporter.importMol(args[1]);

		// read mol 2
		Molecule b = MolImporter.importMol(args[2]);

		int[] ratio = {1,2};

		// create Reactor
		Reactor reactor = new Reactor();

		// extract main product
		//reactor.setProductIndexes(new int[] {1});

		// ignore rule
		//reactor.setIgnoreRules(Reactor.IGNORE_REACTIVITY);

		// set reaction, 
		// using rules in tags if rules are not specified explicitly
		reactor.setReaction(rxn);
		reactor.setRatio(ratio);
		
		// check reactant count
		if (reactor.getReactantCount() != 2) {
			System.err.println("Demo works only for 2-reactant reactions.");
			return;
		}

		
		Molecule[] reactants = new Molecule[2];
		reactants[0] = a;
		reactants[1] = b;
		Molecule[] products = null;
		// set reactants
		reactor.setReactants(reactants);

		// process the reaction
		while ((products = reactor.react()) != null) {
			for (int k=0; k < products.length; ++k) {
				System.out.println(products[k].toFormat("cxsmarts"));
			}
		}

	}


}
