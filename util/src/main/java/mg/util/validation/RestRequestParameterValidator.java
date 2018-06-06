package mg.util.validation;

import javax.ws.rs.WebApplicationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RestRequestParameterValidator extends Validator {

	private Logger logger = LogManager.getLogger(RestRequestParameterValidator.class);
	@Override
	protected void throwException(String exceptionMessage) {
		logger.debug(exceptionMessage);
		throw new WebApplicationException(400);
	}
}
