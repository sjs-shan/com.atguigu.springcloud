package com.atguigu.gmall0218.service.mapper;

import com.atguigu.gmall0218.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    List<SkuSaleAttrValue> selectSkuSaleAttrListValueBySpu(String spuId);
}
