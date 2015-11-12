package mg.util.validation;

import java.util.ArrayList;
import java.util.List;

import mg.util.validation.rule.ValidationRule;

/**
 * Value and validation rules container class. Used to validate the contents of an object.
 */
public class Validatable {

	private Object object = null;
	private List<ValidationRule> validationRules = new ArrayList<ValidationRule>();

	public Validatable(Object object, ValidationRule... rules) {

		this.object = object;

		if (rules != null && rules.length > 0) {

			for (ValidationRule validationRule : rules) {
				validationRules.add(validationRule);
			}
		}
	}

	public List<ValidationRule> getValidationRules() {
		return validationRules;
	}

	public Object getObject() {
		return object;
	}
}
