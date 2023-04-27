package org.hibernate.repository;

import org.hibernate.annotation.Column;
import org.hibernate.annotation.Id;
import org.hibernate.annotation.Table;
import org.hibernate.util.DatabaseConnection;
import org.sametakbal.entity.Category;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class CrudRepository<T,ID>{

    private final Class<T> type;
    private final Connection connection;

    private String findAllQuery = "select * from %s";

    private String findByIdQuery = "select * from %s where %s=?";

    public CrudRepository(Class<T> type) {
        this.type = type;
        this.connection = DatabaseConnection.getConnection();
        prepareFindAllQuery();
        prepareFindByIdQuery();
    }

    public List<T> findAll(){
        List<T> list = new ArrayList<>();
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(findAllQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                T entity = type.getDeclaredConstructor().newInstance();
                Field[] declaredFields = type.getDeclaredFields();
                for (Field f : declaredFields) {
                    Column column = f.getAnnotation(Column.class);
                    f.setAccessible(true);
                    switch (f.getType().getName()) {
                        case "java.lang.Integer" -> f.set(entity, resultSet.getInt(column.name()));
                        case "java.lang.String" -> f.set(entity, resultSet.getString(column.name()));
                        case "java.util.Date" -> f.set(entity, resultSet.getDate(column.name()));
                        default -> f.set(entity, null);
                    }
                }
                list.add(entity);
            }
        }catch (SQLException | InvocationTargetException
                | InstantiationException | IllegalAccessException
                | NoSuchMethodException ex){
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
                T entity = type.getDeclaredConstructor().newInstance();
                Field[] declaredFields = type.getDeclaredFields();
                for (Field f : declaredFields) {
                    Column column = f.getAnnotation(Column.class);
                    f.setAccessible(true);
                    switch (f.getType().getName()) {
                        case "java.lang.Integer" -> f.set(entity, resultSet.getInt(column.name()));
                        case "java.lang.String" -> f.set(entity, resultSet.getString(column.name()));
                        case "java.util.Date" -> f.set(entity, resultSet.getDate(column.name()));
                        default -> f.set(entity, null);
                    }
                }
                optionalEntity = Optional.of(entity);
            }
        }catch (SQLException | InvocationTargetException
                | InstantiationException | IllegalAccessException
                | NoSuchMethodException ex){
            System.out.println(ex.getMessage());
        }
        return optionalEntity;
    }
    private void prepareFindAllQuery(){
        Table table = type.getAnnotation(Table.class);
        findAllQuery = String.format(findAllQuery, table.name());
    }

    private void prepareFindByIdQuery(){
        Field[] declaredFields = Category.class.getDeclaredFields();
        for (Field f : declaredFields) {
            Id idColumn = f.getAnnotation(Id.class);
            if (idColumn != null) {
                Table table = type.getAnnotation(Table.class);
                Column column = f.getAnnotation(Column.class);
                findByIdQuery = String.format(findByIdQuery,table.name(),column.name());
                System.out.println(findByIdQuery);
                break;
            }
        }
    }
}
