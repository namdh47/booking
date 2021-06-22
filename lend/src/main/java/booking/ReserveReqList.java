package booking;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="ReserveReqList_table")
public class ReserveReqList {

        @Id
        private Long id;
        private Integer price;
        private String startDay;
        private String endDay;
        private String name;


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
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

}
