package moderare.expertise.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import moderare.expertise.utils.Pair;
import weka.core.Attribute;
import weka.core.Instances;

public class Dataset extends LinkedList<ModelSample> {

	private static final long serialVersionUID = -767491394093821049L;
	
	public Pair<Dataset, Dataset> split(double percentFirstDataset) throws Exception {
		if (percentFirstDataset < 0 || percentFirstDataset > 1) {
			throw new Exception("Percentage of first dataset must be between 0 and 1");
		}
		
		List<ModelSample> list = new ArrayList<ModelSample>(this);
		Collections.shuffle(list);
		
		Dataset first = new Dataset();
		Dataset second = new Dataset();
		
		int sizeFirstDataset = (int) ((double) percentFirstDataset * (double) size());
		for (int i = 0; i < list.size(); i++) {
			if (i < sizeFirstDataset) {
				first.add(list.get(i));
			} else {
				second.add(list.get(i));
			}
		}
		
		return new Pair<Dataset, Dataset>(first, second);
	}
	
	public Instances getWekaInstances() {
		Instances i = new Instances("DATA", new ArrayList<Attribute>(ModelSample.attributes.values()), size());
		i.setClass(ModelSample.classAttribute);
		
		for(ModelSample s : this) {
			i.add(s.getWekaInstance());
		}
		return i;
	}
}
