package com.xydp.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xydp.dto.Result;
import com.xydp.entity.ShopType;
import com.xydp.mapper.ShopTypeMapper;
import com.xydp.service.IShopTypeService;
import com.xydp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {
        String key = RedisConstants.CACHE_SHOP_TYPE_KEY;
        List<String> typesString = stringRedisTemplate.opsForList().range(key, 0, -1); // 以 -1 表示列表的最后一个元素
        List<ShopType> types = new ArrayList<>();
        // 判断是否存在
        if (typesString != null && typesString.size() > 0) { // 存在
            for (String str : typesString) {
                types.add(JSONUtil.toBean(str, ShopType.class));
            }
            return Result.ok(types);
        }
        // 不存在，查询数据库
        types = query().orderByAsc("sort").list();

        typesString = new ArrayList<>();
        for (ShopType type : types) {
            typesString.add(JSONUtil.toJsonStr(type));
        }
        // 写入redis
        stringRedisTemplate.opsForList().rightPushAll(key, typesString);

        return Result.ok(types);
    }
}
