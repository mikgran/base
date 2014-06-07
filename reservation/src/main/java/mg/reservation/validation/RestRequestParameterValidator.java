package mg.reservation.validation;

import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestRequestParameterValidator extends Validator {

	private Logger logger = LoggerFactory.getLogger(RestRequestParameterValidator.class);
	@Override
	protected void throwException(String exceptionMessage) {
		logger.warn(exceptionMessage);
		throw new WebApplicationException(400);
	}
}
