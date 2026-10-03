/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: /data/data/com.termux/files/usr/bin/aidl -p/data/data/com.termux/files/home/android-sdk/platforms/android-34/framework.aidl -o/data/data/com.termux/files/home/AOSP-Email/Email/app/build/generated/aidl_source_output_dir/debug/out -I/data/data/com.termux/files/home/AOSP-Email/Email/app/src/main/aidl -I/data/data/com.termux/files/home/AOSP-Email/Email/emailcommon/src -I/data/data/com.termux/files/home/AOSP-Email/Email/app/src/debug/aidl -I/data/data/com.termux/files/home/.gradle/caches/9.7.0/transforms/11b07f7ed40de41a636484f855f5b628/transformed/media-1.7.0/aidl -I/data/data/com.termux/files/home/.gradle/caches/9.7.0/transforms/b6ce4972ec05b3b88593b39bf9531ca9/transformed/core-1.13.1/aidl -I/data/data/com.termux/files/home/.gradle/caches/9.7.0/transforms/86ef6ac3984348671af87074d77e8c65/transformed/versionedparcelable-1.1.1/aidl -d/data/data/com.termux/files/usr/tmp/aidl15822522566373828363.d /data/data/com.termux/files/home/AOSP-Email/Email/emailcommon/src/com/android/emailcommon/service/IAccountService.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package com.android.emailcommon.service;
public interface IAccountService extends android.os.IInterface
{
  /** Default implementation for IAccountService. */
  public static class Default implements com.android.emailcommon.service.IAccountService
  {
    @Override public int getAccountColor(long accountId) throws android.os.RemoteException
    {
      return 0;
    }
    @Override public android.os.Bundle getConfigurationData(java.lang.String accountType) throws android.os.RemoteException
    {
      return null;
    }
    @Override public java.lang.String getDeviceId() throws android.os.RemoteException
    {
      return null;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.android.emailcommon.service.IAccountService
  {
    /** Construct the stub and attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.android.emailcommon.service.IAccountService interface,
     * generating a proxy if needed.
     */
    public static com.android.emailcommon.service.IAccountService asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.android.emailcommon.service.IAccountService))) {
        return ((com.android.emailcommon.service.IAccountService)iin);
      }
      return new com.android.emailcommon.service.IAccountService.Stub.Proxy(obj);
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
        case TRANSACTION_getAccountColor:
        {
          long _arg0;
          _arg0 = data.readLong();
          int _result = this.getAccountColor(_arg0);
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        case TRANSACTION_getConfigurationData:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          android.os.Bundle _result = this.getConfigurationData(_arg0);
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_getDeviceId:
        {
          java.lang.String _result = this.getDeviceId();
          reply.writeNoException();
          reply.writeString(_result);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements com.android.emailcommon.service.IAccountService
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
      @Override public int getAccountColor(long accountId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeLong(accountId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getAccountColor, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public android.os.Bundle getConfigurationData(java.lang.String accountType) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.os.Bundle _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(accountType);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getConfigurationData, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, android.os.Bundle.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public java.lang.String getDeviceId() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getDeviceId, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
    }
    static final int TRANSACTION_getAccountColor = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_getConfigurationData = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_getDeviceId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
  }
  /** @hide */
  public static final java.lang.String DESCRIPTOR = "com.android.emailcommon.service.IAccountService";
  public int getAccountColor(long accountId) throws android.os.RemoteException;
  public android.os.Bundle getConfigurationData(java.lang.String accountType) throws android.os.RemoteException;
  public java.lang.String getDeviceId() throws android.os.RemoteException;
  /** @hide */
  static class _Parcel {
    static private <T> T readTypedObject(
        android.os.Parcel parcel,
        android.os.Parcelable.Creator<T> c) {
      if (parcel.readInt() != 0) {
          return c.createFromParcel(parcel);
      } else {
          return null;
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedObject(
        android.os.Parcel parcel, T value, int parcelableFlags) {
      if (value != null) {
        parcel.writeInt(1);
        value.writeToParcel(parcel, parcelableFlags);
      } else {
        parcel.writeInt(0);
      }
    }
  }
}
