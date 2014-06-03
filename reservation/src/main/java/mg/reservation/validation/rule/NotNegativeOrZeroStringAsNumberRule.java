package mg.reservation.validation.rule;

import mg.reservation.util.Common;

public class NotNegativeOrZeroStringAsNumberRule extends NotNegativeOrZeroNumberRule {

	@Override
	public boolean apply(Object object) {
		Long number = Common.getLong(object);
		return super.apply(number);
	}

}
