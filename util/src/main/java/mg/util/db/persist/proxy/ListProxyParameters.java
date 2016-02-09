package mg.util.db.persist.proxy;

import java.util.List;

import mg.util.db.persist.DB;

public class ListProxyParameters<T extends List<?>> {

    private DB db;
    private T list;

    public ListProxyParameters() {
    }

    public ListProxyParameters(DB db, T list) {
        this.db = db;
        this.list = list;
    }

    public DB getDb() {
        return db;
    }

    public T getList() {
        return list;
    }

    public void setDb(DB db) {
        this.db = db;
    }

    public void setList(T list) {
        this.list = list;
    }
}
