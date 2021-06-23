package booking;

public class SaleAssigned extends AbstractEvent {

    private Long id;

    public SaleAssigned(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
