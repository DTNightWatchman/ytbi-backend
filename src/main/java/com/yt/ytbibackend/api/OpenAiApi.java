package com.yt.ytbibackend.api;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class OpenAiApi {

    public static void main(String[] args) {
        //使用代理服务器
        System.getProperties().setProperty("proxySet", "true");
        //代理服务器地址
        System.getProperties().setProperty("http.proxyHost", "52.139.250.209");
        //代理端口
        System.getProperties().setProperty("http.proxyPort", "443");
        System.setProperty("https.proxyHost", "52.139.250.209");
        System.setProperty("https.proxyPort", "443");
        String url = "https://api.openai.com/v1/chat/completions";
        Map<String, String> map = new HashMap<>();
        map.put("model", "gpt-3.5-turbo");
        map.put("messages", "[{\"role\": \"system\", \"content\": \"You are a helpful assistant.\"}, {\"role\": \"user\", \"content\": \"Hello!\"}]");

        String body = HttpRequest.post(url)
                .header("Authorization", "sk-")
                .header("Content-Type", "application/json")
                .body(new Gson().toJson(map)).execute().body();
        System.out.println(body);

    }
}
