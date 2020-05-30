package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.annotations.Column;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public abstract class Model {

    private final HashMap<String, Object> originalValues = new HashMap<>();

    public Model(ResultSet resultSet) {
        this.setOriginal(resultSet);
        this.fillFields(resultSet);
    }

    public HashMap<String, Object> getOriginalValues() {
        return this.originalValues;
    }

    /**
     * Fill all the fields in the class contains the Column annotation
     *
     * @param resultSet record of the database
     *
     * @throws DbLoadException
     */
    protected void fillFields(ResultSet resultSet) {
        for (Field field : this.getClass().getDeclaredFields()) {
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
        this.originalValues.clear();

        try {
            int columnCount = resultSet.getMetaData().getColumnCount();

            for (int column = 1; column <= columnCount; column++) {
                String columnName = resultSet.getMetaData().getColumnName(column);
                this.originalValues.put(columnName, resultSet.getObject(column));
            }
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }
}
