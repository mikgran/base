package mg.reservation.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mg.reservation.validation.rule.ValidationRule;

/**
 * Notes on argument validation:
 * - validation could be done at the end: the class using the parameter.
 * 
 * - if validation is done at the end, all the code that would fail because
 * 		of missing parameter is also run.
 * 
 * - side effects of the missing (null) parameter may lead to obscure error messages:
 * 		i.e. sql exception for bad sql query. Instead a meaningful "primary key
 * 		missing" would lead the developer to the cause of the error faster.
 * 
 * - side effect of validation is that the program flow breaks before any 
 * 		resource critical method calls have been made.
 * 
 * - parameter validation should be used mainly in interfaces where user
 * 		is not the author of the interface they are calling to allow better control
 * 		and understanding of the program: i.e. service and db classes.
 * 
 * - validating parameters is considered as protective coding and some believe 
 * 		that it should be avoided as excessive and unnessecary programming style.
 * 
 * - validating parameters is thought of as a step towards more complex and less 
 * 		understandable code: i.e. consider cyclomatic complexity 
 * 		(http://en.wikipedia.org/wiki/Cyclomatic_complexity) since it produces 
 * 		one more exit point for the method it is used in.
 * 
 * - validating complies to the Clean Code 'prefer exceptions to error codes' 
 * 		best practise.
 * 
 * - extra defects may be produced with adding improper prevalidation to a program. 
 * 
 * - this class exists only to demonstrate the use of extensible validation pattern.
 * 		Use Preconditions.checkNotNull (com.google.common.base) for an alternative.
 */
public class Validator {

	private Map<String, Validatable> validatableObjects = new HashMap<String, Validatable>();

	public Validator add(String name, Object object, ValidationRule... rules) {

		if (name == null || rules == null) {
			throw new IllegalArgumentException(String.format("Name or rules can not be null.", name, rules));
		}

		if (!validatableObjects.containsKey(name)) {

			validatableObjects.put(name, new Validatable(object, rules));
		}

		return this;
	}

	/**
	 * Validates the contents of this validator using the supplied validationRules.
	 * If validation results in any of the parameters being invalid, an InvalidArgumentException 
	 * will be thrown and a message comprised of the names of each validatable and their 
	 * exception messages acquired from the validators.
	 */
	public void validate() {

		boolean raiseException = false;
		StringBuilder exceptionMessage = new StringBuilder();

		for (Map.Entry<String, Validatable> entry : validatableObjects.entrySet()) {

			Validatable argument = entry.getValue();
			List<ValidationRule> validationRules = argument.getValidationRules();

			for (ValidationRule rule : validationRules) {

				boolean validArgument = rule.apply(argument.getObject());
				if (!validArgument) {
					raiseException = true;
					if (exceptionMessage.length() > 0) {
						exceptionMessage.append(", ");
					}
					exceptionMessage.append(entry.getKey());
					exceptionMessage.append(" ");
					exceptionMessage.append(rule.getMessage());
					exceptionMessage.append(" ");
				}
			}
		}

		if (raiseException) {
			throw new IllegalArgumentException(exceptionMessage.toString().trim());
		}
	}

}