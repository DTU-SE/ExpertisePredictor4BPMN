package moderare.expertise.apps;

import moderare.expertise.classifier.NeuralNetwork;
import moderare.expertise.model.ModelingSession;

public class ValidateSingleSample extends ExpertisePredictor4BPMN {

	public static void main(String[] args) throws Exception {
		startup();
		
		System.out.print("Loading classifier... ");
		NeuralNetwork classifier = new NeuralNetwork(modelFilename);
		System.out.println("OK");
		
		System.out.println("\nSINGLE TASK VALIDATION");
		System.out.println("======================\n");
		
		for (String model_id : new String[] {/*"180_modeling_process_eindhoven_2012_1.0", "22122_expert_modeling_task2_1.0"*/ "32_modeling_process_eindhoven_2012_1.0"}) {
			ModelingSession session = new ModelingSession(connection);
			session.loadFromDatabase(model_id);
			System.out.println(session.getModelId() + " (" + session.getSampleClass() + ")");
			for (int windowSize : new int[] {10, 20, 40} ) {
				double globalAccuracy = classifier.computeAccuracy(session, windowSize, 0.6, 0);
				System.out.println("  window size: " + windowSize + " -> " + String.format("%.2f", globalAccuracy) + " global accuracy");
			}
			classifier.exportAccuracyChart(session, new int[] {5, 20, 40} , "charts/accuracies_" + session.getModelId() + ".png");
			classifier.exportCorrectClassificationChart(session, new int[] {5, 20, 40} , "charts/correctness_" + session.getModelId() + ".png", 0.9);
		}
		
		end();
	}
}
