package test;
import java.util.ArrayList;

import chemaxon.reaction.*;
import chemaxon.formats.*;
import chemaxon.struc.*;

public class ReactionReader {
  static public void printRxn(RxnMolecule rxn, String fn) throws Exception {
    System.err.println("Reaction form:");
    System.out.println(rxn.toFormat("smarts"));
    System.err.println();
    MolExporter me = new MolExporter(fn, "mrv", MolExporter.TEXT, null, null);
    me.write(rxn);
    me.flush();
    me.close(MolExporter.C_CLOSE_STREAM);
  }

  static public void splitRxn(String fn) throws Exception {
    // read reaction
    MolImporter importer = new MolImporter(fn);
    Molecule rmol;
    while((rmol = importer.read()) != null) {
      RxnMolecule rxn = (RxnMolecule)rmol;
      ArrayList steps = rxn.getReactionSteps();
      if(steps != null && steps.size() > 0) {
        System.err.println("Come implement multi-step reactions.");
        throw new Exception();
      }
      int i=0;
      int prodCount = rxn.getProductCount();          
      if (prodCount == 1) {
        System.err.println("Original rxn:");
        printRxn(rxn, fn.substring(0,fn.length()-4)+"_"+i+".mrv");
      }
      else {
        for (i=0; i<prodCount; i++) {
          RxnMolecule rxn2 = (RxnMolecule)rmol.cloneMolecule();          
          //            Molecule foo = rxn2.getProduct(i);
          for(int j=0; j<prodCount; j++) {
            if (i!=j) 
              rxn2.removeComponent(RxnMolecule.PRODUCTS, j);
          }
          printRxn(rxn2, fn.substring(0,fn.length()-4)+"_"+i+".mrv");
        }
      }
    }
    importer.close();
  }

    static public void main(String[] args) throws Exception {
	if (args.length < 1) {
	    System.err.println("Usage: ");
	    System.err.println("  java ReactorDemo <reaction> <reactant1> <reactant2>");
	    return;
	}
        java.io.File dir = new java.io.File(args[0]);
        String[] contents = dir.list();
        if (contents != null) {
            for (String file : contents) {
                // System.err.println(file);          
                if (file.endsWith(".mrv")) {
                  System.err.println(file);
                  splitRxn(args[0]+"/"+file);
                }
              }
        } else {
            if (args[0].endsWith(".mrv")) {
                System.err.println(args[0]);
                splitRxn(args[0]);
              }

        }
	// // main part
	// // process reaction in combinatorial mode

	// // create Reactor
	// Reactor reactor = new Reactor();
        // reactor.setReverse(true);

	// // set reaction, 
	// // using rules in tags if rules are not specified
	// //reactor.setReaction(rmol, reactivity);

	// // now rules are included in reaction definition
	// reactor.setReaction(rmol);

        // Molecule[] reactants = new Molecule[2];
	// // Molecule[] reactants = new Molecule[1];
	// Molecule[] products = null;
        // System.err.println("r1:"+reactants1.length+" r2:"+reactants2.length);
        // reactants[0] = reactants1[0];
        // reactants[1] = reactants1[1];
        // reactor.setReactants(reactants);
        // System.err.println("Reactants:");
        // System.err.println(reactants[0].toFormat("smiles"));
        // System.err.println(reactants[1].toFormat("smiles"));
        // // process reaction
        // while ((products = reactor.react()) != null) {
        //   System.err.println("Products:");
        //   for (int k=0; k < products.length; ++k) {
        //     System.err.println(products[k].toFormat("smiles"));
        //   }
        //   RxnMolecule result = reactor.createReaction(products);
        //   System.err.println("Reaction form:");
        //   System.out.println(result.toFormat("smiles"));
        //   System.err.println();
        // }
        // System.err.println("done!");

	// // for (int i=0; i < reactants1.length; ++i) {

	// //     // the first reactant
	// //     reactants[0] = reactants1[i];
	// //     for (int j=0; j < reactants2.length; ++j) {
        // //       System.err.println("");

	// // 	// the second reactant
	// // 	reactants[1] = reactants2[j];

	// // 	// set reactants
	// // 	reactor.setReactants(reactants);

	// // 	// process reaction
	// // 	while ((products = reactor.react()) != null) {
	// // 	    System.err.println("Reactants:");
	// // 	    System.err.println(reactants[0].toFormat("smiles"));
	// // 	    System.err.println(reactants[1].toFormat("smiles"));
	// // 	    System.err.println("Products:");
	// // 	    for (int k=0; k < products.length; ++k) {
	// // 		System.err.println(products[k].toFormat("smiles"));
	// // 	    }
	// // 	    RxnMolecule result = reactor.createReaction(products);
	// // 	    System.err.println("Reaction form:");
	// // 	    System.out.println(result.toFormat("smiles"));
	// // 	    System.err.println();
	// // 	}
	// //     }
	// // }
    }
}
