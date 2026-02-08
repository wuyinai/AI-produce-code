package com.wuyinai.wuaipdce.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class VersionDiffUtils {

    private static final int MAX_DIFF_SIZE = 1024 * 1024;

    public static class FileDiff {
        private String path;
        private String hash;
        private String content;

        public FileDiff() {
        }

        public FileDiff(String path, String hash, String content) {
            this.path = path;
            this.hash = hash;
            this.content = content;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class IncrementalData {
        private List<FileDiff> added;
        private List<String> deleted;
        private List<FileDiff> modified;

        public IncrementalData() {
            this.added = new ArrayList<>();
            this.deleted = new ArrayList<>();
            this.modified = new ArrayList<>();
        }

        public List<FileDiff> getAdded() {
            return added;
        }

        public void setAdded(List<FileDiff> added) {
            this.added = added;
        }

        public List<String> getDeleted() {
            return deleted;
        }

        public void setDeleted(List<String> deleted) {
            this.deleted = deleted;
        }

        public List<FileDiff> getModified() {
            return modified;
        }

        public void setModified(List<FileDiff> modified) {
            this.modified = modified;
        }

        public int getTotalChanges() {
            return added.size() + deleted.size() + modified.size();
        }
    }

    public static class FileSnapshot {
        private Map<String, String> fileHashes;
        private Map<String, String> fileContents;

        public FileSnapshot() {
            this.fileHashes = new HashMap<>();
            this.fileContents = new HashMap<>();
        }

        public Map<String, String> getFileHashes() {
            return fileHashes;
        }

        public void setFileHashes(Map<String, String> fileHashes) {
            this.fileHashes = fileHashes;
        }

        public Map<String, String> getFileContents() {
            return fileContents;
        }

        public void setFileContents(Map<String, String> fileContents) {
            this.fileContents = fileContents;
        }
    }

    public static String calculateMD5(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("计算MD5失败", e);
            return "";
        }
    }

    public static FileSnapshot captureFileSnapshot(File sourceDir) {
        FileSnapshot snapshot = new FileSnapshot();
        
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            return snapshot;
        }

        List<File> files = FileUtil.loopFiles(sourceDir);
        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }

            String relativePath = file.getAbsolutePath().substring(sourceDir.getAbsolutePath().length());
            relativePath = relativePath.replace("\\", "/");

            String content = FileUtil.readUtf8String(file);
            String hash = calculateMD5(content);

            snapshot.getFileHashes().put(relativePath, hash);
            snapshot.getFileContents().put(relativePath, content);
        }

        return snapshot;
    }

    public static IncrementalData calculateDiff(FileSnapshot baseSnapshot, FileSnapshot currentSnapshot) {
        IncrementalData diff = new IncrementalData();

        Map<String, String> baseHashes = baseSnapshot.getFileHashes();
        Map<String, String> currentHashes = currentSnapshot.getFileHashes();

        Set<String> allPaths = new HashSet<>();
        allPaths.addAll(baseHashes.keySet());
        allPaths.addAll(currentHashes.keySet());

        for (String path : allPaths) {
            String baseHash = baseHashes.get(path);
            String currentHash = currentHashes.get(path);

            if (baseHash == null) {
                diff.getAdded().add(new FileDiff(path, currentHash, currentSnapshot.getFileContents().get(path)));
            } else if (currentHash == null) {
                diff.getDeleted().add(path);
            } else if (!baseHash.equals(currentHash)) {
                String oldContent = baseSnapshot.getFileContents().get(path);
                String newContent = currentSnapshot.getFileContents().get(path);
                
                int oldSize = oldContent.getBytes(StandardCharsets.UTF_8).length;
                int newSize = newContent.getBytes(StandardCharsets.UTF_8).length;
                
                String diffContent;
                if (oldSize == 0 || newSize == 0) {
                    diffContent = newContent;
                } else if (Math.abs(newSize - oldSize) > oldSize * 0.5) {
                    diffContent = newContent;
                } else {
                    String lineDiff = computeLineDiff(oldContent, newContent);
                    int diffSize = lineDiff.getBytes(StandardCharsets.UTF_8).length;
                    
                    if (diffSize < newSize * 0.8) {
                        diffContent = lineDiff;
                    } else {
                        diffContent = newContent;
                    }
                }
                
                diff.getModified().add(new FileDiff(path, currentHash, diffContent));
            }
        }

        return diff;
    }

    public static String computeLineDiff(String oldContent, String newContent) {
        if (StrUtil.isBlank(oldContent)) {
            return newContent;
        }
        if (StrUtil.isBlank(newContent)) {
            return "";
        }

        String[] oldLines = oldContent.split("\n");
        String[] newLines = newContent.split("\n");

        int[][] dp = new int[oldLines.length + 1][newLines.length + 1];
        for (int i = 1; i <= oldLines.length; i++) {
            for (int j = 1; j <= newLines.length; j++) {
                if (oldLines[i - 1].equals(newLines[j - 1])) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        List<String> diffLines = new ArrayList<>();
        int i = oldLines.length, j = newLines.length;
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && oldLines[i - 1].equals(newLines[j - 1])) {
                diffLines.add(" " + oldLines[i - 1]);
                i--;
                j--;
            } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
                diffLines.add("+ " + newLines[j - 1]);
                j--;
            } else if (i > 0) {
                diffLines.add("- " + oldLines[i - 1]);
                i--;
            }
        }

        Collections.reverse(diffLines);
        return String.join("\n", diffLines);
    }

    public static String applyDiff(String oldContent, String diffContent) {
        if (StrUtil.isBlank(diffContent)) {
            return oldContent;
        }
        if (StrUtil.isBlank(oldContent)) {
            return diffContent.startsWith("+ ") ? diffContent : oldContent;
        }

        String[] oldLines = oldContent.split("\n");
        String[] diffLines = diffContent.split("\n");

        List<String> result = new ArrayList<>();
        int oldIdx = 0;

        for (String diffLine : diffLines) {
            if (diffLine.startsWith("+ ")) {
                result.add(diffLine.substring(2));
            } else if (diffLine.startsWith("- ")) {
                if (oldIdx < oldLines.length && oldLines[oldIdx].equals(diffLine.substring(2))) {
                    oldIdx++;
                }
            } else if (diffLine.startsWith(" ")) {
                if (oldIdx < oldLines.length && oldLines[oldIdx].equals(diffLine.substring(1))) {
                    result.add(oldLines[oldIdx]);
                    oldIdx++;
                }
            }
        }

        while (oldIdx < oldLines.length) {
            result.add(oldLines[oldIdx]);
            oldIdx++;
        }

        return String.join("\n", result);
    }

    public static Map<String, Object> applyIncrementalData(
        Map<String, Object> baseSnapshot,
        IncrementalData incrementalData
    ) {
        Map<String, Object> result = new HashMap<>(baseSnapshot);

        List<Map<String, Object>> files = (List<Map<String, Object>>) result.get("files");
        if (files == null) {
            files = new ArrayList<>();
        }

        Map<String, Map<String, Object>> fileMap = new HashMap<>();
        for (Map<String, Object> file : files) {
            String path = (String) file.get("path");
            fileMap.put(path, file);
        }

        for (String deletedPath : incrementalData.getDeleted()) {
            fileMap.remove(deletedPath);
        }

        for (FileDiff added : incrementalData.getAdded()) {
            Map<String, Object> file = new HashMap<>();
            file.put("path", added.getPath());
            file.put("content", added.getContent());
            file.put("size", (long) added.getContent().getBytes(StandardCharsets.UTF_8).length);
            fileMap.put(added.getPath(), file);
        }

        for (FileDiff modified : incrementalData.getModified()) {
            Map<String, Object> file = fileMap.get(modified.getPath());
            if (file != null) {
                String oldContent = (String) file.get("content");
                String newContent = applyDiff(oldContent, modified.getContent());
                file.put("content", newContent);
                file.put("size", (long) newContent.getBytes(StandardCharsets.UTF_8).length);
            }
        }

        result.put("files", new ArrayList<>(fileMap.values()));

        Map<String, Object> metadata = (Map<String, Object>) result.get("metadata");
        if (metadata != null) {
            List<Map<String, Object>> fileList = (List<Map<String, Object>>) result.get("files");
            metadata.put("totalFiles", fileList.size());
            metadata.put("totalSize", fileList.stream()
                .mapToLong(f -> (Long) f.get("size"))
                .sum());
        }

        return result;
    }

    public static long calculateStorageSize(IncrementalData incrementalData) {
        long size = 0;
        for (FileDiff added : incrementalData.getAdded()) {
            size += added.getContent().getBytes(StandardCharsets.UTF_8).length;
        }
        for (FileDiff modified : incrementalData.getModified()) {
            size += modified.getContent().getBytes(StandardCharsets.UTF_8).length;
        }
        size += incrementalData.getDeleted().stream().mapToLong(String::length).sum();
        return size;
    }

    public static double calculateCompressionRatio(long incrementalSize, long fullSize) {
        if (fullSize == 0) {
            return 0.0;
        }
        double ratio = (1.0 - (double) incrementalSize / fullSize) * 100;
        return Math.round(ratio * 100.0) / 100.0;
    }

    public static String serializeFileHashes(Map<String, String> fileHashes) {
        return JSONUtil.toJsonStr(fileHashes);
    }

    public static Map<String, String> deserializeFileHashes(String fileHashesJson) {
        if (StrUtil.isBlank(fileHashesJson)) {
            return new HashMap<>();
        }
        return JSONUtil.toBean(fileHashesJson, Map.class);
    }

    public static String serializeIncrementalData(IncrementalData data) {
        return JSONUtil.toJsonStr(data);
    }

    public static IncrementalData deserializeIncrementalData(String dataJson) {
        if (StrUtil.isBlank(dataJson)) {
            return new IncrementalData();
        }
        return JSONUtil.toBean(dataJson, IncrementalData.class);
    }
}
