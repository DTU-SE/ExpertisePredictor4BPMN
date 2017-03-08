package moderare.expertise.model;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import moderare.expertise.exceptions.PredictorException;
import moderare.expertise.exceptions.UnknownAttribute;
import moderare.expertise.exceptions.WrongValueType;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;

public class ModelSample {

	public static final String[] classes = new String[] { "expert", "novice" };
	public static final Map<String, DATA_TYPE> attributeTypes = new HashMap<String, DATA_TYPE>();
	
	static {
		// this is the list of attributes used with the corresponding type
		attributeTypes.put("model_id", DATA_TYPE.SAMPLE_ID);
		attributeTypes.put("expertise", DATA_TYPE.CLASS);
		attributeTypes.put("percent_crossing_edges", DATA_TYPE.NUMERIC);
		attributeTypes.put("percent_orthogonal_seg", DATA_TYPE.NUMERIC);
		attributeTypes.put("mbp", DATA_TYPE.NUMERIC);
		attributeTypes.put("no_ending_points", DATA_TYPE.NUMERIC);
		attributeTypes.put("align_fragments", DATA_TYPE.NUMERIC);
		attributeTypes.put("percent_acts_aligned_frags", DATA_TYPE.NUMERIC);
		attributeTypes.put("percent_acts_not_aligned_frags", DATA_TYPE.NUMERIC);
		attributeTypes.put("no_explicit_gw", DATA_TYPE.NUMERIC);
		attributeTypes.put("no_implicit_gw", DATA_TYPE.NUMERIC);
		attributeTypes.put("no_reused_gw", DATA_TYPE.NUMERIC);
	}
	
	private Map<String, Object> attributes = new HashMap<String, Object>();

	public Instance getWekaInstance() {
		Instance wekaInstance = new DenseInstance(attributeTypes.size() - 1); // we do not wat to consider the SAMPLE_ID attribute
		for (String attribute : attributeTypes.keySet()) {
			try {
				if (attributeTypes.get(attribute) == DATA_TYPE.CLASS) {
					wekaInstance.setValue(new Attribute(attribute, Arrays.asList(classes)), getString(attribute));
				}
				if (attributeTypes.get(attribute) == DATA_TYPE.NUMERIC) {
					wekaInstance.setValue(new Attribute(attribute), getNumeric(attribute));
				}
			} catch (WrongValueType e) {
				e.printStackTrace();
			}
		}
		return wekaInstance;
	}
	
	public void setValue(String attributeName, String value) throws PredictorException {
		setTypedValue(attributeName, value);
	}
	
	public void setValue(String attributeName, Double value) throws PredictorException {
		setTypedValue(attributeName, value);
	}
	
	public String getString(String attributeName) throws WrongValueType {
		return (String) getTypedAttribute(attributeName, DATA_TYPE.SAMPLE_ID, DATA_TYPE.CLASS);
	}
	
	public Double getNumeric(String attributeName) throws WrongValueType {
		return (Double) getTypedAttribute(attributeName, DATA_TYPE.NUMERIC);
	}
	
	private void setTypedValue(String attributeName, Object value) throws PredictorException {
		DATA_TYPE expectedType = attributeTypes.get(attributeName);
		if (expectedType == null) {
			throw new UnknownAttribute();
		}
		if (EnumSet.of(DATA_TYPE.SAMPLE_ID, DATA_TYPE.CLASS).contains(expectedType) && value instanceof String) {
			attributes.put(attributeName, (String) value);
		} else if (expectedType == DATA_TYPE.NUMERIC && value instanceof Double) {
			attributes.put(attributeName, (Double) value);
		} else {
			throw new WrongValueType();
		}
	}
	
	private Object getTypedAttribute(String attributeName, DATA_TYPE expectedType, DATA_TYPE ... otherExpectedType) throws WrongValueType {
		if (EnumSet.of(expectedType, otherExpectedType).contains(attributeTypes.get(attributeName))) {
			return attributes.get(attributeName);
		}
		throw new WrongValueType();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ModelSample) {
			try {
				return getString("model_id").equals(((ModelSample) obj).getString("model_id"));
			} catch (WrongValueType e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
