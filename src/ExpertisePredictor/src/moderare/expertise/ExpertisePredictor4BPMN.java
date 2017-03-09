package moderare.expertise;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;

import moderare.expertise.classifier.NeuralNetwork;
import moderare.expertise.model.DatabaseDataset;
import moderare.expertise.model.Dataset;
import moderare.expertise.model.EXPERTISE;
import moderare.expertise.model.ModelingSession;
import moderare.expertise.utils.Pair;

import com.mysql.cj.jdbc.Driver;

public class ExpertisePredictor4BPMN {

	public static void main(String[] args) throws Exception {
		
		// configuration parameters
		boolean useAlreadyTrainedClassifier = false;
		boolean intraTaskValidation = true;
		boolean interTaskValidation = false;
		boolean singleTaskValidation = false;
		
		
		// ---------------------------------------------------------------------
		System.out.println("STARTUP PROCEDURES");
		System.out.println("==================\n");
		System.out.print("Connecting to database... ");
		DriverManager.registerDriver(new Driver());
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/visual-data", "test", "test");
		System.out.println("OK");
		
		
		// ---------------------------------------------------------------------
		System.out.print("Loading train/test datasets... ");
		DatabaseDataset dataset = new DatabaseDataset(connection);
		for (String task : new String[]{ "mortgage-1"/*, "pre-flight"*/}){
			for (EXPERTISE expertise : EXPERTISE.values()) {
				dataset.addFromDatabase(true,
						Arrays.asList(task),
						Arrays.asList("modeling_process_eindhoven_2012_1.0","expert_modeling_task2_1.0"),
						Arrays.asList(expertise), 0.3, 2000, "rand ASC");
			}
		}
		
		Pair<Dataset, Dataset> trainTest = dataset.split(0.75);
		Dataset trainingDataset = trainTest.getFirst();
		Dataset testDataset = trainTest.getSecond();
		System.out.println("OK");
		
		System.out.print("Loading additional datasets... ");
		DatabaseDataset datasetDifferentTask = new DatabaseDataset(connection);
		datasetDifferentTask.addFromDatabase(
				false,
				Arrays.asList("mortgage-2"),
				null,
				null,
				0,
				1000, "rand ASC");
		
		System.out.println("OK");

		
		// ---------------------------------------------------------------------
		System.out.print("Training classifier... ");
		NeuralNetwork classifier = new NeuralNetwork();
		String modelFilename = "trained-models/nn.model";
		if (useAlreadyTrainedClassifier) {
			classifier.load(modelFilename);
		} else {
			classifier.train(trainingDataset);
			classifier.save(modelFilename);
		}
		System.out.println("OK");
		
		
		// ---------------------------------------------------------------------
		if (intraTaskValidation) {
			System.out.println("\nINTRA-TASK VALIDATION");
			System.out.println("=====================\n");
			System.out.println("Datasets characteristics: " + dataset.size() + " instances, " + trainingDataset.size() + " for training, " + testDataset.size() + " for test\n");
			System.out.println(classifier.printEvaluation(trainingDataset, testDataset));
		}
		
		if (interTaskValidation) {
			System.out.println("\nINTER-TASKS VALIDATION");
			System.out.println("======================\n");
			System.out.println("Second datasets characteristics: " + datasetDifferentTask.size() + " instances\n");
			System.out.println(classifier.printEvaluation(trainingDataset, datasetDifferentTask));
		}
		
		if (singleTaskValidation) {
			System.out.println("\nSINGLE TASK VALIDATION");
			System.out.println("======================\n");
			
			for (String model_id : new String[] {/*"180_modeling_process_eindhoven_2012_1.0", "22122_expert_modeling_task2_1.0"*/ "32_modeling_process_eindhoven_2012_1.0"}) {
				ModelingSession session = new ModelingSession(connection);
				session.loadFromDatabase(model_id);
				System.out.println(session.getModelId() + " (" + session.getSampleClass() + ")");
				for (int windowSize : new int[] {10, 20, 40} ) {
					double globalAccuracy = classifier.computeAccuracy(session, windowSize, 0.6);
					System.out.println("  window size: " + windowSize + " -> " + String.format("%.2f", globalAccuracy) + " global accuracy");
				}
				classifier.exportAccuracyChart(session, new int[] {5, 20, 40} , "charts/accuracies_" + session.getModelId() + ".svg");
				classifier.exportCorrectClassificationChart(session, new int[] {5, 20, 40} , "charts/correctness_" + session.getModelId() + ".svg", 0.9);
			}
		}
		
		// ---------------------------------------------------------------------
		connection.close();
		System.out.println("\nFinished, bye!");
	}

}
