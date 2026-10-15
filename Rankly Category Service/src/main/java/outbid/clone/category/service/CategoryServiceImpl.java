package outbid.clone.category.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbid.clone.category.dto.CategoryRequest;
import outbid.clone.category.dto.CategoryResponse;
import outbid.clone.category.entity.Category;
import outbid.clone.category.exception.CategoryNotFoundException;
import outbid.clone.category.exception.DuplicateCategoryException;
import outbid.clone.category.repository.CategoryRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<CategoryResponse> getAllCategories() {

        return categoryRepository
                .findByIsActiveTrueOrderByDisplayOrderAscNameAsc()
                .stream()
                .map(category -> modelMapper.map(category, CategoryResponse.class))
                .toList();
    }

    @Override
    public CategoryResponse getCategoryById(UUID id) {

        Category category = categoryRepository
                .findByIdAndIsActiveTrue(id)
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "Category not found with id: " + id
                        )
                );

        return modelMapper.map(category, CategoryResponse.class);
    }

    @Override
    public CategoryResponse getCategoryBySlug(String slug) {

        Category category = categoryRepository.findBySlugAndIsActiveTrue(slug)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category not found with slug: " + slug));

        return modelMapper.map(category, CategoryResponse.class);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {

        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateCategoryException(
                    "Category with slug already exists: " + request.getSlug());
        }

        Category category = modelMapper.map(request, Category.class);

        category.setIsActive(true);

        if (category.getDisplayOrder() == null) {
            category.setDisplayOrder(0);
        }

        Category savedCategory = categoryRepository.save(category);

        return modelMapper.map(savedCategory, CategoryResponse.class);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {

        Category category = findActiveById(id);

        if (categoryRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
            throw new DuplicateCategoryException(
                    "Category with slug already exists: " + request.getSlug());
        }

        modelMapper.map(request, category);

        if (category.getDisplayOrder() == null) {
            category.setDisplayOrder(0);
        }

        Category updatedCategory = categoryRepository.save(category);

        return modelMapper.map(updatedCategory, CategoryResponse.class);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {

        Category category = findActiveById(id);

        category.setIsActive(false);

        categoryRepository.save(category);
    }

    @Override
    public long getCategoryCount() {
        return categoryRepository.countByIsActiveTrue();
    }

    private Category findActiveById(UUID id) {
        return categoryRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category not found with id: " + id));
    }
}