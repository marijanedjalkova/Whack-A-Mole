import java.util.ArrayList;

public class FSM {
	State currentState;
	ArrayList<State> states;
	// assume that all states are connected with each other
	// can get to any state from any state
	
	
	public FSM(){
		states = new ArrayList<State>();
		for (int i= 0; i < 5; i++)
			states.add(new State((char) ('a' + i)));
		currentState = states.get(0);
	}
	
	public State getState(int pos){
		currentState = states.get(pos % 5);
		return states.get(pos % 5);
	}
	
	
}
