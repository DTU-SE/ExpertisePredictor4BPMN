package moderare.expertise.exceptions;

import moderare.expertise.model.DATA_TYPE;

public class WrongValueType extends PredictorException {

	private static final long serialVersionUID = -8229603061770670027L;

	public WrongValueType(String attributeName, DATA_TYPE expectedType, Object value) {
		super("The value (`" + value + "`, " + value.getClass() + ") specified for the attribute (`" + attributeName + "`) does not match with the expected type (`" + expectedType + "`)");
	}
	
	public WrongValueType(String attributeName, DATA_TYPE expectedType) {
		super("The value specified for the attribute (`" + attributeName + "`) does not match with the expected type (`" + expectedType + "`)");
	}
}
