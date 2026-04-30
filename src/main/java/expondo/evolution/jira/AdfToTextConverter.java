package expondo.evolution.jira;

import java.util.List;
import java.util.Map;

/**
 * Minimal converter from Atlassian Document Format (ADF) to plain text.
 *
 * Supports the most common block types in JIRA descriptions:
 * paragraph, heading, bulletList, orderedList, listItem, hardBreak,
 * codeBlock, blockquote, table.
 *
 * Tables are rendered with tab-separated cells.
 * Anything not recognized is rendered as the concatenation of its text children.
 *
 * For our use case (JIRA Polaris descriptions) this is good enough; we accept
 * the lossy conversion (no styling, no images, no mentions resolved) as a
 * conscious tradeoff for simplicity.
 */
public final class AdfToTextConverter {

    private AdfToTextConverter() {}

    @SuppressWarnings("unchecked")
    public static String convert(Object adf) {
        if (adf == null) return null;
        if (adf instanceof String s) return s; // Old API style fallback
        if (!(adf instanceof Map<?, ?> root)) return null;

        StringBuilder sb = new StringBuilder();
        appendNode((Map<String, Object>) root, sb, 0);
        return sb.toString().strip();
    }

    @SuppressWarnings("unchecked")
    private static void appendNode(Map<String, Object> node, StringBuilder sb, int listDepth) {
        if (node == null) return;
        String type = (String) node.get("type");
        List<Map<String, Object>> content = (List<Map<String, Object>>) node.get("content");

        if (type == null) return;

        switch (type) {
            case "doc" -> appendChildren(content, sb, listDepth);
            case "paragraph" -> {
                appendChildren(content, sb, listDepth);
                sb.append("\n\n");
            }
            case "heading" -> {
                appendChildren(content, sb, listDepth);
                sb.append("\n\n");
            }
            case "bulletList", "orderedList" -> {
                if (content != null) {
                    int idx = 1;
                    for (Map<String, Object> item : content) {
                        sb.append("  ".repeat(listDepth));
                        sb.append(type.equals("orderedList") ? (idx++ + ". ") : "- ");
                        appendNode(item, sb, listDepth + 1);
                    }
                }
            }
            case "listItem" -> {
                appendChildren(content, sb, listDepth);
                if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
                    // already ended with newline from inner paragraph
                } else {
                    sb.append("\n");
                }
            }
            case "text" -> {
                Object text = node.get("text");
                if (text != null) sb.append(text);
            }
            case "hardBreak" -> sb.append("\n");
            case "codeBlock", "blockquote" -> {
                appendChildren(content, sb, listDepth);
                sb.append("\n\n");
            }
            case "table" -> {
                if (content != null) {
                    for (Map<String, Object> row : content) {
                        List<Map<String, Object>> cells = (List<Map<String, Object>>) row.get("content");
                        if (cells != null) {
                            for (int i = 0; i < cells.size(); i++) {
                                StringBuilder cellBuf = new StringBuilder();
                                appendNode(cells.get(i), cellBuf, 0);
                                sb.append(cellBuf.toString().strip().replace("\n", " "));
                                if (i < cells.size() - 1) sb.append("\t");
                            }
                            sb.append("\n");
                        }
                    }
                    sb.append("\n");
                }
            }
            case "tableRow", "tableCell", "tableHeader" -> appendChildren(content, sb, listDepth);
            default -> appendChildren(content, sb, listDepth);
        }
    }

    private static void appendChildren(List<Map<String, Object>> content, StringBuilder sb, int listDepth) {
        if (content == null) return;
        for (Map<String, Object> child : content) {
            appendNode(child, sb, listDepth);
        }
    }
}
