package vocalize;

import java.util.Map;
import java.util.Set;

import it.kfi.kvm.library.CONSTANTS;
import it.kfi.kvm.library.models.KVMCommands;
import vocalize.KVMHelper;
import vocalize.MyStringUtils;

/**
 * Created by AMAGNONI on 21/03/2018.
 */

public class VoicePrompts {

    public static void speakOnlyPrompt(String sPrompt, KVMHelper.IVISCallback ivisCallback) {
        KVMCommands vis = new KVMCommands();
        vis.newBuilder("speakOnlyPrompt")
                .setPrompt(sPrompt)
                .setResultType(CONSTANTS.ResultType.NONE)
                .buildAndAdd();
        KVMHelper.getKVMHelper().execInstructions(vis, ivisCallback);
    }

    public static void speakPromptWithConfirmation(String sPrompt, KVMHelper.IVISCallback ivisCallback) {
        KVMCommands vis = new KVMCommands();
        vis.newBuilder("speakPromptWithConfirmation")
                .setPrompt(sPrompt, PromptPriority.LISTEN_ALL)
                .setResultType(CONSTANTS.ResultType.RESULT_FROM_VOICE)
                .addGrammar("confirmations")
                .buildAndAdd();
        KVMHelper.getKVMHelper().execInstructions(vis, ivisCallback);

    }

    public static void speakYesNoPrompt(String sPrompt, KVMHelper.IVISCallback ivisCallback) {
        KVMCommands vis = new KVMCommands();

        vis.newBuilder("speakYesNoPrompt")
                .setPrompt(sPrompt)
                .setResultType(CONSTANTS.ResultType.RESULT_FROM_VOICE)
                .addGrammar("yesno")
                .buildAndAdd();

        KVMHelper.getKVMHelper().execInstructions(vis, ivisCallback);

    }

    public static void speakChoiceFromList(Map<String, String> mapList, String sPrompt, KVMHelper.IVISCallback ivisCallback){
        KVMCommands vis = new KVMCommands();
        final String finalVisName = "flow_exit";

        // la key della mappa diventa l'instruction name, il value si utilizza per il prompt
        Set<String> sets = mapList.keySet();

        // il dialogo vien interrotto quando l'operatore accetta una opzione, va ripetuto se risponde "no" a tutti
        for(String skeyname : sets){
            String svalue = MyStringUtils.rtrim(mapList.get(skeyname));
            svalue += sPrompt;
            vis.newBuilder(skeyname)
                    .setPrompt(svalue)
                    .setResultType(CONSTANTS.ResultType.RESULT_FROM_VOICE)
                    .addGrammar("yesno")
                    .addExpectedResult("si", finalVisName)
                    .addExpectedResult("no", null)
                    .setValidationPrompt("comando %s non valido")
                    .buildAndAdd();
        }

        vis.newBuilder(finalVisName)
                .setResultType(CONSTANTS.ResultType.NONE)
                .buildAndAdd();

        KVMHelper.getKVMHelper().execInstructions(vis, ivisCallback);

    }
}
