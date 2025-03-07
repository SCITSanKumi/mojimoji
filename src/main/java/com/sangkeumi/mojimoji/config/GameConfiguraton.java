package com.sangkeumi.mojimoji.config;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class GameConfiguraton {

    private final String introMessage;
    private final String systemMessage;

    private final int defaultHP = 100;
    private final int defaultMP = 100;
    private final String defaultLocation = "태초마을";

    public GameConfiguraton() {
        this.introMessage =
        """
어둑해진 하늘 아래, 낡은 목조 다리 위로 안개가 깔린다. 마을 입구에서 바라본 숲에는 한기마저 감돈다.
언젠가부터 이곳을 무대로 괴이한 사건이 일어나기 시작했다. 누군가는 숲 깊은 곳에 살고 있는 옛 신전의 저주라 말하고, 또 다른 이는 오래전부터 봉인된 마물이 깨어난 것이라 수군댄다.
어느 날, 당신은 마을 장로의 부탁을 받게 된다. 다정한 미소 뒤에 감춰진 그의 얼굴에는 걱정과 불안이 스쳐 지나간다. “숲 속에서 사라진 자들의 흔적을 찾아주게. 이 마을은 자네 같은 모험가를 기다려 왔네.”
장로의 의뢰를 수락한 순간, 기묘한 바람이 불어와 당신의 등을 떠민다. 영문 모를 불안과 호기심이 뒤섞인 채, 당신은 울창한 숲 속으로 첫 발을 내딛는다.
        """.stripIndent();

        this.systemMessage = """
당신은 텍스트 기반 RPG 게임 엔진입니다. 다음과 같은 규칙에 따라 답변을 생성하세요.

1. 모든 출력은 JSON 객체 형태로 구성해야 합니다.
2. 각 서술(내레이션) 혹은 캐릭터의 대사는 각각 별도의 JSON 객체로 표기합니다.
3. JSON 객체는 아래 예시와 같은 형식을 따릅니다.

예시:

{"name": "naration"} 내레이션 내용
{"name": "NPC이름"} NPC 대사


4. 한 장면이 종료될 때마다 게임 상태를 담은 JSON 객체를 출력합니다.
    이 객체에는 "hp", "mp", "current_location", "isEnded" 같은 키가 포함됩니다.

예시:

{"hp": 100, "mp": 100, "current_location": "마을 광장", "isEnded": false}


5. 그 외의 텍스트(예: 안내 문구, 추가 설명)는 출력하지 말고,
    오직 JSON 형식의 텍스트만 결과로 반환하세요.

이전 줄거리(assistant 역할 메시지)와
[현재 플레이어 상태: 체력 99, 정신력 99, 위치: 울창한 숲] (assistant 역할 메시지)을 참고하여,
플레이어가 방금 보낸 요청(user 역할 메시지)에 대응하는 다음 상황을 JSON 형식으로 진행해 주세요.
            """.stripIndent();

    }
}
