package com.yt.ytbibackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yt.ytbibackend.model.entity.Chart;
import com.yt.ytbibackend.service.ChartService;
import com.yt.ytbibackend.mapper.ChartMapper;
import org.springframework.stereotype.Service;

/**
* @author lenovo
* @description 针对表【chart(图标信息表)】的数据库操作Service实现
* @createDate 2023-06-21 01:40:27
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

}




