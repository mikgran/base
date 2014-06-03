package mg.reservation.validation;

import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestRequestValidator extends Validator {

	private Logger logger = LoggerFactory.getLogger(RestRequestValidator.class);
	
	@Override
	protected void throwException(String exceptionMessage) {
		logger.warn(exceptionMessage);
		throw new WebApplicationException(400); // toimprove: add message and handing for the client. 		
	}
}
