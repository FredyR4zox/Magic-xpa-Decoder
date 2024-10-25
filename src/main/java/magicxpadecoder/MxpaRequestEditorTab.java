package magicxpadecoder;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

public class MxpaRequestEditorTab implements BurpExtension {
    private MontoyaApi api;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;

        api.extension().setName("Magic xpa Decoder");

        // Register tab on requests and responses
        MxpaHttpRequestResponseEditorProvider editorProvider = new MxpaHttpRequestResponseEditorProvider(api);

        api.userInterface().registerHttpRequestEditorProvider(editorProvider);
        api.userInterface().registerHttpResponseEditorProvider(editorProvider);

        api.http().registerHttpHandler(new MxpaHttpHandler(api));

        api.logging().logToOutput("Extension successfully loaded!");
    }
}
