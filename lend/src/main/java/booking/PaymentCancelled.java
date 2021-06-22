
package booking;

public class PaymentCancelled extends AbstractEvent {

    private Long matchId;
    private String customer;

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }
    public String getStudent() {
        return customer;
    }

    public void setStudent(String customer) {
        this.customer = customer;
    }
}

