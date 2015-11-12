package mg.util.validation;

import static mg.util.validation.rule.ValidationRule.NOT_NEGATIVE_OR_ZERO_AS_STRING;

import javax.ws.rs.WebApplicationException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.util.validation.RestRequestParameterValidator;

public class RestRequestParameterValidatorTest {

	String ARG_1 = "arg1";
	String expectedWebApplicationExceptionMessage = "HTTP 400 Bad Request";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testValidationWithoutObject() {

		thrown.expect(WebApplicationException.class);
		thrown.expectMessage(expectedWebApplicationExceptionMessage);

		new RestRequestParameterValidator()
				.add(ARG_1, null, NOT_NEGATIVE_OR_ZERO_AS_STRING)
				.validate();
	}

	@Test
	public void testValidationWith1() {

		new RestRequestParameterValidator()
				.add(ARG_1, "1", NOT_NEGATIVE_OR_ZERO_AS_STRING)
				.validate();
	}

	@Test
	public void testValidationWith0() {

		thrown.expect(WebApplicationException.class);
		thrown.expectMessage(expectedWebApplicationExceptionMessage);

		new RestRequestParameterValidator()
				.add(ARG_1, "0", NOT_NEGATIVE_OR_ZERO_AS_STRING)
				.validate();
	}


}
