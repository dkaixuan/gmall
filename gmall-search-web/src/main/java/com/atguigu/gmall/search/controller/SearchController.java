package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.SearchService;
import com.atguigu.gmall.webutil.util.GmallUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class SearchController {

    @Reference
    private SearchService searchService;

    @Reference
    private AttrService attrService;

    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam,ModelMap map) {

        List<PmsSearchSkuInfo> skuLsInfoList = searchService.list(pmsSearchParam);

        map.put("skuLsInfoList", skuLsInfoList);

        Set<String> valueIdSet = new HashSet<>();
        //抽取检索结果所包含的平台属性集合
        for (PmsSearchSkuInfo pmsSearchSkuInfo : skuLsInfoList) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }


        List<PmsBaseAttrInfo> baseAttrInfolist = attrService.getAttrValueListByValueId(valueIdSet);

        String[] delValueIds = pmsSearchParam.getValueId();
        //对平台属性集合进一步处理，去掉当前条件中valueId所在的属性组
        if (delValueIds != null) {
            List<PmsSearchCrumb> pmsSearchCrumbList = new ArrayList<>();
            for (String delValueId : delValueIds) {
                Iterator<PmsBaseAttrInfo> iterator = baseAttrInfolist.iterator();
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                // 生成面包屑的参数
                pmsSearchCrumb.setValueId(delValueId);
                pmsSearchCrumb.setUrlParam(getUrlParamForCrumb(pmsSearchParam, delValueId));
                while (iterator.hasNext()) {
                PmsBaseAttrInfo pmsBaseAttrInfo= iterator.next();
                List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                    String id = pmsBaseAttrValue.getId();
                        if (delValueId.equals(id)) {
                            // 查找面包屑的属性值名称
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            //删除该属性值所在的属性组
                            iterator.remove();
                        }
                    }
                }
                pmsSearchCrumbList.add(pmsSearchCrumb);
            }
            map.put("attrValueSelectedList",pmsSearchCrumbList);
        }
        map.put("attrList",baseAttrInfolist);

        String urlParam = getUrlParam(pmsSearchParam);

        map.put("urlParam",urlParam);
        String keyword = pmsSearchParam.getKeyword();

        if (GmallUtils.strEffectiveCheck(keyword)) {
            map.put("keyword", keyword);
        }

        return "list";
    }



    private String getUrlParamForCrumb(PmsSearchParam pmsSearchParam, String delValueId) {
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] valueIds = pmsSearchParam.getValueId();

        String urlParam = "";

        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }

        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }

        if (valueIds!= null) {
            for (String valueId : valueIds) {
                if (!valueId.equals(delValueId)) {
                    urlParam += "&valueId=" + valueId;
                }
            }
        }

        return urlParam;
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam) {
        String keyword = pmsSearchParam.getKeyword();

        String catalog3Id = pmsSearchParam.getCatalog3Id();

        List<PmsSkuAttrValue> skuAttrValueList = pmsSearchParam.getSkuAttrValueList();

        String[] valueIds = pmsSearchParam.getValueId();


        String urlParam = "";

        if (GmallUtils.strEffectiveCheck(keyword)) {

            if (GmallUtils.strEffectiveCheck(urlParam)) {
                urlParam += "&";
            }

            urlParam += "keyword=" + keyword;

        }

        if (GmallUtils.strEffectiveCheck(catalog3Id)) {
            if (GmallUtils.strEffectiveCheck(catalog3Id)) {
                urlParam += "&";
            }
            urlParam += "catalog3Id=" + catalog3Id;

        }

        if (valueIds != null) {
            for (String valueId : valueIds) {
                urlParam += "&valueId=" + valueId;
            }
        }


        return urlParam;
    }


}
