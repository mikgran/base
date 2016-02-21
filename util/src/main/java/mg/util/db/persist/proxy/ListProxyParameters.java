package mg.util.db.persist.proxy;

import java.util.List;

import mg.util.db.persist.DB;

public class ListProxyParameters<T extends List<?>> {

    public final DB db;
    public final T list;
    public final String listPopulationSql;

    public ListProxyParameters(DB db, T list, String listPopulationSql) {
        this.db = db;
        this.list = list;
        this.listPopulationSql = listPopulationSql;
    }

    @SuppressWarnings("unused")
    private ListProxyParameters() {
        this.db = null;
        this.list = null;
        this.listPopulationSql = "";
    }
}
