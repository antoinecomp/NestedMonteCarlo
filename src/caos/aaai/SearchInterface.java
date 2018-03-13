package caos.aaai;

import java.util.Collection;

import caos.aaai.OperatorLibrary.Operator.GroundedOperator;
import caos.aaai.astar.Heuristic;

public interface SearchInterface {
	public void initializeSearch(OperatorLibrary lib, StartingMaterialsLibrary sml, int bound);
	public void search(State initialState);
	public Collection<GroundedOperator> getAnswer();
	public void dumpSearchSpace() throws Exception;
	public boolean getSuccess();
}
