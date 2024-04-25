package com.heima.common.aliyun;

import com.alibaba.fastjson.JSON;
import com.aliyun.green20220302.Client;
import com.aliyun.green20220302.models.ImageModerationRequest;
import com.aliyun.green20220302.models.ImageModerationResponse;
import com.aliyun.green20220302.models.ImageModerationResponseBody;
import com.aliyun.green20220302.models.ImageModerationResponseBody.ImageModerationResponseBodyData;
import com.aliyun.green20220302.models.ImageModerationResponseBody.ImageModerationResponseBodyDataResult;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.green.model.v20180509.ImageSyncScanRequest;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.http.HttpResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.heima.common.aliyun.util.ClientUploader;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.*;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aliyun")
public class GreenImageScan {

    private String accessKeyId;
    private String secret;
    private  String imageService;

    /**
     * 创建请求客户端
     *
     * @param accessKeyId
     * @param accessKeySecret
     * @param endpoint
     * @return
     * @throws Exception
     */
    public static Client createClient(String accessKeyId, String accessKeySecret, String endpoint) throws Exception {
        Config config = new Config();
        config.setAccessKeyId(accessKeyId);
        config.setAccessKeySecret(accessKeySecret);
        // 设置http代理。
        //config.setHttpProxy("http://10.10.xx.xx:xxxx");
        // 设置https代理。
        //config.setHttpsProxy("https://10.10.xx.xx:xxxx");
        // 接入区域和地址请根据实际情况修改
        // 接入地址列表：https://help.aliyun.com/document_detail/467828.html?#section-uib-qkw-0c8
        config.setEndpoint(endpoint);
        return new Client(config);
    }

    public static ImageModerationResponse invokeFunction(String accessKeyId, String accessKeySecret, String endpoint,String service,List<byte[]> imageList) throws Exception {
        //注意，此处实例化的client请尽可能重复使用，避免重复建立连接，提升检测性能。
        Client client = createClient(accessKeyId, accessKeySecret, endpoint);

        // 创建RuntimeObject实例并设置运行参数
        RuntimeOptions runtime = new RuntimeOptions();

        // 检测参数构造。
        Map<String, String> serviceParameters = new HashMap<>();
        //公网可访问的URL。
        serviceParameters.put("imageUrl", imageList.toString());
        //待检测数据唯一标识
        serviceParameters.put("dataId", UUID.randomUUID().toString());

        ImageModerationRequest request = new ImageModerationRequest();
        // 图片检测service：内容安全控制台图片增强版规则配置的serviceCode，示例：baselineCheck
        // 支持service请参考：https://help.aliyun.com/document_detail/467826.html?0#p-23b-o19-gff
        request.setService(service);
        request.setServiceParameters(JSON.toJSONString(serviceParameters));

        ImageModerationResponse response = null;
        try {
            response = client.imageModerationWithOptions(request, runtime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public Map imageScan(List<byte[]> imageList) throws Exception {

        // 接入区域和地址请根据实际情况修改。
        ImageModerationResponse response = invokeFunction(accessKeyId, secret, "green-cip.cn-shanghai.aliyuncs.com",imageService,imageList);
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("suggestion", "pass");
        try {

            return  resultMap;
            // 自动路由。
//            if (response != null) {
//                //区域切换到cn-beijing。
//                if (500 == response.getStatusCode() || (response.getBody() != null && 500 == (response.getBody().getCode()))) {
//                    // 接入区域和地址请根据实际情况修改。
//                    response = invokeFunction(accessKeyId, secret, "green-cip.cn-beijing.aliyuncs.com",imageService,imageList);
//                }
//            }
//            // 打印检测结果。
//
//            if (response != null) {
//                ImageModerationResponseBody body = null;
//                if (response.getStatusCode() == 200) {
//                    resultMap.put("suggestion", "pass");
//                    body = response.getBody();
//                    System.out.println("requestId=" + body.getRequestId());
//                    System.out.println("code=" + body.getCode());
//                     System.out.println("msg=" + body.getMsg());
//                    if (body.getCode() == 200) {
//                        ImageModerationResponseBodyData data = body.getData();
//                        System.out.println("dataId=" + data.getDataId());
//                        List<ImageModerationResponseBodyDataResult> results = data.getResult();
//                        for (ImageModerationResponseBodyDataResult result : results) {
//
//                            String labels = result.getLabel();
//                            if (labels != null) {
//                                String[] labelArray = labels.split(",");
//                                for (String label : labelArray) {
//                                    label = label.trim();
//                                    if (label.equals("pornographic_adultContent") || label.equals("pornographic_adultContent_tii") ||
//                                            label.equals("sexual_suggestiveContent") || label.equals("sexual_partialNudity") ||
//                                            label.equals("political_politicalFigure") || label.equals("political_politicalFigure_name_tii") ||
//                                            label.equals("political_politicalFigure_metaphor_tii") || label.equals("political_TVLogo") ||
//                                            label.equals("political_map") || label.equals("political_racism_tii") ||
//                                            label.equals("violent_horrificContent") || label.equals("violent_horrific_tii") ||
//                                            label.equals("contraband_drug") || label.equals("contraband_gamble") ||
//                                            label.equals("contraband_gamble_tii") || label.equals("political_historicalNihility_tii") ||
//                                            label.equals("political_religion_tii") || label.equals("political_taintedCelebrity")) {
//                                        resultMap.put("suggestion", "block");
//                                        resultMap.put("label", label);
//                                    }
//                                }
//                            }
//                            return resultMap;
//
//                        }
//
//                    }
//                } else {
//                    System.out.println("image moderation not success. code:" + body.getCode());
//                    return  resultMap;
//                }
//            } else {
//                    System.out.println("response not success. status:" + response.getStatusCode());
//                    return null;
//                }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;


    }

}
