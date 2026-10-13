package com.badminton.product.entity;

import jakarta.persistence.*;

@Entity
@Table(name="product_variants")
public class ProductVariant extends com.badminton.common.entity.BaseEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="variant_id") private Long variantId;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="product_id",nullable=false) private Product product;
 @Column(nullable=false,length=100) private String sku;
 @Column(name="color_name",nullable=false,length=80) private String color;
 @Column(name="size_label",nullable=false,length=50) private String size;
 @Column(nullable=false) private Integer stock;
 @Column(nullable=false,length=30) private String status="ACTIVE";
 public Long getVariantId(){return variantId;} public void setVariantId(Long v){variantId=v;}
 public Product getProduct(){return product;} public void setProduct(Product v){product=v;}
 public String getSku(){return sku;} public void setSku(String v){sku=v;}
 public String getColor(){return color;} public void setColor(String v){color=v;}
 public String getSize(){return size;} public void setSize(String v){size=v;}
 public Integer getStock(){return stock;} public void setStock(Integer v){stock=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
}
