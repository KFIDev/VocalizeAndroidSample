package it.kfi.myvocalizeapp;

import androidx.appcompat.app.AppCompatActivity;

import it.kfi.kvm.library.CONSTANTS;
import it.kfi.kvm.library.KVMClient;
import it.kfi.kvm.library.models.KVMCommandResponse;
import it.kfi.kvm.library.models.KVMCommandResponses;
import it.kfi.kvm.library.models.KVMCommands;
import vocalize.ExitCodes;
import vocalize.KVMHelper;
import vocalize.PromptPriority;
import vocalize.VoicePrompts;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {

    boolean isConfiguring;
    boolean isKVMPaused;
    boolean isOnSetup;

    Button butSay;
    Button butSpeakNumbers;
    Button butDictateNote;

    public enum MainMessages {
        TO_BE_LOGGED(0),
        TO_BE_SHOWED(1),
        HEADSET_STATE_CHANGE(2),
        KVM_PAUSE_STATE(3),
        EXTERNAL_BT_SETUP(5);
        private int value;

        MainMessages(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        KVMHelper.getKVMHelper(this).startServiceBind(mMainMessenger);

        setButtonSay();
        setButtongetNumbers();
        setButtonDictateNote();
    }

    private void setButtonSay() {
        butSay = findViewById(R.id.butSpeak);
        butSay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                butSay.setEnabled(false);
                showtextInView("Working...");
                VoicePrompts.speakOnlyPrompt("Dite pronto se siete pronti!", new KVMHelper.IVISCallback() {
                    @Override
                    public void onCompleted(ExitCodes exitCode) {
                        butSay.setEnabled(true);
                        showtextInView("Fatto!");
                    }

                    @Override
                    public void onAborted(ExitCodes exitCode) {

                    }

                    @Override
                    public void onSingleInstructionResult(KVMCommandResponse response) {

                    }
                });
            }
        });
    }

    private void setButtongetNumbers() {
        butSpeakNumbers = findViewById(R.id.butSayNumbers);
        butSpeakNumbers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showtextInView("Working...");
                KVMCommands voiceCmds = new KVMCommands();
                voiceCmds.newBuilder("demoNumbers")
                        .setPrompt("dite una serie di numeri")
                        .addGrammar("sil_digits")
                        .setResultType(CONSTANTS.ResultType.RESULT_FROM_VOICE)
                        .buildAndAdd();

                KVMHelper.getKVMHelper().execInstructions(voiceCmds, new KVMHelper.IVISCallback() {
                    @Override
                    public void onCompleted(ExitCodes exitCode) {
                        showSpokenNumbers();
                    }

                    @Override
                    public void onAborted(ExitCodes exitCode) {
                    }

                    @Override
                    public void onSingleInstructionResult(KVMCommandResponse response) {
                    }
                });

            }
        });
    }

    private void setButtonDictateNote(){
        butDictateNote = findViewById(R.id.butDictate);
        butDictateNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showtextInView("Working...");
                KVMCommands voiceCmds = new KVMCommands();
                voiceCmds.newBuilder("demoNumbers")
                        .setPrompt("pronuncia una nota libera", PromptPriority.NO_PRIORITY)
                        .setResultType(CONSTANTS.ResultType.RESULT_FROM_VOICE)
                        .buildAndAdd();

                KVMHelper.getKVMHelper().execInstructions(voiceCmds, new KVMHelper.IVISCallback() {
                    @Override
                    public void onCompleted(ExitCodes exitCode) {
                        showSpokenNumbers();
                    }

                    @Override
                    public void onAborted(ExitCodes exitCode) {
                    }

                    @Override
                    public void onSingleInstructionResult(KVMCommandResponse response) {
                    }
                });

            }
        });
    }

    private void showSpokenNumbers() {
        KVMCommandResponses resps = KVMHelper.getKVMHelper().getResultsSync();

        if(resps != null && resps.responses.size()>0){
            String respToShow = "";
            for(KVMCommandResponse resp:resps.responses){
                respToShow += resp.result;
            }

            showtextInView(respToShow);
        }

    }

    private void showtextInView(String textToShow) {
        final String S = textToShow;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tvText = findViewById(R.id.textView);
                tvText.setText(S);
            }
        });
    }

    Handler messageForMainActivity = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

            if (message.what == MainMessages.TO_BE_LOGGED.getValue()) {
                Bundle datatbl = message.getData();
                String data = datatbl.getString("DATALOGGER");

            } else if (message.what == MainMessages.TO_BE_SHOWED.getValue()) {
                Bundle datatbl = message.getData();
                String data = datatbl.getString("DATATOSHOW");
                String stitle = datatbl.getString("TITLEDATATOSHOW");

            } else if (message.what == MainMessages.KVM_PAUSE_STATE.getValue()) {
                Bundle kvmBundleState = message.getData();
                isKVMPaused = kvmBundleState.getBoolean("IS_KVM_PAUSED");

            } else if (message.what == MainMessages.EXTERNAL_BT_SETUP.getValue()) {
                isOnSetup = true;
                KVMHelper.getKVMHelper(MainActivity.this).startExternalBtConfig();

            }

            return true;
        }
    });
    final Messenger mMainMessenger = new Messenger(messageForMainActivity);


}
