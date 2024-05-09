package com.heima.article.test;


import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.ArticleApplication;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes= ArticleApplication.class)
@RunWith(SpringRunner.class)
public class ArticleFreemarkerTest {
    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    @Autowired
    private Configuration configuration;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private ApArticleService apArticleService;
    @Test
    public void testAllFreemarker() throws Exception {
        //1.获取所有文章id
        ApArticle apArticle = new ApArticle();
        List<ApArticle> apArticles = apArticleService.list();
        for (ApArticle article : apArticles) {
            //2.获取文章内容
            ApArticleContent apArticleContent = apArticleContentMapper
                    .selectOne(Wrappers
                            .<ApArticleContent>lambdaQuery()
                            .eq(ApArticleContent::getArticleId, article.getId()));
            if(apArticleContent!=null&& StringUtils.isNotBlank(apArticleContent.getContent())){
                //3.文章内容通过freemarker生成静态html页面
                Template template = configuration.getTemplate("article.ftl");
                //3.1 创建模型
                Map<String,Object> content=new HashMap();
                content.put("content", JSONArray.parseArray(apArticleContent.getContent()));
                //3.2 输出流
                StringWriter writer = new StringWriter();
                //3.3 合成方法
                template.process(content,writer);
                //4.把静态页面上传到minio
                //4.1 文件流
                InputStream inputStream = new ByteArrayInputStream(writer.toString().getBytes());
                String path = fileStorageService.uploadHtmlFile("",apArticleContent.getArticleId()+".html",inputStream);
                //5.把静态页面的路径保存到数据库
                apArticleService.update(Wrappers
                        .<ApArticle>lambdaUpdate()
                        .eq(ApArticle::getId,apArticleContent.getArticleId())
                        .set(ApArticle::getStaticUrl,path));
            }
        }
    }
}
