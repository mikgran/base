package mg.reservation.validation.rule;

public class NumberOfCharactersRule extends ValidationRule {

	private int charactersRequired = -1;

	public NumberOfCharactersRule() {
	}

	private NumberOfCharactersRule(int charactersRequired) {
		this.charactersRequired = charactersRequired;
	}

	@Override
	public boolean apply(Object object) {

		if (object != null && charactersRequired > 0 && object.toString().length() >= charactersRequired) {
			return true;
		}
		return false;
	}

	@Override
	public String getMessage() {
		return charactersRequired > 0 ? "must contain at least " + charactersRequired + " characters" : "no number of chars defined: returning invalidargumentexception by default";
	}

	public NumberOfCharactersRule atLeast(int charactersRequired) {
		return new NumberOfCharactersRule(charactersRequired);
	}

}
