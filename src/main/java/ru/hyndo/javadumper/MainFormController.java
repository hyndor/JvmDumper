package ru.hyndo.javadumper;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.sun.tools.attach.VirtualMachineDescriptor;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;
import ru.hyndo.javadumper.outmode.AsmifierOutMode;
import ru.hyndo.javadumper.outmode.FernFlowerOutMode;
import ru.hyndo.javadumper.outmode.OutMode;
import ru.hyndo.javadumper.outmode.RawByteCodeOutMode;
import sun.jvm.hotspot.memory.SystemDictionary;
import sun.jvm.hotspot.oops.Klass;
import sun.jvm.hotspot.runtime.VM;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"RegExpSingleCharAlternation", "RegExpRedundantEscape"})
public class MainFormController implements Initializable {

    private static final String[] KEYWORDS = new String[] {
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    private VMsProvider vMsProvider;

    @FXML
    private TreeView<String> treeClassView;
    private CodeArea codeArea;
    @FXML
    private ChoiceBox<OutMode> outputMode;
    @FXML
    private ChoiceBox<VirtualMachineDescriptor> applicationPid;
    @FXML
    private Button dumpVmButton;
    @FXML
    private Button refreshVMsButton;
    @FXML
    private GridPane secondInnerGridPane;
    private Path tempOutPutDirectory;

    private void initClassOutputInfo() {
    }

    private void initApplicationPids() {
        applicationPid.setConverter(new StringConverter<VirtualMachineDescriptor>() {
            @Override
            public String toString(VirtualMachineDescriptor vm) {
                return vmToString(vm);
            }

            @Override
            public VirtualMachineDescriptor fromString(String string) {
                throw new UnsupportedOperationException("Cannot convert");
            }
        });
        vMsProvider
                .getVMs()
                .stream()
                .filter(Objects::nonNull)
                .forEach(vm ->
                        applicationPid
                                .getItems()
                                .add(vm));
        Iterator<VirtualMachineDescriptor> vmiter = applicationPid.getItems().iterator();
        if(vmiter.hasNext()) {
            applicationPid.setValue(vmiter.next());
        }
    }

    private String vmToString(VirtualMachineDescriptor vm) {
        if(vm.displayName().length() > 90) {
            return vm.id() + " " + vm.displayName().substring(0, 90);
        }
        return vm.id() + " " + vm.displayName();
    }

    private void initChoiceBox() {
        OutMode asmifier = new AsmifierOutMode(codeArea);
        outputMode.getItems().add(asmifier);
        outputMode.getItems().add(new FernFlowerOutMode(tempOutPutDirectory, codeArea));
        outputMode.getItems().add(new RawByteCodeOutMode(codeArea));
        outputMode.setValue(asmifier);
        outputMode.setConverter(new StringConverter<OutMode>() {
            @Override
            public String toString(OutMode object) {
                return object.name();
            }

            @Override
            public OutMode fromString(String string) {
                throw new UnsupportedOperationException();
            }
        });
    }

    private void initTreeClassview() {
        treeClassView.setShowRoot(false);
        treeClassView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null) return;
            if(newValue.getChildren().isEmpty()) {
                String fullClassName = newValue.getParent().getValue() + "." + newValue.getValue();
                if(applicationPid.getValue() !=  null) {
                    VmDataProvider vmDataProvider = new VmDataProvider(applicationPid.getValue(), VmDataProvider.DumpMode.DUMP_CLASS, fullClassName, tempOutPutDirectory);
                    vmDataProvider.dumpClass(outputMode.getValue());
                }
            }
        });
    }

    public MainFormController setVMsProvider(VMsProvider vMsProvider) {
        this.vMsProvider = vMsProvider;
        return this;
    }

    private void initClassOutputInfoElement() {
        codeArea = new CodeArea();
        codeArea.setEditable(false);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        Subscription cleanupWhenNoLongerNeedIt = codeArea
                .multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .subscribe(ignore -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));
        secondInnerGridPane.add(codeArea, 0, 1, 1, 1);
    }

    public void init() {
        try {
            tempOutPutDirectory = Files.createTempDirectory("jvmdumper");
        } catch (IOException e) {
            e.printStackTrace();
        }
        initClassOutputInfoElement();
        initTreeClassview();
        initChoiceBox();
        initClassOutputInfo();
        initApplicationPids();
        StyleClassedTextArea textArea = new StyleClassedTextArea();

        dumpVmButton.setOnMouseClicked(event -> {
            if(event.getClickCount() == 1) {
                updateCurrentVM();
            }
        });
        refreshVMsButton.setOnMouseClicked(event -> {
            if(event.getClickCount() == 1) {
                applicationPid.getItems().clear();
                initApplicationPids();
            }
        });
    }

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("BRACE") != null ? "brace" :
                                            matcher.group("BRACKET") != null ? "bracket" :
                                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                                            matcher.group("STRING") != null ? "string" :
                                                                    matcher.group("COMMENT") != null ? "comment" :
                                                                            null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private void updateCurrentVM() {
        Set<String> classNames = new VmDataProvider(applicationPid.getValue(), VmDataProvider.DumpMode.CLASS_LIST).getClassNames();
        //       package, classname
        Multimap<String, String> packages = HashMultimap.create();
        TreeItem<String> root = new TreeItem<>("Root");
        for (String className : classNames) {
            int index = className.lastIndexOf(".");
            if(index != -1) {
                String packageName = className.substring(0, index);
                String klass = className.substring(index + 1);
                packages.put(packageName, klass);
            }
        }
        Map<String, TreeItem<String>> treeItemsOfPackages = new TreeMap<>();
        packages.forEach((packageName, klass) -> {
            TreeItem<String> treeItem = treeItemsOfPackages.computeIfAbsent(packageName, (__) -> new TreeItem<>(packageName));
            treeItem.getChildren().add(new TreeItem<>(klass));
        });
        root.getChildren().addAll(treeItemsOfPackages.values());
        treeClassView.setRoot(root);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
