package mg.util.db.persist.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.util.db.persist.support.Contact;
import mg.util.db.persist.support.Contact5;

// TOIMPROVE: test coverage: unannotated fields cases.
// TOIMPROVE: test coverage: other type cases.
public class FieldBuilderFactoryTest {

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String NAME_X = "nameX";
    private static final String NAME_X_MAIL_COM = "nameX@mail.com";
    private static final String NEW_NAME_X = "newNameX";
    private static final String PHONE_555_555_5555 = "(555) 555-5555";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // mvn -DfailIfNoTests=false -Dtest=FieldBuilderFactoryTest test
    @Test
    public void testGetValue() throws Exception {

        Contact contact = new Contact(1, NAME_X, NAME_X_MAIL_COM, PHONE_555_555_5555);

        List<FieldBuilder> fieldBuilders;
        fieldBuilders = Arrays.stream(contact.getClass().getDeclaredFields())
                              .map(declaredField -> FieldBuilderFactory.of(contact, declaredField))
                              .filter(fieldBuilder -> fieldBuilder.isDbField())
                              .collect(Collectors.toList());

        FieldBuilder nameField;
        nameField = fieldBuilders.stream()
                                 .filter(fieldBuilder -> NAME.equals(fieldBuilder.getName()))
                                 .findFirst()
                                 .get();

        assertTrue("field should be an instance of: ", nameField instanceof VarCharBuilder);
        assertEquals("field should have the name: ", NAME, nameField.getName());
        assertEquals("field value should be: ", NAME_X, nameField.getFieldValue(contact));

        Object fieldValue = nameField.getFieldValue(contact);
        assertNotNull(fieldValue);
        assertTrue(fieldValue instanceof String);
        assertEquals("fieldValue should be: ", NAME_X, fieldValue);
    }

    @Test
    public void testSetValueNumerics() throws Exception {

        Contact contact2 = new Contact(1, NAME_X, NAME_X_MAIL_COM, PHONE_555_555_5555);
        Contact contact3 = new Contact();
        contact3.setName(NAME_X);
        contact3.setEmail(NAME_X_MAIL_COM);
        contact3.setPhone(PHONE_555_555_5555);

        List<FieldBuilder> fieldBuilders;
        fieldBuilders = Arrays.stream(contact2.getClass().getDeclaredFields())
                              .map(declaredField -> FieldBuilderFactory.of(contact2, declaredField))
                              .filter(fieldBuilder -> fieldBuilder.isDbField())
                              .collect(Collectors.toList());

        FieldBuilder idField;
        idField = fieldBuilders.stream()
                               .filter(fieldBuilder -> ID.equals(fieldBuilder.getName()))
                               .findFirst()
                               .get();

        Object fieldValue = idField.getFieldValue(contact2);
        assertNotNull(fieldValue);
        assertEquals("fieldValue should have type:", Long.class, fieldValue.getClass());
        assertEquals("fieldValue should be: ", Long.valueOf(1L), fieldValue);

        idField.setFieldValue(contact3, 444L);

        fieldValue = idField.getFieldValue(contact3);
        assertNotNull(fieldValue);
        assertEquals("fieldValue should have type:", Long.class, fieldValue.getClass());
        assertEquals("fieldValue should be: ", Long.valueOf(444L), fieldValue);
    }

    @Test
    public void testSetValueNumericsNull() throws Exception {

        Contact5 contact5 = new Contact5(null, NAME_X, NAME_X_MAIL_COM, PHONE_555_555_5555);

        List<FieldBuilder> fieldBuilders;
        fieldBuilders = Arrays.stream(contact5.getClass().getDeclaredFields())
                              .map(declaredField -> FieldBuilderFactory.of(contact5, declaredField))
                              .filter(fieldBuilder -> fieldBuilder.isDbField())
                              .collect(Collectors.toList());

        FieldBuilder idField;
        idField = fieldBuilders.stream()
                               .filter(fieldBuilder -> ID.equals(fieldBuilder.getName()))
                               .findFirst()
                               .get();

        Object fieldValue = idField.getFieldValue(contact5);
        assertNull(fieldValue);

        // case null id in object
        idField.setFieldValue(contact5, 444L);

        fieldValue = idField.getFieldValue(contact5);
        assertNotNull(fieldValue);
        assertEquals("fieldValue should have type:", Long.class, fieldValue.getClass());
        assertEquals("fieldValue should be: ", Long.valueOf(444L), fieldValue);

        System.out.println("contact:: " + contact5);
        idField.setFieldValue(contact5, "");
        System.out.println("contact:: " + contact5);
    }

    @Test
    public void testSetValueString() throws Exception {

        Contact contact2 = new Contact(1, NAME_X, NAME_X_MAIL_COM, PHONE_555_555_5555);

        List<FieldBuilder> fieldBuilders;
        fieldBuilders = Arrays.stream(contact2.getClass().getDeclaredFields())
                              .map(declaredField -> FieldBuilderFactory.of(contact2, declaredField))
                              .filter(fieldBuilder -> fieldBuilder.isDbField())
                              .collect(Collectors.toList());

        FieldBuilder nameField;
        nameField = fieldBuilders.stream()
                                 .filter(fieldBuilder -> NAME.equals(fieldBuilder.getName()))
                                 .findFirst()
                                 .get();

        assertTrue("field should be an instance of: ", nameField instanceof VarCharBuilder);
        assertEquals("field name should be: ", NAME, nameField.getName());
        assertEquals("field value should be: ", NAME_X, nameField.getFieldValue(contact2));

        nameField.setFieldValue(contact2, NEW_NAME_X);

        assertNotNull("field value should not be null after setting it.", nameField.getFieldValue(contact2));
        assertEquals("field value after setting it should be: ", NEW_NAME_X, contact2.getName());

    }

}
