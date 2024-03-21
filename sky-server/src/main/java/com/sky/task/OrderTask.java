package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时处理订单信息 Spring Task
 */

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单
     */
    @Scheduled(cron = "0 * * * * ? ") // 每分钟触发一次
    public void processTimeoutOrder(){
        log.info("定时处理超时订单：{}", LocalDateTime.now());

        // 当前时间减去十五分钟
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        // 查询所有满足条件的订单（超时未支付）
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.TO_BE_CONFIRMED, time);

        // 取消订单
        if(ordersList != null && !ordersList.isEmpty()){
            for(Orders orders: ordersList){
                // 设置订单为取消
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());

                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理一直处于派送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点触发
    public void processDeliveryOrder(){
        log.info("定时处理处于派送中的订单， {}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        // 查询所有满足条件的订单（一直处于派送中）
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);

        // 完成订单
        if(ordersList != null && !ordersList.isEmpty()){
            for(Orders orders: ordersList){
                // 设置订单为完成
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
