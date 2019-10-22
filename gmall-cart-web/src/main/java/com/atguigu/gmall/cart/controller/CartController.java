package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.webutil.annotations.LoginRequired;
import com.atguigu.gmall.webutil.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.*;

@Controller
public class CartController {

    @Reference
    private SkuService skuService;
    @Reference
    private CartService cartService;

    /**
     * 刷新选中状态
     * @param isChecked
     * @param skuId
     * @param request
     * @param response
     * @param session
     * @param modelMap
     * @return
     */
    @LoginRequired(loginSuccess =false)
    @RequestMapping("checkCart")
    public String checkCart(String isChecked,String skuId,HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {

        String memberId = (String) request.getSession().getAttribute("memberId");

        // 调用服务，修改状态
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setIsChecked(isChecked);
        cartService.checkCart(omsCartItem);
        // 将最新的数据从缓存中查出，渲染给内嵌页
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        modelMap.put("cartList",omsCartItems);
        BigDecimal totalAmount=getTotalAmount(omsCartItems);
        modelMap.put("totalAmount",totalAmount);
        return "cartListInner";
    }

    /**
     * 查询购物车列表
     * 计算总价格
     * @param request
     * @param modelMap
     * @return
     */
    @LoginRequired(loginSuccess =false)
    @RequestMapping("cartList")
    public String cartList(HttpServletRequest request, ModelMap modelMap) {
        List<OmsCartItem> omsCartItemList = new ArrayList<>();

        String memberId = (String) request.getSession().getAttribute("memberId");

        OmsCartItem omsCartItem = new OmsCartItem();
        if (StringUtils.isNotBlank(memberId)) {
            //用户已经登陆,查询缓存
            omsCartItemList= cartService.cartList(memberId);
        } else {
            //没登陆,查询Cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)) {
                omsCartItemList= JSON.parseArray(cartListCookie, OmsCartItem.class);
            }
            for (OmsCartItem cartItem : omsCartItemList) {
                cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
            }
        }

        modelMap.put("cartList",omsCartItemList);
        BigDecimal totalAmount=getTotalAmount(omsCartItemList);
        modelMap.put("totalAmount",totalAmount);
        return "cartList";
    }

    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItemList) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItemList) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();
            if (omsCartItem.getIsChecked().equals("1")) {
                totalAmount = totalAmount.add(totalPrice);
            }
        }
        return totalAmount;
    }


    /**
     * 添加商品到购物车
     * @param skuId
     * @param quantity
     * @param request
     * @param response
     * @return
     */
    @LoginRequired(loginSuccess =false)
    @RequestMapping("/addToCart")
    public String addToCart(String skuId,int quantity, HttpServletRequest request, HttpServletResponse response,HttpSession session) {
        List<OmsCartItem> omsCartItemList = new ArrayList<>();

        // 调用商品服务查询商品信息
        PmsSkuInfo skuInfo = skuService.getSkuInfoById(skuId);

        // 将商品信息封装成购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductId(skuInfo.getProductId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("11111111111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setIsChecked("0");
        omsCartItem.setQuantity(new BigDecimal(quantity));

        // 判断用户是否登录
        String memberId= (String) request.getSession().getAttribute("memberId");

        if (StringUtils.isBlank(memberId)) {
            //用户没有登陆
            //cookie里原有的购物车数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isBlank(cartListCookie)) {
                //cookie为空
                omsCartItemList.add(omsCartItem);
            }else {
                //cookie 不为空
                omsCartItemList = JSON.parseArray(cartListCookie, OmsCartItem.class);
                //判断添加的购物车数据在Cookie中是否存在
               boolean exist=if_cart_exists(omsCartItemList, omsCartItem);
                if (exist) {
                    //如果该商品存在，则更新该商品购物车添加的数量
                    for (OmsCartItem cartItem : omsCartItemList) {
                        if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                            cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                        }
                    }
                }else {
                    //如果不存在，则添加该商品到购物车
                    omsCartItemList.add(omsCartItem);
                }
            }
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItemList), 60 * 60 * 72, true);
        }else {
            //用户已经登陆
            //从数据库中查出购物车数据
            OmsCartItem omsCartItemFromDb = cartService.ifCartExistByUser(memberId,skuId);
            if (omsCartItemFromDb == null) {
                //用户没有添加商品
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname("test小明");
                omsCartItem.setQuantity(new BigDecimal(quantity));
                omsCartItem.setIsChecked("0");
                cartService.addCart(omsCartItem);
            } else {
                //用户添加了商品，更新购物车当前商品数量
                omsCartItemFromDb.setQuantity(omsCartItemFromDb.getQuantity().add(omsCartItem.getQuantity()));
                cartService.updateCart(omsCartItemFromDb);
            }
            // 同步缓存
            cartService.flushCartCache(memberId);
        }

        session.setAttribute("skuInfo",skuInfo);
        session.setAttribute("skuNum",quantity);
        return "redirect:/success.html";
    }


    private boolean if_cart_exists(List<OmsCartItem> omsCartItemList, OmsCartItem omsCartItem) {
        boolean b=false;
        for (OmsCartItem cartItem : omsCartItemList) {
            String productSkuId = cartItem.getProductSkuId();
            if (productSkuId.equals(omsCartItem.getProductSkuId())) {
                b = true;
            }
        }
        return b;
    }

    @LoginRequired(loginSuccess =false)
    @RequestMapping("success")
    public String toSuccess(HttpSession session,Map map) {
        PmsSkuInfo skuInfo = (PmsSkuInfo) session.getAttribute("skuInfo");
        Object skuNum =session.getAttribute("skuNum");

        map.put("skuInfo", skuInfo);
        map.put("skuNum", skuNum);
        return "success";
    }



}
