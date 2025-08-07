package com.wuyinai.wuaipdce;

import com.wuyinai.wuaipdce.core.AiCodeGeneratorFacade;
import com.wuyinai.wuaipdce.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("构建一个登录页面，最多不超过30行", CodeGenTypeEnum.MULTI_FILE);
        Assertions.assertNotNull(file);
    }
}
