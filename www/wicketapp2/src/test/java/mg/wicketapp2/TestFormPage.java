package mg.wicketapp2;

import mg.wicketapp2.config.WicketApplication;
import mg.wicketapp2.page.FormPage;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Simple test using the WicketTester
 */
public class TestFormPage
{
	private WicketTester tester;

	@Before
	public void setUp()
	{
		tester = new WicketTester(new WicketApplication());
	}

	@Ignore
	@Test
	public void homepageRendersSuccessfully()
	{
		// start and render the test page
		tester.startPage(FormPage.class);

		// assert rendered page class
		tester.assertRenderedPage(FormPage.class);
	}
}
