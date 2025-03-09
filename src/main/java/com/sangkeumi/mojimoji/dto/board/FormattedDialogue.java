package com.sangkeumi.mojimoji.dto.board;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface FormattedDialogue {
    String userContent();
    String gptContent(); // JSON 원본 데이터 제공 메서드

    default String formattedGptContent() {
        return formatDialogue(gptContent());
    }

    private static String formatDialogue(String inputText) {
        if (inputText == null || inputText.isBlank()) {
            return "";
        }

        // 1. 끝에 붙은 JSON 메타데이터 제거 (예: {"hp":100, ...} 부분)
        String cleanedText = inputText.replaceAll("\\{\\s*\"hp\".*", "").trim();
        StringBuilder formattedText = new StringBuilder();

        // 2. 입력이 JSON 태그로 시작하지 않으면, 초기 내레이션 부분을 분리하여 추가
        if (!cleanedText.startsWith("{\"name\":")) {
            int idx = cleanedText.indexOf("{\"name\":");
            if (idx > 0) {
                String narrationPart = cleanedText.substring(0, idx).trim();
                if (!narrationPart.isEmpty()) {
                    formattedText.append(narrationPart).append("\n\n");
                }
                cleanedText = cleanedText.substring(idx);
            } else {
                // JSON 태그가 없으면 그대로 반환
                return cleanedText;
            }
        }

        // 3. {"name": "XXX"} 태그와 그 뒤의 텍스트를 캡처하는 패턴 (DOTALL: 개행 포함)
        Pattern pattern = Pattern.compile("\\{\"name\":\\s*\"([^\"]+)\"\\}(.*?)(?=\\{\"name\":|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(cleanedText);

        while (matcher.find()) {
            String speaker = matcher.group(1).trim();
            String blockText = matcher.group(2).trim();
            // speaker가 narration인 경우 이름 없이 텍스트만 출력
            if ("narration".equalsIgnoreCase(speaker)) {
                formattedText.append(blockText).append("\n\n");
            } else {
                formattedText.append(speaker).append(" : ").append(blockText).append("\n\n");
            }
        }

        return formattedText.toString().trim();
    }
}
