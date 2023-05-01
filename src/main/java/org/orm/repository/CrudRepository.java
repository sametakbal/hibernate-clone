package org.orm.repository;

import org.orm.annotation.Column;
import org.orm.annotation.Id;
import org.orm.annotation.Table;
import org.orm.util.DatabaseConnection;
import org.orm.util.StringUtil;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CrudRepository <T,ID>{

    private final Class<T> type;
    private final Class<ID> idType;
    private final Connection connection;

    private String tableName;

    private String idColumnName;

    private String findAllQuery = "select * from %s";

    private String findByIdQuery = "select * from %s where %s=?";

    private String insertQuery ="insert into %s (%s) values (%s)";

    private String updateQuery= "update %s set %s where %s";

    private String deleteByIdQuery = "delete from %s where %s=?";

    public CrudRepository(Class<T> type,Class<ID> idType) {
        this.type = type;
        this.idType = idType;
        this.connection = DatabaseConnection.getConnection();
        prepareFindAllQuery();
        prepareFindAndDeleteByIdByIDQuery();
        prepareInsertQuery();
        prepareUpdateQuery();
    }

    public T save(T entity)  {
        try{
            Field[] fields = type.getDeclaredFields();
            for (Field field: fields) {
                field.setAccessible(true);
                Id idAnnotation = field.getAnnotation(Id.class);
                if (idAnnotation != null) {
                    if (field.get(entity) != null) {
                        update(entity);
                    }else{
                        insert(entity);
                    }
                }
            }
        }catch (IllegalAccessException ex){
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public List<T> findAll(){
        List<T> list = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(findAllQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(getEntityFromResultSet(resultSet));
            }
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }

        return list;
    }

    public Optional<T> findById(ID id){
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(findByIdQuery);
            switch (idType.getName()){
                case "java.lang.Integer"-> preparedStatement.setInt(1,(int)id);
                case "java.lang.String"-> preparedStatement.setString(1,(String)id);
                default -> {}
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(getEntityFromResultSet(resultSet));
            }
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }

        return Optional.empty();
    }

    public boolean existById(ID id){
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(findByIdQuery);
            switch (idType.getName()){
                case "java.lang.Integer"-> preparedStatement.setInt(1,(int)id);
                case "java.lang.String"-> preparedStatement.setString(1,(String)id);
                default -> {}
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }

        return false;
    }

    public Optional<T> deleteById(ID id){
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(deleteByIdQuery);
            switch (idType.getName()){
                case "java.lang.Integer"-> preparedStatement.setInt(1,(int)id);
                case "java.lang.String"-> preparedStatement.setString(1,(String)id);
                default -> {}
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(getEntityFromResultSet(resultSet));
            }
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }

        return Optional.empty();
    }

    private T getEntityFromResultSet(ResultSet resultSet) throws Exception {
        T entity = type.getDeclaredConstructor().newInstance();
        Field[] fields = type.getDeclaredFields();
        for (Field field: fields) {
            Column column = field.getAnnotation(Column.class);
            field.setAccessible(true);
            switch (field.getType().getName()){
                case "java.lang.Integer"-> field.set(entity,resultSet.getInt(column.name()));
                case "java.lang.String"-> field.set(entity,resultSet.getString(column.name()));
                case "java.sql.Date"-> field.set(entity,resultSet.getDate(column.name()));
                default -> field.set(entity,null);
            }
        }
        return entity;
    }

    private T update(T entity){
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery, Statement.RETURN_GENERATED_KEYS);
            preparedStatementSetValues(preparedStatement,entity);
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                return null;
            }
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private T insert(T entity){
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            preparedStatementSetValues(preparedStatement,entity);
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                return null;
            }
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private void preparedStatementSetValues(PreparedStatement preparedStatement,T entity) throws Exception{
        Field[] fields = type.getDeclaredFields();
        int columnIndex = 1;
        for (Field field: fields) {
            Id idAnnotation = field.getAnnotation(Id.class);
            field.setAccessible(true);
            if (idAnnotation == null) {
                switch (field.getType().getName()){
                    case "java.lang.Integer"-> preparedStatement.setInt(columnIndex++,(int)field.get(entity));
                    case "java.lang.String"-> preparedStatement.setString(columnIndex++,(String)field.get(entity));
                    case "java.sql.Date"-> preparedStatement.setDate(columnIndex++,(Date)field.get(entity));
                    default -> {}
                }
            }
        }
        for (Field field: fields) {
            Id idAnnotation = field.getAnnotation(Id.class);
            field.setAccessible(true);
            if (idAnnotation != null) {
                field.setAccessible(true);
                switch (field.getType().getName()){
                    case "java.lang.Integer"-> preparedStatement.setInt(columnIndex,(int)field.get(entity));
                    case "java.lang.String"-> preparedStatement.setString(columnIndex,(String)field.get(entity));
                    case "java.sql.Date"-> preparedStatement.setDate(columnIndex,(Date)field.get(entity));
                    default -> {}
                }
                break;
            }
        }
    }

    private void prepareFindAllQuery(){
        findAllQuery = String.format(findAllQuery,getTableName());
    }



    private void prepareFindAndDeleteByIdByIDQuery(){
        Field[] fields = type.getDeclaredFields();
        for (Field field: fields) {
            Id idAnn = field.getAnnotation(Id.class);
            if (idAnn != null) {
                Column column = field.getAnnotation(Column.class);
               findByIdQuery = String.format(findByIdQuery, getTableName(), column.name());
               deleteByIdQuery = String.format(deleteByIdQuery, getTableName(), column.name());
               idColumnName = column.name();
            }
        }
    }

    private void prepareInsertQuery(){
        Field[] fields = type.getDeclaredFields();
        StringBuilder columnNames = new StringBuilder();
        int columnCount = 0;
        for (Field field:fields) {
            Id id = field.getAnnotation(Id.class);
            if (id == null) {
                Column column = field.getAnnotation(Column.class);
                columnNames.append(column.name()).append(",");
                columnCount++;
            }
        }
        String questionMarks = "?,".repeat(columnCount);
        insertQuery = String.format(insertQuery,getTableName(),
                StringUtil.removeLastCharacter(columnNames.toString()),
                StringUtil.removeLastCharacter(questionMarks));
    }

    private void prepareUpdateQuery(){
        Field[] fields = type.getDeclaredFields();
        StringBuilder columnNames = new StringBuilder();
        StringBuilder idColumn = new StringBuilder();
        for (Field field:fields) {
            Id id = field.getAnnotation(Id.class);
            Column column = field.getAnnotation(Column.class);
            if (id == null) {
                columnNames.append(column.name()).append("=?,");
            }else{
                idColumn.append(column.name()).append("=?");
            }
        }
        updateQuery = String.format(updateQuery,getTableName(),
                StringUtil.removeLastCharacter(columnNames.toString())
                ,idColumn);
    }

    private String getTableName(){
        if (tableName == null) {
            Table table = type.getAnnotation(Table.class);
            tableName = table.name();
        }
        return tableName;
    }
}
