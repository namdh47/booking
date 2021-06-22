package booking;

import booking.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired ReserveRepository reserveRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverLendAssigned_StatusUpdate(@Payload LendAssigned lendAssigned){

    
        if(lendAssigned.validate()){

            System.out.println("##### listener wheneverPickupAssigned : " + lendAssigned.toJson());

            reserveRepository.findById(lendAssigned.getMatchId()).ifPresent(reserve ->{
                System.out.println("##### wheneverPickupAssigned_MatchRepository.findById : exist" );
                reserve.setStatus(lendAssigned.getEventType()); 
                reserveRepository.save(reserve);
            });
        }
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
