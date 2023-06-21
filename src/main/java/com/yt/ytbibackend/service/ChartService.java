package com.yt.ytbibackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yt.ytbibackend.model.dto.chart.ChartQueryRequest;
import com.yt.ytbibackend.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yt.ytbibackend.model.vo.ChartVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author lenovo
* @description 针对表【chart(图标信息表)】的数据库操作Service
* @createDate 2023-06-21 01:40:27
*/
public interface ChartService extends IService<Chart> {

    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);

    Page<ChartVO> getChartVOPage(Page<Chart> chartPage);

    ChartVO getChartVO(Chart chart);
}
