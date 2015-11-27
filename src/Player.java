import org.encog.mathutil.rbf.RBFEnum;

public class Player {
	NeuralNet net;
	boolean isKohonen;
	UnscriptedNeuralNet unet;
	int unscripted_threshold = 2;

	
	public void initialiseUnscriptedNet(){
		unet = new UnscriptedNeuralNet();
		isKohonen = false;
	}

	
	public int[] makeUnscriptedGuess(int clue, State curState, int move, int rightStateValue){
		int[] res = makeUGuess(clue, curState);
		int guess = res[0];
		int stateGuess = res[1];
		unet.update(clue, curState.getValue(), guess, stateGuess, move, rightStateValue);
		
		return makeUGuess(clue, curState);
	}
	
	public int[] makeUGuess(int clue, State curState){
		int stateValue = clue;
		if (curState != null){
			stateValue = curState.getValue();
		}
		if (this.isKohonen)
			return makeKohonenGuess(clue, stateValue);
		return unet.makeAnalogDecision(clue, stateValue);
	}

	
	public int[] makeGuess(int clue, State curState){
		int stateValue = clue;
		if (curState != null){
			stateValue = curState.getValue();
		}
		if (this.isKohonen)
			return makeKohonenGuess(clue, stateValue);
		return net.makeAnalogDecision(clue, stateValue);
	}
	
	public int[] makeKohonenGuess(int clue, int stateValue) {
		
		double max = 0;
		int maxi = 0;
		double[] results = net.makeKohonenDecision(clue, stateValue);
		for (int i = 0; i < 100; i++){
			if (results[i]>max){
				max = results[i];
				maxi = i;
			}
		}
		int stateGuess = 0;
		if (results[100] < results[101])
			stateGuess = 1;
		return new int[]{maxi, stateGuess};
		
	}
	
	public void learn(
			int numNeuronsPerDimension, 
			double vnwIndex, 
			RBFEnum rbf, 
			TrainEnum trainMethod, 
			boolean isKohonen){
		this.isKohonen = isKohonen;
		net.learn(numNeuronsPerDimension, vnwIndex, rbf, trainMethod, isKohonen);
		
	}
	
	public void inilialiseScriptedNet(){
		net = new NeuralNet();
	}

	
	public void finish(){
		Machine.shutDown();
	}
}
