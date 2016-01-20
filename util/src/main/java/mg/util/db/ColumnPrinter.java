package mg.util.db;

import static mg.util.validation.Validator.validateNotNull;
import static mg.util.validation.Validator.validateNotNullOrEmpty;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import mg.util.Common;

public class ColumnPrinter {

    public static void print(ResultSet resultSet) throws SQLException {

        validateNotNull("resultSet", resultSet);

        ColumnPrinter columnPrinter = new ColumnPrinter();

        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            columnPrinter.addHeader(metaData.getTableName(i) + "." + metaData.getColumnLabel(i));
        }
        while (resultSet.next()) {
            for (int j = 1; j <= columnCount; j++) {
                columnPrinter.add(resultSet.getString(j));
            }
        }
        System.out.print(columnPrinter.toString());
    }

    private int columnIndex = 0;
    private List<Integer> columnSizes = new ArrayList<>();
    private String delimiter = " ";
    private int delimiterSize = 1;
    private boolean padLeft = true;
    private List<List<String>> rows = new ArrayList<>();

    public void add(String col) {
        validateNotNullOrEmpty("col", col);

        if (rows.isEmpty() || columnIndex + 1 >= columnSizes.size()) {
            rows.add(new ArrayList<String>());
        }

        List<String> lastRow = rows.get(rows.size() - 1);
        lastRow.add(col);
        cycleColumnIndex();
    }

    public void addHeader(String col) {
        validateNotNullOrEmpty("col", col);
        columnSizes.add(col.length());
        add(col);
    }

    public void delimiter(String delimiter) {
        this.delimiter = validateNotNull("delimiter", delimiter);
        this.delimiterSize = delimiter.length();
    }

    public void padLeft() {
        this.padLeft = true;
    }

    public void padRight() {
        this.padLeft = false;
    }

    @Override
    public String toString() {

        // this method has less operations, but leaves the delimiter at the end of the line; remove the last delimiter.
        String result;
        result = rows.stream()
                     .map(row -> {
                         String rowData = Common.zip(row.stream(),
                                                     columnSizes.stream(),
                                                     (column, colSize) -> padToSize(column + delimiter, (colSize + delimiterSize)))
                                                .reduce("", (columnA, columnB) -> columnA + columnB);

                         rowData = rowData.substring(0, rowData.lastIndexOf(delimiter));

                         return rowData + "\n";
                     })
                     .reduce("", (rowA, rowB) -> rowA + rowB);

        return result;
    }

    public String toStringLegacyForGiggles() {

        // how not to do things right here folks:
        List<List<String>> resultRows;
        resultRows = rows.stream()
                         .map(row -> {
                             if ("".equals(delimiter)) {
                                 return row;
                             }
                             String joined = row.stream()
                                                .collect(Collectors.joining(delimiter));
                             List<String> newRow = Arrays.stream(joined.split("(?<=" + delimiter + ")")) // lookahead splitter
                                                         .map(a -> a.trim())
                                                         .collect(Collectors.toList());
                             return newRow;
                         })
                         .map(row -> Common.zip(row.stream(),
                                                columnSizes.stream(),
                                                (column, colSize) -> padToSize(column, (colSize + delimiterSize)))
                                           .collect(Collectors.toList()))
                         .collect(Collectors.toList());

        String result;
        result = resultRows.stream()
                           .map(row -> {
                               String rowData = row.stream()
                                                   .map(s -> s.toString())
                                                   .collect(Collectors.joining());
                               return rowData + "\n";
                           })
                           .reduce("", (a, b) -> a + b);

        return result;
    }

    private void cycleColumnIndex() {
        columnIndex++;
        if (columnIndex >= columnSizes.size()) {
            columnIndex = 0;
        }
    }

    private String padToSize(String s, Integer n) {
        if (padLeft) {
            return String.format("%1$-" + (n) + "s", s);
        } else {
            return String.format("%1$" + (n) + "s", s);
        }
    }
}
