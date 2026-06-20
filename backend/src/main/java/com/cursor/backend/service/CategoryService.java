package com.cursor.backend.service;

import com.cursor.backend.dto.CategoryRequest;
import com.cursor.backend.dto.CategoryResponse;
import com.cursor.backend.entity.Category;
import com.cursor.backend.exception.BadRequestException;
import com.cursor.backend.exception.ResourceNotFoundException;
import com.cursor.backend.mapper.EntityMapper;
import com.cursor.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(entityMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return entityMapper.toCategoryResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.findByName(request.getName()).isPresent()) {
            throw new BadRequestException("Category already exists: " + request.getName());
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Created category id={}", saved.getId());
        return entityMapper.toCategoryResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        categoryRepository.findByName(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException("Category already exists: " + request.getName());
                });

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());

        Category saved = categoryRepository.save(category);
        log.info("Updated category id={}", saved.getId());
        return entityMapper.toCategoryResponse(saved);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
        log.info("Deleted category id={}", id);
    }
}
