import org.encog.mathutil.rbf.RBFEnum;

public class Player {
	NeuralNet net;
	boolean isKohonen;
	
	public Player(){
		net = new NeuralNet();
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

	
	public void finish(){
		Machine.shutDown();
	}
}
