package moderare.expertise;

import java.awt.BasicStroke;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import moderare.expertise.utils.expertise.Pair;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.SerializationHelper;

import com.mysql.cj.jdbc.Driver;

public class Classify {
	
	enum TYPE {
		STRING, DOUBLE, INT
	}
	
	public static Connection connection = null;
	public static String LABEL_ATTRIBUTE_NAME = "expertise";
	public static List<String> expertiseLevels = new LinkedList<String>();
	public static Map<String, TYPE> types = new HashMap<String, Classify.TYPE>();
	public static Map<String, Attribute> attributes = new HashMap<String, Attribute>();

	public static void main(String[] args) throws Exception {
		DriverManager.registerDriver(new Driver());
		Properties properties = new Properties();
		properties.put("user", "test");
		properties.put("password", "test");
		properties.put("connectTimeout", "500");
		connection = DriverManager.getConnection("jdbc:mysql://localhost/visual-data", properties);
		
		// label target
		expertiseLevels.add("expert");
		expertiseLevels.add("novice");
		
		// associate each attribute with corresponding type
		types.put(LABEL_ATTRIBUTE_NAME, TYPE.STRING);
		types.put("percent_crossing_edges", TYPE.DOUBLE);
		types.put("percent_orthogonal_seg", TYPE.DOUBLE);
		types.put("mbp", TYPE.DOUBLE);
		types.put("no_ending_points", TYPE.INT);
		types.put("align_fragments", TYPE.DOUBLE);
		types.put("percent_acts_aligned_frags", TYPE.DOUBLE);
		types.put("percent_acts_not_aligned_frags", TYPE.DOUBLE);
		types.put("no_explicit_gw", TYPE.INT);
		types.put("no_implicit_gw", TYPE.INT);
		types.put("no_reused_gw", TYPE.INT);
		
		// construct the list of weka attributes
		for (String attribute : types.keySet()) {
			if (attribute.equals(LABEL_ATTRIBUTE_NAME)) {
				attributes.put(attribute, new Attribute(attribute, expertiseLevels));
			} else {
				attributes.put(attribute, new Attribute(attribute));
			}
		}
		
		// ---------------------------------------------------------------------
		System.out.print("Building datasets... ");
		int trainTestSamplePerClass = 5000;
		int validationSamplePerClass = 10;
		
		// datasets for validation
		Map<String, String> validation = new HashMap<String, String>();
		ResultSet rs = connection.createStatement().executeQuery(""
				+ "(select distinct model_id, expertise from metrics_evolution where expertise = \"expert\" and task = \"mortgage-1\" order by rand limit " + validationSamplePerClass/2 + ") union "
				+ "(select distinct model_id, expertise from metrics_evolution where expertise = \"expert\" and task = \"pre-flight\" order by rand limit " + validationSamplePerClass/2 + ") union "
				+ "(select distinct model_id, expertise from metrics_evolution where expertise = \"novice\" and task = \"mortgage-1\" order by rand limit " + validationSamplePerClass/2 + ") union "
				+ "(select distinct model_id, expertise from metrics_evolution where expertise = \"novice\" and task = \"pre-flight\" order by rand limit " + validationSamplePerClass/2 + ")");
		while(rs.next()) {
			validation.put(rs.getString("model_id"), rs.getString("expertise"));
		}
//		validation.clear();
//		validation.put("22469", "expert");
//		validation.put("22475", "expert");
//		validation.put("60", "novice");
//		validation.put("94", "novice");
		
		// construct the training/test datasets
		Instances instances = createDatasets(trainTestSamplePerClass, validation.keySet(), new String[] { "mortgage-1"/*, "pre-flight"*/ });
		Pair<Instances, Instances> trainTestSets = splitTrainingAnsTestSet(instances, 80, true);
		System.out.println("Done!");
		System.out.println("Data created (" + instances.size() + " instances, " + trainTestSets.getFirst().size() + " for training, " + trainTestSets.getSecond().size() + " for test)");
		
		
		// ---------------------------------------------------------------------
		// build the classifier
		String modelFileName = "C:\\Users\\Andrea\\Desktop\\classification.model";
		System.out.print("Loading model... ");
		AbstractClassifier classifier = (AbstractClassifier) SerializationHelper.read(modelFileName);
		
//		System.out.print("Building classifier... ");
////		AbstractClassifier classifier = createClassifier_SVM(trainTestSets.getFirst());
//		AbstractClassifier classifier = createClassifier_NN(trainTestSets.getFirst());
//		SerializationHelper.write(modelFileName, classifier);
//		System.out.println("Done!");
		
		
		// ---------------------------------------------------------------------
		// evaluate the classifier
		System.out.print("Evaluating model... ");
		Evaluation eval = new Evaluation(trainTestSets.getFirst());
		eval.evaluateModel(classifier, trainTestSets.getSecond());
		System.out.println("Done!");
		System.out.println(eval.toSummaryString());
		System.out.println(eval.toMatrixString());

		
		// ---------------------------------------------------------------------
		// evaluate the classifier on different modeling task
		System.out.print("Evaluating model on a different modeling task... ");
		ResultSet rs2 = connection.createStatement().executeQuery("(select * from metrics_evolution where task = 'mortgage-2' order by rand() limit 5000)");
		Instances instances2 = new Instances("DATA", new ArrayList<Attribute>(attributes.values()), 5000);
		instances2.setClass(attributes.get("expertise"));
		populateInstances(instances2, rs2);
		Evaluation eval2 = new Evaluation(instances2);
		eval2.evaluateModel(classifier, instances2);
		System.out.println("Done!");
		System.out.println(eval2.toSummaryString());
		System.out.println(eval2.toMatrixString());
		
		
		// ---------------------------------------------------------------------
		System.out.println("Evaluating instances");
		List<String> modelIds = new LinkedList<String>(validation.keySet());
		Collections.sort(modelIds);
		for(String model_id : modelIds) {
			String query = "select * from metrics_evolution where model_id = \"" + model_id + "\" order by modeling_time";
//			processOnePPM_window(classifier, connection.createStatement().executeQuery(query), process_id, validation.get(process_id), 20);
//			processOnePPM_finalScore(classifier, connection.createStatement().executeQuery(query), process_id, validation.get(process_id));
//			processOnePPM_finalScore_window(classifier, connection.createStatement().executeQuery(query), new int[]{5, 20, 40}, 0.6);
//			processOnePPM_plot(classifier, connection.createStatement().executeQuery(query), new int[]{5, 20, 40},
//				new File("C:\\Users\\Andrea\\Desktop\\charts\\absolute_" + model_id + ".png"),
//				new File("C:\\Users\\Andrea\\Desktop\\charts\\correct_" + model_id + ".png"), 0.6);
		}
		
		
		// ---------------------------------------------------------------------
		// final stuff
		connection.close();
	}
	
	public static void processOnePPM_finalScore(AbstractClassifier classifier, ResultSet rs, String process_id, String expertise) throws Exception {
		System.out.print("Evaluating " + process_id + " (" + expertise + ")... ");
		
		Instances dataUnlabeled = new Instances("new-dataset", new ArrayList<Attribute>(attributes.values()), 1);
		dataUnlabeled.setClass(attributes.get(LABEL_ATTRIBUTE_NAME));
		int total = 0;
		int correct = 0;
		while(rs.next()) {
			Instance i = createInstance(rs);
			i.setDataset(dataUnlabeled);
			String predict = dataUnlabeled.classAttribute().value((int) classifier.classifyInstance(i));
			if (predict.equals(rs.getString("expertise"))) {
				correct++;
			}
			total++;
		}
		System.out.println(Math.round((double)correct / total * 100.0) + "% (" + correct + " out of " + total + ")");
	}
	
	public static void processOnePPM_window(AbstractClassifier classifier, ResultSet rs, String process_id, String expertise, int windowSize) throws SQLException, Exception {
		System.out.println("Evaluating " + process_id + " (which is " + expertise + ")... ");
		
		List<Double> relativeTimestamps = new ArrayList<Double>();
		Map<Double, String> predictions = new HashMap<Double, String>();
		
		Instances dataUnlabeled = new Instances("new-dataset", new ArrayList<Attribute>(attributes.values()), 1);
		dataUnlabeled.setClass(attributes.get(LABEL_ATTRIBUTE_NAME));
		while(rs.next()) {
			Instance i = createInstance(rs);
			i.setDataset(dataUnlabeled);
			Double relativeTime = rs.getDouble("relative_modeling_time");
			relativeTimestamps.add(relativeTime);
			predictions.put(relativeTime, dataUnlabeled.classAttribute().value((int) classifier.classifyInstance(i)));
		}
		
		for(int i = 0; i < relativeTimestamps.size(); i++) {
			Double time = relativeTimestamps.get(i);
			System.out.print(time + "\t");
			int correctInWindow = 0;
			if (i > windowSize) {
				for (int j = i; j > i - windowSize; j--) {
					if (predictions.get(relativeTimestamps.get(j)).equals(expertise)) {
						correctInWindow++;
					}
				}
				System.out.print(correctInWindow + "\t" + windowSize);
			}
			System.out.println("");
		}
		System.out.println("");
	}
	
	public static void processOnePPM_plot(AbstractClassifier classifier, ResultSet rs, int[] windowSizes, File targetAbsolute, File targetCorrect, double minSupport) throws SQLException, Exception {
		rs.next();
		String model_id = rs.getString("model_id");
		String expertise = rs.getString("expertise");
		String task = rs.getString("task");
		
		System.out.print("Evaluating " + model_id + " (which is " + expertise + ")... ");
		
		List<Double> relativeTimestamps = new ArrayList<Double>();
		Map<Double, String> predictions = new HashMap<Double, String>();
		
		Instances dataUnlabeled = new Instances("new-dataset", new ArrayList<Attribute>(attributes.values()), 1);
		dataUnlabeled.setClass(attributes.get(LABEL_ATTRIBUTE_NAME));
		do {
			Instance i = createInstance(rs);
			i.setDataset(dataUnlabeled);
			Double relativeTime = rs.getDouble("relative_modeling_time");
			relativeTimestamps.add(relativeTime);
			predictions.put(relativeTime, dataUnlabeled.classAttribute().value((int) classifier.classifyInstance(i)));
		} while(rs.next());
		
		
		// absolute values charts
		if (targetAbsolute != null) {
			XYSeriesCollection dataset = new XYSeriesCollection();
			for (int windowSize : windowSizes) {
				XYSeries series = new XYSeries("Window size " + windowSize, true);
				for(int i = 0; i < relativeTimestamps.size(); i++) {
					Double time = relativeTimestamps.get(i);
					int correctInWindow = 0;
					if (i > windowSize) {
						for (int j = i; j > i - windowSize; j--) {
							if (predictions.get(relativeTimestamps.get(j)).equals(expertise)) {
								correctInWindow++;
							}
						}
						series.add((double) time, ((double) correctInWindow / (double) windowSize));
					}
				}
			    dataset.addSeries(series);
			}
			JFreeChart chart = ChartFactory.createScatterPlot(model_id + " (" + expertise + ", " + task + ")",
					"Relative time", // x axis label
					"Correctness ratio", // y axis label
					dataset, // data
					PlotOrientation.VERTICAL, //
					true, // include legend
					true, // tooltips
					false // urls
					);
			XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
			for (int i = 0; i < windowSizes.length; i++) {
				renderer.setSeriesLinesVisible(i, true);
				renderer.setSeriesShapesVisible(i, false);
				renderer.setSeriesStroke(i, new BasicStroke(3f));
			}
			XYPlot plot = (XYPlot) chart.getPlot();
			plot.setRenderer(renderer);
			plot.getRangeAxis().setRange(0.0, 1.0);
	
			ChartUtilities.saveChartAsPNG(targetAbsolute, chart, 800, 600);
		}
		
		// correct classification chart
		if (targetCorrect != null) {
			CombinedDomainXYPlot plot2 = new CombinedDomainXYPlot(new NumberAxis("Relative time"));
			for (int windowSize : windowSizes) {
				XYSeriesCollection dataset2 = new XYSeriesCollection();
				XYSeries series = new XYSeries("Window size " + windowSize, true);
				for(int i = 0; i < relativeTimestamps.size(); i++) {
					Double time = relativeTimestamps.get(i);
					int correctInWindow = 0;
					if (i > windowSize) {
						for (int j = i; j > i - windowSize; j--) {
							if (predictions.get(relativeTimestamps.get(j)).equals(expertise)) {
								correctInWindow++;
							}
						}
						double correct = 0;
						if (((double) correctInWindow / (double) windowSize) >= minSupport) {
							correct = 1;
						}
						series.add((double) time, correct);
					}
				}
				dataset2.addSeries(series);
				XYPlot subplot = new XYPlot(dataset2, new NumberAxis(), new NumberAxis("Parts orrectly classified"), new XYAreaRenderer());
				subplot.getRangeAxis().setRange(0.0, 1.0);
				plot2.add(subplot);
			}
			JFreeChart chart2 = new JFreeChart(model_id + " (" + expertise + ", " + task + ")", JFreeChart.DEFAULT_TITLE_FONT, plot2, true);
			ChartUtilities.saveChartAsPNG(targetCorrect, chart2, 800, 600);
		}
		System.out.println("Done!");
	}
	
	public static void processOnePPM_finalScore_window(AbstractClassifier classifier, ResultSet rs, int[] windowSizes, double minSupport) throws SQLException, Exception {
		rs.next();
		String process_id = rs.getString("process_id");
		String expertise = rs.getString("expertise");
		String task = rs.getString("task");
		
		System.out.println("Evaluating " + process_id + " (" + expertise + ", "+ task +")");
		
		List<Double> relativeTimestamps = new ArrayList<Double>();
		Map<Double, String> predictions = new HashMap<Double, String>();
		
		Instances dataUnlabeled = new Instances("new-dataset", new ArrayList<Attribute>(attributes.values()), 1);
		dataUnlabeled.setClass(attributes.get(LABEL_ATTRIBUTE_NAME));
		do {
			Instance i = createInstance(rs);
			i.setDataset(dataUnlabeled);
			Double relativeTime = rs.getDouble("relative_modeling_time");
			relativeTimestamps.add(relativeTime);
			predictions.put(relativeTime, dataUnlabeled.classAttribute().value((int) classifier.classifyInstance(i)));
		} while(rs.next());
		
		for (int windowSize : windowSizes) {
			int total= 0;
			int correct = 0;
			for(int i = 0; i < relativeTimestamps.size(); i++) {
				int correctInWindow = 0;
				if (i > windowSize) {
					for (int j = i; j > i - windowSize; j--) {
						if (predictions.get(relativeTimestamps.get(j)).equals(expertise)) {
							correctInWindow++;
						}
					}
					if (((double) correctInWindow / (double) windowSize) >= minSupport) {
						correct++;
					}
					total++;
				}
			}
			System.out.println("   w = " + windowSize + " -> " + Math.round((double)correct / total * 100.0) + "% (" + correct + "/" + total + ")");
		}
		System.out.println("");
	}
	
	private static Instances createDatasets(int samplePerClass, Set<String> processIdsToAvoid, String[] tasks) throws SQLException {
		Instances instances = new Instances("DATA", new ArrayList<Attribute>(attributes.values()), samplePerClass*2);
		instances.setClass(attributes.get("expertise"));
		
		for (String task : tasks) {
			populateInstances(instances,
					connection.createStatement().executeQuery("select * from metrics_evolution where "
							+ "(task = \""+ task +"\") and "
							+ "(relative_modeling_time >= 0.3) and "
							+ "(percent_crossing_edges is not null) and "
							+ "(percent_orthogonal_seg is not null) and "
							+ "(mbp is not null) and "
							+ "(no_ending_points is not null) and "
							+ "(align_fragments is not null) and "
							+ "(percent_acts_aligned_frags is not null) and "
							+ "(percent_acts_not_aligned_frags is not null) and "
							+ "(no_explicit_gw is not null) and "
							+ "(no_implicit_gw is not null) and "
							+ "(no_reused_gw is not null) and "
							+ "(expertise = \"expert\") "
							+ "order by rand limit "+ (samplePerClass / tasks.length)));
			populateInstances(instances,
					connection.createStatement().executeQuery("select * from metrics_evolution where "
							+ "(task = \""+ task +"\") and "
							+ "(relative_modeling_time >= 0.3) and "
							+ "(percent_crossing_edges is not null) and "
							+ "(percent_orthogonal_seg is not null) and "
							+ "(mbp is not null) and "
							+ "(no_ending_points is not null) and "
							+ "(align_fragments is not null) and "
							+ "(percent_acts_aligned_frags is not null) and "
							+ "(percent_acts_not_aligned_frags is not null) and "
							+ "(no_explicit_gw is not null) and "
							+ "(no_implicit_gw is not null) and "
							+ "(no_reused_gw is not null) and "
							+ "(expertise = \"novice\") "
							+ "order by rand limit "+ (samplePerClass / tasks.length)));
		}
		return instances;
	}
	
	private static Instance createInstance(ResultSet resultSet) throws SQLException {
		Instance i = new DenseInstance(attributes.size());
		for (String attribute : attributes.keySet()) {
			if (types.get(attribute) == TYPE.STRING) {
				i.setValue(attributes.get(attribute), resultSet.getString(attribute));
			} else if (types.get(attribute) == TYPE.DOUBLE) {
				i.setValue(attributes.get(attribute), resultSet.getDouble(attribute));
			} else if (types.get(attribute) == TYPE.INT) {
				i.setValue(attributes.get(attribute), resultSet.getInt(attribute));
			}
		}
		return i;
	}
	
	private static void populateInstances(Instances instances, ResultSet resultSet) throws SQLException {
		while (resultSet.next()) {
			instances.add(createInstance(resultSet));
		}
	}
	
	private static AbstractClassifier createClassifier_SVM(Instances instances) throws Exception {
		
		PolyKernel kernel = new PolyKernel();
		kernel.setExponent(1);
		
		SMO classifier = new SMO();
		classifier.setDebug(false);
		classifier.setC(1.0);
		classifier.setFilterType(new SelectedTag(SMO.FILTER_NORMALIZE, SMO.TAGS_FILTER));
		classifier.setToleranceParameter(0.001);
		classifier.setEpsilon(1.0e-12);
		classifier.setBuildCalibrationModels(false);
		classifier.setNumFolds(-1);
		classifier.setRandomSeed(1);
		classifier.setKernel(kernel);
		
		classifier.buildClassifier(instances);
		
		return classifier;
	}

	private static AbstractClassifier createClassifier_NN(Instances instances) throws Exception {
	
		MultilayerPerceptron classifier = new MultilayerPerceptron();
		classifier.setTrainingTime(500);
		classifier.setLearningRate(0.3);
		classifier.setMomentum(0.2);
		classifier.setHiddenLayers("50"); // one hidden layer with 25 neurons
		
		classifier.buildClassifier(instances);
		
		return classifier;
	}

	private static Pair<Instances, Instances> splitTrainingAnsTestSet(Instances instances, int trainingPerc, boolean random) {
		
		int trainingSize = (int) Math.ceil(instances.size() / 2);
		if (trainingPerc > 0 || trainingPerc <= 100) {
			trainingSize = (int) (((double)trainingPerc / 100.0) * (double) instances.size());
		}
		
		int testSize = instances.size() - trainingSize;
		
		Instances training = new Instances(instances, trainingSize);
		Instances test = new Instances(instances, testSize);
		
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < instances.size(); ++i) {
			ids.add(i);
		}
		
		if (random) {
			java.util.Collections.shuffle(ids);
		} 
		
		for (int i = 0; i < trainingSize; ++i) {
			training.add(instances.get(ids.get(i)));
		}
		
		for (int i = trainingSize; i < instances.size(); ++i) {
			test.add(instances.get(ids.get(i)));
		}
		
		return new Pair<Instances, Instances>(training, test);
	}
}
