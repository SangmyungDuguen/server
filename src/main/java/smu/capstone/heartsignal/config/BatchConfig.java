package smu.capstone.heartsignal.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.item.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.core.user.OAuth2User;
import reactor.core.publisher.Flux;
import smu.capstone.heartsignal.domain.oAuth2UserInfo.OAuth2UserInfo;
import smu.capstone.heartsignal.domain.oAuth2UserInfo.OAuth2UserInfoRepository;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


@Configuration
@EnableScheduling
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j

public class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final OAuth2UserInfoRepository oAuth2UserInfoRepository;

    @Bean
    public Job OldSessionJob(){
        return jobBuilderFactory.get("oldSessionJob")
                .start(OldSessionStep())
                .build();
    }

    @Bean
    public Step OldSessionStep(){
        return stepBuilderFactory.get("oldSessionStep")
                .<OAuth2UserInfo, OAuth2UserInfo> chunk(10)
                .reader(oldSessionReader())
                .processor(oldSessionProcessor())
                .writer(oldSessionWriter())
                .build();
    }

    public class QueueItemReader<T> implements ItemReader<T> {
        private Queue<T> queue;

        public QueueItemReader(Flux<T> datas) {
            this.queue = new LinkedList<>(datas.collectList().block());
        }

        @Override
        public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
            return queue.poll();
        }
    }
    @Bean
    @StepScope
    public QueueItemReader<OAuth2UserInfo> oldSessionReader(){
        Flux<OAuth2UserInfo> oauthList = oAuth2UserInfoRepository.findAll();
        return new QueueItemReader<>(oauthList);
    }

    public ItemProcessor<OAuth2UserInfo, OAuth2UserInfo> oldSessionProcessor(){
        return oAuth2UserInfo -> {
            LocalDateTime before = oAuth2UserInfo.getTime();
            LocalDateTime after = before.plusHours(24);
            LocalDateTime now = LocalDateTime.now();
            oAuth2UserInfo.setOld(now.isAfter(after));
            return oAuth2UserInfo;
        };
    }

    public ItemWriter<OAuth2UserInfo> oldSessionWriter(){
        return list -> {
            for(OAuth2UserInfo o : list){
                if(o.getOld()){
                    list.remove(o);
                }
            }
            oAuth2UserInfoRepository.saveAll(list).subscribe();
        };
    }


    private final JobLauncher jobLauncher;
    @Scheduled(cron = "0 0 1 * * *")
    public void OldSessionScheduler() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        System.out.println(LocalDateTime.now());
        JobParameters param = new JobParametersBuilder()
                .toJobParameters();
        JobExecution execution = jobLauncher.run(OldSessionJob(), param);
        log.info("job finished with status : " + execution.getStatus());
    }
}
