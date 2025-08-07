package com.wuyinai.wuaipdce.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.wuyinai.wuaipdce.ai.model.HtmlCodeResult;
import com.wuyinai.wuaipdce.ai.model.MultiFileCodeResult;
import com.wuyinai.wuaipdce.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class CodeFileSaver {

    // 文件保存根目录
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 保存 HtmlCodeResult
     */
    public static File saveHtmlCodeResult(HtmlCodeResult result) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());// value: html
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        //writeToFile 方法将 HTML 内容以 UTF-8 编码写入文件。
        return new File(baseDirPath);
    }

    /**
     * 保存 MultiFileCodeResult
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult result) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());// value: multi_file
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        writeToFile(baseDirPath, "style.css", result.getCssCode());
        writeToFile(baseDirPath, "script.js", result.getJsCode());
        return new File(baseDirPath);
    }

    /**
     * 构建唯一目录路径：tmp/code_output/bizType_雪花ID
     */
    private static String buildUniqueDir(String bizType) {
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        //获取到文件类型，然后拼接雪花ID
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        //File.separator 是通用文件路径分隔符，比如是 Windows 系统，那么分隔符是 \，
        // 如果是 Linux 系统，那么分隔符是 /
        FileUtil.mkdir(dirPath);
        //创建目录
        return dirPath;
    }

    /**
     * 写入单个文件
     */
    private static void writeToFile(String dirPath, String filename, String content) {
        String filePath = dirPath + File.separator + filename;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }
}
