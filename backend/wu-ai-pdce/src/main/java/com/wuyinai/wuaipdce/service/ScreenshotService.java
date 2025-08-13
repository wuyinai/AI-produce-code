package com.wuyinai.wuaipdce.service;

import org.springframework.stereotype.Service;

@Service
public interface ScreenshotService {
    String generateAndUploadScreenshot(String webUrl);
}
