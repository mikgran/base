package mg.reservation.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvalidArgumentsValidator {

	private Map<String, Argument> arguments = new HashMap<String, Argument>();

	public InvalidArgumentsValidator add(String key, Argument argument) {

		if (!arguments.containsKey(key)) {

			arguments.put(key, argument);
		}

		return this;
	}

	/**
	 * Validates the contents of this validator using the designated validationRules.
	 */
	public void validate() {

		boolean raiseException = false;
		StringBuilder exceptionMessage = new StringBuilder();

		for (Map.Entry<String, Argument> entry : arguments.entrySet()) {

			Argument argument = entry.getValue();
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
