package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.SalesTop10ReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
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
     * 修改订单状态
     * @param orderStatus
     * @param orderPaidStatus
     * @param checkOutTime
     * @param id
     */
    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus}, checkout_time = #{checkoutTime} where id = #{id}")
    void updateStatus(@Param("orderStatus") Integer orderStatus,
                      @Param("orderPaidStatus") Integer orderPaidStatus,
                      @Param("checkoutTime") LocalDateTime checkOutTime,
                      @Param("id") Long id);

    /**
     * 订单分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    @Select("select count(*) from orders where status = #{status}")
    Integer countStatus(Integer status);

    /**
     * 查询超时订单
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time = #{orderTime}")
    List<Orders> getOrders(Integer status, LocalDateTime orderTime);

    /**
     * 动态查询每日营业额
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 动态查询订单数量（工作台使用）
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 动态查询订单数量
     * @param beginTime
     * @param endTime
     * @param status
     * @return
     */
    Integer getOrdersCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status);

    /**
     * 销量排名查询
     * @param beginTime
     * @param endTime
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime beginTime, LocalDateTime endTime);
}
