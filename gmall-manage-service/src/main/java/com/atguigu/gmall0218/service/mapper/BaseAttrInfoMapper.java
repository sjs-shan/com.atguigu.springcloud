package com.atguigu.gmall0218.service.mapper;

import com.atguigu.gmall0218.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(String catalog3Id);

    List<BaseAttrInfo> selectAttrInfoListByIds(@Param("valueIds") String valueIds);
}
