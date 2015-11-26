import java.util.ArrayList;

public class FSM {
	State currentState;
	ArrayList<State> states;
	// assume that all states are connected with each other
	// can get to any state from any state
	
	
	public FSM(){
		states = new ArrayList<State>();
		for (int i= 0; i < 2; i++)
			states.add(new State(i));
		currentState = null;
	}

	public void updateState(int move) {
		currentState =  states.get(move % 2);
	}
	

	
	
}
