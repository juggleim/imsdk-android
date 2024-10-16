package com.juggle.im.internal.logger.action;

import com.juggle.im.internal.UploadManager;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.internal.logger.IJLog;

/**
 * @author Ye_Guli
 * @create 2024-05-23 11:23
 */
class UploadDefaultRunnable extends UploadRunnable {
    private static final String TAG = "UploadDefaultRunnable";
    private String mMessageId;
    private JIMCore mCore;

    public void setMessageId(String messageId) {
        mMessageId = messageId;
    }

    public void setCore(JIMCore core) {
        mCore = core;
    }

    @Override
    public void doRealUpload(String filePath) {
        UploadManager uploadManager = new UploadManager(mCore);
        uploadManager.uploadLog(filePath, mMessageId, new IJLog.Callback() {
            @Override
            public void onSuccess() {
                notifyUploadActionCallbackSuccess();
                finish();
            }

            @Override
            public void onError(int code, String msg) {
                notifyUploadActionCallbackFail(code, msg);
                finish();
            }
        });
    }

//    private void doRealUploadByAction(File logFile) {
//        try {
//            doPostRequest(mUploadUrl, logFile, mRequestHeaders);
//        } catch (Exception e) {
//            e.printStackTrace();
//            notifyUploadActionCallbackFail(-1, "doRealUploadByAction failed, e= " + e.getMessage());
//        }
//    }

//    private void doPostRequest(String mUploadUrl, File file, Map<String, String> mRequestHeaders) {
//        String boundary = UUID.randomUUID().toString();
//        InputStream inputStream = null;
//        OutputStream outputStream = null;
//        HttpURLConnection connection = null;
//        ByteArrayOutputStream responseStream;
//        byte[] buffer = new byte[2048];
//        try {
//            URL url = new URL(mUploadUrl);
//            connection = (HttpURLConnection) url.openConnection();
//            if (connection instanceof HttpsURLConnection) {
//                ((HttpsURLConnection) connection).setHostnameVerifier(new HostnameVerifier() {
//                    @SuppressLint("BadHostnameVerifier")
//                    @Override
//                    public boolean verify(String hostname, SSLSession session) {
//                        return true;
//                    }
//                });
//            }
//            connection.setReadTimeout(Constants.LOG_UPLOAD_TIME_OUT);
//            connection.setConnectTimeout(Constants.LOG_UPLOAD_TIME_OUT);
//            connection.setDoInput(true);
//            connection.setDoOutput(true);
//            connection.setUseCaches(false);
//            connection.setAllowUserInteraction(false);
//            connection.setChunkedStreamingMode(0);
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Charset", Constants.LOG_UPLOAD_CHARSET);
//            connection.setRequestProperty("connection", Constants.LOG_UPLOAD_KEEP_ALIVE);
//            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
//            //添加请求头
//            Set<Map.Entry<String, String>> entrySet = mRequestHeaders.entrySet();
//            for (Map.Entry<String, String> tempEntry : entrySet) {
//                connection.addRequestProperty(tempEntry.getKey(), tempEntry.getValue());
//            }
//            outputStream = connection.getOutputStream();
//            //写入文件参数
//            if (file != null) {
//                String fileParam = new StringBuilder()
//                        .append(Constants.LOG_UPLOAD_PREFIX)
//                        .append(boundary)
//                        .append(Constants.LOG_UPLOAD_LINE_END)
//                        .append("Content-Disposition: form-data; name=\"log\"; filename=\"").append(file.getName()).append("\"").append(Constants.LOG_UPLOAD_LINE_END)
//                        .append("Content-Type: application/octet-stream; charset=").append(Constants.LOG_UPLOAD_CHARSET).append(Constants.LOG_UPLOAD_LINE_END)
//                        .append(Constants.LOG_UPLOAD_LINE_END)
//                        .toString();
//                outputStream.write(fileParam.getBytes());
//                //写入文件
//                FileInputStream fileInputStream = new FileInputStream(file);
//                int bytesRead;
//                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
//                    outputStream.write(buffer, 0, bytesRead);
//                }
//                fileInputStream.close();
//                outputStream.write(Constants.LOG_UPLOAD_LINE_END.getBytes());
//            }
//            //请求结束标志
//            byte[] end_data = (Constants.LOG_UPLOAD_PREFIX + boundary + Constants.LOG_UPLOAD_PREFIX + Constants.LOG_UPLOAD_LINE_END).getBytes();
//            outputStream.write(end_data);
//            outputStream.flush();
//            //得到响应码
//            int statusCode = connection.getResponseCode();
//            if (statusCode / 100 == 2) {
//                responseStream = new ByteArrayOutputStream();
//                inputStream = connection.getInputStream();
//                int bytesRead;
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    responseStream.write(buffer, 0, bytesRead);
//                }
//                String resultData = responseStream.toString();
//                JSONObject jsonResponse = new JSONObject(resultData);
//                int code = jsonResponse.getInt("code");
//                if (code == 0) {
//                    notifyUploadActionCallbackSuccess();
//                } else {
//                    notifyUploadActionCallbackFail(-1, "doPostRequest failed, resultData= " + resultData);
//                }
//            } else {
//                notifyUploadActionCallbackFail(-1, "doPostRequest failed, statusCode= " + statusCode);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            notifyUploadActionCallbackFail(-1, "doPostRequest failed, e= " + e.getMessage());
//        } finally {
//            if (outputStream != null) {
//                try {
//                    outputStream.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            if (inputStream != null) {
//                try {
//                    inputStream.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            if (connection != null) {
//                try {
//                    connection.disconnect();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            //上传完成后删除文件
//            if (file != null) {
//                try {
//                    file.delete();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
}