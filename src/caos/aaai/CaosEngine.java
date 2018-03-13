package caos.aaai;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


import caos.aaai.OperatorLibrary.Operator.GroundedOperator;
import caos.aaai.PNS.ProofNumberSearch;
import caos.aaai.PNS.ExhaustiveSearch;
import caos.aaai.astar.Heuristic;
import chemaxon.reaction.*;
import chemaxon.formats.*;
import chemaxon.struc.*;

public class CaosEngine{

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception {
		if (args.length < 4) {
			System.err.println("operatorlibrary <path to rxn library> <path to starting material directory> <path to goal molecule> bound draw [path to supplemental rxn library] [path to supplemental starting material directory] [exhaustivesearch]");
			return;
		}
		System.err.println("Bound:"+args[3]+".");
		System.err.println("Draw:"+args[4]+".");
		int bound = (new Integer(args[3])).intValue();
		int draw = (new Integer(args[4])).intValue(); // 0 for no drawing, 1 for solution, 2 for dumping search space
		boolean exhaustiveSearch = args.length == 8;
			
		// Load starting material database
		StartingMaterialsLibrary sml = new StartingMaterialsLibrary(args[1]);
		System.err.println("Starting Material Library size:"+sml.size());
		if (args.length > 3) {
			StartingMaterialsLibrary sml2 = new StartingMaterialsLibrary(args[6]);
			System.err.println("Supplemental starting material library at "+args[6]+" size:"+sml2.size());
			for(Molecule m : sml2) {
				sml.add(m);
			}
			System.err.println("Final Starting Material Library size:"+sml.size());
		}

		// Load reaction library
		OperatorLibrary ol = new OperatorLibrary(args[0]);
		System.err.println("Operator Library size:"+ol.size());
		if (args.length > 4) {
			OperatorLibrary ol2 = new OperatorLibrary(args[5]);
			System.err.println("Supplemental operator library size:"+ol2.size());
			for(OperatorLibrary.Operator o : ol) {
				ol2.add(o);
			}
			ol = ol2;
			System.err.println("Final Operator Library size:"+ol.size());
		}

		// Load goal
		Molecule goal = null;
		Molecule mol;
		MolImporter importer1 = new MolImporter(args[2]);
		ArrayList<Molecule> r1 = new ArrayList<Molecule>();
		while ((mol = importer1.read()) != null) {
			r1.add(mol);
		}
		importer1.close();
		System.err.println("Goal:");
		for(Molecule m: r1) {
			System.err.println(m.toFormat("smarts"));
		}
		if (r1.size() > 1) {
			throw new Error("Why do you have multiple goal molecules?");
		}
		goal = r1.get(0);

		// Setup search
		State initialState = new State(goal, sml.contains(goal));
		SearchInterface search;
		if(!exhaustiveSearch) {
			System.err.println("Proofing.");
			search = new ProofNumberSearch(); 
		} else {
			System.err.println("Exhausting.");
			search = new ExhaustiveSearch();
		}
		 
		search.initializeSearch(ol, sml, bound);
		search.search(initialState);
		System.err.println("oik3 Done searching.");
		Collection<GroundedOperator> c = search.getAnswer();
		try {
			if(draw > 0) {
				OperatorLibrary.forwardizeAnswer(c, sml);
				System.err.println("Drawing collection.");
				OperatorLibrary.drawCollection(c);
			}
		} catch (InternalError e) {
		}
		if(draw > 1) {
			System.err.println();
			System.err.println("Dumping search space.");
			try {
				search.dumpSearchSpace();
			} catch (InternalError e) {
			}
		}
		System.err.println("Done.");

		System.err.println();
		System.out.println("We "+(search.getSuccess()?"synthesized!":"failed to synthezise!"));
		OperatorLibrary.printAnswer(goal, c);
	}

}
