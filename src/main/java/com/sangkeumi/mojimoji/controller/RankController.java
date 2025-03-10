package com.sangkeumi.mojimoji.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sangkeumi.mojimoji.dto.rank.*;
import com.sangkeumi.mojimoji.service.RankService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/rank")
@Tag(name = "Rank API", description = "랭킹 관련 API")
public class RankController {

    private final RankService rankService;

    @GetMapping("/rankpage")
    public String Rank() {
        return "rank/rankpage";
    }

    @GetMapping("/overall")
    @ResponseBody
    public List<Ranking> getOverallRankings() {
        return rankService.getOverallRankings();
    }

    // 한자 컬렉션 순위 엔드포인트
    @GetMapping("/kanji")
    @ResponseBody
    public List<KanjiRanking> getKanjiRankings() {
        return rankService.getKanjiRankings();
    }

    // 책 관련 순위 엔드포인트
    @GetMapping("/story")
    @ResponseBody
    public List<BookRanking> getBookRankings() {
        return rankService.getBookRankings();
    }

    // 좋아요 관련 순위 엔드포인트
    @GetMapping("/likes")
    @ResponseBody
    public List<LikeRanking> getLikesRankings() {
        return rankService.getLikesRankings();
    }

}
