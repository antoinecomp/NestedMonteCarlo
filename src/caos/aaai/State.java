package caos.aaai;

import java.util.Collection;

import test.ViewJTable;

import caos.aaai.OperatorLibrary.Operator.GroundedOperator;
import chemaxon.reaction.ReactionException;
import chemaxon.reaction.Reactor;
import chemaxon.sss.search.SearchException;
import chemaxon.struc.Molecule;
import chemaxon.struc.RxnMolecule;

public class State {
	private Molecule molecule;
	private boolean isStartingMaterial;
	int hash;
	
	public State(Molecule molecule, boolean solved) {
		this.molecule = molecule;
		this.isStartingMaterial = solved; 
	
		// This could fail if cxsmarts doesn't auto-canonicalize...
		// which is safe, albeit inefficient, if the only use is as a cache
		this.hash = molecule.toFormat("cxsmarts").hashCode();  

	}

	public Molecule getMolecule() {
		return this.molecule;
	}

	public boolean isGoal() {
		return isStartingMaterial;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (! (obj instanceof State)) return false;
		if (this.hash != ((State)obj).hash) return false;
		try {
			return MolecularUtils.molecularEquivalence(this.molecule, ((State)obj).getMolecule());
		} catch (SearchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Error();
		}
	}
	
	public String toString() {
		return getMolecule().toFormat("cxsmarts");
	}

	public int size() {
		return  molecule.getAtomCount();
	}

}
