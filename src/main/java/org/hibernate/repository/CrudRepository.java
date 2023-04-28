package org.hibernate.repository;

import org.hibernate.annotation.Column;
import org.hibernate.annotation.Id;
import org.hibernate.annotation.Table;
import org.hibernate.util.DatabaseConnection;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class CrudRepository<T,ID>{

    private final Class<T> type;
    private final Connection connection;

    private String findAllQuery = "select * from %s";
    private String findByIdQuery = "select * from %s where %s=?";
    private String insertQuery = "insert into %s (%s) values (%s)";

    public CrudRepository(Class<T> type) {
        this.type = type;
        this.connection = DatabaseConnection.getConnection();
        prepareFindAllQuery();
        prepareFindByIdQuery();
        prepareInsertQuery();
    }

    public List<T> findAll(){
        List<T> list = new ArrayList<>();
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(findAllQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                T entity = getEntityFromResultSet(resultSet);
                list.add(entity);
            }
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        return list;
    }

    public Optional<T> findById(ID id){
        Optional<T> optionalEntity = Optional.empty();
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(findByIdQuery);
            preparedStatement.setInt(1,(Integer) id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                T entity = getEntityFromResultSet(resultSet);
                optionalEntity = Optional.of(entity);
            }
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        return optionalEntity;
    }

    public T save(T entity){
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            setPreparedStatementValues(preparedStatement,entity);
            preparedStatement.execute();
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private void setPreparedStatementValues(PreparedStatement preparedStatement,T entity) throws IllegalAccessException, SQLException {
        Field[] declaredFields = type.getDeclaredFields();
        int i=1;
        for (Field f : declaredFields) {
            //Column column = f.getAnnotation(Column.class);
            if (f.getAnnotation(Id.class)==null) {
                f.setAccessible(true);
                switch (f.getType().getName()) {
                    case "java.lang.String" -> preparedStatement.setString(i++, (String) f.get(entity));
                    case "java.sql.Date" -> preparedStatement.setDate(i++, (Date) f.get(entity));
                    default -> {
                    }
                }
            }
        }
    }

    private T getEntityFromResultSet(ResultSet resultSet) throws Exception {
        T entity = type.getDeclaredConstructor().newInstance();
        Field[] declaredFields = type.getDeclaredFields();
        for (Field f : declaredFields) {
            Column column = f.getAnnotation(Column.class);
            f.setAccessible(true);
            switch (f.getType().getName()) {
                case "java.lang.Integer" -> f.set(entity, resultSet.getInt(column.name()));
                case "java.lang.String" -> f.set(entity, resultSet.getString(column.name()));
                case "java.sql.Date" -> f.set(entity, resultSet.getDate(column.name()));
                default -> f.set(entity, null);
            }
        }
        return entity;
    }

    private void prepareFindAllQuery(){
        Table table = type.getAnnotation(Table.class);
        findAllQuery = String.format(findAllQuery, table.name());
    }



    private void prepareFindByIdQuery(){
        Field[] declaredFields = type.getDeclaredFields();
        for (Field f : declaredFields) {
            Id idColumn = f.getAnnotation(Id.class);
            if (idColumn != null) {
                Table table = type.getAnnotation(Table.class);
                Column column = f.getAnnotation(Column.class);
                findByIdQuery = String.format(findByIdQuery,table.name(),column.name());
                break;
            }
        }
    }

    private void prepareInsertQuery(){
        Table table = type.getAnnotation(Table.class);
        Field[] declaredFields = type.getDeclaredFields();
        StringBuilder columnNames = new StringBuilder();
        int columnCount = 0;
        for (Field f : declaredFields) {
            Id idColumn = f.getAnnotation(Id.class);
            if (idColumn == null) {
                Column column = f.getAnnotation(Column.class);
                columnNames.append(column.name()).append(',');
                columnCount++;
            }
        }
        String questionMarks = "?,".repeat(columnCount);
        insertQuery = String.format(insertQuery,table.name(),
                columnNames.substring(0,columnNames.length()-1),
                questionMarks.substring(0,questionMarks.length()-1));
        System.out.println(insertQuery);
    }
}
