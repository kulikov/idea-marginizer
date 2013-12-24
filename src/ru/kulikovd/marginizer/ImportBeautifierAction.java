package ru.kulikovd.marginizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang.StringUtils;


/**
 */
public class ImportBeautifierAction extends AnAction {

    public ImportBeautifierAction() {
        super("ImportBeautifier");
    }

    /**
     * Disable when no editor available
     */
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(event.getData(PlatformDataKeys.EDITOR) != null);
    }

    /**
     * Perform action
     */
    public void actionPerformed(final AnActionEvent event) {
        Project currentProject = event.getData(PlatformDataKeys.PROJECT);

        CommandProcessor.getInstance().executeCommand(currentProject, new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        ImportBeautifier.run(event);
                    }
                });
            }
        }, "ImportBeautifier", UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION);
    }
}


class ImportBeautifier {

    private static Pattern packagaRegexp = Pattern.compile("^\\s*package ([^;\\s]+)", Pattern.MULTILINE);

    private static Comparator<String> comparator = new Comparator<String>() {
        @Override
        public int compare(String s, String s2) {
            return s.replaceAll("[\\{_]", "1").compareTo(s2.replaceAll("[\\{_]", "1"));
        }
    };

    public static void run(final AnActionEvent event) {

        Editor editor = event.getData(PlatformDataKeys.EDITOR);

        if (editor != null) {
            final Document document = editor.getDocument();

            SelectionModel selectionModel = editor.getSelectionModel();
            boolean hasSelection = selectionModel.hasSelection();

            String text = document.getText();
            Matcher packageMatch = packagaRegexp.matcher(text);

            List<String> jvGroup = new ArrayList<String>();
            List<String> scGroup = new ArrayList<String>();
            List<String> party3Group = new ArrayList<String>();
            List<String> userGroup = new ArrayList<String>();

            if (packageMatch.find()) {
                String pack = packageMatch.group(1);
                String[] splits = pack.split("\\.");
                String prefix = splits[0] + "." + splits[1];

                String txt = "";
                int offsetStart = 0;
                int offsetEnd = 0;

                if (hasSelection) {
                    offsetStart = document.getLineStartOffset(document.getLineNumber(selectionModel.getSelectionStart()));
                    offsetEnd = document.getLineEndOffset(document.getLineNumber(selectionModel.getSelectionEnd()));
                    txt = document.getCharsSequence().subSequence(offsetStart, offsetEnd).toString();
                } else {
                    offsetStart = document.getLineStartOffset(2);
                    txt = document.getCharsSequence().subSequence(offsetStart, document.getCharsSequence().length()).toString();

                    int n = 1;
                    for (String l : txt.split("\n")) {
                        if (l.startsWith("import") || l.trim().equals("")) {
                            n += 1;
                        } else {
                            break;
                        }
                    }

                    offsetEnd = document.getLineEndOffset(n);
                    txt = document.getCharsSequence().subSequence(offsetStart, offsetEnd).toString();
                }

                Matcher m = Pattern.compile("^\\s*").matcher(txt);
                String startSpaces = m.find() ? m.group(0) : "";

                m = Pattern.compile("\\s*$").matcher(txt);
                String endSpaces = m.find() ? m.group(0) : "";

                String[] lines = txt.split("import ");

                if (lines.length > 1) {
                    for (String t : lines) {
                        String tS = t.trim();
                        if (tS.isEmpty()) {
                            continue;
                        }
                        if (tS.startsWith("java.")) {
                            jvGroup.add(tS);
                        } else if (tS.startsWith("scala.")) {
                            jvGroup.add(tS); // scala and java in single group
                        } else if (tS.startsWith(prefix)) {
                            userGroup.add(tS);
                        } else {
                            party3Group.add(tS);
                        }
                    }

                    Collections.sort(jvGroup, comparator);
                    Collections.sort(scGroup, comparator);
                    Collections.sort(userGroup, comparator);
                    Collections.sort(party3Group, comparator);

                    String result = "";
                    if (!jvGroup.isEmpty()) {
                        result += "\n\nimport " + StringUtils.join(jvGroup, "\nimport ");
                    }
                    if (!scGroup.isEmpty()) {
                        result += "\n\nimport " + StringUtils.join(scGroup, "\nimport ");
                    }
                    if (!party3Group.isEmpty()) {
                        result += "\n\nimport " + StringUtils.join(party3Group, "\nimport ");
                    }
                    if (!userGroup.isEmpty()) {
                        String maxPref = maxPrefix(pack, userGroup);

                        if (maxPref == null) {
                            result += "\n\nimport " + StringUtils.join(userGroup, "\nimport ");
                        } else {
                            List<String> thisModule = new ArrayList<String>();
                            List<String> otherModule = new ArrayList<String>();
                            for (String t : userGroup) {
                                if (t.startsWith(maxPref)) {
                                    thisModule.add(t);
                                } else {
                                    otherModule.add(t);
                                }
                            }
                            if (!otherModule.isEmpty())
                                result += "\n\nimport " + StringUtils.join(otherModule, "\nimport ");
                            if (!thisModule.isEmpty())
                                result += "\n\nimport " + StringUtils.join(thisModule, "\nimport ");
                        }
                    }
                    document.replaceString(offsetStart, offsetEnd, startSpaces + result.trim() + endSpaces);
                }
            }
        }
    }

    private static String maxPrefix(String pack, List<String> userGroup) {
        int matchCount = 0;
        for (String t : userGroup) {
            if (t.startsWith(pack)) {
                if (matchCount > 0) {
                    return pack;
                }
                matchCount++;
            }
        }
        String[] splits = pack.split("\\.");
        if (splits.length > 3) {
            return maxPrefix(pack.substring(0, pack.lastIndexOf(".")), userGroup);
        }
        return null;
    }

    public static void log(String msg) {
        Messages.showMessageDialog(msg, "Info", Messages.getInformationIcon());
    }
}
