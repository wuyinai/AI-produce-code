package com.wuyinai.wuaipdce.core;

import cn.hutool.json.JSONUtil;
import com.wuyinai.wuaipdce.ai.AiCodeGeneratorService;
import com.wuyinai.wuaipdce.ai.AiCodeGeneratorServiceFactory;
import com.wuyinai.wuaipdce.ai.model.HtmlCodeResult;
import com.wuyinai.wuaipdce.ai.model.MultiFileCodeResult;
import com.wuyinai.wuaipdce.ai.model.message.AiResponseMessage;
import com.wuyinai.wuaipdce.ai.model.message.ToolExecutedMessage;
import com.wuyinai.wuaipdce.ai.model.message.ToolRequestMessage;
import com.wuyinai.wuaipdce.constant.AppConstant;
import com.wuyinai.wuaipdce.core.builder.VueProjectBuilder;
import com.wuyinai.wuaipdce.exception.BusinessException;
import com.wuyinai.wuaipdce.exception.ErrorCode;
import com.wuyinai.wuaipdce.model.enums.CodeGenTypeEnum;
import com.wuyinai.wuaipdce.core.parser.CodeParserExecutor;
import com.wuyinai.wuaipdce.core.saver.CodeFileSaverExecutor;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
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
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    /**
     * TokenStream与Flux适配方法
     * 适配方法需要监听tokenStream的AI响应，工具调用，工具调用完成等事件。将不同事件封装为不同的消息
     * @param tokenStream TokenStream对象
     * @return Flux对象
     * 为你生成代码：
     * 选择工具
     * 工具调用
     * ....
     * 生成代码结束。
     *
     *
     */


    private Flux<String> processTokenCodeStream(TokenStream tokenStream,Long appId) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse)->{//监听AI响应事件
                AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                //将AI响应封装为AiResponseMessage对象
                sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                //将消息对象转为JSON字符串并通过sink发射出去
            })
                    .onPartialToolExecutionRequest((index,toolExecutionRequest)->{
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })//监听工具调用事件
                    .onToolExecuted((ToolExecution toolExecution)->{
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })//监听工具调用完成事件
                    .onCompleteResponse((ChatResponse chatResponse)->{
                        // 执行Vue项目构建（同步执行，确保预览实时项目已就绪）
                        String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                        vueProjectBuilder.buildProject(projectPath);
                        sink.complete();
                    })//注册完整响应事件监听器，当AI完成整个响应时触发
                    .onError((Throwable error)->{
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }


    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum ,Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorServiceWithCache(appId);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appId);
                // 这个 switch 表达式根据 codeGenTypeEnum 的不同值执行不同的代码生成逻辑，
                // 并通过 yield 返回相应的处理结果。
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE,appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     */
    public Flux<String> generateAndSaveCodeStreaming(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorServiceWithCache(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStreaming(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML , appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiHtmlCodeStreaming(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                TokenStream codeStream = aiCodeGeneratorService.generateVueCodeStreaming(appId,userMessage);
                yield processTokenCodeStream(codeStream,appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 通用流式代码处理方法
     *
     * @param codeStream  代码流
     * @param codeGenType 代码生成类型
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType,Long appId) {
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(chunk -> {
            // 实时收集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            // 流式返回完成后保存代码
            try {
                String completeCode = codeBuilder.toString();
                // 使用执行器解析代码
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                // 使用执行器保存代码
                File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId);
                log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }

}
