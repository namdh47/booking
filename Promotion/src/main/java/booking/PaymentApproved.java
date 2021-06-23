
package booking;

public class PaymentApproved extends AbstractEvent {

    private Long matchId;
    private Integer price;
    private String customer;
    private String paymentAction;
    private String startDay;
    private String endDay;
    private String name;

    public Long getId() {
        return matchId;
    }

    public void setId(Long matchId) {
        this.matchId = matchId;
    }
    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
    public String getStudent() {
        return customer;
    }

    public void setStudent(String customer) {
        this.customer = customer;
    }
    public String getStateAction() {
        return paymentAction;
    }

    public void setStateAction(String paymentAction) {
        this.paymentAction = paymentAction;
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
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

