package mg.reservation.validation;

import java.util.ArrayList;
import java.util.List;

public class Argument {

	private String key = "";
	private Object object = null;
	private List<ValidationRule> validationRules = new ArrayList<ValidationRule>();

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public List<ValidationRule> getValidationRules() {
		return validationRules;
	}

	public void setValidationRules(List<ValidationRule> validationRules) {
		this.validationRules = validationRules;
	}

	public Argument(String key, Object object, List<ValidationRule> validationRules) {
		super();
		this.key = key;
		this.object = object;
		this.validationRules = validationRules;
	}

	public static class ArgumentBuilder {

		private String key = "";
		private Object object = null;
		private List<ValidationRule> validationRules = new ArrayList<ValidationRule>();

		public ArgumentBuilder setKey(String key) {
			if (key == null) {
				throw new IllegalArgumentException("Key can not be null.");
			}
			this.key = key;
			return this;
		}

		public ArgumentBuilder setObject(Object object) {
			this.object = object;
			return this;
		}

		public <T extends ValidationRule> ArgumentBuilder addRule(T rule) {
			if (rule == null) {
				throw new IllegalArgumentException("Rule can not be null.");
			}
			this.validationRules.add(rule);
			return this;
		}

		public Argument build() {
			return new Argument(key, object, validationRules);
		}

	}
}
