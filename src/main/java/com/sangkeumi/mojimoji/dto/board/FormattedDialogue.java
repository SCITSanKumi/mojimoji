package com.sangkeumi.mojimoji.dto.board;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@ToString
public abstract class FormattedDialogue {
    protected String userContent;
    protected String gptContent;      // 파싱된 대화 내용 (teacher 제외)
    protected String teacherContent;

    protected int hp;
    protected int gold;
    protected List<String> inventory = new ArrayList<>();
    protected String currentLocation;

    protected FormattedDialogue(String userContent, String gptContentRaw) {
        this.userContent = userContent;
        parseStoryContent(gptContentRaw);
    }

    /**
     * GPT 원본 JSON 데이터를 파싱하여 공통 필드를 설정합니다.
     *
     * @param gptContentRaw GPT 원본 JSON 대화 내용 (메타데이터 포함)
     */
    protected void parseStoryContent(String gptContentRaw) {
        // 1. 메타데이터 추출 (예: {"hp":100, "gold":100, "inventory":[], "current_location":"작은 마을", ...})
        Pattern metaPattern = Pattern.compile(
                "\\{\\s*\"hp\"\\s*:\\s*(\\d+)\\s*,\\s*\"gold\"\\s*:\\s*(\\d+)\\s*,\\s*\"inventory\"\\s*:\\s*(\\[[^\\]]*\\])\\s*,\\s*\"current_location\"\\s*:\\s*\"([^\"]+)\""
        );
        Matcher metaMatcher = metaPattern.matcher(gptContentRaw);
        if (metaMatcher.find()) {
            this.hp = Integer.parseInt(metaMatcher.group(1));
            this.gold = Integer.parseInt(metaMatcher.group(2));
            this.inventory = parseInventory(metaMatcher.group(3));
            this.currentLocation = metaMatcher.group(4);
        }

        // 2. 메타데이터 블록 제거 (메타데이터는 보통 마지막에 위치한다고 가정)
        String cleanedText;
        int metaStart = gptContentRaw.lastIndexOf("{\"hp\"");
        if (metaStart >= 0) {
            cleanedText = gptContentRaw.substring(0, metaStart).trim();
        } else {
            cleanedText = gptContentRaw;
        }

        StringBuilder nonTeacherBuilder = new StringBuilder();
        StringBuilder teacherBuilder = new StringBuilder();

        // 3. 만약 cleanedText가 JSON 태그("{\"name\":")로 시작하지 않으면, 초기 내레이션 부분을 추가
        if (!cleanedText.startsWith("{\"name\":")) {
            int idx = cleanedText.indexOf("{\"name\":");
            if (idx > 0) {
                String initialPart = cleanedText.substring(0, idx).trim();
                if (!initialPart.isEmpty()) {
                    nonTeacherBuilder.append(initialPart).append("\n\n");
                }
                cleanedText = cleanedText.substring(idx);
            } else {
                nonTeacherBuilder.append(cleanedText);
                cleanedText = "";
            }
        }

        // 4. {"name": "xxx"} 태그와 그 뒤의 텍스트를 파싱 (개행 포함)
        Pattern dialoguePattern = Pattern.compile("\\{\"name\":\\s*\"([^\"]+)\"\\}(.*?)(?=\\{\"name\":|$)", Pattern.DOTALL);
        Matcher dialogueMatcher = dialoguePattern.matcher(cleanedText);
        while (dialogueMatcher.find()) {
            String speaker = dialogueMatcher.group(1).trim();
            String blockText = dialogueMatcher.group(2).trim();
            if ("teacher".equalsIgnoreCase(speaker)) {
                teacherBuilder.append(blockText).append("\n\n");
            } else if ("narration".equalsIgnoreCase(speaker)) {
                nonTeacherBuilder.append(blockText).append("\n\n");
            } else {
                nonTeacherBuilder.append(speaker).append(" : ").append(blockText).append("\n\n");
            }
        }

        this.gptContent = nonTeacherBuilder.toString().trim();
        this.teacherContent = teacherBuilder.toString().trim();
    }

    /**
     * 인벤토리 JSON 배열 문자열을 List<String>으로 변환합니다.
     * (예: '["item1", "item2"]' → List containing "item1", "item2")
     *
     * @param invStr 인벤토리 JSON 문자열
     * @return 인벤토리 아이템 목록
     */
    protected static List<String> parseInventory(String invStr) {
        List<String> list = new ArrayList<>();
        invStr = invStr.trim();
        if (invStr.startsWith("[") && invStr.endsWith("]")) {
            String content = invStr.substring(1, invStr.length() - 1).trim();
            if (!content.isEmpty()) {
                String[] items = content.split(",");
                for (String item : items) {
                    String cleaned = item.trim();
                    if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
                        cleaned = cleaned.substring(1, cleaned.length() - 1);
                    }
                    if (!cleaned.isEmpty()) {
                        list.add(cleaned);
                    }
                }
            }
        }
        return list;
    }
}
