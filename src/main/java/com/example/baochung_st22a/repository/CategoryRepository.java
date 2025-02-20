package com.example.baochung_st22a.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.baochung_st22a.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

	public Boolean existsByName(String name);

	public List<Category> findByIsActiveTrue();

}
