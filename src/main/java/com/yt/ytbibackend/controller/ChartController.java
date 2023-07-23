package com.yt.ytbibackend.controller;
import java.util.Arrays;
import java.util.Date;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.yt.ytbibackend.annotation.AuthCheck;
import com.yt.ytbibackend.bizmq.BiMessageProducer;
import com.yt.ytbibackend.common.BaseResponse;
import com.yt.ytbibackend.common.DeleteRequest;
import com.yt.ytbibackend.common.ErrorCode;
import com.yt.ytbibackend.common.ResultUtils;
import com.yt.ytbibackend.constant.CommonConstant;
import com.yt.ytbibackend.constant.FileConstant;
import com.yt.ytbibackend.constant.UserConstant;
import com.yt.ytbibackend.exception.BusinessException;
import com.yt.ytbibackend.exception.ThrowUtils;
import com.yt.ytbibackend.manager.AiManager;
import com.yt.ytbibackend.manager.RedisLimiterManager;
import com.yt.ytbibackend.model.dto.chart.*;
import com.yt.ytbibackend.model.dto.file.UploadFileRequest;
import com.yt.ytbibackend.model.entity.Chart;
import com.yt.ytbibackend.model.entity.User;
import com.yt.ytbibackend.model.enums.FileUploadBizEnum;
import com.yt.ytbibackend.model.vo.BiResponse;
import com.yt.ytbibackend.model.vo.ChartVO;
import com.yt.ytbibackend.service.ChartService;
import com.yt.ytbibackend.service.UserService;
import com.yt.ytbibackend.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/DTNightWatchman">YTbaiduren</a>
 * @from
 */
@RestController
@RequestMapping("/post")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        chart.setUserId(loginUser.getId());
        chartService.save(chart);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);

        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<ChartVO> getChartVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chartService.getChartVO(chart));
    }

    /**
     * 分页获取列表（封装类）
     * @param chartQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @PostMapping("/admin/list/page/vo")
    public BaseResponse<Page<ChartVO>> listChartVOByPageAll(@RequestBody ChartQueryRequest chartQueryRequest) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartService.getChartVOPage(chartPage));
    }

    /**
     * 分页获取列表（封装类）
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ChartVO>> listChartVOByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        QueryWrapper<Chart> queryWrapper = chartService.getQueryWrapper(chartQueryRequest);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        queryWrapper.eq("userId", loginUser.getId());
        Page<Chart> chartPage = chartService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(chartService.getChartVOPage(chartPage));
    }

    @Resource
    private AiManager aiManager;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    /**
     * 校验文件安全
     * @param multipartFile
     */
    private void ifSafeFile(MultipartFile multipartFile) {
        long size = multipartFile.getSize();
        final long ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件过大");
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "csv", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
    }

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @PostMapping("/gen")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 校验
        ifSafeFile(multipartFile);
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称为空或过长");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }
        // 压缩后的数据
        String res = ExcelUtils.excelToCsv(multipartFile);
        // 构建请求
//        分析需求:
//        线型图
//        原始数据:
//        日期,用户数
//        1号,10
//        2号,20
//        3号,30
        StringBuilder userInput = new StringBuilder("分析需求:\n");
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据:\n");
        userInput.append(res);

        String result = aiManager.doChat(CommonConstant.BI_MODEL_ID, userInput.toString());
        String[] split = result.split("【【【【【");
        if (split.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "ai生成结果错误");
        }
        String option = split[1];
        String analyzeResult = split[2];
        BiResponse biResponse = new BiResponse();
        biResponse.setGenOption(option);
        biResponse.setGenResult(analyzeResult);
        // 插入数据
        Chart chart = new Chart();
        chart.setUserId(loginUser.getId());
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(res);
        chart.setChartType(chartType);
        chart.setGenChart(option);
        chart.setGenResult(analyzeResult);
        boolean save = chartService.save(chart);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "保存图表错误");

        return ResultUtils.success(biResponse);

    }

    /**
     * 文件上传,异步生成结果
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/v2/gen")
    public BaseResponse<BiResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                             GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 校验
        ifSafeFile(multipartFile);
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称为空或过长");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }
        // 压缩后的数据
        String res = ExcelUtils.excelToCsv(multipartFile);
        // 构建请求
//        分析需求:
//        线型图
//        原始数据:
//        日期,用户数
//        1号,10
//        2号,20
//        3号,30
        StringBuilder userInput = new StringBuilder("分析需求:\n");
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据:\n");
        userInput.append(res);
        // 先进行插入数据库操作
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(res);
        chart.setChartType(chartType);
        chart.setStatus(0); // 设置等待的初始状态
        chart.setUserId(loginUser.getId());
        boolean save = chartService.save(chart);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "保存图表失败");
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        CompletableFuture.runAsync(() -> {
            // todo 将状态修改为 执行中，执行成功后修改为已完成，失败就改为失败
            UpdateWrapper<Chart> chartUpdateWrapper = new UpdateWrapper<>();
            chartUpdateWrapper.set("status", 1);
            chartUpdateWrapper.eq("id", chart.getId());
            boolean b = chartService.update(chartUpdateWrapper);
            if (!b) {
                chartService.handleChartUpdateError(chart.getId(), "修改图表状态异常");
                return;
            }
            String result = aiManager.doChat(CommonConstant.BI_MODEL_ID, userInput.toString());
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
            }
        }, threadPoolExecutor);
//        BiResponse biResponse = new BiResponse();
//        biResponse.setGenOption(option);
//        biResponse.setGenResult(analyzeResult);
//
//        return ResultUtils.success()
        return ResultUtils.success(biResponse);
    }


    @Resource
    private BiMessageProducer biMessageProducer;
    /**
     * 文件上传,发送到消息队列中,异步生成结果
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/v3/gen")
    public BaseResponse<BiResponse> genChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 校验
        ifSafeFile(multipartFile);
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称为空或过长");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }
        // 压缩后的数据
        String res = ExcelUtils.excelToCsv(multipartFile);
        // 构建请求
        long biModelId = 1671934662349438978L;

        StringBuilder userInput = new StringBuilder("分析需求:\n");
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据:\n");
        userInput.append(res);
        // 先进行插入数据库操作
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(res);
        chart.setChartType(chartType);
        chart.setStatus(0); // 设置等待的初始状态
        chart.setUserId(loginUser.getId());
        boolean save = chartService.save(chart);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "保存图表失败");
        long newChartId = chart.getId();
        biMessageProducer.sendMessage(String.valueOf(newChartId));
        return ResultUtils.success(new  BiResponse());
    }



    /**
     * 分页获取当前的数据的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<ChartVO>> listMyChartVOByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartService.getChartVOPage(chartPage));
    }

    // endregion


    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);

        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

}
