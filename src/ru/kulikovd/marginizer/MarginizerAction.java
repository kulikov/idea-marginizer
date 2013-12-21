package ru.kulikovd.marginizer;

import java.util.ArrayList;
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
public class MarginizerAction extends AnAction {

    public MarginizerAction() {
        super("Marginizer");
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
                        Marginizer.run(event);
                    }
                });
            }
        }, "Marginizer", UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION);
    }
}


class Marginizer {

    public static void run(final AnActionEvent event) {

        Editor editor = event.getData(PlatformDataKeys.EDITOR);

        if (editor != null) {
            final Document document = editor.getDocument();

            SelectionModel selectionModel = editor.getSelectionModel();
            boolean hasSelection = selectionModel.hasSelection();

            if (hasSelection) {
                int offsetStart = document.getLineStartOffset(document.getLineNumber(selectionModel.getSelectionStart()));
                int offsetEnd   = document.getLineEndOffset(document.getLineNumber(selectionModel.getSelectionEnd()));

                String txt = document.getCharsSequence().subSequence(offsetStart, offsetEnd).toString();

                String[] lines = txt.split("/\\r\\n|\\n|\\r/");

                Pattern pt = Pattern.compile("^([^→=⇒]+)([→=⇒].+)$");

                ArrayList<ArrayList<String>> output = new ArrayList<ArrayList<String>>(lines.length);

                int maxLen = 0;

                for (String l : lines) {
                    Matcher m = pt.matcher(l);

                    ArrayList<String> nl = new ArrayList<String>();

                    if (m.find()) {
                        nl.add(0, "");
                        nl.add(1, m.group(1).replaceAll("\\s+$", ""));
                        nl.set(0, nl.get(1).replaceAll("^\\s+", ""));
                        nl.add(2, m.group(2));

                        maxLen = Math.max(nl.get(0).length(), maxLen);
                    } else {
                        nl.add(0, l);
                    }

                    output.add(nl);
                }

                ArrayList<String> result = new ArrayList<String>();

                for (ArrayList<String> n : output) {
                    if (n.size() > 1) {
                        result.add(n.get(1) + StringUtils.repeat(" ", maxLen + 1 - n.get(0).length()) + n.get(2));
                    } else {
                        result.add(n.get(0));
                    }
                }

                document.replaceString(offsetStart, offsetEnd, StringUtils.join(result, "\n"));
            }
        }
    }

    public static void log(String msg) {
        Messages.showMessageDialog(msg, "Info", Messages.getInformationIcon());
    }
}
