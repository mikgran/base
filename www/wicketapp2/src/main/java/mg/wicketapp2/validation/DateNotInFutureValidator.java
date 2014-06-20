package mg.wicketapp2.validation;

import java.util.Date;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class DateNotInFutureValidator implements IValidator<Date> {

	private static final long serialVersionUID = -3715973856829155522L;

	@Override
	public void validate(IValidatable<Date> validatable) {

		Date date = validatable.getValue();

		if (date != null && date.getTime() > new Date().getTime()) {

			error(validatable, "notInFuture");
		}
	}

	private void error(IValidatable<Date> validatable, String errorKey) {
		ValidationError error = new ValidationError();
		error.addKey(getClass().getSimpleName() + "." + errorKey);
		validatable.error(error);
	}
}
