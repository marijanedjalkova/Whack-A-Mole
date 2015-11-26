import org.encog.Encog;
import org.encog.mathutil.rbf.RBFEnum;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.neural.pattern.RadialBasisPattern;
import org.encog.neural.rbf.RBFNetwork;

public class KohonenMachine {
    public static double[][] INPUT;
    public static double[][] IDEAL;
    public static RBFNetwork network;

    
    public KohonenMachine(){
     
        int dimensions = 2;
        int numNeuronsPerDimension = 50;
        
        double volumeNeuronWidth = 0.9 / numNeuronsPerDimension;
       
        boolean includeEdgeRBFs = true;
        
        RadialBasisPattern pattern = new RadialBasisPattern();
        pattern.setInputNeurons(dimensions);
        pattern.setOutputNeurons(102);
        
        int numNeurons = (int)Math.pow(numNeuronsPerDimension, dimensions);
        pattern.addHiddenLayer(numNeurons);
        network = (RBFNetwork)pattern.generate();
       
        network.setRBFCentersAndWidthsEqualSpacing(0, 1, RBFEnum.Gaussian, volumeNeuronWidth, includeEdgeRBFs);

        create2DSmoothTainingDataGit();
      
        MLDataSet trainingSet = new BasicMLDataSet(INPUT, IDEAL);
        MLTrain train = new ResilientPropagation(network, trainingSet);

        int epoch = 1;
        do
        {
            train.iteration();
            if (epoch > 0)
            	System.out.println("Epoch #" + epoch + " Error:" + train.getError());
            epoch++;
        } while ((epoch < 100) && (train.getError() > 0.000001));
    }

    
    public static double[] makeDecision(int clue, int stateValue){
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

    static void create2DSmoothTainingDataGit(){
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
