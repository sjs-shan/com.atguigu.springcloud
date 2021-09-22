package com.atguigu.gmall0218.service.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0218.bean.*;
import com.atguigu.gmall0218.config.RedisUtil;
import com.atguigu.gmall0218.service.ManageService;
import com.atguigu.gmall0218.service.constance.ManageConst;
import com.atguigu.gmall0218.service.mapper.*;
import org.apache.commons.lang.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private  SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private RedisUtil redisUtil;


    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {

        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);

        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);

        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);

        return baseAttrInfoMapper.select(baseAttrInfo);
    }

    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //修改操作
        if (baseAttrInfo.getId() !=null || baseAttrInfo.getId().length()>0){
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        }else{
            //添加操作
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        //先清空baseAttrvalue里面的值 然后插入里面
        //清空数据的条件 根据attrId为依据
        BaseAttrValue baseAttrValueDel = new BaseAttrValue();
        baseAttrValueDel.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValueDel);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (attrValueList !=null && attrValueList.size()>0){
            for (BaseAttrValue baseAttrValue :
                    attrValueList) {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(baseAttrValue);

            }
        }


    }

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {

        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);

        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);

        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);

        baseAttrInfo.setAttrValueList(baseAttrValueList);

        return baseAttrInfo;
    }

    @Override
    public List<SpuInfo> getSpuList(SpuInfo spuInfo) {

        List<SpuInfo> spuInfoList = spuInfoMapper.select(spuInfo);


        return spuInfoList;
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {

        //保存数据
        // spuinfo
        // spuImage
        // spuSaleAttr
        //spuSaleAttrValue

        spuInfoMapper.insert(spuInfo);

        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList !=null && spuImageList.size()>0){
            for (SpuImage spuImage :
                    spuImageList) {
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);
            }
        }
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList !=null && spuSaleAttrList.size()>0){
            for (SpuSaleAttr spuSaleAttr :
                    spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(spuSaleAttr);

                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList !=null && spuSaleAttrValueList.size()>0){

                    for (SpuSaleAttrValue spuSaleAttrValue :
                            spuSaleAttrValueList) {
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                    }
                }
            }
        }


    }

    @Override
    public List<SpuImage> getSpuImageList(SpuImage spuImage) {

        List<SpuImage> spuImageList = spuImageMapper.select(spuImage);
        return spuImageList;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        //调用mapper
        //涉及两张表关联查询

        List<SpuSaleAttr> spuSaleAttrList=spuSaleAttrMapper.selectSpuSaleAttrList(spuId);

        return spuSaleAttrList;
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {

        //skuInfo
        skuInfoMapper.insertSelective(skuInfo);
        //skuImage
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList !=null && skuImageList.size()>0){
            for (SkuImage skuImage :
                    skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(skuImage);
            }
        }
        //skuAttrValue
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList !=null && skuAttrValueList.size()>0){
            for (SkuAttrValue skuAttrValue :
                    skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }


        //skuSaleAttrValue
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList !=null && skuSaleAttrValueList.size()>0){
            for (SkuSaleAttrValue skuSaleAttrValue :
                    skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
        }


    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    private SkuInfo getSkuInfoJedis(String skuId){
        //获取jedis
        SkuInfo skuInfo=null;
        Jedis jedis =null;
        try{

            jedis = redisUtil.getJedis();
            //定义key：见名知义：sku:skuId:info
            String skuKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKUKEY_SUFIX;

            String skuJson = jedis.get(skuKey);

            if (skuJson ==null || skuJson.length()==0){
                //试着枷锁
                System.out.println("缓存中没有数据");
                //执行set命令
                //定义上锁key=sku:skuId:lock
                String skuLockKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKULOCK_SUFFIX;
                String lockKey = jedis.set(skuLockKey, "good", "NX", "PX", ManageConst.SKUKEY_EXPORE_PX);

                if ("ok".equals(lockKey)){
                    //此时加锁成功
                     skuInfo = getSkuInfo(skuId);
                    //将数据放入缓存
                    //将对象转换成字符串
                    String skuRedisStr = JSON.toJSONString(skuInfo);
                    jedis.setex(skuKey,ManageConst.SKUKEY_TIMEOUT,skuRedisStr);

                    jedis.del(skuLockKey);
                    return skuInfo;
                }else {
                    Thread.sleep(1000);

                    return getSkuInfo(skuId);
                }
            }else {
                SkuInfo skuInfo1 = JSON.parseObject(skuJson, SkuInfo.class);
                return skuInfo1;
            }

        }catch(Exception e){

            e.printStackTrace();

        }finally {
            if (jedis !=null){
                jedis.close();
            }
        }
        return getSkuInfo(skuId);
    }

    public SkuInfo getSkuInfoRedission(String skuId){
        //业务逻辑代码
        SkuInfo skuInfo=null;
        Jedis jedis=null;
        RLock lock=null;

        try{
            Config config = new Config();

            config.useSingleServer().setAddress("redis://192.168.17.133:6379");

            RedissonClient redissonClient = Redisson.create(config);

            //使用redisson 调用getLock
             lock = redissonClient.getLock("yourLock");

             lock.lock(10, TimeUnit.SECONDS);

             jedis = redisUtil.getJedis();

            String skuKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKULOCK_SUFFIX;

            if (jedis.exists(skuKey)){
                String skuJson = jedis.get(skuKey);
                SkuInfo skuInfo1 = JSON.parseObject(skuJson, SkuInfo.class);
                return skuInfo1;
            }else{
                skuInfo = getSkuInfo(skuId);
                jedis.setex(skuKey,ManageConst.SKUKEY_TIMEOUT, JSON.toJSONString(skuInfo));

                return skuInfo;
            }


        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if (jedis !=null){
                jedis.close();
            }
            if (lock !=null){
                lock.unlock();
            }



        }
return getSkuInfo(skuId);


    }


    @Override
    public SkuInfo getSkuInfo(String skuId) {

        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        skuInfo.setSkuImageList(getSkuImageBySkuId(skuId));


        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {


        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo.getId(),skuInfo.getSpuId());
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        return skuSaleAttrValueMapper.selectSkuSaleAttrListValueBySpu(spuId);
    }

    @Override
    public List<SkuImage> getSkuImageBySkuId(String skuId) {
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);

        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);

        return skuImageList;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {

        String valueIds = StringUtils.join(attrValueIdList.toArray(), ",");
        System.out.println("valueIds"+valueIds);

        return baseAttrInfoMapper.selectAttrInfoListByIds(valueIds);


    }
}
