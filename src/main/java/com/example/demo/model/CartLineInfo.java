package com.example.demo.model;

public class CartLineInfo {
	 
    private ProductInfo productInfo;
 
    public CartLineInfo() {

    }
 
    public ProductInfo getProductInfo() {
        return productInfo;
    }
 
    public void setProductInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
    }
 
    public double getAmount() {
        return this.productInfo.getPrice();
    }
    
}
