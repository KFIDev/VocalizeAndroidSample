package vocalize;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.lang.ref.WeakReference;

import it.kfi.kvm.library.CONSTANTS;
import it.kfi.kvm.library.KVMClient;
import it.kfi.kvm.library.KVMHandler;
import it.kfi.kvm.library.exceptions.KVMServiceException;
import it.kfi.kvm.library.exceptions.ServiceUnboundException;
import it.kfi.kvm.library.models.KVMCommandResponse;
import it.kfi.kvm.library.models.KVMCommandResponses;
import it.kfi.kvm.library.models.KVMCommands;
import it.kfi.myvocalizeapp.MainActivity;

/**
 * Created by AMAGNONI on 09/03/2018.
 */

public class KVMHelper extends KVMHandler {

    Context context;
    WeakReference<Context> wctx;
    boolean isServiceBound = false;
	
	private static KVMHelper kvmHelper = null;
    private static KVMClient kvmClient = null;

    private Messenger mainActivityMessenger;
    private String TAG = "KVMHelper";
		
    private IASRSetupCallback iasrSetupCallback;
    private IKVMSetupCallback ikvmSetupCallback;
    private IVISCallback ivisCallback;
    private IVocalizeServiceConnected iVocalizeServiceConnected;
	

    public static KVMClient getKVM() {
        if (kvmClient == null) {
            kvmClient = new KVMClient();
        }
        return kvmClient;
    }

	public static KVMHelper getKVMHelper(Context ctx) {
        if (kvmHelper == null) {
            kvmHelper = new KVMHelper(ctx);
        }
        kvmHelper.refreshContext(ctx);
        return kvmHelper;
    }

    public static KVMHelper getKVMHelper() {
        return kvmHelper;
    }

    public static void ungetKVMHelper(){
        if (kvmHelper != null) {
            kvmHelper.stopServiceBind();
        }
        kvmHelper = null;
    }

    private static boolean headsetStatus;
    public static boolean isHeadsetConnected() {
        return headsetStatus;
    }
    public static void setHeadsetStatus(boolean headsetStat) {
        headsetStatus = headsetStat;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if(wctx.get() != null) getKVM().unbind(wctx.get());
        isServiceBound = false;
    }

    public interface IVISCallback {
        void onCompleted(ExitCodes exitCode);

        void onAborted(ExitCodes exitCode);

        void onSingleInstructionResult(KVMCommandResponse response);
    }

    public interface IASRSetupCallback {
        void onCompleted();

        void onAborted();
    }

    public interface IKVMSetupCallback {
        void onCompleted();

        void onAborted();
    }

    public interface IVocalizeServiceConnected{
        void onConnected();
    }

    public void startNoiseSample(IASRSetupCallback iasc) {
        try {
            iasrSetupCallback = iasc;
            getKVM().startASRConfigSync();

        } catch (RemoteException | ServiceUnboundException | KVMServiceException e) {
            e.printStackTrace();
            ungetKVMHelper();
            isServiceBound = false;
            if(ivisCallback!=null) ivisCallback.onAborted(ExitCodes.RESET);

        }
    }

    public void getHeadsetStatus(){
        try {
            setHeadsetStatus(getKVM().getHeasetStatusSync());
        } catch (ServiceUnboundException | KVMServiceException e) {
            e.printStackTrace();
            ungetKVMHelper();
            isServiceBound = false;
            if(ivisCallback!=null) {
                ivisCallback.onAborted(ExitCodes.RESET);
            }
        }
    }

    public void doSetUserId(String userid){
        try {
            getKVM().setProperties(userid, "");
        } catch (RemoteException | ServiceUnboundException | KVMServiceException e) {
            e.printStackTrace();
            ungetKVMHelper();
            isServiceBound = false;
            if(ivisCallback!=null) ivisCallback.onAborted(ExitCodes.RESET);
        }
    }


    public void startHeadsetConfig() {
        try {
            getKVM().startHeadsetConfigSync();
        } catch (RemoteException | ServiceUnboundException | KVMServiceException e) {
            e.printStackTrace();
            ungetKVMHelper();
            isServiceBound = false;
            if(ivisCallback!=null) ivisCallback.onAborted(ExitCodes.RESET);
        }
    }

    public boolean getExternalBtStatus(){
        try {
            return getKVM().getExternalBtStatusSync();
        } catch (ServiceUnboundException | KVMServiceException e) {
            e.printStackTrace();
            ungetKVMHelper();
            isServiceBound = false;
            if(ivisCallback!=null) {
                ivisCallback.onAborted(ExitCodes.RESET);
            }
            
            return false;
        }
    }


    public void startExternalBtConfig() {
        try {
            getKVM().startExtBtConfigSync();
        } catch (RemoteException | ServiceUnboundException | KVMServiceException e) {
            e.printStackTrace();
            ungetKVMHelper();
            isServiceBound = false;
            if(ivisCallback!=null) ivisCallback.onAborted(ExitCodes.RESET);
        }
    }

    public void doConfigureKVM(){
        try {
            getKVM().startConfigSync();
        } catch (RemoteException | ServiceUnboundException |  KVMServiceException e) {
            e.printStackTrace();
            ungetKVMHelper();
            isServiceBound = false;
            if(ivisCallback!=null) ivisCallback.onAborted(ExitCodes.RESET);

        }
    }

    public void doPauseKVM(){
        try {
            getKVM().pause();
        } catch (RemoteException | ServiceUnboundException  e) {
            e.printStackTrace();
            ungetKVMHelper();
            isServiceBound = false;
            if(ivisCallback!=null) ivisCallback.onAborted(ExitCodes.RESET);

        }
    }

    public void doResumeKVM(){
        try {
            getKVM().resume();
        } catch (RemoteException | ServiceUnboundException  e) {
            e.printStackTrace();
            ungetKVMHelper();
            isServiceBound = false;
            if(ivisCallback!=null) ivisCallback.onAborted(ExitCodes.RESET);

        }
    }

    public void doResetKVM(){
        try {
            getKVM().reset();
        } catch (RemoteException | ServiceUnboundException  e) {
            e.printStackTrace();
            ungetKVMHelper();
            isServiceBound = false;
            if(ivisCallback!=null) ivisCallback.onAborted(ExitCodes.RESET);

        }
    }

    public void execInstructions(KVMCommands vis, IVISCallback iviscallback) {
        try {
            ivisCallback = iviscallback;
            getKVM().executeCommands(vis);
        } catch (RemoteException | ServiceUnboundException e) {
            e.printStackTrace();
            ungetKVMHelper();
            isServiceBound = false;
            if(ivisCallback!=null) ivisCallback.onAborted(ExitCodes.RESET);

        }
    }

    private KVMHelper(Context ctx) {
        super();
        context = ctx;
        wctx = new WeakReference<>(ctx);
    }

    public void refreshContext(Context ctx){
        wctx = new WeakReference<>(ctx);
    }

    public void stopServiceBind(){
        getKVM().unbind(context);
    }

    public void startServiceBind(Messenger mainActivityhandler){
        this.mainActivityMessenger =  mainActivityhandler;
        if (!isServiceBound) {
            Log.d(TAG, "startServiceBind()");
            getKVM().bind(context, this);
            isServiceBound=true;
        }
    }

    public void startServiceBind(Messenger mainActivityhandler, IVocalizeServiceConnected ivsc) {
        this.mainActivityMessenger = mainActivityhandler;
        this.iVocalizeServiceConnected = ivsc;
        if (!isServiceBound) {
            Log.d(TAG, "startServiceBind()");
            if(wctx.get()!=null) getKVM().bind(wctx.get(), this);
            isServiceBound = true;
        } else {
            iVocalizeServiceConnected.onConnected();
        }
    }

    public KVMCommandResponses getResultsSync(){
        try {
            return getKVM().getResultsSync();
        } catch (RemoteException | ServiceUnboundException | KVMServiceException e) {
            e.printStackTrace();
            ungetKVMHelper();
            isServiceBound = false;
            if(ivisCallback!=null) ivisCallback.onAborted(ExitCodes.RESET);

        }

        return null;
    }

    private void sendKVMPauseChangeToActivity(Boolean isPaused) {
        if (mainActivityMessenger != null) {
            Message msg = Message.obtain(null, MainActivity.MainMessages.KVM_PAUSE_STATE.getValue());
            Bundle bdata = new Bundle();
            bdata.putBoolean("IS_KVM_PAUSED", isPaused);
            msg.setData(bdata);
            try {
                mainActivityMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }


    public boolean getServiceBoundState(){
        return isServiceBound;
    }
	
	/**
	* Events form the service
	**/

    @Override
    public void onCommandError(KVMCommandResponse response) {
        Log.d(TAG, "onCommandError()" + response.result);
        if(ivisCallback!=null) ivisCallback.onAborted(ExitCodes.SEVERE);
    }

    @Override
    public void onCommandCancel(String commandName) {
        Log.d(TAG, "onCommandCancel()");
        if(ivisCallback!=null) ivisCallback.onAborted(ExitCodes.REWIND);
    }

    @Override
    public void onCommandSuccess(KVMCommandResponse response) {
        Log.d(TAG, "onCommandSuccess()");
        if(ivisCallback!=null) ivisCallback.onSingleInstructionResult(response);
    }

    @Override
    public void onExecCommandsStarted() {
        Log.d(TAG, "onExecCommandsStarted()");
    }

    @Override
    public void onExecCommandsFinished() {
        Log.d(TAG, "onExecCommandsFinished()");
        if(ivisCallback!=null) ivisCallback.onCompleted(ExitCodes.SUCCESS);
    }

    @Override
    public void onExecCommandsError(String error) {
        Log.d(TAG, "onExecCommandsError(): " + error);
        if(ivisCallback!=null) ivisCallback.onAborted(ExitCodes.SEVERE);
    }

    @Override
    public void onGetStatus(String status, String currentInstructionName) {
        Log.d(TAG, "onGetStatus()");
        if(status.equals(CONSTANTS.BUNDLE_HEADSET_STATUS_TAG)){
            Log.d(TAG, "Headset Status: " + currentInstructionName);
            setHeadsetStatus(currentInstructionName.equals("TRUE"));
        }
    }

    @Override
    public void onGetResponses(KVMCommandResponses responses) {
        Log.d(TAG, "onGetResponses()");
    }

    @Override
    public void onGetResponsesError(String error) {
        Log.d(TAG, "onGetResponsesError()");
    }

    @Override
    public void onLoadProfileError(String error) {
        Log.d(TAG, "onLoadProfileError()");
    }

    @Override
    public void onLoadProfileSuccess() {
        Log.d(TAG, "onLoadProfileSuccess()");
    }

    @Override
    public void onPaused() {
        Log.d(TAG, "onPaused()");
        sendKVMPauseChangeToActivity(true);
    }

    @Override
    public void onPauseError(String error) {
        Log.d(TAG, "onPauseError()");
        doResetKVM();
    }

    @Override
    public void onResumed() {
        Log.d(TAG, "onResumed()");
        sendKVMPauseChangeToActivity(false);
    }

    @Override
    public void onResumeError(String error) {
        Log.d(TAG, "onResumeError()");
    }

    @Override
    public void onReset() {
        Log.d(TAG, "onReset()");
        if(ivisCallback!=null) ivisCallback.onAborted(ExitCodes.RESET);
    }

    @Override
    public void onConfigStarted() {
        Log.d(TAG, "onConfigStarted()");
    }

    @Override
    public void onStartConfigError(String error) {
        Log.d(TAG, "onStartConfigError()");
    }

    @Override
    public void onConfigASRStarted() {
        if(iasrSetupCallback!=null) iasrSetupCallback.onCompleted();
        Log.d(TAG, "onConfigASRStarted()");
    }

    @Override
    public void onStartASRConfigError(String error) {
        Log.d(TAG, "onStartASRConfigError()");
    }

    @Override
    public void onPropertiesChanged() {

    }

    @Override
    public void onConfigError(String error) {
        Log.d(TAG, "onConfigError()");
        if(iasrSetupCallback!=null) iasrSetupCallback.onAborted();
    }

    @Override
    public void onConfigSuccess(String error) {
        Log.d(TAG, "onConfigSuccess()");
        if(iasrSetupCallback!=null) iasrSetupCallback.onCompleted();
    }

}
