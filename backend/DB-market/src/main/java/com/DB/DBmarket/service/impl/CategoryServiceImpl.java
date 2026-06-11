package com.DB.DBmarket.service.impl;

import com.DB.DBmarket.mapper.CategoryMapper;
import com.DB.DBmarket.mapper.ProdImgMapper;
import com.DB.DBmarket.mapper.ProductMapper;
import com.DB.DBmarket.pojo.Category;
import com.DB.DBmarket.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import java.util.List;

@Service("CategoryService")
public class CategoryServiceImpl implements CategoryService {
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private ProductMapper productMapper;

    @Resource
    private ProdImgMapper prodImgMapper;
    @Override
    public List<Category> getAllCategories() {
        return categoryMapper.getAllCategories();
    }

    @Override
    public boolean addCategory(String category){
       Integer found= categoryMapper.search(category);
       if(found!=0)
           return  false;
       else
           categoryMapper.addCategory(category);
       return true;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(String category) {
        try {
            prodImgMapper.deleteProdImgByCategory(category);
            productMapper.deleteProductByCategory(category);
            categoryMapper.deleteCategory(category);
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return;
        }
    }
}
