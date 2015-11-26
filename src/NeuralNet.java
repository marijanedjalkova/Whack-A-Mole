import org.encog.Encog;
import org.encog.mathutil.rbf.RBFEnum;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.neural.networks.training.propagation.quick.QuickPropagation;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.neural.networks.training.propagation.scg.ScaledConjugateGradient;
import org.encog.neural.pattern.RadialBasisPattern;
import org.encog.neural.rbf.RBFNetwork;

public class NeuralNet {
    public static double[][] INPUT;
    public static double[][] IDEAL;
    public static RBFNetwork network;
    public int outputNeurons;
    public boolean isKohonen;
    int numNeuronsPerDimension;
    double vnwIndex;
    RBFEnum rbf;
    TrainEnum trainMethod;
    
    // 50, 0.9, 102, RBFEnum.Gaussian, TrainEnum.SVDTraining
    public void learn(int numNeuronsPerDimension, double vnwIndex, RBFEnum rbf, TrainEnum trainMethod, boolean isKohonen){
    	this.numNeuronsPerDimension = numNeuronsPerDimension;
    	this.vnwIndex = vnwIndex;
    	this.rbf = rbf;
    	this.trainMethod = trainMethod;
    	this.isKohonen = isKohonen;
    	if (this.isKohonen)
    		this.outputNeurons = 102;
    	else
    		this.outputNeurons = 2;
    	int dimensions = 2;
        if (network != null)
        	network.reset();
        
        double volumeNeuronWidth = vnwIndex / numNeuronsPerDimension;
       
        boolean includeEdgeRBFs = true;
        
        RadialBasisPattern pattern = new RadialBasisPattern();
        pattern.setInputNeurons(dimensions);
        pattern.setOutputNeurons(outputNeurons);
        
        int numNeurons = (int)Math.pow(numNeuronsPerDimension, dimensions);
        pattern.addHiddenLayer(numNeurons);
        network = (RBFNetwork)pattern.generate();
       
        network.setRBFCentersAndWidthsEqualSpacing(0, 1, rbf, volumeNeuronWidth, includeEdgeRBFs);
        
        if (this.isKohonen)
        	createKohonenTrainingData();
        else
        	createAnalogTrainingData();
      
        MLDataSet trainingSet = new BasicMLDataSet(INPUT, IDEAL);
        MLTrain train;
        
        switch (trainMethod) {
		case RESILIENTPROPAGATION:
			train = new ResilientPropagation(network, trainingSet);
			break;
		case BACKPROPAGATION:
			train = new Backpropagation(network, trainingSet);
			break;
		case QUICKPROPAGATION:
			train = new QuickPropagation(network, trainingSet);
			break;
		case SCALEDCONJUGATEGRADIENT:
			train = new ScaledConjugateGradient(network, trainingSet);
			break;
		default:
			train = new ResilientPropagation(network, trainingSet);
			break;
		}
        

        int epoch = 1;
        int maxEpoch = getMaxEpoch(trainMethod);
        System.out.println("Training " + rbf + " " + trainMethod + " " + isKohonen);
        do
        {
            train.iteration();
            if ( epoch % (maxEpoch / 10)==0)
            //if (epoch > 0)
            	System.out.println("Epoch #" + epoch + " Error:" + train.getError());
            epoch++;
        } while ((epoch < maxEpoch) && (train.getError() > 0.000001));
    }
    
    public static int getMaxEpoch(TrainEnum trainMethod){
    	switch (trainMethod) {
		case RESILIENTPROPAGATION:
			return 100;
		case BACKPROPAGATION:
			return 100;
		case QUICKPROPAGATION:
			return 100;
		case SCALEDCONJUGATEGRADIENT:
			return 100;
		default:
			return 100;
		}
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
