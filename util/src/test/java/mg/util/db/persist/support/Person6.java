package mg.util.db.persist.support;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.Id;
import mg.util.db.persist.annotation.OneToMany;
import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

// very cut down persons
/**
 * Don't use this test class with anything else: create your own instead.
 */
@Table(name = "persons6")
public class Person6 extends Persistable {

    @VarChar
    private String firstName = "";

    @Id
    private long id = 0;

    @VarChar
    private String lastName = "";

    @OneToMany
    private List<Todo6> todos = new ArrayList<>();

    public Person6() {
        super();
    }

    public Person6(String firstName, String lastName) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Person6(String firstName, String lastName, List<Todo6> todos) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.todos = todos;
    }

    public String getFirstName() {
        return firstName;
    }

    public long getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public List<Todo6> getTodos() {
        return todos;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setTodos(List<Todo6> todos) {
        this.todos = todos;
    }

    @Override
    public String toString() {
        return format("%s('%s', '%s', '%s', '%s')", getClass().getSimpleName(), id, firstName, lastName, todos);
    }
}
