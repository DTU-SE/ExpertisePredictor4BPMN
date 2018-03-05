package moderare.expertise.apps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import moderare.expertise.classifier.NeuralNetwork;
import moderare.expertise.model.DatabaseDataset;
import moderare.expertise.model.Dataset;
import moderare.expertise.model.EXPERTISE;
import moderare.expertise.model.ModelSample;
import moderare.expertise.model.ModelingSession;
import moderare.expertise.utils.DatabaseUtils;
import moderare.expertise.utils.Pair;

public class ValidateOverTime extends ExpertisePredictor4BPMN {
	
	private static int getBin(double relative_time, int bins) {
		return (int) (Math.floor(relative_time * bins));
	}
	
	public static NeuralNetwork trainClassifier(String[] tasks, int trainingSize) {
		
		System.out.print("Loading training dataset... ");
		DatabaseDataset dataset = new DatabaseDataset(connection);
		for (String task : tasks){
			for (EXPERTISE expertise : EXPERTISE.values()) {
				dataset.addFromDatabase(true,
						Arrays.asList(task),
						null,
						Arrays.asList(expertise), 0.3, trainingSize, "rand ASC");
			}
		}
		System.out.println("OK");
		
		System.out.print("Training classifier... ");
		NeuralNetwork classifier = new NeuralNetwork();
		classifier.train(dataset, false);
		System.out.println("OK");
		
		return classifier;
	}
	
	private static StringBuffer prepareCSV_expertise_over_time (double from, double to, double step_size) {
		StringBuffer sb = new StringBuffer();
		sb.append("model_id");
		sb.append(",taskname");
		sb.append(",class");
		
		
		double time_rel = from;
		while (time_rel < to) {
			sb.append(","+time_rel);
			time_rel += step_size;
		}
		sb.append(","+to);
		sb.append(",stable from");
		sb.append("\n");
		return sb;
	}
	
	public static void main(String[] args) throws Exception {
		startup();
		
		boolean useAlreadyTrainedClassifier = true;
		
		String train_tasks[] = new String[] { "mortgage-1" }; 
		String validate_tasks[] = new String[] { "pre-flight" };
		
		String train_taskString = "";
		for (String task : train_tasks) train_taskString += task+"_";
		int train_size = 8000;
		
		String validate_taskString = "";
		for (String task : validate_tasks) validate_taskString += task+"_";
		
		NeuralNetwork classifier;
		String modelFilename = "trained-models/"+train_taskString+train_size+"_nn.model";
		File modelFile = new File(modelFilename);
		if (useAlreadyTrainedClassifier && modelFile.exists()) {
			classifier = new NeuralNetwork();
			classifier.load(modelFilename);
		} else {
			classifier = trainClassifier(train_tasks, 8000);
			classifier.save(modelFilename);
		}
		
		List<String> model_ids = new ArrayList<String>();

		// get the names of all the modeling sessions
		for (String task : validate_tasks) {
			String query = "select distinct model_id from metrics_evolution where task='"+task+"'";
			ResultSet resultSet = connection.createStatement().executeQuery(query);
			while (resultSet.next()) {
				model_ids.add(resultSet.getString("model_id"));
			}
		}
		
		int total_time_steps = 20; // number of time steps from 0 to 1
		double time_from = 0.3;
		double time_to = 1.0;
		StringBuffer csv_exp_over_time = prepareCSV_expertise_over_time(time_from, time_to, 1/(double)total_time_steps);

		// plot each modeling session over time wrt. classifiers
		for (String model_id : model_ids) {
			ModelingSession session = new ModelingSession(connection);
			session.loadFromDatabase(model_id);
			System.out.println(model_id);
			
			double expert_count[] = new double[total_time_steps+1];
			double total_count[] = new double[total_time_steps+1];
			
			for (ModelSample m : session) {
				EXPERTISE e = classifier.classifyInstance(m);
				int e_count = (e == EXPERTISE.EXPERT) ? 1 : 0;
				Double time = m.getNumeric("relative_modeling_time");
				expert_count[getBin(time, total_time_steps)] += e_count;
				total_count[getBin(time, total_time_steps)] += 1;
			}
			
			// smoothen and normalize the expert counts wrt. total counts 
			for (int i=0; i<expert_count.length; i++) {
				// copy from previous values if no data in current bin
				if (total_count[i] == 0) {
					if (i == 0) {
						expert_count[i] = 0;
					} else {
						expert_count[i] = expert_count[i-1];
					}
				} else {
					expert_count[i] = expert_count[i]/total_count[i];
				}
			}

			csv_exp_over_time.append(model_id);
			csv_exp_over_time.append(","+session.getTaskName());
			csv_exp_over_time.append(","+session.getSampleClass());
			double one_since = -1;
			for (int i=0; i<expert_count.length; i++) {
				if (i/(double)total_time_steps >= time_from && i/(double)total_time_steps <= time_to)
					csv_exp_over_time.append(","+expert_count[i]);
				
				if (expert_count[i] == 1 && one_since == -1) {
					one_since = i/(double)total_time_steps;
				} else if (one_since != -1 && expert_count[i] != 1) {
					one_since = -1;
				}
			}
			csv_exp_over_time.append(","+one_since);
			csv_exp_over_time.append("\n");
		}
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("charts/T"+train_taskString+"V"+validate_taskString+"over_time.csv"));
			writer.append(csv_exp_over_time);
		} finally {
			writer.close();
		}
	}

}
