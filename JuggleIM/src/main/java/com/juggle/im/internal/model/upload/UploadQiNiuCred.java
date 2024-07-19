package com.juggle.im.internal.model.upload;

import androidx.annotation.NonNull;

/**
 * @author Ye_Guli
 * @create 2024-05-28 16:10
 */
public class UploadQiNiuCred {
   private String mDomain;
   private String mToken;

   public String getDomain() {
      return mDomain;
   }

   public void setDomain(String domain) {
      this.mDomain = domain;
   }

   public String getToken() {
      return mToken;
   }

   public void setToken(String token) {
      this.mToken = token;
   }

   @NonNull
   @Override
   public String toString() {
      return "UploadQiNiuCred{" +
              "mDomain='" + mDomain + '\'' +
              ", mToken='" + mToken + '\'' +
              '}';
   }
}