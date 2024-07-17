package com.jet.im.internal.downloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 保存下载文件信息类
 *
 * @author lvhongzhen
 */
public class DownloadInfo{
    private String tag;
    // 文件流存储路径
    private String filePath;
    // 文件信息存储路径
    private String infoPath;

    // 下载地址
    private String url;
    // 文件大小
    private long length;
    // 是否正在下载
    private boolean isDownLoading;

    private List<SliceInfo> sliceInfoList = new ArrayList<>();
    private List<String> sliceInfoPathList = new ArrayList<>();

    public List<String> getSliceInfoPathList() {
        return sliceInfoPathList;
    }

    public void addSliceInfo(SliceInfo info) {
        this.sliceInfoList.add(info);
    }

    public void addSliceInfoPath(String infoPath) {
        this.sliceInfoPathList.add(infoPath);
    }

    public List<SliceInfo> getSliceInfoList() {
        return sliceInfoList;
    }

    public DownloadInfo() {
        // default implementation ignored
    }

    public DownloadInfo(String filePath, String url, String tag) {
        this.filePath = filePath;
        this.url = url;
        this.tag = tag;
    }

    public String getInfoPath() {
        return infoPath;
    }

    public void setInfoPath(String infoPath) {
        this.infoPath = infoPath;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    /**
     * 获取文件大小
     *
     * @return 文件大小
     */
    public long getLength() {
        return length;
    }

    /**
     * 设置文件大小
     *
     * @param length 文件大小
     */
    public void setLength(long length) {
        this.length = length;
    }

    /**
     * 获取下载已完成进度
     *
     * @return 下载以已完成进度
     */
    public boolean isFinished() {
        for (SliceInfo info : sliceInfoList) {
            if (!info.isFinish()) {
                return false;
            }
        }
        return true;
    }

    public long currentFileLength() {
        int result = 0;
        for (SliceInfo info : sliceInfoList) {
            result += info.getCurrentLength();
        }
        return result;
    }

    public int currentProgress() {
        return (int) (currentFileLength() * 100 / length);
    }

    /**
     * 获取文件名
     *
     * @return 文件名
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * 设置文件名
     *
     * @param filePath 文件名
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 获取下载地址
     *
     * @return 下载地址
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置下载地址
     *
     * @param url 下载地址
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取是否正在下载
     *
     * @return 是否正在下载
     */
    public boolean isDownLoading() {
        return isDownLoading;
    }

    /**
     * 设置是否正在下载
     *
     * @param downLoading 是否正在下载
     */
    public void setDownLoading(boolean downLoading) {
        isDownLoading = downLoading;
    }

    public static class SliceInfo implements IDownloadInfo {
        // 分片索引
        private int partNumber;
        // 分片占下载进度比
        private int proportion;
        // 分片流大小
        private long maxLength;
        // 当前已下载大小
        private long currentLength;
        // 当前下载开始节点
        private long startRange;
        // 当前下载结束节点
        private long endRange;
        // 分片缓存流存放地址
        private String slicePath;
        // 分片信息存放地址
        private String infoPath;

        private String url;
        private String tag;
        private Map<String, String> header = new HashMap<>();

        public SliceInfo() {
            // default implementation ignored
        }
        public void setPartNumber(int partNumber) {
            this.partNumber = partNumber;
        }

        public void setProportion(int proportion) {
            this.proportion = proportion;
        }

        public void setSlicePath(String slicePath) {
            this.slicePath = slicePath;
        }

        public void setInfoPath(String infoPath) {
            this.infoPath = infoPath;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public boolean isFinish() {
            return currentLength >= maxLength;
        }

        public int getPartNumber() {
            return partNumber;
        }

        public String getSlicePath() {
            return slicePath;
        }

        public long getStartRange() {
            return startRange;
        }

        public long getEndRange() {
            return endRange;
        }

        public void setStartRange(long startRange) {
            this.startRange = startRange;
        }

        public void setEndRange(long endRange) {
            this.endRange = endRange;
        }

        public long getMaxLength() {
            return maxLength;
        }

        public long getCurrentLength() {
            return currentLength;
        }

        public void setMaxLength(long maxLength) {
            this.maxLength = maxLength;
        }

        public void setCurrentLength(long currentLength) {
            this.currentLength = currentLength;
        }

        public String getInfoPath() {
            return infoPath;
        }

        public int getCurrentProportion() {
            return (int) (proportion * currentLength / maxLength);
        }

        public long getCurrentRange() {
            return startRange + currentLength;
        }

        public int getProportion() {
            return proportion;
        }

        @Override
        public String getSavePath() {
            return getSlicePath();
        }

        @Override
        public String getDownloadUrl() {
            return url;
        }

        @Override
        public long getFileLength() {
            return maxLength;
        }

        @Override
        public String getTag() {
            return tag;
        }

        public Map<String, String> getHeader() {
            return header;
        }

        public void setHeader(Map<String, String> header) {
            if (header != null) {
                this.header.putAll(header);
            }
        }
    }
}
