package moderare.expertise.apps;

import java.util.Arrays;

import moderare.expertise.classifier.Classifier;
import moderare.expertise.classifier.DecisionTree;
import moderare.expertise.classifier.LogisticRegression;
import moderare.expertise.classifier.NeuralNetwork;
import moderare.expertise.classifier.RandomForest;
import moderare.expertise.classifier.SVM_Poly;
import moderare.expertise.classifier.SVM_RBF;
import moderare.expertise.model.DatabaseDataset;
import moderare.expertise.model.EXPERTISE;
import weka.classifiers.Evaluation;

/**
 * This code has been used for the student-practitioner paper.
 * 
 * @author Andrea Burattin
 */
public class CrossValidate extends ExpertisePredictor4BPMN {

	public static void main(String[] args) throws Exception {
		startup();
		
		for (int size : new int[] {
				500,
				1000,
				2000,
				4000
				}) {
			
			System.out.println("=== TESTING WITH " + (size*2) + " INSTANCES ===");
			
			System.out.print("Loading dataset... ");
			DatabaseDataset dataset = new DatabaseDataset(connection);
			for (String task : new String[]{
					"mortgage-1",
					"pre-flight"
					}){
				for (EXPERTISE expertise : EXPERTISE.values()) {
					dataset.addFromDatabase(true,
							Arrays.asList(task),
							null,
							Arrays.asList(expertise), 0.3, size, "rand ASC");
				}
			}
			System.out.println("OK");
			
			for (Classifier classifier : new Classifier[] {
					new NeuralNetwork(),
					new SVM_Poly(),
					new SVM_RBF(),
					new DecisionTree(),
					new RandomForest(),
					new LogisticRegression()
			}) {
				
				System.out.println("=== TESTING WITH " + classifier.getClass().getSimpleName().toUpperCase() + " ===");
				
				Evaluation crossValidation = classifier.crossValidation(dataset, 10, false);
				System.out.println(classifier.printEvaluation(crossValidation));
			}
		}
		
		end();
	}
}
