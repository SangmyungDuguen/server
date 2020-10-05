package smu.capstone.heartsignal.eventHandler.beat;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import smu.capstone.heartsignal.domain.user.BeatInfo;
import smu.capstone.heartsignal.domain.user.User;
import smu.capstone.heartsignal.domain.user.UserRepository;
import smu.capstone.heartsignal.service.beatService.BeatService;
import smu.capstone.heartsignal.service.mailService.MailService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BeatHandler {

    @Autowired
    private MailService mailService;

    @Autowired
    private final BeatService beatService;

    @Autowired
    private final UserRepository userRepository;

    private static boolean mailFlag = false; // RMSSD 이상 시 Mail 전송 제한 FLAG

    @EventListener
    @Async
    public void doEvent(BeatEvent event) throws Exception {
        List<BeatInfo> beatInfoList = event.getBeatInfoList();
        int size = beatInfoList.size();
        List<BeatInfo> subBeatInfoList;
        subBeatInfoList = size >= 10 ? beatInfoList.subList(size - 10, size - 1) : beatInfoList; // Init. Beat Insert NullPointerException 처리

        Double rmssd = beatService.getRmssdFromBeats(subBeatInfoList);
//        Double hti = beatService.getHTIFromRRIntervals(beatInfoList);

        alertInfo(rmssd, subBeatInfoList.get(subBeatInfoList.size() - 1).getBeat(), event.getEmail(), event.getAge()); // Distinguish HRV and Send Email

        userRepository.findById(event.getEmail()).subscribe(u -> {
            User newUser = User.builder().email(u.getEmail()).name(u.getName()).build();
            newUser.setBeatList(beatInfoList);
            newUser.setRmssd(rmssd);
            newUser.setLatitude(event.getLatitude());
            newUser.setLongitude(event.getLongitude());
            newUser.setAddress(event.getAddress());
            userRepository.save(newUser).subscribe(); // From Galaxy Gear. User Info Save
        });
    }

    // TODO : Debug - RMSSD 이상(AGE에 따른 HRV 정상 수치) 판단 논문 || 머신러닝 모델 ***
    private void alertInfo(final double rmssd, final double beat, final String email, final String age) throws Exception {
//        System.out.println("==============================\n[AGE:" + age + "] rmssd: " + rmssd + ", beat: " + beat); // debug

        // age는 DB에 들어가지 않음
        switch (Integer.parseInt(age) / 10) { // 각 AGE 기준 (95% 신뢰도) -> 참고논문 [https://www.sciencedirect.com/science/article/pii/S0735109797005548]
            case 5: // 50 대
                if (!(rmssd >= 0.13 && rmssd <= 0.53) && !(beat >= 53 && beat <= 100)) {
                    mailAlert(email);
                } else {
//                    System.out.println("HRV 정상 : No problem with RMSSD && BEAT"); // debug
                    mailFlag = false; // RMSSD 정상으로 돌아오면 flag를 false
                }
                break;
            case 6:
                if (!(rmssd >= 0.11 && rmssd <= 0.45) && !(beat >= 52 && beat <= 99)) {
                    mailAlert(email);
                } else {
//                    System.out.println("HRV 정상 : No problem with RMSSD && BEAT"); // debug
                    mailFlag = false;
                }
                break;
            case 7:
                if (!(rmssd >= 0.09 && rmssd <= 0.38) && !(beat >= 51 && beat <= 98)) {
                    mailAlert(email);
                } else {
//                    System.out.println("HRV 정상 : No problem with RMSSD && BEAT"); // debug
                    mailFlag = false;
                }
                break;
            case 8:
                if (!(rmssd >= 0.08 && rmssd <= 0.32) && !(beat >= 49 && beat <= 97)) {
                    mailAlert(email);
                } else {
//                    System.out.println("HRV 정상 : No problem with RMSSD && BEAT"); // debug
                    mailFlag = false;
                }
                break;
            case 9:
                if (!(rmssd >= 0.07 && rmssd <= 0.28) && !(beat >= 48 && beat <= 96)) {
                    mailAlert(email);
                } else {
//                    System.out.println("HRV 정상 : No problem with RMSSD && BEAT"); // debug
                    mailFlag = false;
                }
                break;
            default: // 10 ~ 40 대
                if (!(rmssd >= 0.15 && rmssd <= 1.03) && !(beat >= 54 && beat <= 105)) {
                    mailAlert(email);
                } else {
//                    System.out.println("HRV 정상 : No problem with RMSSD && BEAT"); // debug
                    mailFlag = false;
                }
                break;
        }
    }
    private void mailAlert(final String email) throws Exception { // HRV 이상 시 메일 전송 (무한정 메일 전송 방지)
//        System.out.println("HRV 이상 : RMSSD anomaly occurs"); // debug
        synchronized (this) { // 동기화
            if (!mailFlag) { // RMSSD 이상 시 Mail 전송 제한 FLAG (한 번만 전송)
                mailService.sendMail(email); // 한번전송
//                System.out.println("[[[Mail Sender]]]]"); // debug
            }
        }
        mailFlag = true; // RMSSD 이상 시 Mail 전송 제한 FLAG
    }
}