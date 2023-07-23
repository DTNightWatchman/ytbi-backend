package com.yt.ytbibackend.bizmq;

import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.rabbitmq.client.Channel;
import com.yt.ytbibackend.common.ErrorCode;
import com.yt.ytbibackend.constant.CommonConstant;
import com.yt.ytbibackend.exception.BusinessException;
import com.yt.ytbibackend.manager.AiManager;
import com.yt.ytbibackend.model.entity.Chart;
import com.yt.ytbibackend.service.ChartService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class BiMessageConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;



    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);
        if (StringUtils.isBlank(message)) {
            // 消息拒绝
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);

        if (chart == null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // todo 将状态修改为 执行中，执行成功后修改为已完成，失败就改为失败
        UpdateWrapper<Chart> chartUpdateWrapper = new UpdateWrapper<>();
        chartUpdateWrapper.set("status", 1);
        chartUpdateWrapper.eq("id", chart.getId());
        boolean b = chartService.update(chartUpdateWrapper);
        if (!b) {
            chartService.handleChartUpdateError(chart.getId(), "修改图表状态异常");
            channel.basicNack(deliveryTag, false, false);
            return;
        }
        String result = aiManager.doChat(CommonConstant.BI_MODEL_ID, chartService.buildInput(chart));
        String[] split = result.split("【【【【【");
        if (split.length < 3) {
            //throw new BusinessException(ErrorCode.SYSTEM_ERROR, "ai生成结果错误");
            chartService.handleChartUpdateError(chart.getId(), "ai生成结果错误");
            return;
        }
        String option = split[1];
        String analyzeResult = split[2];
        chartUpdateWrapper.set("status", 2);
        chartUpdateWrapper.set("genChart", option);
        chartUpdateWrapper.set("genResult", analyzeResult);
        boolean update = chartService.update(chartUpdateWrapper);
        if (!update) {
            chartService.handleChartUpdateError(chart.getId(), "修改状态失败");
            channel.basicNack(deliveryTag, false, false);
        }
        // 消息确认
        channel.basicAck(deliveryTag, false);
    }


}