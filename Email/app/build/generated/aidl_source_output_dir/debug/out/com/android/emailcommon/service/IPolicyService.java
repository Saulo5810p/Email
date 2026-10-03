/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: /data/data/com.termux/files/usr/bin/aidl -p/data/data/com.termux/files/home/android-sdk/platforms/android-34/framework.aidl -o/data/data/com.termux/files/home/AOSP-Email/Email/app/build/generated/aidl_source_output_dir/debug/out -I/data/data/com.termux/files/home/AOSP-Email/Email/app/src/main/aidl -I/data/data/com.termux/files/home/AOSP-Email/Email/emailcommon/src -I/data/data/com.termux/files/home/AOSP-Email/Email/app/src/debug/aidl -I/data/data/com.termux/files/home/.gradle/caches/9.7.0/transforms/11b07f7ed40de41a636484f855f5b628/transformed/media-1.7.0/aidl -I/data/data/com.termux/files/home/.gradle/caches/9.7.0/transforms/b6ce4972ec05b3b88593b39bf9531ca9/transformed/core-1.13.1/aidl -I/data/data/com.termux/files/home/.gradle/caches/9.7.0/transforms/86ef6ac3984348671af87074d77e8c65/transformed/versionedparcelable-1.1.1/aidl -d/data/data/com.termux/files/usr/tmp/aidl13791082379864177907.d /data/data/com.termux/files/home/AOSP-Email/Email/emailcommon/src/com/android/emailcommon/service/IPolicyService.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package com.android.emailcommon.service;
public interface IPolicyService extends android.os.IInterface
{
  /** Default implementation for IPolicyService. */
  public static class Default implements com.android.emailcommon.service.IPolicyService
  {
    @Override public boolean isActive(com.android.emailcommon.provider.Policy policies) throws android.os.RemoteException
    {
      return false;
    }
    @Override public void setAccountHoldFlag(long accountId, boolean newState) throws android.os.RemoteException
    {
    }
    // Legacy compatability for Exchange shipped with KK
    @Override public void setAccountPolicy(long accountId, com.android.emailcommon.provider.Policy policy, java.lang.String securityKey) throws android.os.RemoteException
    {
    }
    // New version
    @Override public void setAccountPolicy2(long accountId, com.android.emailcommon.provider.Policy policy, java.lang.String securityKey, boolean notify) throws android.os.RemoteException
    {
    }
    @Override public void remoteWipe() throws android.os.RemoteException
    {
    }
    @Override public boolean canDisableCamera() throws android.os.RemoteException
    {
      return false;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.android.emailcommon.service.IPolicyService
  {
    /** Construct the stub and attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.android.emailcommon.service.IPolicyService interface,
     * generating a proxy if needed.
     */
    public static com.android.emailcommon.service.IPolicyService asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.android.emailcommon.service.IPolicyService))) {
        return ((com.android.emailcommon.service.IPolicyService)iin);
      }
      return new com.android.emailcommon.service.IPolicyService.Stub.Proxy(obj);
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
        case TRANSACTION_isActive:
        {
          com.android.emailcommon.provider.Policy _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.android.emailcommon.provider.Policy.CREATOR);
          boolean _result = this.isActive(_arg0);
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        case TRANSACTION_setAccountHoldFlag:
        {
          long _arg0;
          _arg0 = data.readLong();
          boolean _arg1;
          _arg1 = (0!=data.readInt());
          this.setAccountHoldFlag(_arg0, _arg1);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_setAccountPolicy:
        {
          long _arg0;
          _arg0 = data.readLong();
          com.android.emailcommon.provider.Policy _arg1;
          _arg1 = _Parcel.readTypedObject(data, com.android.emailcommon.provider.Policy.CREATOR);
          java.lang.String _arg2;
          _arg2 = data.readString();
          this.setAccountPolicy(_arg0, _arg1, _arg2);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_setAccountPolicy2:
        {
          long _arg0;
          _arg0 = data.readLong();
          com.android.emailcommon.provider.Policy _arg1;
          _arg1 = _Parcel.readTypedObject(data, com.android.emailcommon.provider.Policy.CREATOR);
          java.lang.String _arg2;
          _arg2 = data.readString();
          boolean _arg3;
          _arg3 = (0!=data.readInt());
          this.setAccountPolicy2(_arg0, _arg1, _arg2, _arg3);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_remoteWipe:
        {
          this.remoteWipe();
          break;
        }
        case TRANSACTION_canDisableCamera:
        {
          boolean _result = this.canDisableCamera();
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements com.android.emailcommon.service.IPolicyService
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
      @Override public boolean isActive(com.android.emailcommon.provider.Policy policies) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, policies, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_isActive, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void setAccountHoldFlag(long accountId, boolean newState) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeLong(accountId);
          _data.writeInt(((newState)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_setAccountHoldFlag, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      // Legacy compatability for Exchange shipped with KK
      @Override public void setAccountPolicy(long accountId, com.android.emailcommon.provider.Policy policy, java.lang.String securityKey) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeLong(accountId);
          _Parcel.writeTypedObject(_data, policy, 0);
          _data.writeString(securityKey);
          boolean _status = mRemote.transact(Stub.TRANSACTION_setAccountPolicy, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      // New version
      @Override public void setAccountPolicy2(long accountId, com.android.emailcommon.provider.Policy policy, java.lang.String securityKey, boolean notify) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeLong(accountId);
          _Parcel.writeTypedObject(_data, policy, 0);
          _data.writeString(securityKey);
          _data.writeInt(((notify)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_setAccountPolicy2, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void remoteWipe() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_remoteWipe, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public boolean canDisableCamera() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_canDisableCamera, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
    }
    static final int TRANSACTION_isActive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_setAccountHoldFlag = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_setAccountPolicy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_setAccountPolicy2 = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_remoteWipe = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_canDisableCamera = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
  }
  /** @hide */
  public static final java.lang.String DESCRIPTOR = "com.android.emailcommon.service.IPolicyService";
  public boolean isActive(com.android.emailcommon.provider.Policy policies) throws android.os.RemoteException;
  public void setAccountHoldFlag(long accountId, boolean newState) throws android.os.RemoteException;
  // Legacy compatability for Exchange shipped with KK
  public void setAccountPolicy(long accountId, com.android.emailcommon.provider.Policy policy, java.lang.String securityKey) throws android.os.RemoteException;
  // New version
  public void setAccountPolicy2(long accountId, com.android.emailcommon.provider.Policy policy, java.lang.String securityKey, boolean notify) throws android.os.RemoteException;
  public void remoteWipe() throws android.os.RemoteException;
  public boolean canDisableCamera() throws android.os.RemoteException;
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
