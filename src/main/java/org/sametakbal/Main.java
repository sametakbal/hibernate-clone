package org.sametakbal;

import org.hibernate.repository.CrudRepository;
import org.sametakbal.entity.Category;
import org.sametakbal.util.DatabaseConnection;


public class Main {
    public static void main(String[] args) {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        CrudRepository<Category> repository = new CrudRepository<>(Category.class, databaseConnection.connect());
        for (Category cat:repository.findAll()) {
            System.out.println(cat.getName());
        }
    }
}