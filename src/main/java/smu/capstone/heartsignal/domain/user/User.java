package smu.capstone.heartsignal.domain.user;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
@Data
public class User {
    @Id
    private String email;
    private String name;
    private String image;
    private String latitude;
    private String longitude;
    private String address;
    private List<BeatInfo> beatList;
    private Double rmssd;

    @Builder
    public User(String email, String name) {
        this.email = email;
        this.name = name;
        this.image = "";
        this.beatList = new ArrayList<>();
        this.latitude = "";
        this.longitude = "";
        this.address = "";
        this.rmssd = 0.0;
    }
}
