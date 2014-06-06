package mg.reservation.validation.rule;

import mg.reservation.util.Common;

/**
 * Tests whether an applied object is equal or higher than one.
 * Object is assumed to be number as String. 
 */
public class NotNegativeOrZeroStringAsNumberRule extends NotNegativeOrZeroNumberRule {

	@Override
	public boolean apply(Object object) {
		Long number = Common.getLong(object);
		return super.apply(number);
	}

}
