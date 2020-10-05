package smu.capstone.heartsignal.controller.apiController;

import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import smu.capstone.heartsignal.domain.user.BeatInfo;
import smu.capstone.heartsignal.domain.user.User;
import smu.capstone.heartsignal.domain.user.UserRepository;
import smu.capstone.heartsignal.eventHandler.beat.BeatEvent;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserApiController {
    private final ApplicationEventPublisher publisher;
    private final UserRepository userRepository;

    @GetMapping
    public Flux<User> findAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/save")
    public Mono<User> saveUser(@RequestBody Map<String, Object> req) {
        String email = (String) req.get("email");
        String name = (String) req.get("name");
        return userRepository.save(User.builder().email(email).name(name).build());
    }

    @GetMapping("/{email}")
    public Mono<User> findUserByEmail(@PathVariable String email) {
        return userRepository.findById(email);
    }

    @PostMapping("/{email}/send/beat")
    public Mono<User> sendBeatInfo(@PathVariable String email, @RequestBody Map<String, Object> req) {
        // time_str = "2016-03-04 11:30:43"
        String time_str = (String) req.get("time");
        String beat_str = (String) req.get("beat");
        String rrInterval_str = (String) req.get("rrInterval");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(time_str, formatter);
        Double beat = Double.valueOf(beat_str);
        Double rrInterval = Double.valueOf(rrInterval_str);
        BeatInfo beatInfo = BeatInfo.of(dateTime, beat, rrInterval);

        return userRepository.findById(email).flatMap(u1 -> {
            List<BeatInfo> beatList = u1.getBeatList();
            beatList.add(beatInfo);
            u1.setBeatList(beatList);

//            publisher.publishEvent(BeatEvent.builder().email(u1.getEmail()).name(u1.getName()).beatInfoList(u1.getBeatList()).build());

            return userRepository.save(u1);
        });
    }

//    @PostMapping("/{email}/send/info")
//    public Mono<User> sendBeatGpsInfo(@PathVariable String email, @RequestBody Map<String, Object> req) {
//        String time_str = (String) req.get("time");
//        String beat_str = (String) req.get("beat");
//        String rrInterval_str = (String) req.get("rrInterval");
//        String latitude = (String) req.get("latitude");
//        String longitude = (String) req.get("longitude");
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        LocalDateTime dateTime = LocalDateTime.parse(time_str, formatter);
//        Double beat = Double.valueOf(beat_str);
//        Double rrInterval = Double.valueOf(rrInterval_str);
//        BeatInfo beatInfo = BeatInfo.of(dateTime, beat, rrInterval);
//
//        String url = "https://dapi.kakao.com/v2/local/geo/coord2address.json?x=" + longitude + "&y=" + latitude + "&input_coord=WGS84";
//        String addr = "";
//        try {
//            addr = getRegionAddress(getJSONData(url));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        String finalAddr = addr;
//        System.out.println(latitude + ", " + longitude);
//        System.out.println(addr);
//
//        return userRepository.findById(email)
//                .publishOn(Schedulers.elastic())
//                .flatMap(u1 -> {
//                    List<BeatInfo> beatList = u1.getBeatList();
//                    beatList.add(beatInfo);
//                    u1.setBeatList(beatList);
//                    u1.setLatitude(latitude);
//                    u1.setLongitude(longitude);
//                    u1.setAddress(finalAddr);
////                    publisher.publishEvent(BeatEvent.builder().email(u1.getEmail()).name(u1.getName()).beatInfoList(u1.getBeatList()).build());
//
//                    publisher.publishEvent(
//                            BeatEvent.builder()
//                                    .email(u1.getEmail())
//                                    .name(u1.getName())
//                                    .beatInfoList(u1.getBeatList())
//                                    .latitude(u1.getLatitude())
//                                    .longitude(u1.getLongitude())
//                                    .address(u1.getAddress())
//                                    .build());
//
//                    return userRepository.findById(email);
////                    return userRepository.save(u1);
//                });
//    }

    @GetMapping("/{email}/delete")
    public Mono<User> deleteUserInfo(@PathVariable String email){
        return userRepository.findById(email).flatMap(u1->{
            u1.setBeatList(new ArrayList<>());
            return userRepository.save(u1);
        });
    }

    @GetMapping("/last/{email}")
    public Mono<BeatInfo> lastBeatInfo(@PathVariable String email){
        return userRepository.findById(email).flatMap(u1->{
            List<BeatInfo> beatList = u1.getBeatList();
            int size = beatList.size();
            BeatInfo beatInfo = beatList.get(size-1);
            return Mono.just(beatInfo);
        });
    }


    // TODO: 2020-07-30 delete this method
    @GetMapping("/deleteAll")
    public void deleteAll() {
        userRepository.deleteAll().subscribe();
    }

    private String getJSONData(String apiUrl) throws Exception {
        final String apikey = "29775eb2bcdb5a3bf0a74914a0b560c2";
        String jsonString = "";
        String buf;

        URL url = new URL(apiUrl);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        String auth = "KakaoAK " + apikey;
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-Requested-With", "curl");
        conn.setRequestProperty("Authorization", auth);

        BufferedReader br = new BufferedReader(new InputStreamReader(
                conn.getInputStream(), "UTF-8"));
        while ((buf = br.readLine()) != null) {
            jsonString += buf;
        }
        return jsonString;
    }

    private String getRegionAddress(String jsonString) {

        String value = "";
        JSONObject jObj = (JSONObject) JSONValue.parse(jsonString);
        JSONObject meta = (JSONObject) jObj.get("meta");
        long size = (long) meta.get("total_count");

        if (size > 0) {
            JSONArray jArray = (JSONArray) jObj.get("documents");
            JSONObject subJobj = (JSONObject) jArray.get(0);
            JSONObject roadAddress = (JSONObject) subJobj.get("road_address");
            if (roadAddress == null) {
                JSONObject subsubJobj = (JSONObject) subJobj.get("address");
                value = (String) subsubJobj.get("address_name");
            } else {
                value = (String) roadAddress.get("address_name");
            }
            if (value.equals("") || value == null) {
                subJobj = (JSONObject) jArray.get(1);
                subJobj = (JSONObject) subJobj.get("address");
                value = (String) subJobj.get("address_name");
            }
        }
        return value;
    }

    @PostMapping("/{email}/{age}/send/info")
    public Mono<User> sendBeatGpsInfo(@PathVariable String email, @PathVariable String age, @RequestBody Map<String, Object> req){
        String time_str = (String) req.get("time"); // "2016-03-04 11:30:43"
        String beat_str = (String) req.get("beat");
        String rrInterval_str = (String) req.get("rrInterval");
        String latitude = (String) req.get("latitude");
        String longitude = (String) req.get("longitude");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(time_str, formatter);
        Double beat = Double.valueOf(beat_str);
        Double rrInterval = Double.valueOf(rrInterval_str);
        BeatInfo beatInfo = BeatInfo.of(dateTime, beat, rrInterval);

        String url = "https://dapi.kakao.com/v2/local/geo/coord2address.json?x=" + longitude + "&y=" + latitude + "&input_coord=WGS84";
        String addr = "";
        try {
            addr = getRegionAddress(getJSONData(url));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String finalAddr = addr;
        return userRepository.findById(email)
                .publishOn(Schedulers.elastic())
                .flatMap(u1 -> {

                    List<BeatInfo> beatList;
                    try { // Init. Beat Insert NullPointerException 처리
                        beatList = u1.getBeatList().isEmpty() ? new ArrayList<>() : u1.getBeatList();
                    }catch (NullPointerException e){
                        beatList = new ArrayList<>();
                    }

                    beatList.add(beatInfo);
                    u1.setBeatList(beatList);
                    u1.setLatitude(latitude);
                    u1.setLongitude(longitude);
                    u1.setAddress(finalAddr);
                    publisher.publishEvent(BeatEvent.builder().email(u1.getEmail()).age(age).name(u1.getName()).beatInfoList(u1.getBeatList()).latitude(u1.getLatitude()).longitude(u1.getLongitude()).address(u1.getAddress()).build());

                    return userRepository.findById(email); // publisher 코드가 비동기 이벤트 처리여서 이전에 저장되어있던 Address 정보가 ResponseBody로 담아서 출력 됨
                });
    }
}