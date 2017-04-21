package moderare.expertise.apps;

import java.sql.ResultSet;
import java.util.Arrays;

import moderare.expertise.classifier.NeuralNetwork;
import moderare.expertise.model.DatabaseDataset;
import moderare.expertise.model.EXPERTISE;
import moderare.expertise.model.ModelingSession;

public class ValidateWholeSessions extends ExpertisePredictor4BPMN {

	public static void main(String[] args) throws Exception {
		startup();
		
		boolean useAlreadyTrainedClassifier = true;
		boolean trainWithSmote = false;
		boolean evaluateOverTime = true;
		String task = "pre-flight";
//		String task = "mortgage-1";
		
		String idsForTrainingPreFlight = "'2050_expert_modeling_task1_1.0','2071_expert_modeling_task1_1.0','2077_expert_modeling_task1_1.0','2084_expert_modeling_task1_1.0','2087_expert_modeling_task1_1.0','22115_expert_modeling_task1_1.0','22118_expert_modeling_task1_1.0','22121_expert_modeling_task1_1.0','22124_expert_modeling_task1_1.0','22127_expert_modeling_task1_1.0','22130_expert_modeling_task1_1.0','22133_expert_modeling_task1_1.0','22139_expert_modeling_task1_1.0','22142_expert_modeling_task1_1.0','22145_expert_modeling_task1_1.0','22148_expert_modeling_task1_1.0','22151_expert_modeling_task1_1.0','22154_expert_modeling_task1_1.0','22340_expert_modeling_task1_1.0','22438_expert_modeling_task1_1.0','22441_expert_modeling_task1_1.0','22444_expert_modeling_task1_1.0','22447_expert_modeling_task1_1.0','22450_expert_modeling_task1_1.0','22453_expert_modeling_task1_1.0','22456_expert_modeling_task1_1.0','22462_expert_modeling_task1_1.0','22465_expert_modeling_task1_1.0','22468_expert_modeling_task1_1.0','100_nve_modeling_task1_1.0','103_nve_modeling_task1_1.0','106_nve_modeling_task1_1.0','109_nve_modeling_task1_1.0','112_nve_modeling_task1_1.0','115_nve_modeling_task1_1.0','118_nve_modeling_task1_1.0','121_nve_modeling_task1_1.0','124_nve_modeling_task1_1.0','127_nve_modeling_task1_1.0','131_nve_modeling_task1_1.0','134_nve_modeling_task1_1.0','137_nve_modeling_task1_1.0','140_nve_modeling_task1_1.0','143_nve_modeling_task1_1.0','146_nve_modeling_task1_1.0','149_nve_modeling_task1_1.0','152_nve_modeling_task1_1.0','155_nve_modeling_task1_1.0','158_nve_modeling_task1_1.0','161_nve_modeling_task1_1.0','164_nve_modeling_task1_1.0','167_nve_modeling_task1_1.0','170_nve_modeling_task1_1.0','174_nve_modeling_task1_1.0','177_nve_modeling_task1_1.0','180_nve_modeling_task1_1.0','183_nve_modeling_task1_1.0','186_nve_modeling_task1_1.0','189_nve_modeling_task1_1.0','192_nve_modeling_task1_1.0','195_nve_modeling_task1_1.0','198_nve_modeling_task1_1.0','201_nve_modeling_task1_1.0','204_nve_modeling_task1_1.0','207_nve_modeling_task1_1.0','210_nve_modeling_task1_1.0','213_nve_modeling_task1_1.0','216_nve_modeling_task1_1.0','219_nve_modeling_task1_1.0','222_nve_modeling_task1_1.0','225_nve_modeling_task1_1.0','228_nve_modeling_task1_1.0','231_nve_modeling_task1_1.0','234_nve_modeling_task1_1.0','237_nve_modeling_task1_1.0','240_nve_modeling_task1_1.0','243_nve_modeling_task1_1.0','246_nve_modeling_task1_1.0','249_nve_modeling_task1_1.0','252_nve_modeling_task1_1.0','255_nve_modeling_task1_1.0','258_nve_modeling_task1_1.0','261_nve_modeling_task1_1.0','264_nve_modeling_task1_1.0','267_nve_modeling_task1_1.0','270_nve_modeling_task1_1.0','273_nve_modeling_task1_1.0','276_nve_modeling_task1_1.0','279_nve_modeling_task1_1.0','282_nve_modeling_task1_1.0','285_nve_modeling_task1_1.0','288_nve_modeling_task1_1.0','291_nve_modeling_task1_1.0','294_nve_modeling_task1_1.0','297_nve_modeling_task1_1.0','300_nve_modeling_task1_1.0','303_nve_modeling_task1_1.0','306_nve_modeling_task1_1.0','309_nve_modeling_task1_1.0','312_nve_modeling_task1_1.0','315_nve_modeling_task1_1.0','318_nve_modeling_task1_1.0','321_nve_modeling_task1_1.0','324_nve_modeling_task1_1.0','327_nve_modeling_task1_1.0','332_nve_modeling_task1_1.0','335_nve_modeling_task1_1.0','338_nve_modeling_task1_1.0','341_nve_modeling_task1_1.0','344_nve_modeling_task1_1.0','347_nve_modeling_task1_1.0','350_nve_modeling_task1_1.0','354_nve_modeling_task1_1.0','357_nve_modeling_task1_1.0','361_nve_modeling_task1_1.0','364_nve_modeling_task1_1.0','367_nve_modeling_task1_1.0','370_nve_modeling_task1_1.0'";
		String idsForTrainingMortgage1 = "'2051_expert_modeling_task2_1.0','2072_expert_modeling_task2_1.0','2078_expert_modeling_task2_1.0','2085_expert_modeling_task2_1.0','2088_expert_modeling_task2_1.0','22116_expert_modeling_task2_1.0','22119_expert_modeling_task2_1.0','22122_expert_modeling_task2_1.0','22125_expert_modeling_task2_1.0','22128_expert_modeling_task2_1.0','22131_expert_modeling_task2_1.0','22134_expert_modeling_task2_1.0','22140_expert_modeling_task2_1.0','22143_expert_modeling_task2_1.0','22146_expert_modeling_task2_1.0','22149_expert_modeling_task2_1.0','22152_expert_modeling_task2_1.0','22155_expert_modeling_task2_1.0','22341_expert_modeling_task2_1.0','22439_expert_modeling_task2_1.0','22442_expert_modeling_task2_1.0','22445_expert_modeling_task2_1.0','22448_expert_modeling_task2_1.0','100_modeling_process_eindhoven_2012_1.0','102_modeling_process_eindhoven_2012_1.0','104_modeling_process_eindhoven_2012_1.0','106_modeling_process_eindhoven_2012_1.0','108_modeling_process_eindhoven_2012_1.0','110_modeling_process_eindhoven_2012_1.0','112_modeling_process_eindhoven_2012_1.0','114_modeling_process_eindhoven_2012_1.0','116_modeling_process_eindhoven_2012_1.0','118_modeling_process_eindhoven_2012_1.0','11_modeling_process_eindhoven_2012_1.0','120_modeling_process_eindhoven_2012_1.0','122_modeling_process_eindhoven_2012_1.0','124_modeling_process_eindhoven_2012_1.0','126_modeling_process_eindhoven_2012_1.0','128_modeling_process_eindhoven_2012_1.0','130_modeling_process_eindhoven_2012_1.0','132_modeling_process_eindhoven_2012_1.0','134_modeling_process_eindhoven_2012_1.0','136_modeling_process_eindhoven_2012_1.0','138_modeling_process_eindhoven_2012_1.0','13_modeling_process_eindhoven_2012_1.0','140_modeling_process_eindhoven_2012_1.0','142_modeling_process_eindhoven_2012_1.0','144_modeling_process_eindhoven_2012_1.0','146_modeling_process_eindhoven_2012_1.0','148_modeling_process_eindhoven_2012_1.0','150_modeling_process_eindhoven_2012_1.0','152_modeling_process_eindhoven_2012_1.0','154_modeling_process_eindhoven_2012_1.0','156_modeling_process_eindhoven_2012_1.0','158_modeling_process_eindhoven_2012_1.0','15_modeling_process_eindhoven_2012_1.0','160_modeling_process_eindhoven_2012_1.0','162_modeling_process_eindhoven_2012_1.0','164_modeling_process_eindhoven_2012_1.0','166_modeling_process_eindhoven_2012_1.0','168_modeling_process_eindhoven_2012_1.0','170_modeling_process_eindhoven_2012_1.0','172_modeling_process_eindhoven_2012_1.0','174_modeling_process_eindhoven_2012_1.0','176_modeling_process_eindhoven_2012_1.0','178_modeling_process_eindhoven_2012_1.0','17_modeling_process_eindhoven_2012_1.0','180_modeling_process_eindhoven_2012_1.0','182_modeling_process_eindhoven_2012_1.0','184_modeling_process_eindhoven_2012_1.0','186_modeling_process_eindhoven_2012_1.0','188_modeling_process_eindhoven_2012_1.0','190_modeling_process_eindhoven_2012_1.0','192_modeling_process_eindhoven_2012_1.0','194_modeling_process_eindhoven_2012_1.0','196_modeling_process_eindhoven_2012_1.0','198_modeling_process_eindhoven_2012_1.0','19_modeling_process_eindhoven_2012_1.0','200_modeling_process_eindhoven_2012_1.0','202_modeling_process_eindhoven_2012_1.0','204_modeling_process_eindhoven_2012_1.0','206_modeling_process_eindhoven_2012_1.0','208_modeling_process_eindhoven_2012_1.0','210_modeling_process_eindhoven_2012_1.0','212_modeling_process_eindhoven_2012_1.0','214_modeling_process_eindhoven_2012_1.0','216_modeling_process_eindhoven_2012_1.0','218_modeling_process_eindhoven_2012_1.0','21_modeling_process_eindhoven_2012_1.0','220_modeling_process_eindhoven_2012_1.0','222_modeling_process_eindhoven_2012_1.0','224_modeling_process_eindhoven_2012_1.0','226_modeling_process_eindhoven_2012_1.0','228_modeling_process_eindhoven_2012_1.0','230_modeling_process_eindhoven_2012_1.0','232_modeling_process_eindhoven_2012_1.0','234_modeling_process_eindhoven_2012_1.0','236_modeling_process_eindhoven_2012_1.0','238_modeling_process_eindhoven_2012_1.0','23_modeling_process_eindhoven_2012_1.0','240_modeling_process_eindhoven_2012_1.0','242_modeling_process_eindhoven_2012_1.0','244_modeling_process_eindhoven_2012_1.0','246_modeling_process_eindhoven_2012_1.0','248_modeling_process_eindhoven_2012_1.0','250_modeling_process_eindhoven_2012_1.0','252_modeling_process_eindhoven_2012_1.0','254_modeling_process_eindhoven_2012_1.0','256_modeling_process_eindhoven_2012_1.0','258_modeling_process_eindhoven_2012_1.0','25_modeling_process_eindhoven_2012_1.0','260_modeling_process_eindhoven_2012_1.0','262_modeling_process_eindhoven_2012_1.0','264_modeling_process_eindhoven_2012_1.0','266_modeling_process_eindhoven_2012_1.0','268_modeling_process_eindhoven_2012_1.0','271_modeling_process_eindhoven_2012_1.0','27_modeling_process_eindhoven_2012_1.0','30_modeling_process_eindhoven_2012_1.0','32_modeling_process_eindhoven_2012_1.0','34_modeling_process_eindhoven_2012_1.0','36_modeling_process_eindhoven_2012_1.0','38_modeling_process_eindhoven_2012_1.0','40_modeling_process_eindhoven_2012_1.0','42_modeling_process_eindhoven_2012_1.0','44_modeling_process_eindhoven_2012_1.0','46_modeling_process_eindhoven_2012_1.0','48_modeling_process_eindhoven_2012_1.0','4_modeling_process_eindhoven_2012_1.0','50_modeling_process_eindhoven_2012_1.0','52_modeling_process_eindhoven_2012_1.0'";
		
		String setId = idsForTrainingMortgage1;
		if (task.equals("pre-flight")) {
			setId = idsForTrainingPreFlight;
		}
		
		System.out.print("Loading train/test datasets... ");
		DatabaseDataset dataset = new DatabaseDataset(connection);
		for (EXPERTISE expertise : EXPERTISE.values()) {
			dataset.addFromDatabase(true,
					Arrays.asList(task),
					null,
					Arrays.asList(expertise),
					Arrays.asList("model_id in (" + setId + ")"),
					0.3, 20000, "rand ASC");
		}
		System.out.println("OK");
		
		System.out.print("Training classifier... ");
		NeuralNetwork classifier = new NeuralNetwork();
//		SVM classifier = new SVM();
		String modelFilename = "trained-models/nn-" + task + "-" + (trainWithSmote? "smote" : "nosmote") + ".model";
		if (useAlreadyTrainedClassifier) {
			classifier.load(modelFilename);
		} else {
			classifier.train(dataset, trainWithSmote);
			classifier.save(modelFilename);
		}
		System.out.println("OK");
		
		
		int[] windowSizes = new int[] {1, 5, 10, 15};
		double[] minThresholds = new double[] {0.6, 0.75, 0.9};
		
		ResultSet resultSet = connection.createStatement().executeQuery("select distinct model_id from metrics_evolution where task = '" + task + "' and model_id not in (" + setId + ") order by rand");
		while (resultSet.next()) {
			ModelingSession session = new ModelingSession(connection);
			session.loadFromDatabase(resultSet.getString("model_id"));
			if (evaluateOverTime) {
				System.out.println(session.getModelId() + " (" + session.getSampleClass() + ")");
				for (int windowSize : windowSizes) {
					for (double minThreshold : minThresholds) {
						System.out.print(windowSize + " - " + minThreshold + "\t");
						for (double i = 0; i < 0.9; i += 0.1d) {
							double localAccuracy = classifier.computeLocalAccuracy(session, windowSize, minThreshold, i, i + 0.1d);
							System.out.print(localAccuracy + "\t");
						}
						System.out.println("");
					}
				}
			} else {
				// overall evaluation
				System.out.print(session.getModelId() + " (" + session.getSampleClass() + ")\t");
				for (double minThreshold : minThresholds) {
					System.out.print(minThreshold + "\t");
				}
				System.out.println("");
				for (int windowSize : windowSizes) {
					System.out.print(windowSize + "\t");
					for (double minThreshold : minThresholds) {
						double globalAccuracy = classifier.computeAccuracy(session, windowSize, minThreshold, 0);
						System.out.print(globalAccuracy + "\t");
					}
					System.out.println("");
				}
			}
//			System.out.println(session.getModelId() + " - " + session.getSampleClass());
//			System.out.println(classifier.computeSessionAccuracy(session, 5, 0.6));
//			classifier.exportAccuracyChart(session, windowSizes, "charts/accuracies_" + session.getModelId() + ".png");
//			classifier.exportCorrectClassificationChart(session, windowSizes , "charts/correctness_" + session.getModelId() + ".png", 0.6);
		}
		
		end();

	}

}
