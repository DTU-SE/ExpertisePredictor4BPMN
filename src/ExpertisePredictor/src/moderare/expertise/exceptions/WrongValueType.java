package moderare.expertise.exceptions;

public class WrongValueType extends PredictorException {

	private static final long serialVersionUID = -8229603061770670027L;

	public WrongValueType() {
		super("The value specified for the attribute does not match with the expected type");
	}
}
