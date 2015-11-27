import java.util.HashMap;
import java.util.Random;
import org.encog.Encog;
import org.encog.mathutil.rbf.RBFEnum;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.pattern.RadialBasisPattern;
import org.encog.neural.rbf.RBFNetwork;

public class UnscriptedNeuralNet {
	   public static double[][] INPUT;
	    public static double[][] IDEAL;
	    public static RBFNetwork network;
	    
	    public int outputNeurons;
	    public boolean isKohonen;
	    int numNeuronsPerDimension;
	    double vnwIndex;
	    RBFEnum rbf;
	    
	    public UnscriptedNeuralNet(){
	    	setup(7, 2, RBFEnum.Gaussian, false);
	    }
	    
	    public void update(int clue, int inputState, int guess, int stateGuess, int move, int rightStateValue){
	    	
	    	applyChanges(clue, inputState, guess, stateGuess, move, rightStateValue);
	    }
	    
	    public double[] generateConfigs(int length){
	    	Random r = new Random();
	    	double[] changes = new double[length];
	    	for (int i = 0; i < changes.length; i++){
	    		int sign = r.nextDouble() > 0.5 ? 1 : -1;
	    		changes[i] = sign * r.nextDouble() / 100;
	    	}
	    	return changes;
	    }
	    
	  
	    
	    public void applyChanges(int clue, int inputState, int guess, int stateGuess, int move, int rightStateValue){
	    	//System.out.println("Error was " + (Math.abs(move - guess)));
	    	double errorBefore = Math.abs(move - guess) + Math.abs(rightStateValue - stateGuess);
	    	HashMap<Double, double[]> tests = new HashMap<Double, double[]>();
	    	double [] weights = network.getFlat().getWeights();
	    	double[] copy = weights.clone();
	    	for (int i = 0; i < 100; i++){
	    		copy = weights.clone();
	    		double[] ch = generateConfigs(weights.length);
	    		for (int j = 0; j < weights.length; j++){
	    			copy[j] += ch[j];
	    		}
	    		network.getFlat().setWeights(copy);
	    		int[] resAFTER = makeAnalogDecision(clue, inputState);
	    		double errorAFTER = Math.abs(move - resAFTER[0]) + Math.abs(rightStateValue - resAFTER[1]);
	    		tests.put(errorAFTER, ch);
	    	}
	    	double minError = 10000000;
	    	double[] bestCh = null;
	    	for (double err : tests.keySet()){
	    		if (err < minError){
	    			minError = err;
	    			bestCh = tests.get(err);
	    		}
	    	}
	    	double errorAFTER = errorBefore;
	    	int[] resAFTER;
	    	int count = 0;
	    	do{
	    		count++;
	    		errorBefore = errorAFTER;
		    	for (int i = 0; i < weights.length; i++){
		    		weights[i] += bestCh[i];
		    	}
		    	network.getFlat().setWeights(weights);
	    		resAFTER = makeAnalogDecision(clue, inputState);
	    		errorAFTER = Math.abs(move - resAFTER[0]) + Math.abs(rightStateValue - resAFTER[1]);
		    	//System.out.println("before " + errorBefore + ", after" + errorAfter);
	    	}while (errorAFTER < errorBefore && (errorBefore - errorAFTER > 0.01));
	    	System.out.println("Used " + count + " cycles");
	    	undoLastChanges(network.getFlat().getWeights(), bestCh);
	    }
	    
	    public void undoLastChanges(double[] weights, double[] changes){
	    	for (int i = 0; i < weights.length; i++){
	    		weights[i] -= changes[i];
	    	}
	    	network.getFlat().setWeights(weights);
	    }
	    
	    
	    public void setup(int numNeuronsPerDimension, double vnwIndex, RBFEnum rbf, boolean isKohonen){
	    	this.numNeuronsPerDimension = numNeuronsPerDimension;
	    	this.vnwIndex = vnwIndex;
	    	this.rbf = rbf;
	    	this.isKohonen = isKohonen;
	    	if (this.isKohonen)
	    		this.outputNeurons = 102;
	    	else
	    		this.outputNeurons = 2;
	    	int dimensions = 2;
	    	
	        double volumeNeuronWidth = vnwIndex / numNeuronsPerDimension;
	       
	        boolean includeEdgeRBFs = true;
	        
	        RadialBasisPattern pattern = new RadialBasisPattern();
	        pattern.setInputNeurons(dimensions);
	        pattern.setOutputNeurons(outputNeurons);
	        
	        int numNeurons = (int)Math.pow(numNeuronsPerDimension, dimensions);
	        pattern.addHiddenLayer(numNeurons);
	        network = (RBFNetwork)pattern.generate();
	       
	        network.setRBFCentersAndWidthsEqualSpacing(0, 1, rbf, volumeNeuronWidth, includeEdgeRBFs);

	    }
	    
	    
	    public double[] makeKohonenDecision(int clue, int stateValue){
	 	   double[] inputArray = {scale(clue), scaleState(stateValue)};
	 	   double[][] iArray = new double[1][2];
	 	   iArray[0] = inputArray;
	 	   BasicMLDataSet input_set = new BasicMLDataSet(iArray, null);
	 	   MLData output = network.compute(input_set.get(0).getInput());
	 	   double[] result = new double[102];
	 	   for (int i = 0; i < 102; i++){
	 		   result[i] = output.getData(i);
	 	   }
	 	   return result;
	    }
	    
	    public int[] makeAnalogDecision(int clue, int stateValue){
	    	double[] inputArray = {scale(clue), scaleState(stateValue)};
	  	   double[][] iArray = new double[1][2];
	  	   iArray[0] = inputArray;
	  	   BasicMLDataSet input_set = new BasicMLDataSet(iArray, null);
	  	   MLData output = network.compute(input_set.get(0).getInput());
	  	   return new int[] {inverseScale(output.getData(0)), inverseStateScale(output.getData(1))};
	    }
	    
	    
	    public static void shutDown(){
	 	   Encog.getInstance().shutdown();
	    }
	     
	    private static int calculate(int clue, int stateValue){
	    	int result = 0;
	 	  if (stateValue == 0){
	 		  result = clue + 5;
	 		  if (result >= 100){
	 			  result = result - 100;
	 		  }
	 	  } else {
	 		   result = clue - 5;
	 		   if (result < 0){
	 			   result = 100 + result;
	 		   }
	 	   }
	 	  return result;
	    }
	    
	    private static int calcState(int pos){
	    	return pos % 2;
	    }

	    static void createAnalogTrainingData(){
	    	 int iLimit = 99;
	         int kLimit = 1;
	         
	         INPUT = new double[(iLimit + 1) * (kLimit + 1)][];
	         IDEAL = new double[(iLimit + 1) * (kLimit + 1)][];
	         for (int i = 0; i <= iLimit; i++)
	         {
	             for (int k = 0; k <= kLimit; k++)
	             {
	                 INPUT[i * (kLimit + 1) + k] = new double[2];
	                 IDEAL[i * (kLimit + 1) + k] = new double[2];
	                 int res = calculate(i, k);
	                 INPUT[i * (kLimit + 1) + k][0] = scale(i);
	                 INPUT[i * (kLimit + 1) + k][1] = scaleState(k);
	                 IDEAL[i * (kLimit + 1) + k][0] = scale(res);
	                 IDEAL[i * (kLimit + 1) + k][1] = scaleState(calcState(res));
	             }
	         }
	    }
	    
	    static void createKohonenTrainingData(){
	        int iLimit = 99;
	        int kLimit = 1;
	        
	        INPUT = new double[(iLimit + 1) * (kLimit + 1)][];
	        IDEAL = new double[(iLimit + 1) * (kLimit + 1)][];
	        for (int i = 0; i <= iLimit; i++){
	            for (int k = 0; k <= kLimit; k++){
	                INPUT[i * (kLimit + 1) + k] = new double[2];
	                IDEAL[i * (kLimit + 1) + k] = new double[102];
	                int res = calculate(i, k);
	                INPUT[i * (kLimit + 1) + k][0] = scale(i);
	                INPUT[i * (kLimit + 1) + k][1] = scaleState(k);
	                for (int j = 0; j <= 101; j++){
	                	IDEAL[i * (kLimit + 1) + k][j] = 0;
	                }
	                IDEAL[i * (kLimit + 1) + k][res] = 1;
	                int stateIdeal = calcState(res);

	                IDEAL[i * (kLimit + 1) + k][100 + stateIdeal] = 1;
	                
	            }
	        }
	    }
	    
	    private static double scaleState(int value){
	    	if (value == 0)
	    		return 0.9;
	    	return 0.1;
	    }
	    
	    public static int inverseStateScale(double i){
	    	return (int)Math.round(i);
	    }
	    
	    private static double scale(double i){
	    	return (double) i / 100;
	    }
	    
	    public static int inverseScale(double i){
	    	return (int)Math.round(((i ) * 100));
	    }
}
