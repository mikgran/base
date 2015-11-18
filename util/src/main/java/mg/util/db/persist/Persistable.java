package mg.util.db.persist;

public class Persistable {

    protected int id = 0;
    protected boolean dirty = false;
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public boolean isDirty() {
        return dirty;
    }
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
