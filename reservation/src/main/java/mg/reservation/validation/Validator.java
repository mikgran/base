package mg.reservation.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mg.reservation.validation.rule.ValidationRule;

public class Validator {

	private Map<String, Validatable> arguments = new HashMap<String, Validatable>();

	public Validator add(String name, Object object, ValidationRule... rules) {
		
		if (name == null || rules == null) {
			throw new IllegalArgumentException(String.format("Name or rules can not be null.", name, rules));
		}
		
		if (!arguments.containsKey(name)) {

			arguments.put(name, new Validatable(object, rules));
		}

		return this;
	}

	/**
	 * Validates the contents of this validator using the designated validationRules.
	 */
	public void validate() {

		boolean raiseException = false;
		StringBuilder exceptionMessage = new StringBuilder();

		for (Map.Entry<String, Validatable> entry : arguments.entrySet()) {

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
