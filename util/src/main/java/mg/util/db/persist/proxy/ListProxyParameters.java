package mg.util.db.persist.proxy;

import java.util.List;

import mg.util.db.persist.DB;
import mg.util.db.persist.Persistable;

public class ListProxyParameters<T extends List<?>> {

    public final DB db;
    public final boolean fetched;
    public final T list;
    public final String listPopulationSql;
    public final Persistable refPersistable;

    public ListProxyParameters(DB db, T list, String listPopulationSql, Persistable refPersistable) {
        this.db = db;
        this.list = list;
        this.listPopulationSql = listPopulationSql;
        this.refPersistable = refPersistable;
        this.fetched = false;
    }

    public ListProxyParameters(ListProxyParameters<T> p) {
        this.db = p.db;
        this.fetched = p.fetched;
        this.list = p.list;
        this.listPopulationSql = p.listPopulationSql;
        this.refPersistable = p.refPersistable;
    }

    public ListProxyParameters(ListProxyParameters<T> p, boolean fetched) {
        this.db = p.db;
        this.fetched = fetched;
        this.list = p.list;
        this.listPopulationSql = p.listPopulationSql;
        this.refPersistable = p.refPersistable;
    }


    @SuppressWarnings("unused")
    private ListProxyParameters() {
        this.db = null;
        this.list = null;
        this.listPopulationSql = "";
        this.refPersistable = null;
        this.fetched = false;
    }
}
