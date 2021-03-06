# 아무거나 대여 서비스


# Table of contents

- [아무거나 대여 서비스](#---)
  - [서비스 시나리오 및 요구사항 분석](#서비스-시나리오-및-요구사항-분석)
    - 서비스 개요 및 시나리오
    - 요구사항
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
    - 개요 및 구성 목표
    - 서비스 설계를 위한 Event Storming
    - 헥사고날 아키텍처 다이어그램 도출
  - [구현방안 및 검증](#구현방안-및-검증)
    - DDD 의 적용
    - 폴리글랏 퍼시스턴스
    - 동기식 호출과 Fallback 처리
    - 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성
    - SAGA / Correlation
    - CQRS
  - [배포 및 운영](#배포-및-운영)
    - CI/CD 설정
    - 동기식 호출 / 서킷 브레이킹 / 장애격리
    - 오토스케일 아웃
    - 무정지 재배포


# 서비스 시나리오 및 요구사항 분석


## 서비스 개요 및 시나리오 

- 필요한 물건 빌려쓰기 및 대여 시스템
- 개인이든 사업자든 사용자가 자신이 필요한 물품을 비용, 대여기간을 정하여 요청하면 물건을 가진 아무나 매칭하여 대여하는 방식  

## 요구사항

- 기능적 요구사항
 1. 고객이 자신의 필요한 아이템, 대여기간, 가격을 입력하여 대여를 요청한다.
 2. 고객이 비용을 먼저 결제한다.
 3. 고객이 결제하면 대여자들은 요청목록에서 확인할 수 있다. 
 4. 대여자는 대여를 선택하여 확정한다. 
 5. 대여자가 매칭을 확정하면 고객은 알람을 받는다. 
 6. 정기적인 프로모션 행사가 가능해야 한다.
 7. 프로모션시 할인가격으로 대여되는 물품은 취소가 불가능해야 한다.
 8. 고객은 매칭을 취소할 수 있다. 
 9. 고객은 마이페이지에서 대여물품의 요청 상태를 확인할 수 있다. 
 10. 고객이 요청 상태를 수시로 조회할 수 있다.

- 비기능적 요구사항
 1. 트랜잭션
    1. 결제가 되지 않은 요청건은 요청목록에서 확인할 수 없어야 한다. (Sync 호출)
 1. 장애격리
    1. 대여자의 매칭기능이 수행되지 않더라도 고객 요청은 이상없이 받고 장애가 복구된 후 요청목록에 입력될 수 있어야 한다. (Async(Event-driven), Eventual Consistency)
    1. 결제 시스템에 과부하가 걸리면 사용자를 잠시동안 받지 않고 부하가 해소된 후에 다시 시도하도록 유도한다. (Circuit breaker, fallback)
 1. 성능
    1. 시스템 부하에 무관하게 고객이 수시로 자신의 요청상태를 확인할 수 있어야 한다. (CQRS)
    1. 요청상태가 바뀔때마다 알림을 줄 수 있어야 한다. (Event-driven)

# 체크포인트


## 평가항목
- Saga
- CQRS
- Correlation
- Req/Resp
- Gateway
- Deploy/ Pipeline
- Circuit Breaker
- Autoscale (HPA)
- Zero-downtime deploy (Readiness Probe)
- Config Map/ Persistence Volume
- Polyglot
- Self-healing (Liveness Probe)


# 분석/설계


## 개요 및 구성 목표
- 구성원 개인 역할 중심의 Horizontally-Aligned 조직에서 서비스 중심의 Vertically-Aligned 조직으로 전환되면서
각 서비스를 분리하여 Domain-driven한 마이크로서비스 아키텍쳐를 클라우드 네이티브하게 구현한다.


### AS-IS 조직 (Horizontally-Aligned)
![MSA1](https://user-images.githubusercontent.com/82796039/122975706-9a46ff80-d3ce-11eb-85b3-060cb8d1edf5.png)

### TO-BE 조직 (Vertically-Aligned)
![MSA2](https://user-images.githubusercontent.com/82796039/122975767-ab900c00-d3ce-11eb-8ab4-161559f368ff.png)


## 서비스 설계를 위한 Event Storming
- MSAEz를 이용하여 Event Storming을 진행하였다.
- 결과 링크:  http://www.msaez.io/#/storming/TSmzfftYDjNKHL0AljlY5eliW213/8c5407ae52a77343092383412bae392a
- 도출 순서는 다음과 같다.
- **이벤트 도출 - 부적격 이벤트 탈락 - 액터/커맨드 - 어그리게잇 - 바인디드 컨텍스트 - 폴리시 - 컨텍스트 매핑 - 요구사항 검증 및 보완**

### 이벤트 도출
![MSA3](https://user-images.githubusercontent.com/82796039/122975812-b5197400-d3ce-11eb-8fc5-a85e10202f40.png)


### 부적격 이벤트 탈락
![MSA4](https://user-images.githubusercontent.com/82796039/122975845-bcd91880-d3ce-11eb-84bc-69c3c0181409.png)

- 과정 중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함 (UI 이벤트. 시스템 동작 등)


### 액터, 커맨드 부착하여 가독성개선
![MSA5](https://user-images.githubusercontent.com/82796039/123001779-10a62a80-d3ec-11eb-8809-535b02e94414.png)


### 어그리게잇으로 묶기
![MSA6](https://user-images.githubusercontent.com/82796039/123001795-169c0b80-d3ec-11eb-9b35-6d8c36cc0f06.png)

    - 대여 요청, 결제 이력, 대여 매칭은 그와 연결된 command 와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 그룹핑함


### 바운디드 컨텍스트로 묶기
![MSA7](https://user-images.githubusercontent.com/82796039/123001808-1d2a8300-d3ec-11eb-81bc-a4d9798fca41.png)

    - 도메인 서열 분리 
        - Core Domain: 대여 요청, 대여 매칭, 프로모션 - 없어서는 안될 핵심 서비스이며, 연간 Up-time SLA 수준을 99.999% 목표, 배포주기는 app의 경우 1주일 1회 미만, store의 경우 1개월 1회 미만
        - Supporting Domain: 고객/대여자 지원 서비스(미포함) - 경쟁력을 위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 각 팀의 자율이나 표준 스프린트 주기가 1주일 이므로 1주일 1회 이상을 기준으로 함.
        - General Domain: 결제 - 결제서비스로 3rd Party 외부 서비스를 사용하는 것이 경쟁력이 높음 (향후 전환 예정)


### 폴리시 부착
![MSA8](https://user-images.githubusercontent.com/82796039/123001824-23206400-d3ec-11eb-895e-a573efb576f5.png)


### 폴리시의 이동과 컨텍스트 매핑 (점선은 Pub/Sub, 실선은 Req/Resp)
![MSA9](https://user-images.githubusercontent.com/82796039/123001844-287dae80-d3ec-11eb-94db-76e424e7c4f5.png)


### 완성된 1차 모형
![MSA10](https://user-images.githubusercontent.com/82796039/123001872-30d5e980-d3ec-11eb-93ea-eab4b8b49d3b.png)


### 1차 완성본에 대한 기능적 요구사항 검증
![MSA11](https://user-images.githubusercontent.com/82796039/123001893-36cbca80-d3ec-11eb-959b-8406994b56cf.png)

    - 고객이 원하는 물품,기간,비용을 기입한다 (ok)
    - 고객이 비용을 결제한다 (ok)
    - 고객이 결제하면 대여자들은 요청목록을 확인하고 대여매칭을 확정할 수 있다 (ok)
    - 정기적인 프로모션 행사가 가능해야 한다.(ok)
    - 프로모션시 할인가격으로 대여되는 물품은 취소가 불가능해야 한다.(ok)

![MSA12](https://user-images.githubusercontent.com/82796039/123001913-3d5a4200-d3ec-11eb-8ff7-57fa8e20501a.png)

    - 고객이 대여 주문을 취소할 수 있다 (ok)
    - 대여 주문이 취소되면 대여 매칭이 취소된다 (ok)
    - 고객이 대여상태를 중간중간 조회한다 (View-green sticker 의 추가로 ok)
    - 대여, 매칭 상태가 바뀔 때 마다 mypage를 통해 확인한다 (?)
    
    
### 모델 수정
![MSA13](https://user-images.githubusercontent.com/82796039/123001931-43e8b980-d3ec-11eb-85f0-6fe96e7e78e2.png)

- 수정된 모델은 모든 요구사항을 커버함.


### 비기능 요구사항 검증
![MSA14](https://user-images.githubusercontent.com/82796039/123001967-4ba85e00-d3ec-11eb-8806-edffb7ecc2bf.png)

    - 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
    - 고객 주문 시 결제처리:  결제가 완료되지 않은 주문은 절대 받지 않는다는 경영자의 철학에 따라, ACID 트랜잭션 적용. 주문완료 시 결제처리에 대해서는 Request-Response 방식 처리
    - 결제 완료 시 대여자 매칭처리:  reserve에서 lend 마이크로서비스로 매칭요청이 전달되는 과정에 있어서 lend 마이크로 서비스가 별도의 배포주기를 가지기 때문에 Eventual Consistency 방식으로 트랜잭션 처리함.
    - 나머지 모든 inter-microservice 트랜잭션: 결재상태, 매칭상태 등 모든 이벤트에 대해 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, Eventual Consistency 를 기본으로 채택함.


## 헥사고날 아키텍처 다이어그램 도출
![MSA-h](https://user-images.githubusercontent.com/82796039/122990895-66c0a100-d3df-11eb-9c1d-f97b4eeb5e47.png)

    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐
    

# 구현방안 및 검증
- 분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 **Spring Boot**로 구현하였다.
- 구현한 각 서비스  로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 8085 이다)

```
cd reserve
mvn spring-boot:run

cd lend
mvn spring-boot:run  

cd payment
mvn spring-boot:run 

cd promotion
mvn spring-boot:run 

cd mypage
mvn spring-boot:run  

cd gateway
mvn spring-boot:run  
```


## DDD(Domain-Driven Design)의 적용
- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (예시는 reserve 마이크로 서비스 - reserve.java).
- Event Storming을 통한 아키텍쳐와 Domain 구조로 DDD의 적용을 확인하며, 각 Domain에 해당하는 Entity는 다음과 같다.
- **reserve(대여요청) / payment(결제) / lend(대여매칭) / promotion(할인대여) / mypage(마이페이지)**
![22222](https://user-images.githubusercontent.com/82796039/123024190-a0130400-d413-11eb-84c2-6b582d6d83f3.jpg)

```
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
        ReserveRequested reserveRequested = new ReserveRequested();
        BeanUtils.copyProperties(this, reserveRequested);
        reserveRequested.publishAfterCommit();

        booking.external.Payment payment = new booking.external.Payment();

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

    //사용자가 해당 결재를 취소 했을 경우에는 status를 Cancel로 업데이트 보냄 
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
```
- **Entity Pattern** 과 **Repository Pattern** 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록
데이터 접근 어댑터를 자동 생성하기 위하여 **Spring Data REST** 의 RestRepository를 적용하였다.
```
package booking;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="reserves", path="reserves")
public interface ReserveRepository extends PagingAndSortingRepository<Reserve, Long>{

}

```
---
#### 검증 및 테스트
- 적용 후 REST API 의 테스트
```
# reserve 서비스의 요청처리
http POST http://localhost:8081/reserves price=20000 startDay=20210624 endDay=20210624 customer=andy  status=approve
```
![1-111](https://user-images.githubusercontent.com/82796039/123021007-41975700-d40e-11eb-8f9d-cd2eabbe4b3e.jpg)
```
# reseve 서비스의 상태확인 
http http://localhost:8081/reserves/2
```
![1-22](https://user-images.githubusercontent.com/82796039/123021187-94710e80-d40e-11eb-8bcc-c2605f232b31.jpg)
```
# reseve 서비스에 대한 lend 서비스의 응답
http http://localhost:8082/lends matchId=2 lenrer=andy
```
![1-33](https://user-images.githubusercontent.com/82796039/123021747-9daeab00-d40f-11eb-96d1-8c3148a5a8d6.jpg)

## 폴리글랏 퍼시스턴스
- reserve 서비스는 다른 서비스와 구별을 위해 hsqldb를 사용 하였다.
이를 위해 reserve 내 pom.xml에 dependency를 h2database에서 hsqldb로 변경 하였다.
```
# pom.xml
```
![aaa](https://user-images.githubusercontent.com/82796039/123068761-27309e00-d44d-11eb-8ace-a3c516c0c3f7.jpg)


## 동기식 호출과 Fallback 처리
- 분석 단계에서의 조건 중 하나로 대여요청(reserve)->결제(payment) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다.
호출 프로토콜은 이미 앞서 Rest Repository에 의해 노출되어있는 REST 서비스를 **FeignClient** 를 이용하여 호출하도록 한다. 

- 결제서비스를 호출하기 위하여 FeignClient를 이용하여 Service 대행 인터페이스(Proxy)를 구현 
```
# PaymentService.java
```
![12](https://user-images.githubusercontent.com/82796039/123071405-98715080-d44f-11eb-9130-473bd6793c84.jpg)

- 주문을 받은 직후(@PostPersist) 결제를 요청하도록 처리
```
# Reserve.java (Entity)
```
![13](https://user-images.githubusercontent.com/82796039/123071593-c191e100-d44f-11eb-9277-68b66a138759.jpg)


#### 검증 및 테스트
- 서비스를 임의로 정지하면 
- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 결제 시스템이 장애가 나면 대여요청에 대한 주문처리도 되지 않는 것이 확인 됨:
```
# 결제 (payment) 서비스를 잠시 내려놓음 (ctrl+c)
```
#대여요청(실패확인)
```
http POST http://localhost:8081/reserves price=11111 startDay=20210624 endDay=20210624 customer=andy  status=approve
```
![3-1](https://user-images.githubusercontent.com/82796039/123066880-6d84fd80-d44b-11eb-9e7c-a7e8567048b8.jpg)

#결제서비스 재기동
```
cd payment
mvn spring-boot:run
```

#대여요청(성공확인)
```
http POST http://localhost:8081/reserves price=33333 startDay=20210624 endDay=20210624 customer=andy  status=approve
```
![3-2](https://user-images.githubusercontent.com/82796039/123066946-7bd31980-d44b-11eb-81fe-b14962a14b12.jpg)
- 또한 과도한 서비스 요청시에 서비스 장애가 일어날수 있다.(서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)
---

## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성


### 비동기식 호출
- 결제가 이루어진 후에 이를 lend 시스템으로 알려주는 행위는 동기가 아닌 비동기 식으로 구현하여, 대여요청/결제시스템에 블로킹을 주지는 않는다. 
- 이를 위하여 결제이력에 기록을 남긴 후에 곧바로 결제승인이 되었다는 도메인 이벤트를 **Apache Kafka**로 송출한다(Publish)

![bbb](https://user-images.githubusercontent.com/82796039/123069257-94dcca00-d44d-11eb-9c12-bdba62202522.jpg)   

- lend 서비스에서는 결제승인 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler를 구현한다:

![ccc](https://user-images.githubusercontent.com/82796039/123069363-af16a800-d44d-11eb-8476-21be47d33e97.jpg)


### 시간적 디커플링 / 장애격리
- lend 서비스와 payment 시스템은 전혀 결합성이 없어, 만약 lend 시스템이 에러가 생기더라도 고객이 대여요청(reserve) 결제(payment)시스템은 정상적이어야 한다.
---
#### 검증 및 테스트
- lend 서비스를 잠시 종료한 후 대여요청/결제요청

```
#대여요청처리(성공)
http POST http://localhost:8081/reserves price=77777 startDay=20210624 endDay=20210624 customer=andynam status=approve
```
![9-1](https://user-images.githubusercontent.com/82796039/123077575-20a62480-d455-11eb-8685-bf31c41df557.jpg)


-결제서비스(payment)가 정상적으로 동작했는지 조회

```
http http://localhost:8083/payments/2
```
![9-2](https://user-images.githubusercontent.com/82796039/123077804-5945fe00-d455-11eb-9874-9cd6903468a0.jpg)


-lend 서비스 재가동
```
cd lend
mvn spring-boot:run
```
-lend service 요청 목록 확인

```
http http://localhost:8082/reserveReqLists/2     # 정상적으로 요청이 들어온 것 확인
```
![9-3](https://user-images.githubusercontent.com/82796039/123077993-81cdf800-d455-11eb-8cfd-0477eb1a2e5e.jpg)

-lend 기능 확인
```
http http://localhost:8082/lends matchId=2 lender=andynam   #정상적으로 매핑
```
![9-4](https://user-images.githubusercontent.com/82796039/123078200-b346c380-d455-11eb-9d5d-4e1b6d595175.jpg)

## SAGA / Correlation
- 대여승인(lend) 시스템에서 상태가 승인으로 변경되면 대여요청(reserve) 시스템 데이터 상태(status)를 update 한다
```
    }
    
    //대여승인이 됐을 경우
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverLendAssigned_StatusUpdate(@Payload LendAssigned lendAssigned){
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println("##### listener  : " + lendAssigned.toJson());
        if(!lendAssigned.validate()) return;
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        myPageRepository.findById(lendAssigned.getMatchId()).ifPresent(MyPage ->{
            System.out.println("##### listener  : " + lendAssigned.toJson());
            MyPage.setLender(lendAssigned.getLender());
            MyPage.setStatus(lendAssigned.getEventType());
            myPageRepository.save(MyPage);
        });
            
    }
```


## CQRS
- status가 변경될때마다 event를 수신하여 조회하도록 mypage에 별도의 view를 구현하여 명령과 조회를 분리했다. 
```
@Service
public class PolicyHandler{
    @Autowired MyPageRepository myPageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReserveCancelled_StatusUpdate(@Payload ReserveCancelled reserveCancelled){

        if(!reserveCancelled.validate()) return;

        myPageRepository.findById(reserveCancelled.getMatchId()).ifPresent(MyPage ->{
            System.out.println("##### listener  : " + reserveCancelled.toJson());
            System.out.println("##### wheneverReserveCancelled_MyPageRepository.findById : exist" );
            MyPage.setStatus(reserveCancelled.getEventType());
            myPageRepository.save(MyPage);
        });
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentCancelled_StatusUpdate(@Payload PaymentCancelled paymentCancelled){

        if(!paymentCancelled.validate()) return;

      
        myPageRepository.findById(paymentCancelled.getMatchId()).ifPresent(MyPage ->{
            System.out.println("##### listener  : " + paymentCancelled.toJson());
            System.out.println("##### wheneverLendCancelled_MyPageRepository.findById : exist" );
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
            System.out.println("##### wheneverLendAssigned_MyPageRepository.findById : exist" );
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
            System.out.println("##### wheneverLendCancelled_MyPageRepository.findById : exist" );
            MyPage.setStatus(lendCancelled.getEventType());
            myPageRepository.save(MyPage);
        });            
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}

}
```
###  mypage view조회
![9-5](https://user-images.githubusercontent.com/82796039/123084621-5569aa00-d45c-11eb-8dd2-cd57cc0509de.jpg)


# 배포 및 운영

## Azure Cloud 설정화면
- cna08664 계정으로 Azure에 서비스가 올라와 있다.
![ddd](https://user-images.githubusercontent.com/82796039/123378680-06408800-d5c8-11eb-932a-f0972c2a9d00.jpg)
![111](https://user-images.githubusercontent.com/82796039/123378749-1a848500-d5c8-11eb-81d0-520df4f1c030.jpg)
![222](https://user-images.githubusercontent.com/82796039/123378256-78649d00-d5c7-11eb-8298-ffa4152112f4.jpg)


## CI/CD 설정
- 각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 Azure를 사용하였으며,
pipeline build script 는 각 프로젝트 폴더 이하에 Dockerfile 과 deployment.yml/service.yaml 에 포함되었다.

* Continuos Integration Pipeline (Azure Cloud Devops)
![image](https://user-images.githubusercontent.com/82796039/123122083-ac817600-d480-11eb-99e3-e40e0fbc2585.png)
![image](https://user-images.githubusercontent.com/82796039/123121995-98d60f80-d480-11eb-8168-c9218ec2a6d0.png)


* Continuos Deployment Pipeline (Azure Cloud Devops)
![image](https://user-images.githubusercontent.com/82796039/123122198-c6bb5400-d480-11eb-812a-8f30fa9010c4.png)



## 동기식 호출 / 서킷 브레이킹 / 장애격리
* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient 옵션을 사용하여 구현함

### reserve의 PaymentService.java (reserve external 서비스)
![111](https://user-images.githubusercontent.com/82796039/123353313-33774100-d59c-11eb-8e52-91cfa226cdff.jpg)
```
- 대여 요청(catch)-->결제(payment) 시의 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 결제 요청이 과도할 경우 CB(Circuit Breaker)를 통하여 장애격리.
- 요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정
```

### application.yml (reserve 서비스)
![111](https://user-images.githubusercontent.com/82796039/123353309-3114e700-d59c-11eb-8ef8-cd779beb1571.jpg)

- 피호출 서비스(결제:payment) 의 임의 부하 처리 - 400 밀리에서 증감 220 밀리 정도 왔다갔다 하게 설정

![444](https://user-images.githubusercontent.com/82796039/123358811-16943b00-d5a7-11eb-9f95-159397a391ef.jpg)
```

* seige툴 사용법
    Default namespace 에 siege 란 이름으로 pod 생성
  $ kubectl run siege --image=cna08664/siege-nginx -n default

* siege pod 에 접속
  $ kubectl exec -it siege -c siege -n default -- /bin/bash

* 부하테스터 **siege** 툴을 통한 서킷 브레이커 동작 확인:
  - 동시사용자 100명
  - 60초 동안 10번 반복하여 실시

$ siege -c100 -t60S -r10 -v --content-type "application/json" 'http://reserve:8080/reserves POST {"price":"7777777", "startDay":"20210624", "endDay":"20210624", "customer":"andynam", "name":"sportscar", "status":"approve"}'
```

- 부하 발생하여 서킷 브레이커가 발동하여 요청에 실패하였고, 밀린 부하가 결재시스템에서 처리되면서 다시 대여요청을 받기 시작한다.

![22](https://user-images.githubusercontent.com/82796039/123354964-a635eb80-d59f-11eb-8e88-bf38a6e7c7f2.jpg)
![33](https://user-images.githubusercontent.com/82796039/123354983-acc46300-d59f-11eb-9689-53802b7e3c6d.jpg)
![aa](https://user-images.githubusercontent.com/82796039/123361284-beab0380-d5a9-11eb-9eac-d9edb40218d3.jpg)

### 오토스케일 아웃
- 동적 Scale out(replica의 자동적 추가,HPA)을 통해 시스템을 확장 해주는 설정 
- 앞서 서킷 브레이커는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 처리가능하도록 자동화된 확장 기능 적용 
- 대여요청(reserve) 및 결제(payment)서비스에 대한 replica를 동적으로 늘려주도록 HPA를 설정 
- 설정은 CPU 사용량이 15프로를 넘어서면 replica를 10개까지 늘려준다:

```
# auto scale-out 설정 (reserve/kubernetes/deployment.yml, payment/kubernetes/deployment.yml)
```
![55](https://user-images.githubusercontent.com/82796039/123355348-763b1800-d5a0-11eb-987f-2127ccc53612.jpg)
```
$ kubectl autoscale deploy reserve --min=1 --max=10 --cpu-percent=15
$ kubectl autoscale deploy payment --min=1 --max=10 --cpu-percent=15
```
![11](https://user-images.githubusercontent.com/82796039/123355489-bd290d80-d5a0-11eb-9ba7-419a7f1cb152.jpg)

---

#### 검증 및 테스트
```
- CB 에서 했던 방식대로 워크로드를 2분 동안 걸어준다.

$ siege -c100 -t60S -r10 -v --content-type "application/json" 'http://reserve:8080/reserves POST {"price":"7777777", "startDay":"20210624", "endDay":"20210624", "customer":"andynam", "name":"sportscar", "status":"approve"}'

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:

$ kubectl get deploy reserve -w
$ kubectl get deploy payment -w
$ kubectl get pod -w
```
- 어느정도 시간이 흐른 후 스케일 아웃이 벌어지는 것을 확인할 수 있다:

![44](https://user-images.githubusercontent.com/82796039/123355003-b948bb80-d59f-11eb-89b8-9ffb8f40f344.jpg)


## 무정지 재배포
* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Readiness 옵션이 추가된 deployment.yml을 적용

```
$ kubectl apply -f payment/kubernetes/deployment.yml
```
![aa](https://user-images.githubusercontent.com/82796039/123363391-5c540200-d5ad-11eb-937a-8c1d00f069c7.jpg)


* 새버전으로의 배포 시작
```
kubectl set image deploy payment payment=cna08664.azurecr.io/payment:v2 -n default
```

* 기존 버전과 새 버전의 reserve pod 공존 중
![zz](https://user-images.githubusercontent.com/82796039/123366459-3c274180-d5b3-11eb-86f6-8b3652b5e9cd.jpg)

* Availability : 100% 확인
![aa](https://user-images.githubusercontent.com/82796039/123361284-beab0380-d5a9-11eb-9eac-d9edb40218d3.jpg)


## Config Map
* application.yml 설정 (reserve 서비스)

- default 부분
![bb](https://user-images.githubusercontent.com/82796039/123365235-db970500-d5b0-11eb-9973-e26718fb0ef8.jpg)

* Config Map 설정
![aa](https://user-images.githubusercontent.com/82796039/123365294-f8333d00-d5b0-11eb-97c2-6dde3ff35687.jpg)

* config map 생성 후 조회
```
$ kubectl create configmap apiurl --from-literal=url=http://payment:8080 --from-literal=fluentd-server-ip=10.xxx.xxx.xxx -n default
$ kubectl get configmap apiurl -o yaml -n default
```
![33](https://user-images.githubusercontent.com/82796039/123367162-73e2b900-d5b4-11eb-85b5-eb27c55fa50e.jpg)

* 위 config map으로 배포한 서비스에서 설정한 URL로 대여요청 호출
```
http POST http://reserve:8080/reserves price=10000 startDay=20160625 endDay=20160630 customer=peter status=Approved name=TedyBear
```
![11](https://user-images.githubusercontent.com/82796039/123366853-e7d09180-d5b3-11eb-947c-4c8ce98dc825.jpg)

## Self-healing (Liveness Probe)

* payment 서비스 정상 확인
![11](https://user-images.githubusercontent.com/82796039/123367749-709bfd00-d5b5-11eb-9fc3-5afa65085b0d.jpg)

* deployment.yml (payment 서비스)에 Liveness Probe 옵션 추가
![22](https://user-images.githubusercontent.com/82796039/123367761-77c30b00-d5b5-11eb-9427-7175ab91145c.jpg)

* payment deploy 재배포 후 liveness 가 적용된 부분 확인
```
$ kubectl describe deploy reserve -n default
```
![33](https://user-images.githubusercontent.com/82796039/123367988-e86a2780-d5b5-11eb-84c4-7ad67687a6ba.jpg)



