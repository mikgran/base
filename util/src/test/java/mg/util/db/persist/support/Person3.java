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
@Table(name = "persons3")
public class Person3 extends Persistable {

    @VarChar
    private String firstName = "";

    @Id
    private int id = 0;

    @VarChar
    private String lastName = "";

    @OneToMany
    private List<Todo2> todos = new ArrayList<>();

    public Person3() {
        super();
    }

    public Person3(String firstName, String lastName, List<Todo2> todos) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.todos = todos;
    }

    public String getFirstName() {
        return firstName;
    }

    public int getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public List<Todo2> getTodos() {
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

    public void setTodos(List<Todo2> todos) {
        this.todos = todos;
    }

    @Override
    public String toString() {
        return format("[id: '%s', firstName: '%s', lastName: '%s', todos: '%s']", id, firstName, lastName, todos);
    }
}
