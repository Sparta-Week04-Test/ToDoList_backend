package sparta.seed.todo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sparta.seed.sercurity.UserDetailsImpl;
import sparta.seed.todo.dto.AchievementResponseDto;
import sparta.seed.todo.service.RankService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", exposedHeaders = "*")
public class RankController {

    private final RankService rankService;

    @GetMapping("/api/rank")
    public ResponseEntity<List<AchievementResponseDto>> getRank() {
        return ResponseEntity.ok()
                .body(rankService.getRankTable());
    }
    @PostMapping("/api/rank")
    public void saveRank() {
        rankService.saveRankTable();
    }


}
