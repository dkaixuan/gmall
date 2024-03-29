package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsBaseAttrValue;
import com.atguigu.gmall.bean.PmsBaseSaleAttr;

import java.util.List;
import java.util.Set;

public interface AttrService {

    List<PmsBaseAttrInfo> getAttrInfo(String catalog3Id);
    List<PmsBaseAttrValue> getAttrValueList(String attrId);
    void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);
    List<PmsBaseSaleAttr> getAll();

    List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> valueIdSet);
}
