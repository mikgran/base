package mg.util.db.persist.support;

import static java.lang.String.format;
import static mg.util.validation.Validator.validateNotNull;

import java.util.List;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.ForeignKey;
import mg.util.db.persist.annotation.OneToMany;
import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

@Table(name = "todos")
public class Todo extends Persistable {

    @OneToMany
    private List<Location> locations;

    @ForeignKey(references = "persons")
    private int personsId = 0;

    @VarChar
    private String todo = "";

    public Todo() {
    }

    public Todo(String todo, List<Location> locations) {
        this.locations = locations;
        this.todo = todo;
    }

    public int getPersonsId() {
        return personsId;
    }

    public String getTodo() {
        return todo;
    }

    public void setTodo(String todo) {
        this.todo = validateNotNull("todo", todo);
    }

    @Override
    public String toString() {
        return format("[id: '%s', todo: '%s']", id, todo);
    }

}
