package com.visibleai.brasstacks.goal;

import com.visibleai.brasstacks.model.Goal;
import com.visibleai.brasstacks.repository.GoalRepository;
import com.visibleai.brasstacks.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    public GoalController(GoalRepository goalRepository, UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<GoalResponse> list(Authentication auth) {
        return goalRepository.findByUserIdOrderByCreatedAtDesc(getUserId(auth)).stream()
                .map(GoalResponse::from)
                .toList();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<GoalResponse> create(Authentication auth, @Valid @RequestBody GoalRequest request) {
        Goal goal = new Goal(
                userRepository.getReferenceById(getUserId(auth)),
                request.title(),
                request.targetDate()
        );
        goal = goalRepository.save(goal);
        return ResponseEntity.status(HttpStatus.CREATED).body(GoalResponse.from(goal));
    }

    @PostMapping("/{id}/complete")
    @Transactional
    public GoalResponse complete(Authentication auth, @PathVariable Long id) {
        Goal goal = goalRepository.findById(id)
                .filter(g -> g.getUser().getId().equals(getUserId(auth)))
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));
        goal.complete();
        goalRepository.save(goal);
        return GoalResponse.from(goal);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        Goal goal = goalRepository.findById(id)
                .filter(g -> g.getUser().getId().equals(getUserId(auth)))
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));
        goalRepository.delete(goal);
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(Authentication auth) {
        return (Long) auth.getCredentials();
    }

    public record GoalRequest(@NotBlank String title, Instant targetDate) {}

    public record GoalResponse(Long id, String title, Instant targetDate, String status,
                                Instant createdAt, Instant completedAt) {
        static GoalResponse from(Goal g) {
            return new GoalResponse(g.getId(), g.getTitle(), g.getTargetDate(),
                    g.getStatus().name(), g.getCreatedAt(), g.getCompletedAt());
        }
    }
}
