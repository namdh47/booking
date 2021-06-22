package booking;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Reserve_table")
public class Reserve {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Integer price;
    private String startDay;
    private String endDay;
    private String customer;
    private String status;
    private String name;

    @PostPersist
    public void onPostPersist(){
        //ReserveCancelled reserveCancelled = new ReserveCancelled();
        //BeanUtils.copyProperties(this, reserveCancelled);
        //reserveCancelled.publishAfterCommit();


        ReserveRequested reserveRequested = new ReserveRequested();
        BeanUtils.copyProperties(this, reserveRequested);
        reserveRequested.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        booking.external.Payment payment = new booking.external.Payment();
        // mappings goes here

        payment.setMatchId(Long.valueOf(this.getId()));
        payment.setPrice(Integer.valueOf(this.getPrice()));
        payment.setPaymentAction("Approved");
        payment.setCustomer(String.valueOf(this.getCustomer()));
        payment.setStartDay(String.valueOf(this.getStartDay()));
        payment.setEndDay(String.valueOf(this.getEndDay()));
        payment.setName(String.valueOf(this.getName()));


        ReserveApplication.applicationContext.getBean(booking.external.PaymentService.class)
            .paymentRequest(payment);


    }

    //사용자가 해당 결재건 취소 했을 경우. (status를 Cancel로 업데이트 보냄) 
    @PreUpdate
    public void onPreUpdate(){
        if("Cancel".equals(status)){
            ReserveCancelled reserveCancelled = new ReserveCancelled();
            BeanUtils.copyProperties(this, reserveCancelled);
            reserveCancelled.publishAfterCommit();
    
        }
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
    public String getStartDay() {
        return startDay;
    }

    public void setStartDay(String startDay) {
        this.startDay = startDay;
    }
    public String getEndDay() {
        return endDay;
    }

    public void setEndDay(String endDay) {
        this.endDay = endDay;
    }
    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }




}
