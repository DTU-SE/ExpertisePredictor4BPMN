package moderare.expertise.apps;

import java.util.Arrays;

import moderare.expertise.classifier.NeuralNetwork;
import moderare.expertise.model.DatabaseDataset;
import moderare.expertise.model.Dataset;
import moderare.expertise.model.EXPERTISE;
import moderare.expertise.utils.Pair;

public class ValidateDatasets extends ExpertisePredictor4BPMN {

	public static void main(String[] args) throws Exception {
		startup();
		
		boolean useAlreadyTrainedClassifier = false;
		boolean intraTaskValidation = true;
		boolean interTaskValidation = false;
		
		System.out.print("Loading train/test datasets... ");
		DatabaseDataset dataset = new DatabaseDataset(connection);
		for (String task : new String[]{ "mortgage-1"/*, "mortgage-2"/*, "pre-flight"*/}){
			for (EXPERTISE expertise : EXPERTISE.values()) {
				dataset.addFromDatabase(true,
						Arrays.asList(task),
						null, //Arrays.asList("modeling_process_eindhoven_2012_1.0","expert_modeling_task2_1.0"),
						Arrays.asList(expertise), 0.3, 1000, "rand ASC");
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
		
		end();
	}
}
