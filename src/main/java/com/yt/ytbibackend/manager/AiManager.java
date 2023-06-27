package com.yt.ytbibackend.manager;

import com.yt.ytbibackend.common.ErrorCode;
import com.yt.ytbibackend.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AiManager {


    @Resource
    private YuCongMingClient yuCongMingClient;


    /**
     * ai对话
     * @param message
     * @return
     */
    public String doChat(Long modelId, String message) {
        DevChatRequest devChatRequest = new DevChatRequest();
        //https://www.yucongming.com/model/1651461974841737218?inviteUser=1656574048366354434
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);
        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        if (response == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "ai响应错误");
        }
        //System.out.println(response);
        if (response.getData() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "ai响应错误");
        }
        if (response.getData().getContent() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "ai响应错误");
        }
        return response.getData().getContent();
    }
}
