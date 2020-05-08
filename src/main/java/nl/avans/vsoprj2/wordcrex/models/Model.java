package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.annotations.Column;
import nl.avans.vsoprj2.wordcrex.models.annotations.PrimaryKey;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public abstract class Model {

    private final HashMap<String, Object> originalValues = new HashMap<>();

    public Model(ResultSet resultSet) {
        setOriginal(resultSet);
        fillFields(resultSet);
    }

    public abstract String getTable();

    public HashMap<String, Object> getOriginalValues() {
        return originalValues;
    }

    public boolean save() {
        try {
            Connection connection = Singleton.getInstance().getConnection();

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append(String.format("UPDATE `%s` SET ", this.getTable()));

            HashMap<String, Object> columnsMap = new HashMap<>();
            HashMap<String, Object> keysMap = new HashMap<>();

            for (Field field : getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    field.setAccessible(true);
                    String column = field.getDeclaredAnnotation(Column.class).value();
                    if (field.isAnnotationPresent(PrimaryKey.class)) {
                        keysMap.put(column, field.get(this));
                    } else {
                        columnsMap.put(column, field.get(this));
                    }
                }
            }

            if (keysMap.size() == 0) {
                return false;
            }

            List<Object> queryParams = new ArrayList<>();

            StringJoiner columnJoiner = new StringJoiner(", ");
            for (Map.Entry<String, Object> entry : columnsMap.entrySet()) {
                columnJoiner.add(String.format("`%s` = ?", entry.getKey()));
                queryParams.add(entry.getValue());
            }
            queryBuilder.append(columnJoiner.toString());

            queryBuilder.append(" WHERE ");

            StringJoiner keysJoiner = new StringJoiner(" AND ");
            for (Map.Entry<String, Object> entry : keysMap.entrySet()) {
                keysJoiner.add(String.format("`%s` = ?", entry.getKey()));
                queryParams.add(entry.getValue());
            }
            queryBuilder.append(keysJoiner.toString());
            queryBuilder.append(";");

            PreparedStatement statement = connection.prepareStatement(queryBuilder.toString());

            for (int i = 0; i < queryParams.size(); i++) {
                Object value = queryParams.get(i);
                statement.setString(i + 1, String.valueOf(value));
                if (value == null) {
                    statement.setNull(i + 1, Types.VARCHAR);
                }
            }

            System.err.println(statement);

            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DbLoadException(e);
        }
    }

    /**
     * Fill all the fields in the class contains the Column annotation
     *
     * @param resultSet  record of the database
     *
     * @throws DbLoadException
     */
    protected void fillFields(ResultSet resultSet) {
        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                try {
                    String column = field.getDeclaredAnnotation(Column.class).value();
                    field.setAccessible(true);
                    field.set(this, resultSet.getObject(column));
                } catch (SQLException ignored) {

                } catch (Exception e) {
                    throw new DbLoadException(e);
                }
            }
        }
    }

    protected void setOriginal(ResultSet resultSet) {
        originalValues.clear();

        try {
            int columnCount = resultSet.getMetaData().getColumnCount();

            for (int column = 1; column <= columnCount; column++) {
                String columnName = resultSet.getMetaData().getColumnName(column);
                originalValues.put(columnName, resultSet.getObject(column));
            }
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }
}
