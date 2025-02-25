package com.example.pmproject.Service;

import com.example.pmproject.DTO.ProductDTO;
import com.example.pmproject.DTO.ProductDTO;
import com.example.pmproject.Entity.Product;
import com.example.pmproject.Entity.Product;
import com.example.pmproject.Repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    
    @Value("${productImgUploadLocation}")
    private String productImgUploadLocation;
    private final imgService imgService;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper=new ModelMapper();
    
    public Page<ProductDTO> productDTOS(Pageable pageable) {
        int page=pageable.getPageNumber()-1;
        int pageLimit=6;
        
        Page<Product> paging=productRepository.findAll(PageRequest.of(page, pageLimit, Sort.Direction.ASC, "productId"));
        
        return paging.map(product -> ProductDTO.builder()
                .name(product.getName())
                .content(product.getContent())
                .price(product.getPrice())
                .img(product.getImg())
                .build());
    }

    public void register(ProductDTO productDTO, MultipartFile imgFile) throws IOException {
        String orginalFileName = imgFile.getOriginalFilename();
        String newFileName = "";

        if(orginalFileName != null) {
            newFileName = imgService.uploadFile(productImgUploadLocation, orginalFileName, imgFile.getBytes());
        }
        productDTO.setImg(newFileName);
        Product product=modelMapper.map(productDTO, Product.class);
        productRepository.save(product);
    }

    public ProductDTO listOne(Long productId) {
        Product product=productRepository.findById(productId).orElseThrow();
        return modelMapper.map(product, ProductDTO.class);
    }

    public void modify(ProductDTO productDTO, MultipartFile imgFile) throws IOException {
        Product product = productRepository.findById(productDTO.getProductId()).orElseThrow();
        String deleteFile = product.getImg();

        String originalFileName = imgFile.getOriginalFilename();
        String newFileName = "";

        if(originalFileName.length() != 0) {
            if(deleteFile.length() != 0) {
                imgService.deleteFile(productImgUploadLocation, deleteFile);
            }

            newFileName = imgService.uploadFile(productImgUploadLocation, originalFileName, imgFile.getBytes());
            productDTO.setImg(newFileName);
        }
        productDTO.setProductId(product.getProductId());
        Product modify=modelMapper.map(productDTO, Product.class);

        productRepository.save(modify);
    }

    public void delete(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        imgService.deleteFile(productImgUploadLocation, product.getImg());

        productRepository.deleteById(productId);
    }
}
