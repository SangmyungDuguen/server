package smu.capstone.heartsignal.controller.apiController;

import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.web.bind.annotation.*;
import smu.capstone.heartsignal.domain.user.User;
import smu.capstone.heartsignal.domain.user.UserRepository;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

@RestController
@RequestMapping("/api/gps")
@RequiredArgsConstructor
public class GpsApiController {

    private final UserRepository userRepository;

    @PostMapping("{email}/send/gps")
    public boolean gpsAddress(@PathVariable String email, @RequestBody Map<String, Object> search) throws Exception {

        String lat = (String)search.get("latitude");
        String lon = (String)search.get("longitude");

        String url = "https://dapi.kakao.com/v2/local/geo/coord2address.json?x="+lon+"&y="+lat+"&input_coord=WGS84";
        String addr = "";
        try {
            addr = getRegionAddress(getJSONData(url));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String finalAddr = addr;
        userRepository.findById(email).subscribe(u1->{
            User newUser = User.builder().email(u1.getEmail()).name(u1.getName()).build();
            newUser.setBeatList(u1.getBeatList());
            newUser.setLatitude(lat);
            newUser.setLongitude(lon);
            newUser.setAddress(finalAddr);
            newUser.setRmssd(u1.getRmssd());
            userRepository.save(newUser).subscribe();
        });
        System.out.println("lat: " + lat + ", lon: " + lon + ", address : " + addr); // debug
        return true;
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
}
