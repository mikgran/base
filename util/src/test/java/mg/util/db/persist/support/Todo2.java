package mg.util.db.persist.support;

import static java.lang.String.format;
import static mg.util.validation.Validator.validateNotNull;

import java.util.List;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.ForeignKey;
import mg.util.db.persist.annotation.Id;
import mg.util.db.persist.annotation.OneToMany;
import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

@Table(name = "todos2")
public class Todo2 extends Persistable {

    @Id
    private int id = 0;

    @OneToMany
    private List<Location> locations;

    @ForeignKey(references = "persons2", field="id")
    private int personsId = 0;

    @VarChar
    private String todo = "";

    public Todo2() {
    }

    public Todo2(String todo, List<Location> locations) {
        this.locations = locations;
        this.todo = todo;
    }

    public int getId() {
        return id;
    }

    public int getPersonsId() {
        return personsId;
    }

    public String getTodo() {
        return todo;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTodo(String todo) {
        this.todo = validateNotNull("todo", todo);
    }

    @Override
    public String toString() {
        return format("[id: '%s', todo: '%s']", id, todo);
    }

}
