package mg.util.db.persist;

import static java.lang.String.format;

import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

@Table(name = "todos")
public class Todo extends Persistable {

    // @ForeignKey(references="Person")
    // private int id = 0;

    @VarChar
    private String todo = "";

    public Todo(String todo) {
        this.setTodo(todo);
    }

    public String getTodo() {
        return todo;
    }

    public void setTodo(String todo) {
        this.todo = todo;
    }
    
    @Override
    public String toString() {
        return format("[id: '%s', todo: '%s']", id, todo);
    }
    
}
