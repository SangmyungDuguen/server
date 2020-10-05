package smu.capstone.heartsignal.service.beatService;

import lombok.RequiredArgsConstructor;
import org.nd4j.autodiff.samediff.ops.SDOps;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.stereotype.Service;
import smu.capstone.heartsignal.domain.user.BeatInfo;
import smu.capstone.heartsignal.domain.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BeatService extends SDOps {

    public Double getRmssdFromBeats(List<BeatInfo> beats) {
        List<Double> beatDiff = new ArrayList<>();
        if(beats.size() > 1){ // Init. Beat Insert NullPointerException 처리
            for (int i = 0; i < beats.size() - 1; i++) {
                Double diff = beats.get(i).getBeat() - beats.get(i + 1).getBeat();
                beatDiff.add(Math.pow(diff, 2));
            }
        }else{
            Double diff = beats.get(0).getBeat(); // Debug
            beatDiff.add(Math.pow(diff, 2));
        }

        INDArray diffArray = Nd4j.create(beatDiff);
        Number diffMean = diffArray.meanNumber();
        return Math.sqrt((Double) diffMean);
    }

    // TODO : Debug HTI Calculation
    public Double getHTIFromRRIntervals(List<BeatInfo> subBeatInfoList){
        Double sum = 0.0, max = 0.0;
        for(int i = 0; i < subBeatInfoList.size(); i++){
            sum += subBeatInfoList.get(i).getRrInterval();
            if(max < subBeatInfoList.get(i).getRrInterval()){
                max = subBeatInfoList.get(i).getRrInterval();
            }
        }
        return sum / max;
    }
}
