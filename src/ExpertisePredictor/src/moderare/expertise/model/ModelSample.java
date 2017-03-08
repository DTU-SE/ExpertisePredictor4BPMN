package moderare.expertise.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import weka.core.Attribute;
import weka.core.DenseInstance;

public class ModelSample {

	public static final List<String> classes = new LinkedList<String>();
	public static final Map<String, DATA_TYPE> types = new HashMap<String, DATA_TYPE>();
	public static final String classAttributeName = "expertise";
	
	static {
		// label classes
		classes.add("expert");
		classes.add("novice");
		
		// associate each attribute with corresponding type
		types.put("expertise", DATA_TYPE.STRING);
		types.put("percent_crossing_edges", DATA_TYPE.DOUBLE);
		types.put("percent_orthogonal_seg", DATA_TYPE.DOUBLE);
		types.put("mbp", DATA_TYPE.DOUBLE);
		types.put("no_ending_points", DATA_TYPE.INT);
		types.put("align_fragments", DATA_TYPE.DOUBLE);
		types.put("percent_acts_aligned_frags", DATA_TYPE.DOUBLE);
		types.put("percent_acts_not_aligned_frags", DATA_TYPE.DOUBLE);
		types.put("no_explicit_gw", DATA_TYPE.INT);
		types.put("no_implicit_gw", DATA_TYPE.INT);
		types.put("no_reused_gw", DATA_TYPE.INT);
	}
	
	private String id = null;
	private DenseInstance wekaInstance = null;
	private Map<String, Attribute> attributes = null;

	public ModelSample() {
		// construct the list of weka attributes
		this.attributes = new HashMap<String, Attribute>();
		for (String attribute : types.keySet()) {
			if (attribute.equals(classAttributeName)) {
				this.attributes.put(attribute, new Attribute(attribute, classes));
			} else {
				this.attributes.put(attribute, new Attribute(attribute));
			}
		}
		
		// construct the other attributes
		this.id = "";
		this.wekaInstance = new DenseInstance(attributes.size());
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public void setValue(String attributeName, double value) {
		wekaInstance.setValue(attributes.get(attributeName), value);
	}
	
	public void setValue(String attributeName, String value) {
		wekaInstance.setValue(attributes.get(attributeName), value);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ModelSample) {
			return (id.equals(((ModelSample) obj).id));
		}
		return false;
	}
}
