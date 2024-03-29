package com.yt.ytbibackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yt.ytbibackend.common.ErrorCode;
import com.yt.ytbibackend.constant.CommonConstant;
import com.yt.ytbibackend.exception.ThrowUtils;
import com.yt.ytbibackend.mapper.ChartMapper;
import com.yt.ytbibackend.model.dto.chart.ChartQueryRequest;
import com.yt.ytbibackend.model.entity.Chart;
import com.yt.ytbibackend.model.vo.ChartVO;
import com.yt.ytbibackend.service.ChartService;
import com.yt.ytbibackend.utils.ExcelUtils;
import com.yt.ytbibackend.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author YT摆渡人
* @description 针对表【chart(图标信息表)】的数据库操作Service实现
* @createDate 2023-06-21 01:40:27
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chartQueryRequest.getId();
        String goal = chartQueryRequest.getGoal();
        String name = chartQueryRequest.getName();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.like(StringUtils.isNotEmpty(goal), "goal", goal);
        queryWrapper.like(StringUtils.isNotEmpty(name), "name", name);
        queryWrapper.eq(StringUtils.isNotEmpty(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(true, false, "createTime");
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    @Override
    public Page<ChartVO> getChartVOPage(Page<Chart> chartPage) {
        List<Chart> chartList = chartPage.getRecords();
        Page<ChartVO> postVOPage = new Page<>(chartPage.getCurrent(), chartPage.getSize(), chartPage.getTotal());
        if (CollectionUtils.isEmpty(chartList)) {
            return postVOPage;
        }
        // 填充信息
        List<ChartVO> postVOList = chartList.stream().map(chart -> {
            ChartVO chartVO = new ChartVO();
            BeanUtils.copyProperties(chart, chartVO);
            return chartVO;
        }).collect(Collectors.toList());
        postVOPage.setRecords(postVOList);
        return postVOPage;
    }


    @Override
    public ChartVO getChartVO(Chart chart) {
        ChartVO chartVO = new ChartVO();
        BeanUtils.copyProperties(chart, chartVO);
        return chartVO;
    }

    @Override
    public void handleChartUpdateError(long chartId, String execMessage)  {
        UpdateWrapper<Chart> chartUpdateWrapper = new UpdateWrapper<>();
        chartUpdateWrapper.set("status", 3);
        chartUpdateWrapper.set("execMessage", execMessage);
        chartUpdateWrapper.eq("id", chartId);
        boolean update = this.update(chartUpdateWrapper);
        if (!update) {
            log.error("修改图表状态失败 - " + chartId);
        }
    }

    @Override
    public String buildInput(Chart chart) {
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }
        // 压缩后的数据
        String res = chart.getChartData();

        return "分析需求:\n" + userGoal + "\n" +
                "原始数据:\n" +
                res;
    }

}




