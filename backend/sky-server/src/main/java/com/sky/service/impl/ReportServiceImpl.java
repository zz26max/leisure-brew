package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {


    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final WorkspaceService workspaceService;

    public ReportServiceImpl(OrderMapper orderMapper, UserMapper userMapper, WorkspaceService workspaceService) {
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.workspaceService = workspaceService;
    }

    /**
     * 营业额数据统计
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverReport(LocalDate begin, LocalDate end) {
        //调用方法创建集合存放begin到end之间全部日期
        List<LocalDate> dateList = dateList(begin, end);

        //创建集合存放营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //获得每天的开始时间、结束时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //查询订单表获得每天的营业额->营业额指已完成状态的订单
            Map map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);

            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0:turnover;
            turnoverList.add(turnover);
        }

        //封装VO对象返回
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 用户数量统计
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserReport(LocalDate begin, LocalDate end) {
        //调用方法创建集合存放begin到end之间全部日期
        List<LocalDate> dateList = dateList(begin, end);

        //创建集合存放新增用户数量
        List<Integer> newUserList = new ArrayList<>();
        //创建集合存放用户总数量
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("endTime", endTime);
            Integer totalUser = userMapper.countByMap(map);
            totalUserList.add(totalUser);
            map.put("beginTime", beginTime);
            Integer newUser = userMapper.countByMap(map);
            newUserList.add(newUser);
        }

        //封装VO对象返回
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }

    /**
     * 订单数量统计
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrdersReport(LocalDate begin, LocalDate end) {
        //调用方法创建集合存放begin到end之间全部日期
        List<LocalDate> dateList = dateList(begin, end);

        //查询每日订单数量
        List<Integer> orderCountList = new ArrayList<>(); //每日订单总数
        List<Integer> validOrderCountList = new ArrayList<>(); //每日有效订单数
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //查询每日总订单数量
            Integer ordersCount = getOrdersCount(beginTime, endTime, null);
            //查询每日有效订单数量
            Integer validOrdersCount = getOrdersCount(beginTime, endTime, Orders.COMPLETED);

            orderCountList.add(ordersCount);
            validOrderCountList.add(validOrdersCount);
        }

        //计算订单总数、订单完成率
        Integer totalOrdersCount = orderCountList.stream().reduce(Integer::sum).get(); //订单总数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get(); //有效订单数
        Double orderCompletionRate = 0.0;
        if (totalOrdersCount != 0){
            orderCompletionRate = Double.valueOf(validOrderCount/totalOrdersCount);
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ",")) //日期
                .orderCountList(StringUtils.join(orderCountList, ",")) //每日订单数
                .validOrderCountList(StringUtils.join(validOrderCountList, ",")) //每日有效订单数
                .totalOrderCount(totalOrdersCount) //订单总数
                .validOrderCount(validOrderCount) //有效订单数
                .orderCompletionRate(orderCompletionRate) //订单完成率
                .build();
    }

    /**
     * 销量排名查询
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getSalesTop10Report(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(begin, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10List = orderMapper.getSalesTop10(beginTime, endTime);

        List<String> nameList = salesTop10List.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = salesTop10List.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();
    }

    /**
     * 导出运营报表
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) {
        // 统计周期：最近30天（不含当天）
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate beginDate = endDate.minusDays(30);

        // 先查询30天汇总数据，填充报表头部统计区
        LocalDateTime beginTime = LocalDateTime.of(beginDate, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(endDate, LocalTime.MAX);
        BusinessDataVO businessData = workspaceService.getBusinessData(beginTime, endTime);

        // 设置响应头，让浏览器按Excel附件下载
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=运营报表.xlsx");

        ClassPathResource resource = new ClassPathResource("template/运营数据报表模板.xlsx");
        try (InputStream inputStream = resource.getInputStream();
             XSSFWorkbook excel = new XSSFWorkbook(inputStream);
             ServletOutputStream outputStream = response.getOutputStream()) {

            Sheet sheet = excel.getSheetAt(0);

            // 报表标题区域：填充统计时间范围
            Row row2 = sheet.getRow(1);
            row2.getCell(1).setCellValue("时间：" + beginDate + "至" + endDate);

            // 报表概览区域：填充汇总指标
            Row row4 = sheet.getRow(3);
            row4.getCell(2).setCellValue(businessData.getTurnover());
            row4.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row4.getCell(6).setCellValue(businessData.getNewUsers());

            Row row5 = sheet.getRow(4);
            row5.getCell(2).setCellValue(businessData.getValidOrderCount());
            row5.getCell(4).setCellValue(businessData.getUnitPrice());

            // 明细区域：按天写入30天运营数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = beginDate.plusDays(i);
                BusinessDataVO dailyData = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX)
                );

                Row row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(dailyData.getTurnover());
                row.getCell(3).setCellValue(dailyData.getValidOrderCount());
                row.getCell(4).setCellValue(dailyData.getOrderCompletionRate());
                row.getCell(5).setCellValue(dailyData.getUnitPrice());
                row.getCell(6).setCellValue(dailyData.getNewUsers());
            }

            // 输出Excel到响应流
            excel.write(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException("导出运营数据报表失败", e);
        }
    }

    //计算日期方法
    private List<LocalDate> dateList(LocalDate begin, LocalDate end){
        //创建集合存放begin到end之间全部日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            //如果begin != end，往后加一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }//循环退出，dateList里存放了全部日期
        return dateList;
    }

    //统计订单数量方法
    private Integer getOrdersCount(LocalDateTime beginTime, LocalDateTime endTime,Integer status){
        Map map = new HashMap();
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        map.put("status", status);
        return orderMapper.getOrdersCount(beginTime,endTime,status);
    }
}
