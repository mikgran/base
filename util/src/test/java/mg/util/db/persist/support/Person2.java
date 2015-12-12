package mg.util.db.persist.support;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.OneToMany;
import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

// very cut down persons
@Table(name = "persons")
public class Person2 extends Persistable {

    @VarChar
    private String firstName = "";

    @VarChar
    private String lastName = "";

    @OneToMany
    private List<Todo> todos = new ArrayList<>();
    
    public Person2() {
        super();
    }

    public Person2(String firstName, String lastName, List<Todo> todos) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.todos = todos;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public List<Todo> getTodos() {
        return todos;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setTodos(List<Todo> todos) {
        this.todos = todos;
    }

    @Override
    public String toString() {
        return format("[id: '%s', firstName: '%s', lastName: '%s', todos: '%s']", id, firstName, lastName, todos);
    }

}
