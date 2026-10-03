/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: /data/data/com.termux/files/usr/bin/aidl -p/data/data/com.termux/files/home/android-sdk/platforms/android-34/framework.aidl -o/data/data/com.termux/files/home/AOSP-Email/Email/app/build/generated/aidl_source_output_dir/debug/out -I/data/data/com.termux/files/home/AOSP-Email/Email/app/src/main/aidl -I/data/data/com.termux/files/home/AOSP-Email/Email/emailcommon/src -I/data/data/com.termux/files/home/AOSP-Email/Email/app/src/debug/aidl -I/data/data/com.termux/files/home/.gradle/caches/9.7.0/transforms/11b07f7ed40de41a636484f855f5b628/transformed/media-1.7.0/aidl -I/data/data/com.termux/files/home/.gradle/caches/9.7.0/transforms/b6ce4972ec05b3b88593b39bf9531ca9/transformed/core-1.13.1/aidl -I/data/data/com.termux/files/home/.gradle/caches/9.7.0/transforms/86ef6ac3984348671af87074d77e8c65/transformed/versionedparcelable-1.1.1/aidl -d/data/data/com.termux/files/usr/tmp/aidl17081456394576421399.d /data/data/com.termux/files/home/AOSP-Email/Email/emailcommon/src/com/android/emailcommon/service/IEmailServiceCallback.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package com.android.emailcommon.service;
public interface IEmailServiceCallback extends android.os.IInterface
{
  /** Default implementation for IEmailServiceCallback. */
  public static class Default implements com.android.emailcommon.service.IEmailServiceCallback
  {
    /*
     * Ordinary results:
     *   statuscode = 1, progress = 0:      "starting"
     *   statuscode = 0, progress = n/a:    "finished"
     * 
     * If there is an error, it must be reported as follows:
     *   statuscode = err, progress = n/a:  "stopping due to error"
     * 
     * *Optionally* a callback can also include intermediate values from 1..99 e.g.
     *   statuscode = 1, progress = 0:      "starting"
     *   statuscode = 1, progress = 30:     "working"
     *   statuscode = 1, progress = 60:     "working"
     *   statuscode = 0, progress = n/a:    "finished"
     */
    /**
     * Callback to indicate that a particular attachment is being synced
     * messageId = the message that owns the attachment
     * attachmentId = the attachment being synced
     * statusCode = 0 for OK, 1 for progress, other codes for error
     * progress = 0 for "start", 1..100 for optional progress reports
     */
    @Override public void loadAttachmentStatus(long messageId, long attachmentId, int statusCode, int progress) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.android.emailcommon.service.IEmailServiceCallback
  {
    /** Construct the stub and attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.android.emailcommon.service.IEmailServiceCallback interface,
     * generating a proxy if needed.
     */
    public static com.android.emailcommon.service.IEmailServiceCallback asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.android.emailcommon.service.IEmailServiceCallback))) {
        return ((com.android.emailcommon.service.IEmailServiceCallback)iin);
      }
      return new com.android.emailcommon.service.IEmailServiceCallback.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      if (code == INTERFACE_TRANSACTION) {
        reply.writeString(descriptor);
        return true;
      }
      switch (code)
      {
        case TRANSACTION_loadAttachmentStatus:
        {
          long _arg0;
          _arg0 = data.readLong();
          long _arg1;
          _arg1 = data.readLong();
          int _arg2;
          _arg2 = data.readInt();
          int _arg3;
          _arg3 = data.readInt();
          this.loadAttachmentStatus(_arg0, _arg1, _arg2, _arg3);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements com.android.emailcommon.service.IEmailServiceCallback
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      /*
       * Ordinary results:
       *   statuscode = 1, progress = 0:      "starting"
       *   statuscode = 0, progress = n/a:    "finished"
       * 
       * If there is an error, it must be reported as follows:
       *   statuscode = err, progress = n/a:  "stopping due to error"
       * 
       * *Optionally* a callback can also include intermediate values from 1..99 e.g.
       *   statuscode = 1, progress = 0:      "starting"
       *   statuscode = 1, progress = 30:     "working"
       *   statuscode = 1, progress = 60:     "working"
       *   statuscode = 0, progress = n/a:    "finished"
       */
      /**
       * Callback to indicate that a particular attachment is being synced
       * messageId = the message that owns the attachment
       * attachmentId = the attachment being synced
       * statusCode = 0 for OK, 1 for progress, other codes for error
       * progress = 0 for "start", 1..100 for optional progress reports
       */
      @Override public void loadAttachmentStatus(long messageId, long attachmentId, int statusCode, int progress) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeLong(messageId);
          _data.writeLong(attachmentId);
          _data.writeInt(statusCode);
          _data.writeInt(progress);
          boolean _status = mRemote.transact(Stub.TRANSACTION_loadAttachmentStatus, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_loadAttachmentStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
  }
  /** @hide */
  public static final java.lang.String DESCRIPTOR = "com.android.emailcommon.service.IEmailServiceCallback";
  /*
   * Ordinary results:
   *   statuscode = 1, progress = 0:      "starting"
   *   statuscode = 0, progress = n/a:    "finished"
   * 
   * If there is an error, it must be reported as follows:
   *   statuscode = err, progress = n/a:  "stopping due to error"
   * 
   * *Optionally* a callback can also include intermediate values from 1..99 e.g.
   *   statuscode = 1, progress = 0:      "starting"
   *   statuscode = 1, progress = 30:     "working"
   *   statuscode = 1, progress = 60:     "working"
   *   statuscode = 0, progress = n/a:    "finished"
   */
  /**
   * Callback to indicate that a particular attachment is being synced
   * messageId = the message that owns the attachment
   * attachmentId = the attachment being synced
   * statusCode = 0 for OK, 1 for progress, other codes for error
   * progress = 0 for "start", 1..100 for optional progress reports
   */
  public void loadAttachmentStatus(long messageId, long attachmentId, int statusCode, int progress) throws android.os.RemoteException;
}
