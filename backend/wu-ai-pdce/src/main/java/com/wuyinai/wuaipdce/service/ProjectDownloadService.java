package com.wuyinai.wuaipdce.service;

import jakarta.servlet.http.HttpServletResponse;


public interface ProjectDownloadService {

    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
