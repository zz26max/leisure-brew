package com.sky.service;


import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;
import javax.servlet.http.HttpServletResponse;


public interface ReportService {

    /**
     * 营业额数据统计
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverReport(LocalDate begin, LocalDate end);

    /**
     * 用户数量统计
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getUserReport(LocalDate begin, LocalDate end);

    /**
     * 订单数量统计
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO getOrdersReport(LocalDate begin, LocalDate end);

    /**
     * 销量排名查询
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO getSalesTop10Report(LocalDate begin, LocalDate end);

    /**
     * 导出近30天运营数据报表
     * @param response
     */
    void exportBusinessData(HttpServletResponse response);
}
