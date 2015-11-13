package mg.angular.common;

public class DBConstants {

    public static final String ANGULARJAVA_TEST_DB_DROP = "DROP TABLE IF EXISTS contacts;";

    public static final String ANGULARJAVA_TEST_DB_CREATE = "CREATE TABLE contacts (" +
                                                            "id VARCHAR(40) NOT NULL," +
                                                            "name VARCHAR(40) NOT NULL," +
                                                            "email VARCHAR(40) NOT NULL," +
                                                            "phone VARCHAR(20) NOT NULL," +
                                                            "PRIMARY KEY(ID));";

    public static final String ANGULARJAVA_TEST_DATA_INSERT = "INSERT INTO contacts" +
                                                              "(id, name, email, phone) VALUES" +
                                                              "('name', 'name@email.com', '(111) 111-1111', );";

}
