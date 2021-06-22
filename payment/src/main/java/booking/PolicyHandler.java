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
    @Autowired PaymentRepository paymentRepository;
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReserveCancelled_PaymentCancel(@Payload ReserveCancelled reserveCancelled){

        if(reserveCancelled.validate()){
            System.out.println("################ 책 예약 신청 취소 ################ ");
            System.out.println("##### listener PaymentCancel : " + reserveCancelled.toJson());

            paymentRepository.findById(reserveCancelled.getId()).ifPresent(Payment ->{
                Payment.setPaymentAction("Cancel");
                paymentRepository.save(Payment);
         });

        }
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
