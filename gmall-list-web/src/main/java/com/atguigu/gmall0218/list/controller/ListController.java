package com.atguigu.gmall0218.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0218.bean.*;
import com.atguigu.gmall0218.service.ListService;
import com.atguigu.gmall0218.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;


    @RequestMapping("list.html")
    @ResponseBody
    public String listData(SkuLsParams skuLsParams, HttpServletRequest request) {

        // 设置每页显示的数据条数
        skuLsParams.setPageSize(2);

        SkuLsResult skuLsResult = listService.search(skuLsParams);

        // 显示商品数据
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();

        // 平台属性，平台属性值的查询
        // 获取平台属性值的id集合
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();

        // 通过平台属性的id 查找平台属性名称，平台属性值名称
        List<BaseAttrInfo> baseAttrInfoList = manageService.getAttrList(attrValueIdList);

        // 编写有个方法来判断url 后面参数的名称
        String urlParam = makeUrlParam(skuLsParams);

        // 定义一个面包屑集合
        ArrayList<BaseAttrValue> baseAttrValueList = new ArrayList<>();

        // 使用迭代器

        for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {
            // 平台属性
            BaseAttrInfo baseAttrInfo = iterator.next();
            // 获取平台属性值集合的对象
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            // 循环attrValuelist
            for (BaseAttrValue baseAttrValue : attrValueList) {
                // 获取skuLsparam.getValueId()进行对比
                if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
                    for (String valueId : skuLsParams.getValueId()) {
                        // 如果平台属性值相同 则移除
                        if (valueId.equals(baseAttrValue.getId())) {
                            iterator.remove();
                            // 面包屑组成
                            BaseAttrValue baseAttrValueed = new BaseAttrValue();
                            // 将平台属性值得名称改为面包屑
                            baseAttrValueed.setValueName(baseAttrInfo.getAttrName() + ":" + baseAttrValue.getValueName());
                            // 将用户点击的平台属性值id 传递到makeparam 方法中 重新制作返回的url参数
                            String newUrlparam = makeUrlParam(skuLsParams, valueId);
                            // 重新制作url 参数
                            baseAttrValueed.setUrlParam(newUrlparam);
                            // 将baseAttrValueed 放入到集合中
                            baseAttrValueList.add(baseAttrValueed);

                        }
                    }
                }
            }
        }
            String path = request.getRequestURL().toString();
            System.out.println(path);

            // 保存分页数据
            request.setAttribute("pageNo", skuLsParams.getPageNo());
            request.setAttribute("skuLsResult", skuLsResult.getTotalPages());

            // 保存到作用域
            request.setAttribute("urlParam", urlParam);

            // 保存一个检索关键字
            request.setAttribute("keyword", skuLsParams.getKeyword());

            // 保存一个面包屑
            request.setAttribute("baseAttrValueList", baseAttrValueList);

            // 保存商品属性集合
            request.setAttribute("baseAttrInfoList", baseAttrInfoList);

            //保存商品集合
            request.setAttribute("skuLsInfoList", skuLsInfoList);

            return "list";

    }

    private String makeUrlParam(SkuLsParams skuLsParams, String ... excludeValueIds) {

        String urlParam="";
        if (skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0) {

            urlParam+="keyword="+skuLsParams.getKeyword();
        }
        // 判断三级分类id
        if (skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0) {
            if (urlParam.length() > 0) {
                urlParam += "&";

            }
            urlParam += "catalog3Id" + skuLsParams.getCatalog3Id();

        }
        // 平台属性id
        if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0) {
            for (String valueId : skuLsParams.getValueId()) {
                if (excludeValueIds!=null && excludeValueIds.length>0) {
                    // 获取点击面包屑时的平台属性值id
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueId)) {
                        continue;
                    }
                }
                if (urlParam.length()>0){
                    urlParam+="&";

                }
                urlParam+="valueId"+valueId;

            }
        }

        return urlParam;
    }


}
