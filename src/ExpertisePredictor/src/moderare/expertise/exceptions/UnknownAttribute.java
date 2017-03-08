package moderare.expertise.exceptions;

public class UnknownAttribute extends PredictorException {

	private static final long serialVersionUID = 1023346721271988317L;

	public UnknownAttribute() {
		super("The provided attribute name is unknown");
	}
}
