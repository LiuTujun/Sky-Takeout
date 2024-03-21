package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    /**
     * 用户订单支付
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 根据订单id获取订单信息
     * @param id
     * @return
     */
    OrderVO getById(String id);

    /**
     * 分页查询所有的订单结果
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult list(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 获取各个状态的订单数量
     * @return
     */
    OrderStatisticsVO getOrderNum();

    /**
     * 派送订单
     * @param id
     */
    void delivery(String id);

    /**
     * 接单
     * @param id
     */
    void confirm(long id);

    /**
     * 完成订单
     * @param id
     */
    void complete(String id);

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    void reject(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    void cancel(OrdersCancelDTO ordersCancelDTO);

    /**
     * 用户查看历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult userOrderList(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 用户获取指定Id的订单
     * @param id
     * @return
     */
    OrderVO userGetOrderById(String id);

    /**
     * 用户催单
     * @param id
     */
    void reminder(Long id);

    /**
     * 用户再来一单
     * @param id
     */
    void repetition(String id);

    /**
     * 用户取消订单
     * @param id
     */
    void userCancel(String id);
}
