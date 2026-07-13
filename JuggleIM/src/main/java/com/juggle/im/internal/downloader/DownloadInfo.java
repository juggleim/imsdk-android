package com.juggle.im.internal.downloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that stores downloaded file info
 *
 * @author lvhongzhen
 */
public class DownloadInfo{
    private String tag;
    // File stream storage path
    private String filePath;
    // File info storage path
    private String infoPath;

    // Download URL
    private String url;
    // File size
    private long length;
    // Whether downloading is in progress
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
     * Gets the file size
     *
     * @return File size
     */
    public long getLength() {
        return length;
    }

    /**
     * Sets the file size
     *
     * @param length File size
     */
    public void setLength(long length) {
        this.length = length;
    }

    /**
     * Get completed download progress
     *
     * @return Completed download progress
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
     * Get file name
     *
     * @return File name
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Set file name
     *
     * @param filePath File name
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Gets the download URL
     *
     * @return Download URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the download URL
     *
     * @param url Download URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets whether downloading is in progress
     *
     * @return Whether downloading is in progress
     */
    public boolean isDownLoading() {
        return isDownLoading;
    }

    /**
     * Sets whether downloading is in progress
     *
     * @param downLoading Whether downloading is in progress
     */
    public void setDownLoading(boolean downLoading) {
        isDownLoading = downLoading;
    }

    public static class SliceInfo implements IDownloadInfo {
        // Slice index
        private int partNumber;
        // Slice share of download progress
        private int proportion;
        // Slice stream size
        private long maxLength;
        // Current downloaded size
        private long currentLength;
        // Current download start point
        private long startRange;
        // Current download end point
        private long endRange;
        // Slice cache stream storage path
        private String slicePath;
        // Slice info storage path
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
