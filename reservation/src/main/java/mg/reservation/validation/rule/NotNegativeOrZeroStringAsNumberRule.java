package mg.reservation.validation.rule;

import mg.reservation.util.Common;

public class NotNegativeOrZeroStringAsNumberRule extends NotNegativeOrZeroNumberRule {

	@Override
	public boolean apply(Object object) {
		Integer integer = Common.getInteger(object);		
		return super.apply(integer);
	}
	
}
