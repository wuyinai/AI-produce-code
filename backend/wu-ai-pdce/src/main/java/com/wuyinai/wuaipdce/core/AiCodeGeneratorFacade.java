package com.wuyinai.wuaipdce.core;

import com.wuyinai.wuaipdce.ai.AiCodeGeneratorService;
import com.wuyinai.wuaipdce.ai.model.HtmlCodeResult;
import com.wuyinai.wuaipdce.ai.model.MultiFileCodeResult;
import com.wuyinai.wuaipdce.exception.BusinessException;
import com.wuyinai.wuaipdce.exception.ErrorCode;
import com.wuyinai.wuaipdce.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观（门面）类，组合生成和保存功能
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成代码(流式输出)
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 模式类型
     * @return 生成的代码
     */
     public Flux<String> generateAndSaveCodeStreaming(String userMessage,CodeGenTypeEnum codeGenTypeEnum){
         if (codeGenTypeEnum == null) {
             throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
         }
         return switch(codeGenTypeEnum){
             case HTML -> generateHtmlCodeStreaming(userMessage);
             case MULTI_FILE -> generateMultiFileCodeStreaming(userMessage);
             default -> {
                 String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                 throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
             }
         };
     }



    /**
     * 生成 HTML 模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(result);
    }

    /**
     * 生成多文件模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiHtmlCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(result);
    }

    private Flux<String> generateHtmlCodeStreaming(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStreaming(userMessage);
        StringBuilder codeBuilder = new StringBuilder();
        return result
                .doOnNext(chunk -> {
                    codeBuilder.append(chunk);
                })
                .doOnComplete(()->{
                    try{
                        String completeHtmlCode = codeBuilder.toString();
                        HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
                        File saveDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
                        log.info("保存目录：{}", saveDir.getAbsolutePath());
                    }catch (Exception e){
                        log.error("解析HTML 保存失败:{}", e.getMessage());
                    }
                });
    }

    private Flux<String> generateMultiFileCodeStreaming(String userMessage) {
        Flux<String> stringFlux = aiCodeGeneratorService.generateMultiHtmlCodeStreaming(userMessage);
        StringBuilder codestringBuilder = new StringBuilder();
        return stringFlux
                .doOnNext(chunk->{
                    codestringBuilder.append(chunk);
                })
                .doOnComplete(()->{
                    try{
                        String completeMultiFileCode  = codestringBuilder.toString();
                        MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completeMultiFileCode);
                        File saveDir = CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
                        log.info("保存目录：{}", saveDir.getAbsolutePath());
                    }catch (Exception e){
                        log.error("解析多文件保存失败:{}", e.getMessage());
                    }
                });
    }

}
