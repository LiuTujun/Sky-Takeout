package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单信息
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 根据id获取订单信息
     * @param orderId
     * @return
     */
    @Select("select * from orders where id=#{orderId};")
    Orders getById(long orderId);

    /**
     * 分页查询订单信息
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> list(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 获取各个状态的订单数量
     * @param status
     * @return
     */
    @Select("select count(*) from orders where status=#{status}")
    Integer getStatusNum(Integer status);

    /**
     * 更改订单的状态
     * @param orderId
     */
    @Update("update orders set status=#{status} where id=#{orderId}")
    void updateStatus(long orderId, Integer status);

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Update("update orders set status=7, rejection_reason=#{rejectionReason} where id=#{id}")
    void updateStatusAndReson(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    @Update("update orders set status=6, cancel_reason=#{cancelReason} where id=#{id}")
    void updateStatusAndCancelReson(OrdersCancelDTO ordersCancelDTO);

    /**
     * 用户取消订单
     * @param orderId
     */
    @Update("update orders set status=6 where id=#{id}")
    void updataStatus(long orderId);

    /**
     * 查询满足条件的订单
     * @param status
     * @param time
     * @return
     */
    @Select("select * from orders where status=#{status} and order_time<#{time}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime time);

    /**
     * 查询指定日期范围内的订单营业额
     * @param map
     * @return
     */
    Double SumByMap(Map map);

    /**
     * 查询指定日期区间的订单数/有效订单数
     * @param map
     * @return
     */
    Integer countMap(Map map);

    /**
     * 统计指定时间区间内的Top10 商品
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}
