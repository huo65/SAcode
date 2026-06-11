package com.DB.DBmarket.service.impl;

import com.DB.DBmarket.mapper.OrderInfoMapper;
import com.DB.DBmarket.mapper.ProdImgMapper;
import com.DB.DBmarket.mapper.ProductMapper;
import com.DB.DBmarket.mapper.UserMapper;
import com.DB.DBmarket.pojo.Product;
import com.DB.DBmarket.pojo.SearchProductRequest;
import com.DB.DBmarket.pojo.utils.RandomIdGenerator;
import com.DB.DBmarket.service.ProductService;
import org.springframework.data.relational.core.sql.In;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import java.util.List;

@Service("ProductService")
public class ProductServiceImpl implements ProductService {
    @Resource
    private ProductMapper productMapper;

    @Resource
    private ProdImgMapper prodImgMapper;
    @Resource
    private UserMapper userMapper;

    @Resource
    private OrderInfoMapper orderInfoMapper;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addProduct(Product product) {
        try {
             //random ID
            product.setId(RandomIdGenerator.getRandomId());
            //default state
            //-1未通过 0待审核 1审核通过
            product.setState(0);
            productMapper.insertProduct(product.getId(), product.getName(), product.getDescription(), product.getPrice(), product.getMer(), product.getCatName(), product.getNumber(), product.getState());
            for(String img : product.getImageList()) { //insert all images
                prodImgMapper.insertProdImg(product.getId(), img);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            //rollback transaction if exception occurs
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProduct(String id) {
        try {
            //delete product
            productMapper.deleteProduct(id);
            //delete images
            prodImgMapper.deleteProdImg(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            //rollback transaction if exception occurs
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Product> searchProduct(SearchProductRequest searchProductRequest) {
        try {
            if(searchProductRequest.getId() == null){
                searchProductRequest.setUserType("cus");
            }else {
                searchProductRequest.setUserType(userMapper.getUserType(searchProductRequest.getId()));
            }
            List<Product> products = productMapper.searchProduct(searchProductRequest);
            for (Product product : products) {
                product.setImageList(prodImgMapper.getProdImg(product.getId()));
                product.setMerName(userMapper.getNameById(product.getMer()));
            }
            return products;
        } catch (Exception e) {
            e.printStackTrace();
            //rollback transaction if exception occurs
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Product> getAllProducts() {
        try {
            List<Product> products = productMapper.getAllProducts();
            for (Product product : products) {
                product.setImageList(prodImgMapper.getProdImg(product.getId()));
            }
            return products;
        } catch (Exception e) {
            e.printStackTrace();
            //rollback transaction if exception occurs
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }
    }

    @Override
    @Transactional
    public List<Product> getProductById(String productId) {
        // 虽然只会找到一个商品，直接返回一个Product类型的对象也行，但是为了简化控制器的逻辑，这里选择返回一个列表
        // 列表只有一个元素
        try {
            List<Product> products = productMapper.getProductById(productId);
            for (Product product : products) {
                product.setImageList(prodImgMapper.getProdImg(product.getId()));
            }
            return products;
        } catch (Exception e) {
            e.printStackTrace();
            //rollback transaction if exception occurs
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }
    }

    @Override
    public Integer getPrice(String prod) {
        //数据库里的价格是double。。。。
       return productMapper.getPrice(prod);
    }

    @Override
    public void updateState(String id, Integer state) {
        productMapper.updateStateById(id,state);
    }

    @Override
    public void updateProduct(Product product) {
        productMapper.updateProduct(product);

        prodImgMapper.deleteProdImg(product.getId());
        for(String img : product.getImageList()) {
            prodImgMapper.insertProdImg(product.getId(), img);
        }
    }

    @Override
    @Transactional()
    public void calculateRefundAndComp() {
        List<String> prodIds=productMapper.getAllProductsId();
        try {
            for (String id : prodIds) {
                Integer refundNum = orderInfoMapper.getRefundNumById(id);
                Double orderSum = orderInfoMapper.getOrderSumById(id);
                Integer compNum = orderInfoMapper.getCompNumById(id);
                String refundRate = String.valueOf(refundNum / orderSum);
                String compRate = String.valueOf(compNum / orderSum);
                if(orderSum==0)//没有购买过的商品投诉和退款都设置为0
                {
                    refundRate="0.0";
                    compRate="0.0";
                }
                productMapper.updateProductRefundAndComp(id, refundNum, refundRate, compNum, compRate);
            }
        }catch (Exception e) {
            e.printStackTrace();
            //rollback transaction if exception occurs
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }


}
