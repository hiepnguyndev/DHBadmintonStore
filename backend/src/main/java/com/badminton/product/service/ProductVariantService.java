package com.badminton.product.service;
import static com.badminton.common.support.Checks.*;
import com.badminton.common.support.Access;
import com.badminton.product.dto.request.ProductVariantRequest;
import com.badminton.product.dto.response.ProductVariantResponse;
import com.badminton.product.entity.*;
import com.badminton.product.repository.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Transactional
public class ProductVariantService {
 private final ProductVariantRepository variants; private final ProductRepository products;
 public ProductVariantService(ProductVariantRepository v,ProductRepository p){variants=v;products=p;}
 @Transactional(readOnly=true) public List<ProductVariantResponse> list(Long productId,boolean admin){
  if(!products.existsById(productId)) throw new IllegalArgumentException("PRODUCT_NOT_FOUND");
  return variants.findByProductProductIdOrderByColorAscSizeAsc(productId).stream().map(this::view).toList();
 }
 public ProductVariantResponse create(Long productId,ProductVariantRequest r){
  Product p=found(products.lock(productId),"PRODUCT_NOT_FOUND"); ProductVariant v=new ProductVariant(); v.setProduct(p); v.setSku(r.sku().trim());v.setColor(r.color().trim());v.setSize(r.size().trim());v.setStock(r.stock());v.setStatus(r.status()==null?(r.stock()==0?"OUT_OF_STOCK":"ACTIVE"):r.status());variants.saveAndFlush(v);sync(p);return view(v);
 }
 public ProductVariantResponse update(Long id,ProductVariantRequest r){
  ProductVariant v=found(variants.lock(id),"VARIANT_NOT_FOUND");
  require(v.getSku().equals(r.sku().trim())&&v.getColor().equals(r.color().trim())&&v.getSize().equals(r.size().trim()),"VARIANT_IDENTITY_IMMUTABLE","SKU, màu và size không thể sửa");
  v.setStock(r.stock());v.setStatus(r.status()==null?(r.stock()==0?"OUT_OF_STOCK":"ACTIVE"):r.status());sync(v.getProduct());return view(v);
 }
 private void sync(Product p){int total=variants.findByProductProductIdOrderByColorAscSizeAsc(p.getProductId()).stream().mapToInt(ProductVariant::getStock).sum();p.setStock(total);if(p.getStatus()!=ProductStatus.INACTIVE)p.setStatus(total==0?ProductStatus.OUT_OF_STOCK:ProductStatus.ACTIVE);}
 private ProductVariantResponse view(ProductVariant v){return new ProductVariantResponse(v.getVariantId(),v.getProduct().getProductId(),v.getSku(),v.getColor(),v.getSize(),v.getStock(),v.getStatus());}
}
