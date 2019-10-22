package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsProductImage;
import com.atguigu.gmall.bean.PmsProductInfo;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.service.SpuService;
import com.atguigu.gmall.webutil.util.UploadUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin
public class SpuController {

    @Reference
    private SpuService spuService;

    @RequestMapping("spuList")
    public List<PmsProductInfo> spuList(String catalog3Id) {
      List<PmsProductInfo>productInfoList =spuService.getSpuList(catalog3Id);
        return productInfoList;
    }

    /**
     * 保存商品信息
     * @param pmsProductInfo
     * @return
     */

    @RequestMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo) {
        spuService.saveProductInfo(pmsProductInfo);
        return "success";
    }

    /**
     * 上传图片到 FastDfs
     * 返回图片路径给前端
     * @param multipartFile
     * @return
     */
    @RequestMapping("fileUpload")
    public String fileUpload(@RequestParam("file")MultipartFile multipartFile) {
        String imgUrl = UploadUtil.uploadImage(multipartFile);
        System.out.println(imgUrl);
        return imgUrl;
    }


    @RequestMapping("spuSaleAttrList")
    public List<PmsProductSaleAttr> spuSaleAttrList(@RequestParam("spuId")String spuId) {
      List<PmsProductSaleAttr> pmsProductSaleAttrList=spuService.getSaleAttrByProductId(spuId);
        return pmsProductSaleAttrList;
    }


    @RequestMapping("spuImageList")
    public List<PmsProductImage> spuImageList(@RequestParam("spuId") String spuId) {
     List<PmsProductImage> pmsProductImageList=spuService.getSpuImageList(spuId);
        return pmsProductImageList;
    }




}
