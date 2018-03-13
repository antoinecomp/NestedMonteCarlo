package caos.aaai;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import test.ViewJTable;

import chemaxon.reaction.*;
import chemaxon.checkers.StructureChecker;
import chemaxon.checkers.ValenceErrorChecker;
import chemaxon.formats.*;
import chemaxon.sss.search.MolSearchOptions;
import chemaxon.sss.search.SearchException;
import chemaxon.sss.search.StandardizedMolSearch;
import chemaxon.struc.*;
import chemaxon.struc.prop.MStringProp;

public class StartingMaterialsLibrary implements Iterable<Molecule> {
	private Set<Molecule> library;
	private Molecule placeholder;

	public static void drawCollection(Collection<Molecule> c) throws Exception {
		final Object[][] mol_array = new Object[c.size()][3];
		int i=0;
		for(Molecule m : c ) {
			mol_array[i][0] = m.toFormat("cxsmarts");
			mol_array[i][1] = null;
			mol_array[i][2] = m;
			System.err.println("   ~/bin/view.sh \'"+m.toFormat("cxsmarts")+"\'");
			i++;
		}

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ViewJTable.createAndShowGUI(mol_array);
            }
        });
	}

	public StartingMaterialsLibrary(String libraryPath) throws Exception {
		library = loadStartingMaterialsLibrary(libraryPath);
		placeholder = MolImporter.importMol("[Ne]");
	}
	public StartingMaterialsLibrary(StartingMaterialsLibrary lib) throws Exception {
		library = new HashSet<Molecule>();
		library.addAll(lib.library);
		placeholder = lib.placeholder;
	}

	private static Molecule loadMolecule(String fn) throws Exception {
		// read reaction
		System.err.println(fn);
		MolImporter importer = new MolImporter(fn);
		Molecule result = null;
		Molecule mol;
		boolean previousMolecule = false;
		while((mol = importer.read()) != null) {
			result = mol;
			if(previousMolecule) {
				throw new Exception("Multiple molecules in "+fn+"?");
			}
			previousMolecule = true;
		}
		importer.close();
		result.toString();
		return result;
	}

	public Set<Molecule> loadStartingMaterialsLibrary(String libraryPath) throws Exception {
		Set<Molecule> library = new HashSet<Molecule>();
		int pruned_count = 0;
		System.err.println(libraryPath);
		java.io.File dir = new java.io.File(libraryPath);
		String[] contents = dir.list();
		int i=0;
		for (String file : contents) {
			// System.err.println(file);          
			if (file.endsWith(".smiles")) {
				System.err.println("Loading molecule "+(i++));
				Molecule m = loadMolecule(libraryPath+"/"+file);
				MolecularUtils.cleanMolecule(m);
				System.err.println(m.toFormat("cxsmarts"));
				library.add(m);
			}
		}
		return library;
	}
	

	public Molecule getPlaceholder() throws Exception {
		return placeholder;
	}
	
	public int size() {
		return library.size();
	}

	public Iterator<Molecule> iterator() {
		return library.iterator();
	}
	public void add(Molecule m) {
		library.add(m);
	}
	
	
	// TODO: FIXME: BUG: So... matching substructures against the SML lets OCC1C(CO)C(C=O)C(CO)C1COCC1=CC=CC=C1 match OCC1C2C(=O)OC(=O)C2C(CO)C1COCC1=CC=CC=C1 which is technically wrong.
	// TODO: FIXME: This is a pretty inefficient implementation of contains, since it's O(n).  We could write our own molecular hashtable to improve that.
	public boolean contains(Molecule m) throws SearchException {
		return getMolecularMatch(m) != null;
	}
	public Molecule getMolecularMatch(Molecule x) throws SearchException {
		for(Molecule m: library) {
			if (MolecularUtils.molecularIsSomeChiralForm(m, x)) 
				return m;
		}
		return null;
	}
	public Collection<Molecule> getAllMolecularMatches(Molecule x) throws SearchException {
		Collection<Molecule> c = new ArrayList<Molecule>();
		for(Molecule m: library) {
			if (MolecularUtils.molecularIsSomeChiralForm(m, x)) 
				c.add(m);
		}
		return c;
	}

}
