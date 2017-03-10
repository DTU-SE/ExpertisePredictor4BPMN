package moderare.expertise.apps;

import java.util.Arrays;

import moderare.expertise.classifier.NeuralNetwork;
import moderare.expertise.model.DatabaseDataset;
import moderare.expertise.model.EXPERTISE;
import weka.classifiers.Evaluation;

public class CrossValidate extends ExpertisePredictor4BPMN {

	public static void main(String[] args) throws Exception {
		startup();
		
		System.out.print("Loading dataset... ");
		DatabaseDataset dataset = new DatabaseDataset(connection);
		for (String task : new String[]{ "mortgage-1"/*, "mortgage-2"/*, "pre-flight"*/}){
			for (EXPERTISE expertise : EXPERTISE.values()) {
				dataset.addFromDatabase(true,
						Arrays.asList(task),
						null, //Arrays.asList("modeling_process_eindhoven_2012_1.0","expert_modeling_task2_1.0"),
						Arrays.asList(expertise), 0.3, 1000, "rand ASC");
			}
		}
		System.out.println("OK");
		
		NeuralNetwork nn = new NeuralNetwork();
		Evaluation crossValidation = nn.crossValidation(dataset, 10);
		
		System.out.println(nn.printEvaluation(crossValidation));
		
		end();
	}
}
