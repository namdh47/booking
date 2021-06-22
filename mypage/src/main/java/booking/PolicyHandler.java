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
    @Autowired MyPageRepository myPageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReserveCancelled_StatusUpdate(@Payload ReserveCancelled reserveCancelled){

        if(!reserveCancelled.validate()) return;

        myPageRepository.findById(reserveCancelled.getMatchId()).ifPresent(MyPage ->{
            System.out.println("##### listener  : " + reserveCancelled.toJson());
            System.out.println("##### wheneverPickupCancelled_MyPageRepository.findById : exist" );
            MyPage.setStatus(reserveCancelled.getEventType());
            myPageRepository.save(MyPage);
        });
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentCancelled_StatusUpdate(@Payload PaymentCancelled paymentCancelled){

        if(!paymentCancelled.validate()) return;

      
        myPageRepository.findById(paymentCancelled.getMatchId()).ifPresent(MyPage ->{
            System.out.println("##### listener  : " + paymentCancelled.toJson());
            System.out.println("##### wheneverPickupCancelled_MyPageRepository.findById : exist" );
            MyPage.setStatus(paymentCancelled.getEventType());
            myPageRepository.save(MyPage);
        });
            
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentApproved_StatusUpdate(@Payload PaymentApproved paymentApproved){

        if(!paymentApproved.validate()) return;

        System.out.println("##### listener  : " + paymentApproved.toJson());

        MyPage mypage = new MyPage();
        mypage.setId(paymentApproved.getMatchId());
        mypage.setPrice(paymentApproved.getPrice());
        mypage.setStatus(paymentApproved.getEventType());
        mypage.setStartDay(paymentApproved.getStartDay());
        mypage.setEndDay(paymentApproved.getEndDay());
        mypage.setCustomer(paymentApproved.getCustomer());
        mypage.setName(paymentApproved.getName());
        myPageRepository.save(mypage);
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverLendAssigned_StatusUpdate(@Payload LendAssigned lendAssigned){
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println("##### listener  : " + lendAssigned.toJson());
        if(!lendAssigned.validate()) return;
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        myPageRepository.findById(lendAssigned.getMatchId()).ifPresent(MyPage ->{
            System.out.println("##### listener  : " + lendAssigned.toJson());
            System.out.println("##### wheneverPickupAssigned_MyPageRepository.findById : exist" );
            MyPage.setLender(lendAssigned.getLender());
            MyPage.setStatus(lendAssigned.getEventType());
            myPageRepository.save(MyPage);
        });
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverLendCancelled_StatusUpdate(@Payload LendCancelled lendCancelled){

        if(!lendCancelled.validate()) return;

        myPageRepository.findById(lendCancelled.getMatchId()).ifPresent(MyPage ->{
            System.out.println("##### listener  : " + lendCancelled.toJson());
            System.out.println("##### wheneverPickupCancelled_MyPageRepository.findById : exist" );
            MyPage.setStatus(lendCancelled.getEventType());
            myPageRepository.save(MyPage);
        });
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
