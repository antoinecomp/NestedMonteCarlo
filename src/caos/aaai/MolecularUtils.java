package caos.aaai;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chemaxon.calculations.hydrogenize.Hydrogenize;
import chemaxon.checkers.RingStrainErrorChecker;
import chemaxon.checkers.StructureChecker;
import chemaxon.checkers.ValenceErrorChecker;
import chemaxon.checkers.result.StructureCheckerResult;
import chemaxon.checkers.runner.AdvancedCheckerRunner;
import chemaxon.checkers.runner.CheckerRunner;
import chemaxon.checkers.runner.configuration.reader.ConfigurationReader;
import chemaxon.checkers.runner.configuration.reader.XMLBasedConfigurationReader;
import chemaxon.fixers.StructureFixer;
import chemaxon.fixers.ValenceFixer;
import chemaxon.sss.SearchConstants;
import chemaxon.sss.search.MolSearchOptions;
import chemaxon.sss.search.SearchException;
import chemaxon.sss.search.StandardizedMolSearch;
import chemaxon.struc.Molecule;
import chemaxon.struc.StereoConstants;

public class MolecularUtils {
	private static ConfigurationReader configurationReader = null; 

	public static class MolecularMap<V> {
		private class Key {
			int hash;
			Molecule m;
			public Key(Molecule m) {
				this.m = m;
				// This could fail if cxsmarts doesn't auto-canonicalize...
				// which is safe, albeit inefficient, if the only use is as a cache
				this.hash = m.toFormat("cxsmarts").hashCode();  
			}
			@Override
			public int hashCode() {
				return hash;
			}
			@Override
			public boolean equals(Object obj) {
				if(obj instanceof MolecularUtils.MolecularMap.Key) {
					Key k = (Key)obj;
					try {
						return molecularEquivalence(this.m, k.m);
					} catch (SearchException e) {
						return false;
					}
				}
				return false;
			}
		}
		private Map<Key, V> map;
		public MolecularMap() {
			map = new HashMap<Key, V>();
		}
		public V put(Molecule m, V cm) {
			return map.put(new Key(m), cm);
		}
		public V get(Molecule m) {
			return map.get(new Key(m));
		}
	}
	public static class MolecularListMap<V> {
		public static class Pair<V> {
			List<Molecule> lm;
			V v;
			public Pair(List<Molecule> lm, V v) {
				this.lm = lm;
				this.v  = v;
			}
		}
		private class Key {
			int hash;
			public List<Molecule> lm;
			public Key(List<Molecule> lm) {
				this.lm = lm;
				// This could fail if cxsmarts doesn't auto-canonicalize...
				// which is safe, albeit inefficient, if the only use is as a cache
				String s = "";
				for(Molecule m : lm)
					s += m.toFormat("cxsmarts");
				this.hash = s.hashCode();  
			}
			@Override
			public int hashCode() {
				return hash;
			}
			@Override
			public boolean equals(Object obj) {
				if(obj instanceof MolecularUtils.MolecularListMap.Key) {
					Key k = (Key)obj;
					try {
						return molecularListEqual(this.lm, k.lm);
					} catch (SearchException e) {
						return false;
					}
				}
				return false;
			}
		}
		private Map<Key, V> map;
		public MolecularListMap() {
			map = new HashMap<Key, V>();
		}
		public V put(List<Molecule> m, V cm) {
			return map.put(new Key(m), cm);
		}
		public V get(List<Molecule> m) {
			return map.get(new Key(m));
		}
		public V get(List<Molecule> m, V defaultValue) {
			Key k = new Key(m);
			if(!map.containsKey(k)){
				map.put(k, defaultValue);
			}
			return map.get(k);
		}
		public List<Pair<V>> items() {
			List<Pair<V>> l = new ArrayList<Pair<V>>(map.size());
			for(Map.Entry<Key, V> e : map.entrySet()) {
				l.add(new Pair(e.getKey().lm, e.getValue()));
			}
			return l;
		}
	}
	
	public static void printMoleculeList(List<Molecule> lm) {
		for(Molecule m : lm) 
			System.err.println(m.toFormat("cxsmarts"));
	}
	
	public static boolean molecularEquivalence(Molecule a, Molecule b) throws SearchException {
		StandardizedMolSearch sms = new StandardizedMolSearch();
		MolSearchOptions options = new MolSearchOptions(SearchConstants.DUPLICATE);
		sms.setSearchOptions(options);
		sms.setQuery(a);
		sms.setTarget(b);
		boolean matching = sms.isMatching();
		if(false) {
			String astr = "~/bin/view.sh \'"+a.toFormat("cxsmarts")+"\'";
			String bstr = "~/bin/view.sh \'"+b.toFormat("cxsmarts")+"\'";
			if (matching) {
				System.err.println("SAME! "+astr+" "+bstr);
			} else {
				System.err.println("DIFF! "+astr+" "+bstr);

			}
		}
		return matching;
	}

	/**
	 * Will match any chiral form of molecule b
	 * @param a is the target.
	 * @param b is the query.
	 * @return
	 * @throws SearchException
	 */
	public static boolean molecularSatisfiedBySomeChiralForm(Molecule a, Molecule b) throws SearchException {
		// Do I care that this works imperatively?
		MolecularUtils.cleanMolecule(b);
		
		StandardizedMolSearch sms = new StandardizedMolSearch();
//		MolSearchOptions options = new MolSearchOptions(SearchConstants.FULL_FRAGMENT);
		MolSearchOptions options = new MolSearchOptions(SearchConstants.SUBSTRUCTURE);
		sms.setSearchOptions(options);
		sms.setQuery(b);
		sms.setTarget(a);
		boolean matching = sms.isMatching();
		return matching;
	}

	/**
	 * Implementing my own 'contains' method because molecules don't seem to obey .equals()!
	 * @param C
	 * @param x
	 * @return
	 * @throws SearchException
	 */
	public static boolean molecularContains(Collection<Molecule> C, Molecule x) throws SearchException {
		for(Molecule m: C) {
			if (MolecularUtils.molecularEquivalence(m, x)) 
				return true;
		}
		return false;
	}

	/**
	 * Implementing my own 'contains' method because molecules don't seem to obey .equals()!
	 * @param C
	 * @param x
	 * @return
	 * @throws SearchException
	 */
	public static boolean molecularSetsEqual(Collection<Molecule> A, Collection<Molecule> B) throws SearchException {
		if(A.size() != B.size()) {
			return false;
		}
		for(Molecule m: A) {
			if (!MolecularUtils.molecularContains(B, m)) 
				return false;
		}
		for(Molecule m: B) {
			if (!MolecularUtils.molecularContains(A, m)) 
				return false;
		}
		return true;
	}
	

	public static boolean molecularListEqual(List<Molecule> A,
			List<Molecule> B) throws SearchException {
		if(A.size() != B.size()) {
			return false;
		}
		for(int i=0; i<A.size(); i++) {
			Molecule a = A.get(i);
			Molecule b = B.get(i);
			if (!MolecularUtils.molecularEquivalence(a, b)) 
				return false;
		}
		return true;
	}

	/**
	 * Implementing my own 'contains' method because molecules don't seem to obey .equals()!
	 * @param C
	 * @param x
	 * @return
	 * @throws SearchException
	 */
	public static boolean molecularSetsContains(Collection<Collection<Molecule>> moleculeSetsAlreadyReacted, Collection<Molecule> b) throws SearchException {
		for(Collection<Molecule> a: moleculeSetsAlreadyReacted) {
			if (MolecularUtils.molecularSetsEqual(a, b)) 
				return true;
		}
		return false;
	}
	public static boolean molecularSetsContains(List<List<Molecule>> moleculeSetsAlreadyReacted, List<Molecule> b) throws SearchException {
		for(List<Molecule> a: moleculeSetsAlreadyReacted) {
			if (MolecularUtils.molecularSetsEqual(a, b)) 
				return true;
		}
		return false;
	}

	/**
	 * Modify moleculeSets to be the union of moleculeSets and reactionApplicationResults.
	 * This method should not create duplicates in moleculeSets.
	 * 
	 * @param moleculeSets
	 * @param reactionApplicationResults
	 * @throws SearchException 
	 */
	public static void molecularSetsMergeIn(Collection<Collection<Molecule>> moleculeSets,
			Collection<Collection<Molecule>> reactionApplicationResults) throws SearchException {
		for(Collection<Molecule> cm: reactionApplicationResults) {
			MolecularUtils.molecularSetsMergeOneIn(moleculeSets, cm);
		}
	}
	public static void molecularSetsMergeOneIn(Collection<Collection<Molecule>> moleculeSetsAlreadyReacted,
			Collection<Molecule> cm) throws SearchException {
			if(!MolecularUtils.molecularSetsContains(moleculeSetsAlreadyReacted, cm)) {
				moleculeSetsAlreadyReacted.add(cm);
			}
	}
	public static void molecularSetsMergeOneIn(List<List<Molecule>> moleculeSetsAlreadyReacted,
			List<Molecule> cm) throws SearchException {
			if(!MolecularUtils.molecularSetsContains(moleculeSetsAlreadyReacted, cm)) {
				moleculeSetsAlreadyReacted.add(cm);
			}
	}
	

	public static void molecularSetMergeIn(Collection<Molecule> A, Collection<Molecule> B) throws SearchException {
		for(Molecule b: B) {
			molecularSetAdd(A,b);		
		}
	}
	public static void molecularSetAdd(Collection<Molecule> A, Molecule b) throws SearchException {
			if(!MolecularUtils.molecularContains(A, b)) {
				A.add(b);
			}			
	}

	public static Collection<Molecule> copyMoleculeSetExceptForOne(Collection<Molecule> C,
			Molecule x) throws SearchException {
		Collection<Molecule> A = new ArrayList<Molecule>();
		for(Molecule m: C) {
			if (!MolecularUtils.molecularEquivalence(m, x)) {
				A.add(m);
			}
		}
		return A;
	}
	
	public static Collection<Molecule> constructSetDifference(Collection<Molecule> C,
			Collection<Molecule> B) throws SearchException {
		Collection<Molecule> A = new ArrayList<Molecule>();
		for(Molecule m: C) {
			if (!MolecularUtils.molecularContains(B, m)) {
				A.add(m);
			}
		}
		return A;
	}

	/**
	 * Implementing my own 'contains' method because molecules don't seem to obey .equals()!
	 * @param C
	 * @param x
	 * @return
	 * @throws SearchException
	 */
	public static boolean molecularContainsSomeChiralMatch(Collection<Molecule> C, Molecule x) throws SearchException {
		for(Molecule m: C) {
			if (MolecularUtils.molecularSatisfiedBySomeChiralForm(m, x)) 
				return true;
		}
		return false;
	}
	/**
	 * Implementing my own 'contains' method because molecules don't seem to obey .equals()!
	 * @param C
	 * @param x
	 * @return
	 * @throws SearchException
	 */
	public static boolean molecularContainsSomeChiralMatch2(Collection<Molecule> C, Molecule x) throws SearchException {
		for(Molecule m: C) {
			if (MolecularUtils.molecularSatisfiedBySomeChiralForm(x,m)) 
				return true;
		}
		return false;
	}

	public static void cleanMolecule(Molecule m) {
		m.dearomatize();
//		m.hydrogenize(true);
//		Hydrogenize.addHAtoms(m); 
		
		StructureChecker sc = new ValenceErrorChecker();
		StructureCheckerResult scr = sc.check(m);
		StructureFixer sf = new ValenceFixer();
		if (scr != null) {
			sf.fix(scr);
		}

		// TODO: anything else?
		
	}

	/**
		 * Will match any chiral form of molecule b
		 * @param a is the target.
		 * @param b is the query.
		 * @return
		 * @throws SearchException
		 */
		public static boolean molecularIsSomeChiralForm(Molecule a, Molecule b) throws SearchException {
			// Do I care that this works imperatively?
			MolecularUtils.cleanMolecule(b);
			
			StandardizedMolSearch sms = new StandardizedMolSearch();
			MolSearchOptions options = new MolSearchOptions(SearchConstants.FULL_FRAGMENT);
			sms.setSearchOptions(options);
			sms.setQuery(b);
			sms.setTarget(a);
			boolean matching = sms.isMatching();

			return matching;
		}

	/**
		 * Will match any chiral form of molecule b
		 * @param a is the target.
		 * @param b is the query.
		 * @return
		 * @throws SearchException
		 */
		public static boolean molecularEquivalentToSomeChiralForm(Molecule a, Molecule b) throws SearchException {
			// Do I care that this works imperatively?
			MolecularUtils.cleanMolecule(b);
			
			StandardizedMolSearch sms = new StandardizedMolSearch();
			MolSearchOptions options = new MolSearchOptions(SearchConstants.FULL_FRAGMENT);
			options.setStereoSearchType(SearchConstants.STEREO_IGNORE);
			sms.setSearchOptions(options);
			sms.setQuery(b);
			sms.setTarget(a);
			boolean matching = sms.isMatching();

			return matching;
		}

}
