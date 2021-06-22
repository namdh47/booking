package booking;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Lend_table")
public class Lend {

    @Id
    private Long matchId;
    private String lender;

    @PostPersist
    public void onPostPersist(){
        LendAssigned lendAssigned = new LendAssigned();
        BeanUtils.copyProperties(this, lendAssigned);
        lendAssigned.publishAfterCommit();


        LendCancelled lendCancelled = new LendCancelled();
        BeanUtils.copyProperties(this, lendCancelled);
        lendCancelled.publishAfterCommit();


    }


    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }
    public String getLender() {
        return lender;
    }

    public void setLender(String lender) {
        this.lender = lender;
    }




}
