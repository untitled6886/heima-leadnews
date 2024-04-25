package com.heima.common.aliyun;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.green20220302.Client;
import com.aliyun.green20220302.models.TextModerationRequest;
import com.aliyun.green20220302.models.TextModerationResponse;
import com.aliyun.green20220302.models.TextModerationResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;

import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aliyun")
public class GreenTextScan {

    private String accessKeyId;
    private String secret;
    private String textService;
    public Map greeTextScan(String content) throws Exception {

        Config config = new Config();
        /**
         * 阿里云账号AccessKey拥有所有API的访问权限，建议您使用RAM用户进行API访问或日常运维。
         * 常见获取环境变量方式：
         * 方式一：
         *     获取RAM用户AccessKey ID：System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");
         *     获取RAM用户AccessKey Secret：System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
         * 方式二：
         *     获取RAM用户AccessKey ID：System.getProperty("ALIBABA_CLOUD_ACCESS_KEY_ID");
         *     获取RAM用户AccessKey Secret：System.getProperty("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
         */
        config.setAccessKeyId(accessKeyId);
        config.setAccessKeySecret(secret);
        //接入区域和地址请根据实际情况修改
        config.setRegionId("cn-shanghai");
        config.setEndpoint("green-cip.cn-shanghai.aliyuncs.com");
        //连接时超时时间，单位毫秒（ms）。
        config.setReadTimeout(6000);
        //读取时超时时间，单位毫秒（ms）。
        config.setConnectTimeout(3000);
        //设置http代理。
        //config.setHttpProxy("http://10.10.xx.xx:xxxx");
        //设置https代理。
        //config.setHttpsProxy("https://10.10.xx.xx:xxxx");
        // 注意，此处实例化的client请尽可能重复使用，避免重复建立连接，提升检测性能
        Client client = new Client(config);

        // 创建RuntimeObject实例并设置运行参数。
        RuntimeOptions runtime = new RuntimeOptions();
        runtime.readTimeout = 10000;
        runtime.connectTimeout = 10000;

        //检测参数构造
        JSONObject serviceParameters = new JSONObject();
        serviceParameters.put("content", content);

        if (serviceParameters.get("content") == null || serviceParameters.getString("content").trim().length() == 0) {
            System.out.println("text moderation content is empty");
            return null;
        }

        TextModerationRequest textModerationRequest = new TextModerationRequest();
        /*
        文本检测service：内容安全控制台文本增强版规则配置的serviceCode，示例：chat_detection
        */
        textModerationRequest.setService(textService);
        textModerationRequest.setServiceParameters(serviceParameters.toJSONString());

        Map<String, String> resultMap = new HashMap<>();


        try {

            TextModerationResponse response = client.textModerationWithOptions(textModerationRequest, runtime);


            if (response!= null) {
//                JSONObject scrResponse = JSON.parseObject(new String(response.getHttpContent(), "UTF-8"));
                System.out.println(JSON.toJSONString(response, true));
                if (200 == response.getStatusCode()) {
                    TextModerationResponseBody result = response.getBody();
                    Integer code = result.getCode();
                    resultMap.put("suggestion", "pass");
                    if (code != null && code == 200) {

                        TextModerationResponseBody.TextModerationResponseBodyData data = result.getData();
                        String labels = data.getLabels();
                        if (labels != null) {
                            String[] labelArray = labels.split(",");
                            for (String label : labelArray) {
                                label = label.trim();
                                if (label.equals("ad") || label.equals("political_content") ||
                                        label.equals("profanity") || label.equals("contraband") ||
                                        label.equals("sexual_content") || label.equals("violence") ||
                                        label.equals("nonsense") || label.equals("negative_content") ||
                                        label.equals("religion") || label.equals("cyberbullying")) {
                                    resultMap.put("suggestion", "block");
                                }
                            }
                        }
                        String reason = data.getReason();
                        System.out.println("suggestion = [" + labels + "]");
                        resultMap.put("label", labels);
                        resultMap.put("reason", reason);
                        return resultMap;
                    } else {
                        return null;
                    }

                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}
