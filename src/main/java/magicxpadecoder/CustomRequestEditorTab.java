package magicxpadecoder;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

public class CustomRequestEditorTab implements BurpExtension {
    private MontoyaApi api;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;

        api.extension().setName("MagicXPA Decoder");

        // Register tab on requests and responses
        MyHttpRequestResponseEditorProvider editorProvider = new MyHttpRequestResponseEditorProvider(api);

        api.userInterface().registerHttpRequestEditorProvider(editorProvider);
        api.userInterface().registerHttpResponseEditorProvider(editorProvider);

        api.logging().logToOutput("Extension successfully loaded!");
    }
}
