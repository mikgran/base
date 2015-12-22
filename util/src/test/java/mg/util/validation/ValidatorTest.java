package mg.util.validation;

import static mg.util.validation.rule.ValidationRule.DATE_EARLIER;
import static mg.util.validation.rule.ValidationRule.NOT_NULL;
import static mg.util.validation.rule.ValidationRule.NOT_NULL_OR_EMPTY_STRING;
import static mg.util.validation.rule.ValidationRule.NUMBER_OF_CHARACTERS;

import java.util.Date;

import mg.util.validation.Validator;
import mg.util.validation.rule.ValidationRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ValidatorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    String ARG_1 = "arg1";

    @Test
    public void testDateEarlierThan() {

        Date now = new Date();
        Date after = new Date(now.getTime() + 1);

        new Validator().add(ARG_1, now, DATE_EARLIER.than(after))
                       .validate();
    }

    @Test
    public void testDateEarlierThanFail() {

        Date now = new Date();
        Date before = new Date(now.getTime() - 1);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("applied date can not be after than afterDate");

        new Validator().add(ARG_1, now, DATE_EARLIER.than(before))
                       .validate();
    }

    @Test
    public void testEmptyStringValidationWithContent() {

        new Validator().add(ARG_1, "not empty", NOT_NULL_OR_EMPTY_STRING)
                       .validate();
    }

    @Test
    public void testEmptyStringValidationWithNoContent() {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(ARG_1 + " can not be empty or null string");

        new Validator().add(ARG_1, "", NOT_NULL_OR_EMPTY_STRING)
                       .validate();
    }

    @Test
    public void testNotNullValidationWithObject() {

        new Validator().add(ARG_1, "", NOT_NULL)
                       .validate();

        Validator.validateNotNull(ARG_1, "");
    }

    @Test
    public void testNullValidationWithoutObject() {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(ARG_1 + " can not be null.");

        new Validator().add(ARG_1, null, NOT_NULL)
                       .validate();
    }

    @Test
    public void testNullValidationWithoutObject2() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(ARG_1 + " can not be null.");

        Validator.validateNotNull(ARG_1, null);
    }

    @Test
    public void testNullValidationWithoutObject3() {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(ARG_1 + " can not be null.");

        Validator.of(ARG_1, null, NOT_NULL)
                 .validate();
    }

    @Test
    public void testNumberOfCharactersRule() {
        new Validator().add(ARG_1, "cccc", NUMBER_OF_CHARACTERS.atLeast(4))
                       .validate();
    }

    @Test
    public void testNumberOfCharactersRuleFail() {
        int expectedCharacters = 5;

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("must contain at least " + expectedCharacters + " characters");

        new Validator().add(ARG_1, "cccc", NUMBER_OF_CHARACTERS.atLeast(expectedCharacters))
                       .validate();
    }

    @Test
    public void testUsingWithNullArguments() {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Name or rules can not be null.");

        new Validator().add((String) null, "not empty", (ValidationRule) null)
                       .validate();

    }
}
