package moderare.expertise;

import java.sql.Connection;
import java.sql.DriverManager;

import moderare.expertise.classifier.NeuralNetwork;
import moderare.expertise.model.DatabaseDataset;
import moderare.expertise.model.Dataset;
import moderare.expertise.model.EXPERTISE;
import moderare.expertise.utils.Pair;

import com.mysql.cj.jdbc.Driver;

public class ExpertisePredictor4BPMN {

	public static void main(String[] args) throws Exception {
		System.out.println("STARTUP PROCEDURES\n");
		System.out.print("Connecting to database... ");
		DriverManager.registerDriver(new Driver());
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/visual-data", "test", "test");
		System.out.println("OK");
		
		// ---------------------------------------------------------------------
		System.out.print("Loading dataset... ");
		DatabaseDataset dataset = new DatabaseDataset(connection);
		dataset.addFromDatabase(true, "mortgage-1", EXPERTISE.NOVICE, 0.3, 1000, "rand");
		dataset.addFromDatabase(true, "mortgage-1", EXPERTISE.EXPERT, 0.3, 1000, "rand");

		Pair<Dataset, Dataset> trainTest = dataset.split(0.75);
		Dataset trainingDataset = trainTest.getFirst();
		Dataset testDataset = trainTest.getSecond();
		System.out.println("OK");

		// ---------------------------------------------------------------------
		System.out.print("Training classifier... ");
		NeuralNetwork nn = new NeuralNetwork();
//		nn.train(trainingDataset);
//		nn.save("trained.model");
		nn.load("trained.model");
		System.out.println("OK");
		
		// ---------------------------------------------------------------------
		System.out.println("\nTESTING PROCEDURES\n");
		System.out.println("Datasets characteristics: " + dataset.size() + " instances, " + trainingDataset.size() + " for training, " + testDataset.size() + " for test\n");
		System.out.println(nn.printEvaluation(trainingDataset, testDataset));
		
		System.out.println("Finished, bye!");
	}

}
