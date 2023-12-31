package com.xydp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xydp.dto.Result;
import com.xydp.entity.Voucher;
import com.xydp.mapper.VoucherMapper;
import com.xydp.entity.SeckillVoucher;
import com.xydp.service.ISeckillVoucherService;
import com.xydp.service.IVoucherService;
import com.xydp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        // 返回结果
        return Result.ok(vouchers);
    }

    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // 保存优惠券
        save(voucher);
        // 保存秒杀信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
        // 保存库存到redis中
//        stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY + voucher.getId(), String.valueOf(voucher.getStock()));
        Map<String, Object> map = new HashMap<>();
        if (seckillVoucher.getBeginTime() == null) {
            seckillVoucher.setBeginTime(LocalDateTime.now());
        }
        if (seckillVoucher.getEndTime() == null) {
            seckillVoucher.setEndTime(LocalDateTime.now());
        }
        map.put("stock", Integer.toString(seckillVoucher.getStock()));
        map.put("begin", Long.toString(seckillVoucher.getBeginTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
        map.put("end", Long.toString(seckillVoucher.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
        stringRedisTemplate.opsForHash().putAll(RedisConstants.SECKILL + voucher.getId(), map);
    }
}
