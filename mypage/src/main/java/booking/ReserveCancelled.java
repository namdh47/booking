
package booking;

public class ReserveCancelled extends AbstractEvent {

    private Long id;

    public Long getMatchId() {
        return id;
    }

    public void setMathcId(Long id) {
        this.id = id;
    }
}

