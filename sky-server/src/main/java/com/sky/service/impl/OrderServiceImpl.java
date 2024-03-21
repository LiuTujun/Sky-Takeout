package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShopCartMapper shopCartMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 获取地址
     * @param addressBook
     * @return
     */
    private String getAddress(AddressBook addressBook){
        List<String> address = new ArrayList<>();
        address.add(addressBook.getProvinceName());
        address.add(addressBook.getCityName());
        address.add(addressBook.getDistrictName());
        String res = String.join(" ", address);
        return res;
    }

    /**
     * 用户订单支付
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        // 1 处理各种业务异常 （地址簿为空  购物车为空等）
        // 1.1 判断地址簿是否异常
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        // 1.2 购物车是否异常
        Long userId = BaseContext.getCurrentId();
        ShoppingCart cart = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> shoppingCartList = shopCartMapper.list(cart);
        if(shoppingCartList == null || shoppingCartList.isEmpty()){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 2 向订单表中插入一条数据
        Orders orders = BeanUtil.copyProperties(ordersSubmitDTO, Orders.class);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID); // 设置订单支付状态
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee()); // 收货人
        orders.setUserId(userId);
        String address = getAddress(addressBook);
        orders.setAddress(address);

        orderMapper.insert(orders);

        // 3 向订单明细表中插入n条数据
        List<OrderDetail> orderDetailList = shoppingCartList.stream().map(shoppingCart -> {
            OrderDetail orderDetail = BeanUtil.copyProperties(shoppingCart, OrderDetail.class);
            orderDetail.setOrderId(orders.getId()); // 设置orderDetail的Id
            return orderDetail;
        }).collect(Collectors.toList());
        orderDetailMapper.insertBatch(orderDetailList);

        // 4 清空购物车(根据用户Id)
        shopCartMapper.deleteByUserId(userId);

        // 5 封装VO对象返回
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .build();

        // 通过webSocket向客户端浏览器推送消息 type orderId content
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1); // 1 表示来单提醒 2表示客户催单
        map.put("orderId", orders.getId());
        map.put("content", "订单号："+orders.getNumber());
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);

        return orderSubmitVO;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        // 通过webSocket向客户端浏览器推送消息 type orderId content
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1); // 1 表示来单提醒 2表示客户催单
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号："+outTradeNo);

        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 根据订单id获取订单信息
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO getById(String id) {
        long orderId = Long.parseLong(id);
        Orders orders = orderMapper.getById(orderId);
        OrderVO orderVO = BeanUtil.copyProperties(orders, OrderVO.class);

        // 获取订单的所有菜品信息
        List<OrderDetail> orderDetailList = orderDetailMapper.getById(orderId);
        List<String> collect = orderDetailList.stream().map(e -> e.getName()).collect(Collectors.toList());
        String orderDish = String.join(" ", collect);
        orderVO.setOrderDishes(orderDish);
        return orderVO;
    }

    /**
     * 分页查询所有的订单结果
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult list(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 1 设置分页参数
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());


        // 2 根据条件动态查询所有订单
        Page<Orders> pages = orderMapper.list(ordersPageQueryDTO);

        // 3 拼接订单包含的菜品
        List<OrderVO> orderVOList = pages.stream().map(page -> {
            // 复制属性
            OrderVO orderVO = BeanUtil.copyProperties(page, OrderVO.class);

            // 获取订单的所有菜品信息
            List<OrderDetail> orderDetailList = orderDetailMapper.getById(page.getId());
            List<String> collect = orderDetailList.stream().map(e -> e.getName()).collect(Collectors.toList());
            String orderDish = String.join(" ", collect);
            orderVO.setOrderDishes(orderDish);

            return orderVO;
        }).collect(Collectors.toList());

        PageResult pageResult = PageResult.builder().total(pages.getTotal()).records(orderVOList).build();

        return pageResult;
    }

    /**
     * 获取各个状态的订单数量
     *
     * @return
     */
    @Override
    public OrderStatisticsVO getOrderNum() {

        Integer confirmedNum = orderMapper.getStatusNum(Orders.CONFIRMED);
        Integer DELIVERY_IN_PROGRESS_NUM = orderMapper.getStatusNum(Orders.DELIVERY_IN_PROGRESS);
        Integer TO_BE_CONFIRMED_NUM = orderMapper.getStatusNum(Orders.TO_BE_CONFIRMED);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmedNum);
        orderStatisticsVO.setDeliveryInProgress(DELIVERY_IN_PROGRESS_NUM);
        orderStatisticsVO.setToBeConfirmed(TO_BE_CONFIRMED_NUM);
        return orderStatisticsVO;
    }

    /**
     * 派送订单
     *
     * @param id
     */
    @Override
    public void delivery(String id) {
        long orderId = Long.parseLong(id);
        //修改订单为派送中
        orderMapper.updateStatus(orderId, Orders.DELIVERY_IN_PROGRESS);
    }

    /**
     * 接单
     *
     * @param id
     */
    @Override
    public void confirm(long id) {
        //修改订单为已接单
        orderMapper.updateStatus(id, Orders.CONFIRMED);
    }

    /**
     * 完成订单
     *
     * @param id
     */
    @Override
    public void complete(String id) {
        //修改订单为完成订单
        long orderId = Long.parseLong(id);
        orderMapper.updateStatus(orderId, Orders.COMPLETED);
    }

    /**
     * 拒单
     *
     * @param ordersRejectionDTO
     */
    @Override
    public void reject(OrdersRejectionDTO ordersRejectionDTO) {
        orderMapper.updateStatusAndReson(ordersRejectionDTO);
    }

    /**
     * 取消订单
     *
     * @param ordersCancelDTO
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        orderMapper.updateStatusAndCancelReson(ordersCancelDTO);
    }

    /**
     * 用户查看历史订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult userOrderList(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 1 设置分页参数
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());


        // 2 根据条件动态查询所有订单
        Page<Orders> pages = orderMapper.list(ordersPageQueryDTO);

        // 3 拼接订单包含的菜品
        List<OrderVO> orderVOList = pages.stream().map(page -> {
            // 复制属性
            OrderVO orderVO = BeanUtil.copyProperties(page, OrderVO.class);

            // 获取订单的所有菜品信息
            List<OrderDetail> orderDetailList = orderDetailMapper.getById(page.getId());
            orderVO.setOrderDetailList(orderDetailList);

            return orderVO;
        }).collect(Collectors.toList());

        PageResult pageResult = PageResult.builder().total(pages.getTotal()).records(orderVOList).build();

        return pageResult;
    }

    /**
     * 用户获取指定Id的订单
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO userGetOrderById(String id) {
        long orderId = Long.parseLong(id);
        Orders orders = orderMapper.getById(orderId);
        OrderVO orderVO = BeanUtil.copyProperties(orders, OrderVO.class);

        // 对收货人及电话号码进行脱敏展示
        String phone = orderVO.getPhone();
        orderVO.setPhone(phone.substring(0, 2) + "****" + phone.substring(7));
        String consignee = orderVO.getConsignee();
        consignee = consignee.length() == 2 ? consignee.charAt(0) + "*" : consignee.charAt(0) + "*" + consignee.charAt(2);
        orderVO.setConsignee(consignee);

        // 获取订单的所有菜品信息
        List<OrderDetail> orderDetailList = orderDetailMapper.getById(orderId);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 用户催单
     *
     * @param id
     */
    // TODO
    @Override
    public void reminder(Long id) {
        // 根据id查询订单
        Orders orders = orderMapper.getById(id);

        // 查询订单是否存在
        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 通过webSocket向客户端浏览器推送消息 type orderId content
        Map<String, Object> map = new HashMap<>();
        map.put("type", 2); // 1 表示来单提醒 2表示客户催单
        map.put("orderId", orders.getId());
        map.put("content", "订单号: "+orders.getNumber());

        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }

    /**
     * 用户再来一单
     * @param id
     */
    @Override
    @Transactional
    public void repetition(String id) {
        long orderId = Long.parseLong(id);

        //重新生成订单信息
        Orders orders = orderMapper.getById(orderId);
        Orders orders1 = BeanUtil.copyProperties(orders, Orders.class);
        LocalDateTime nowTime = LocalDateTime.now();
        // 重设订单下单时间及期望时间
        orders1.setOrderTime(nowTime);
        orders1.setEstimatedDeliveryTime(nowTime.plusHours(1));
        orders1.setPayStatus(Orders.UN_PAID); // 设置订单支付状态
        orders1.setStatus(Orders.PENDING_PAYMENT);
        orders1.setNumber(String.valueOf(System.currentTimeMillis()));

        orderMapper.insert(orders1);

        // 3 向订单明细表中插入n条数据(复制原先的数据)
        List<OrderDetail> orderDetails = orderDetailMapper.getById(orderId);
        List<OrderDetail> orderDetailList = orderDetails.stream().map(orderDetail -> {
            orderDetail.setId(null);
            orderDetail.setOrderId(orders1.getId());
            return orderDetail;
        }).collect(Collectors.toList());
        orderDetailMapper.insertBatch(orderDetailList);
    }

    /**
     * 用户取消订单
     *
     * @param id
     */
    @Override
    public void userCancel(String id) {
        long orderId = Long.parseLong(id);

        orderMapper.updataStatus(orderId);
    }

}
