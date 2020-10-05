package smu.capstone.heartsignal.eventHandler.beat;

import lombok.Builder;
import lombok.Getter;
import smu.capstone.heartsignal.domain.user.BeatInfo;

import java.util.List;

@Getter
public class BeatEvent {

    private String email;
    private String age;
    private String name;
    private List<BeatInfo> beatInfoList;
    private Double RMSSD;
    private String latitude;
    private String longitude;
    private String address;

    @Builder
    public BeatEvent(String email, String age, String name, List<BeatInfo> beatInfoList, String latitude, String longitude, String address) {
        this.email = email;
        this.age = age;
        this.name = name;
        this.beatInfoList = beatInfoList;
        this.RMSSD = 0.0;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }
}
