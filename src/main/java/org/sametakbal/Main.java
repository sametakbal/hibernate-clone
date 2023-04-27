package org.sametakbal;

import org.hibernate.annotation.Column;
import org.hibernate.annotation.Id;
import org.hibernate.repository.CrudRepository;
import org.sametakbal.entity.Category;

import java.lang.reflect.Field;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        CrudRepository<Category,Integer> repository = new CrudRepository<>(Category.class);
        Optional<Category> optionalCategory = repository.findById(1);
        if (optionalCategory.isPresent()) {
            System.out.println(optionalCategory.get());
        }

    }
}