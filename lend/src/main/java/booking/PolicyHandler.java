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
    @Autowired LendRepository lendRepository;
    @Autowired ReserveReqListRepository ReserveReqListRepository;
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentApproved_LendRequest(@Payload PaymentApproved paymentApproved){
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        //if(!paymentApproved.validate()) return;

        System.out.println("##### listener  : " + paymentApproved.toJson());
        if(!paymentApproved.validate()) return;
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        //승인완료 시 승인완료된 리스트를 저장
        ReserveReqList reserveReqist = new ReserveReqList();
        reserveReqist.setId(paymentApproved.getMatchId());
        reserveReqist.setStartDay(paymentApproved.getStartDay());
        reserveReqist.setEndDay(paymentApproved.getEndDay());
        reserveReqist.setPrice(paymentApproved.getPrice());
        reserveReqist.setName(paymentApproved.getName());
        ReserveReqListRepository.save(reserveReqist);
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentCancelled_LendCancel(@Payload PaymentCancelled paymentCancelled){

        if(!paymentCancelled.validate()) return;

        System.out.println("##### listener  : " + paymentCancelled.toJson());
        lendRepository.findById(paymentCancelled.getMatchId()).ifPresent(Lend->{
            lendRepository.delete(Lend);
        });

        //취소 요청 시, CatchReqList에서도 삭제 추가 0
        ReserveReqListRepository.findById(paymentCancelled.getMatchId()).ifPresent(Lend->{
            ReserveReqListRepository.delete(Lend);
        });
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
