//package test;
//import java.util.ArrayList;
//
//import chemaxon.reaction.*;
//import chemaxon.formats.*;
//import chemaxon.sss.search.MolSearchOptions;
//import chemaxon.sss.search.StandardizedMolSearch;
//import chemaxon.struc.*;
//
//public class RetroTests {
//    static public void main(String[] args) throws Exception {
//	if (args.length < 3) {
//	    System.err.println("Usage: ");
//	    System.err.println("  java ReactorDemo <reaction> <reactant1> <reactant2>");
//	    return;
//	}
//
//	// read reaction
//	MolImporter importer = new MolImporter(args[0]);
//	Molecule rmol = importer.read();
//	importer.close();
//
//	// read reactants
//	Molecule mol = null;
//	MolImporter importer1 = new MolImporter(args[1]);
//	ArrayList r1 = new ArrayList();
//	while ((mol = importer1.read()) != null) {
//	    r1.add(mol);
//	}
//	importer1.close();
//	Molecule[] reactants1 = new Molecule[r1.size()];
//	r1.toArray(reactants1);
//	
//	MolImporter importer2 = new MolImporter(args[2]);
//	ArrayList r2 = new ArrayList();
//	while ((mol = importer2.read()) != null) {
//	    r2.add(mol);
//	}
//	importer2.close();
//	Molecule[] reactants2 = new Molecule[r2.size()];
//	r2.toArray(reactants2);
//
//
//    System.err.println(reactants1[0].toFormat("smiles"));
//    System.err.println(reactants2[0].toFormat("smiles"));
//	System.err.println(reactants1[0].equals(reactants1[0]));
//	System.err.println(reactants1[0].equals(reactants2[0]));
//	
//	
//	StandardizedMolSearch sms = new StandardizedMolSearch();
//	MolSearchOptions options = new MolSearchOptions();
//	options.setSearchType(MolSearchOptions.DUPLICATE);
//	sms.setSearchOptions(options);
//	sms.setQuery(reactants1[0]);
//	sms.setTarget(reactants2[0]);
//	System.err.println(sms.isMatching());
//	
//	// rules - now not needed because included in reaction definition
//	//String reactivity = "!match(ratom(3), \"[#6][N,O,S:1][N,O,S:2]\", 1) && !match(ratom(3), \"[N,O,S:1][C,P,S]=[N,O,S]\", 1)";
//	
//
//	// main part
//	// process reaction in combinatorial mode
//
//	// create Reactor
//	Reactor reactor = new Reactor();
//        reactor.setReverse(true);
//
//	// set reaction, 
//	// using rules in tags if rules are not specified
//	//reactor.setReaction(rmol, reactivity);
//
//	// now rules are included in reaction definition
//	reactor.setReaction(rmol);
//
//    //    Molecule[] reactants = new Molecule[2];
//	 Molecule[] reactants = new Molecule[1];
//	Molecule[] products = null;
//        System.err.println("r1:"+reactants1.length+" r2:"+reactants2.length);
//        reactants[0] = reactants1[0];
//        //reactants[1] = reactants1[1];
//        reactor.setReactants(reactants);
//        System.err.println("Reactants:");
//        System.err.println(reactants[0].toFormat("smiles"));
//        //System.err.println(reactants[1].toFormat("smiles"));
//        // process reaction
//        while ((products = reactor.react()) != null) {
//          System.err.println("Products:");
//          for (int k=0; k < products.length; ++k) {
//            System.err.println(products[k].toFormat("smiles"));
//          }
//          RxnMolecule result = reactor.createReaction(products);
//          System.err.println("Reaction form:");
//          System.out.println(result.toFormat("smiles"));
//          System.err.println();
//        }
//        System.err.println("done!");
//
//	// for (int i=0; i < reactants1.length; ++i) {
//
//	//     // the first reactant
//	//     reactants[0] = reactants1[i];
//	//     for (int j=0; j < reactants2.length; ++j) {
//        //       System.err.println("");
//
//	// 	// the second reactant
//	// 	reactants[1] = reactants2[j];
//
//	// 	// set reactants
//	// 	reactor.setReactants(reactants);
//
//	// 	// process reaction
//	// 	while ((products = reactor.react()) != null) {
//	// 	    System.err.println("Reactants:");
//	// 	    System.err.println(reactants[0].toFormat("smiles"));
//	// 	    System.err.println(reactants[1].toFormat("smiles"));
//	// 	    System.err.println("Products:");
//	// 	    for (int k=0; k < products.length; ++k) {
//	// 		System.err.println(products[k].toFormat("smiles"));
//	// 	    }
//	// 	    RxnMolecule result = reactor.createReaction(products);
//	// 	    System.err.println("Reaction form:");
//	// 	    System.out.println(result.toFormat("smiles"));
//	// 	    System.err.println();
//	// 	}
//	//     }
//	// }
//    }
//}
