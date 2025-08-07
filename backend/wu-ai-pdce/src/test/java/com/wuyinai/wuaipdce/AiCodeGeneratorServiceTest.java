package com.wuyinai.wuaipdce;

import com.wuyinai.wuaipdce.ai.AiCodeGeneratorService;
import com.wuyinai.wuaipdce.ai.model.HtmlCodeResult;
import com.wuyinai.wuaipdce.ai.model.MultiFileCodeResult;
import com.wuyinai.wuaipdce.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 测试一般的代码生成器
 */
@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做个程序员鱼皮的工作记录小工具,不超过20行");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiHtmlCode("做个程序员鱼皮的留言板");
        Assertions.assertNotNull(multiFileCode);
    }


}
