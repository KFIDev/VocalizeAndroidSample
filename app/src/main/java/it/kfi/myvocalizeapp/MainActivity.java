package it.kfi.myvocalizeapp;

import androidx.appcompat.app.AppCompatActivity;

import it.kfi.kvm.library.CONSTANTS;
import it.kfi.kvm.library.KVMClient;
import it.kfi.kvm.library.models.KVMCommandResponse;
import it.kfi.kvm.library.models.KVMCommandResponses;
import it.kfi.kvm.library.models.KVMCommands;
import it.kfi.kvm.library.utils.MyLogger;
import vocalize.ExitCodes;
import vocalize.KVMHelper;
import vocalize.PromptPriority;
import vocalize.VoicePrompts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {

    boolean isConfiguring;
    boolean isKVMPaused;
    boolean isOnSetup;
    boolean bIntenDriven = true;

    private static final String ACTION_VOCALIZE = "it.kfi.kvm.intentservice.action.VOCALIZE";
    private static final String ACTION_CONFIGURE_HEADSET = "it.kfi.kvm.intentservice.action.CONFIG_HEADSET";
    private static final String ACTION_RESET = "it.kfi.kvm.intentservice.action.RESET";
    private static final String EXTRA_COMMAND = "it.kfi.kvm.intentservice.extra.COMMAND";
    private static final String EXTRA_RECEIVER = "it.kfi.kvm.intentservice.extra.RECEIVER";
    private static final String EXTRA_RESULTS = "it.kfi.kvm.intentservice.extra.RESULTS";

    Button butSay;
    Button butSpeakNumbers;
    Button butDictateNote;
    Button butTestIntent;

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

        if (bIntenDriven) {
            startVocalizeService();
        } else {
            KVMHelper.getKVMHelper(this).startServiceBind(mMainMessenger);
        }

        setButtonSay();
        setButtongetNumbers();
        setButtonDictateNote();
        setButtonTestIntent();

        if(bIntenDriven){
            enableAllButtons(false);
        }
    }

    private void setButtonSay() {
        butSay = findViewById(R.id.butSpeak);
        butSay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                butSay.setEnabled(false);
                showtextInView("Working...");
                if (bIntenDriven) {
                    KVMCommands vis = new KVMCommands();
                    vis.newBuilder("speakOnlyPrompt")
                            .setPrompt("Dite pronto se siete pronti!")
                            .setResultType(CONSTANTS.ResultType.NONE)
                            .buildAndAdd();
                    sendCommandThroughIntent(vis);
                } else {
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
            }
        });
    }

    private void setButtongetNumbers() {
        butSpeakNumbers = findViewById(R.id.butSayNumbers);
        butSpeakNumbers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                KVMCommands voiceCmds = new KVMCommands();
                voiceCmds.newBuilder("demoNumbers")
                        .setPrompt("dite una serie di numeri")
                        .addGrammar("luxtest")
                        .setResultType(CONSTANTS.ResultType.RESULT_FROM_VOICE)
                        .buildAndAdd();

                showtextInView("Working...");

                if (bIntenDriven)
                    sendCommandThroughIntent(voiceCmds);
                else
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

    private void setButtonDictateNote() {
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

                if (bIntenDriven)
                    sendCommandThroughIntent(voiceCmds);
                else
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

    private void setButtonTestIntent() {
        butTestIntent = findViewById(R.id.butIntentInstruction);
        butTestIntent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showtextInView("build Vocalize command...");
                String voiceCmds = buildDemoIntentCommand();

                showtextInView("send Intent to Vocalize service...");
                sendCommandThroughIntent(voiceCmds);

            }
        });
        butTestIntent.setEnabled(bIntenDriven);
    }

    private void startVocalizeService() {
        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction("it.kfi.kvm.intentservice.STARTED");
        filter.addAction("it.kfi.kvm.intentservice.RESULTS");
        registerReceiver(myBroadcastReceiver, filter);

        Intent intent = new Intent();
        intent.setClassName("it.kfi.kvm", "it.kfi.kvm.intentservice.VocalizeService");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(intent);
        } else {
            this.startService(intent);
        }
    }

    private String buildDemoIntentCommand() {
        KVMCommands voiceCmds = new KVMCommands();
        voiceCmds.newBuilder("promptIntent")
                .setPrompt("comando tramite intent", PromptPriority.NO_PRIORITY)
                .setResultType(CONSTANTS.ResultType.RESULT_FROM_VOICE)
                .buildAndAdd();
        voiceCmds.newBuilder("numberIntent")
                .setPrompt("dite qualche numero")
                .setResultType(CONSTANTS.ResultType.RESULT_FROM_VOICE)
                .addGrammar("sil_digits")
                .buildAndAdd();

        Gson G = new Gson();

        return G.toJson(voiceCmds, KVMCommands.class);
    }

    private void sendCommandThroughIntent(KVMCommands voiceCmds) {
        enableAllButtons(false);
        Intent intent = new Intent();
        intent.setAction("it.kfi.kvm.intentservice.action.VOCALIZE");
        Bundle B = new Bundle();
        B.putParcelable("kvmcommand", voiceCmds);
        intent.putExtra("it.kfi.kvm.intentservice.extra.COMMAND", B);
        sendBroadcast(intent);
    }

    private void sendCommandThroughIntent(String voiceCmds) {
        enableAllButtons(false);
        Intent intent = new Intent();
        intent.setAction("it.kfi.kvm.intentservice.action.VOCALIZE");
        intent.putExtra("it.kfi.kvm.intentservice.extra.JSON_COMMAND", voiceCmds);
        sendBroadcast(intent);
    }

    private void showSpokenNumbers() {
        KVMCommandResponses resps = KVMHelper.getKVMHelper().getResultsSync();

        if (resps != null && resps.responses.size() > 0) {
            String respToShow = "";
            for (KVMCommandResponse resp : resps.responses) {
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

    private void safeWait(int msec) {
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void enableAllButtons(final boolean benable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                butDictateNote.setEnabled(benable);
                butSay.setEnabled(benable);
                butSpeakNumbers.setEnabled(benable);
                butTestIntent.setEnabled(benable);
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

    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("it.kfi.kvm.intentservice.RESULTS")) {
                Bundle command = intent.getBundleExtra(EXTRA_RESULTS);
                String answer = command.getString("results");
                showtextInView(answer);
                enableAllButtons(true);
            }

            if (action.equals("it.kfi.kvm.intentservice.STARTED")) {
                enableAllButtons(true);
            }
        }
    };
}
