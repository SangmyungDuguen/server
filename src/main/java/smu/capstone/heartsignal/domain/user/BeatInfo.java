package smu.capstone.heartsignal.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(staticName = "of")
public class BeatInfo {
    private LocalDateTime time;
    private Double beat;
    private Double rrInterval;
}
