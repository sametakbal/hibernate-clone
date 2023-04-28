package org.sametakbal;

import org.hibernate.repository.CrudRepository;
import org.sametakbal.entity.Category;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        CrudRepository<Category,Integer> repository = new CrudRepository<>(Category.class);
        List<Category> all = repository.findAll();
        all.forEach(System.out::println);
        Optional<Category> optionalCategory = repository.findById(1);
        optionalCategory.ifPresent(System.out::println);
        Category category = new Category(null, "NAME", new Date(System.currentTimeMillis()));
        repository.save(category);

    }
}