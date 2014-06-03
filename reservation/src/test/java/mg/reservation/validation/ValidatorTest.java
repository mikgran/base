package mg.reservation.validation;

import static mg.reservation.validation.rule.ValidationRule.NOT_NULL_OR_EMPTY_STRING;
import static mg.reservation.validation.rule.ValidationRule.NOT_NULL;
import mg.reservation.validation.rule.ValidationRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ValidatorTest {

	String ARG_1 = "arg1";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testNullValidationWithoutObject() {

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(ARG_1 + " can not be null.");

		new Validator()
				.add(ARG_1, null, NOT_NULL)
				.validate();
	}

	@Test
	public void testNotNullValidationWithObject() {

		new Validator()
				.add(ARG_1, "", NOT_NULL)
				.validate();
	}

	@Test
	public void testEmptyStringValidationWithNoContent() {

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(ARG_1 + " can not be empty or null string");

		new Validator()
				.add(ARG_1, "", NOT_NULL_OR_EMPTY_STRING)
				.validate();
	}

	@Test
	public void testEmptyStringValidationWithContent() {

		new Validator()
				.add(ARG_1, "not empty", NOT_NULL_OR_EMPTY_STRING)
				.validate();
	}

	@Test
	public void testUsingWithNullArguments() {

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Name or rules can not be null.");

		new Validator()
				.add((String) null, "not empty", (ValidationRule) null)
				.validate();

	}

}
