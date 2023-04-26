package org.hibernate.repository;

import org.hibernate.annotation.Column;
import org.hibernate.annotation.Table;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class CrudRepository <T>{

    private final Class<T> type;
    private final Connection connection;

    private String findAllQuery = "select * from %s";

    public CrudRepository(Class<T> type,Connection connection) {
        this.type = type;
        this.connection = connection;
        prepareFindAllQuery();
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
                    f.set(entity,resultSet.getString(column.name()));
                    /*
                    System.out.println(f.getType());
                    System.out.println(f.getName());
                    System.out.println(column.name());
                    * */
                    list.add(entity);
                }
            }
        }catch (SQLException | InvocationTargetException
                | InstantiationException | IllegalAccessException
                | NoSuchMethodException ex){
            System.out.println(ex.getMessage());
        }
        return list;
    }



    private void prepareFindAllQuery(){
        Table table = type.getAnnotation(Table.class);
        findAllQuery = String.format(findAllQuery, table.name());
    }
}
