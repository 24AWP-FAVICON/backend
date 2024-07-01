# TOUS - backend
> Team. Favicon

[프론트엔드 레포 바로 가기](https://github.com/24AWP-FAVICON/frontend)

# 1. Service Introduction
> ToUS(ToUslTours)
- This service is a platform that allows you to easily manage everything from planning your trip to sharing it.
- Users can create amazing travel itineraries, communicate with friends in real time, and vividly share their travel moments on social media.

# 2. System Requirements
> UML; Use-case Diagram
<img width="852" alt="스크린샷 2024-07-01 오후 7 49 19" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/e9fbfc22-ea55-4560-adb8-14253f4ec766">

# 3. System Design
## ERD(Entity-Relationship Diagram)
### (1) Overview
<img width="1030" alt="스크린샷 2024-07-01 오후 7 50 27" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/8f5456a0-f652-4495-8cec-b93d25a95388">

### (2) User Service
<img width="1015" alt="스크린샷 2024-07-01 오후 7 58 29" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/f24c0b43-4a63-49f7-8bd2-76086f1de3a1">

- **AlarmSettings Table:** To store alarm setting values.
- **AlarmEntity table:** To record alarm text and metadata.
- **User table:** Stores user info, authorization, metadata.

user service의 erd입니다.
<br>
저희 tous 프로젝트에서는 구글 로그인을 사용하여 인증을 위임하기 때문에 user 테이블에서 개인정보 관련 필드는 구글id와 닉네임으로 충분하다고 판단했습니다. 생성일, 탈퇴일, 접속시간, 역할 등은 비즈니스 로직에서 필요하기 때문에 user 테이블에 추가했습니다.
<br>
다음으로 알림 설정 테이블에서는 4가지 종류의 알림에 대해 끄고 킬 수 있도록 bit값으로 구성했고 user테이블과 일대일로 연관시켰습니다.<br>
alarmEntity 테이블에서는 알림의 내용, 알림 생성일, 알림 송신자, 알림 수신자, 알림 타입등으로 구성했고 user 테이블과 다대일로 연관시켰습니다.


### (3) SNS Service
<img width="998" alt="스크린샷 2024-07-01 오후 7 58 36" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/1ed3ee04-418a-4244-b0a8-23c168752739">

- **Post Table:** To store created post.
- **Attachment table:** To record attachment metadata attached to post.
- **User table:** Stores user info, authorization, metadata. 

sns service 관련 erd 입니다. 
<br>
post를 생성할 때 클라이언트로부터 제목, 본문, 썸네일 이미지 주소를 입력받아야 하기 때문에 post 테이블에 해당 필드를 생성했고 ‘비공개 설정 여부 가능’ 요구사항에 따라 공개여부도 추가했습니다.
<br>
post에 업로드되는 파일은 s3에 저장하지만 post를 수정하거나 삭제할때 첨부파일을 쉽게 관리하기 위해 attachment 테이블을 만들어 첨부파일의 메타데이터를 저장하고 post와 일대다 연관시켰습니다.
<br>
post에 대한 댓글은 comment테이블에 저장되고 n차 대댓글 요구사항을 구현하기 위해 부모댓글 id를 필드에 추가했습니다.
<br>
차단, 팔로우, post 테이블은 user테이블과 다대일로 연관시켰습니다.
댓글, 좋아요, 조회, 첨부파일 테이블은 post테이블과 다대일로 연관시켰습니다.


### (4) Messenger Service
 <img width="769" alt="스크린샷 2024-07-01 오후 7 58 43" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/2264df81-6f07-417e-a8eb-22e1944f51da">

- **Chat Room Table:** To store chat room information.
- **ChatJoin table:** To record which chat rooms a user is joining.
- **ChatMessage table:** Stores messages sent and received in the chat room.
- **UnreadMember table:** Records users who have not read a specific message.

다음으로는 메신저 서비스에 관한 ERD 입니다. 메신저 서비스는 사용자가 채팅 방에 참여하고 메시지를 주고받을 수 있도록 설계되었습니다. 주요 테이블과 그 역할은 다음과 같습니다
<br>
ChatRoom 테이블에서서는 채팅 방 정보를 저장합니다. ChatJoin을 이용하여 일대다 관계로 사용자와 채팅방 간의 참여 관계를 기록하여 사용자가 어떤 채팅방에 참여하고 있는지 기록합니다. 사용자의 메시지 대화 내역은 기록되어 페이지에 다시 접속하더라도 저장되고 보여야 하므로, ChatMessage 테이블에 채팅방 id, 보낸 발신자의 userId, 메시지의 내용, 메시지 전송시간 등을을 토대로 메시지 정보를 저장합니다. 


### (5) Travel plnner Service
<img width="908" alt="스크린샷 2024-07-01 오후 7 58 49" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/23d2890c-b122-4c66-b753-f098adce39e0">

- **Trip table:** To store basic information for trip planning.
- **TripDate table:** Stores details of a trip itinerary.
- **Location table:** Stores information about places to visit during the trip.
- **Accommodation table:** To store information about accommodations during a trip.

다음으로는 여행 계획 서비스인 Trip Planner 서비스 ERD입니다. Trip Planner 서비스는 사용자가 여행 계획을 세우고 관리할 수 있도록 도와주는 기능을 제공합니다.
<br>
먼저, Trip 테이블은 여행 계획의 기본 정보를 저장합니다. 여기에는 여행 이름, 여행 지역, 여행 시작일과 종료일, 그리고 여행 예산이 포함됩니다. 이 테이블은 사용자가 계획한 여행의 전체적인 개요를 담고 있습니다.
<br>
다음으로, 여행 일정의 세부 정보는 TripDate 테이블에 저장됩니다. TripDate 테이블에는 여행 날짜와 해당 일정의 예산이 저장됩니다. 이를 통해 사용자는 특정 날짜에 어떤 일정을 계획했는지, 그리고 그 날짜에 사용할 예산을 쉽게 확인할 수 있습니다. 
한 날짜에 대해 여러 장소를 방문할 수 있으므로, Location 테이블이 TripDate 테이블과 일대다 관계로 구성됩니다. Location 테이블에는 방문할 장소의 이름과 주소, 그리고 관련된 여행 날짜가 저장됩니다. 
숙소 정보는 Accommodation 테이블에 저장됩니다. Accommodation 테이블은 TripDate 테이블과 일대일 관계로 구성되어 있으며, 여기에는 숙소의 이름과 위치, 그리고 여행 날짜가 저장됩니다. 

<br><br><br><br>


## 4. System Architecture
<img width="1897" alt="스크린샷 2024-07-01 오후 7 58 06" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/8f943620-fd8c-4d32-8632-f16ca3faea65">

# 5. Collaboration Method
## Agile Methodology
**1. Test Driven Development**
<img width="1154" alt="스크린샷 2024-07-01 오후 8 02 08" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/11587360-f13c-433c-8709-97e64c8f0fff">

**2. Pair Programming**
<img width="1136" alt="스크린샷 2024-07-01 오후 8 02 25" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/9439a4e1-3c8c-45d3-8f9b-5c691967fd53">

**3. Code Convention**
<img width="1211" alt="스크린샷 2024-07-01 오후 8 02 39" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/509fc558-29a4-447f-933e-c5c277ccff74">

**4. Issue Management**
<img width="1246" alt="스크린샷 2024-07-01 오후 8 03 11" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/c343363b-4698-4eb0-80fe-e993885ca9fb">

**5. Github Projects, Issue, Pull Request, Code Review**
<img width="1260" alt="스크린샷 2024-07-01 오후 8 03 21" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/4308909e-c372-4986-a3a2-c4f8fead37a5">

# 6. Tech Stack
## API documentation
Use Notion → Swagger UI

## Overview
|Requirements Service|Implements with|
|------|---|
|1. User Service|→ Google Oauth, Redis|
|2. Messenger Service|→ Websocket - STOMP|
|3. Community Service|→ Amazon S3|
|4. Trip Planner Service|→ CRUD API|
|5. Alert Service|→ Kafka, Zookeeper|

## 1. User Service
(PIC: [@YoonYn9915](https://github.com/YoonYn9915))
> Google Oauth Login

<img width="829" alt="스크린샷 2024-07-01 오후 8 27 37" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/36774651-e00f-4603-8e36-44c21e876578">
저희 서비스에서는 구글 소셜 로그인을 구현하기 위해 spring oauth2 client를 사용했습니다.
spring oauth2 client를 이용한 인증 로직을 sequence diagram으로 나타내 보았습니다.
<br>
로그인 플로우는 다음과 같습니다.
클라이언트가 /users/login 엔드포인트에 접속하면 Google 인증 서버의 로그인 페이지로 리디렉션되며, 인증이 완료된 후에는 추후 google api 사용을 위해 google id와 google access token을 redis에 저장하고 회원가입한 새로운 사용자를 mysql user 테이블에 저장합니다. 
tous service의 Access Token과 Refresh Token을 발급해 redis에 저장하고, Access Token은 응답 헤더에, Refresh Token은 쿠키에 저장하여 회원이 로그인 후 JWT를 사용해 stateless 인증을 할 수 있게 합니다.


<img width="859" alt="스크린샷 2024-07-01 오후 8 27 58" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/cf909cf7-fb9c-4e25-bcc1-b23bd7938f96">

1. when google authentication success, execute CustomOAuth2UserService to save user field 
2. get Authentication Principal of logged user
3. save new user to User table.

구글 로그인이 성공하면 CustomOAuth2UserService 클래스를 실행합니다.
CustomOAuth2UserService의 loadUser 메서드에서 구글 인증 정보를 가지고 있는 OAuth2User 클래스를 생성한 후 회원의 구글 id와 nickname을 가져오고 
userId를 사용해 회원가입여부를 확인한 후 mysql user 테이블에 저장합니다.  


<img width="859" alt="스크린샷 2024-07-01 오후 8 28 26" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/93659a04-ff8f-4b1f-b699-60e9ded08f76">

1. save google id and google access token to redis
2. send access token and refresh token to client

google id와 google access token, tous service의 access token과 refresh token을 redis에 저장한 후 access token은 Authorization 헤더에 저장하고 refresh token은 쿠키에 저장하여 응답으로 전송합니다.



## 2. Messenger Service
(PIC: [@Mingguriguri](https://github.com/Mingguriguri))
> **ChatRoom CRUD API**
<img width="1038" alt="스크린샷 2024-07-01 오후 8 19 04" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/fe6f98d9-48e3-4852-ac33-28a08c674cf1">

1. Creating a chat room
2. Retrieving a chat room
3. Getting chat room information
4. Retrieving chat history
5. Renaming a chat room
6. Inviting a user to a chat room
7. Leaving a chat room

> **Websocket 실시간 통신**
<img width="994" alt="스크린샷 2024-07-01 오후 8 10 48" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/1970a7ea-427d-4c60-81d8-8c6ac7cf7443">
채팅을 구현하기 위해서는 서버의 이벤트를 클라이언트로 보내야 합니다. 이 방법은  크게 폴링, SSE, 웹소켓이 있습니다. 폴링은 클라이언트가 HTTP request를 서버로 계속 보내서 이벤트 내용을 전달 받는 방식으로 계속적으로 request를 보내기 때문에 서버의 부담이 갈 수 있고 http 오버헤드가 발생할 수 있습니다.
Server Sent Events, SSE는 서버의 데이터를 실시간, 지속적으로 스트리밍하는 기술입니다. 이는 가볍다는 특징이 있지만 양방향이 아닌 단방향에 사용되는 기술입니다.
웹소켓은 TCP 연결을 통해 양방향 통신 채널을 제공합니다. 웹소켓을 사용하면 실시간으로 데이터를 양쪽에서 주고 받을 수 있습니다.

채팅 기능의 경우, 상대방으로부터 메시지를 실시간으로 받아야 하고, 자신이 작성한 메시지도 실시간으로 보내야 합니다. 즉, 채팅 기능은 실시간으로 양방향 통신을 해야 하기 때문에 웹소켓 방식이 적절합니다.
<br><br><br>

> **WebSocket Configuration Explanation**
- **Problem:**
  - Standard WebSocket works well only when there is one server.
  - When there are multiple servers, there is a problem of sharing session information.

- **Solution:**
  - Considering scalability, we used STOMP to ensure the system operates stably even with multiple WebSocket servers.

- **What is STOMP?:**
  - Stands for Simple Text Oriented Messaging Protocol.
  - A protocol that allows easy message exchange using a message broker.
  - pub-sub (publish-subscribe) model: When a sender publishes a message, subscribers to the same topic receive it.
    
<img width="674" alt="스크린샷 2024-07-01 오후 8 10 55" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/56d6bb00-eb72-4e6d-83b9-4a6228474f7c">

- **Advantages of STOMP:**
  - Not a protocol made exclusively for WebSockets, but can be used with several bidirectional communication protocols.
  - No need to worry about message format and parsing logic, simplifying implementation.

웹소켓은 텍스트와 바이너리 타입의 메시지를 양방향으로 주고받을 수 있는 프로토콜로 메시지를 주고 받는 형식이 따로 정해져 있지는 않습니다. 웹소켓만을 사용하는 프로젝트가 커지게 되면 주고 받는 메시지에 대한 형식이 중요해지게 되고, 그러면 메시지 형식대로 파싱하는 로직도 따로 구현해야 합니다. 하지만 STOMP를 사용하면 메시지 형식에 대한 고민과 파싱 로직을 위한 코드 구현이 필요없어집니다.
저희 팀은 확장성을 고려하여, 웹소켓 서버가 여러 대인 경우에도 시스템이 안정적으로 동작할 수 있도록 STOMP를 사용했습니다.
STOMP는 Simple Text Oriented Messaging Protocol의 약자로, 메시지 브로커를 활용하여 쉽게 메시지를 주고 받을 수 있는 프로토콜입니다. 여기서 pub-sub은 발행과 구독을 의미하며, 이는 발신자가 메시지를 발행하면 같은 구독을 하고 있는 수신자가 그것을 수신하는 메시징 패러다임입니다. 

<br><br><br>
> **Websocket Implementation**
<img width="1662" alt="스크린샷 2024-07-01 오후 8 11 04" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/96f31beb-aa06-4227-836c-4a9e2d370bec">
웹소켓을 구현하기 전에 우선 먼저 웹소켓 의존성을 추가해준 후, ChatMessage에 연결된 채팅방 id를 구독 경로로 정했습니다.

<img width="1128" alt="스크린샷 2024-07-01 오후 8 11 20" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/bb61af14-2ba6-4eca-a1c4-3c8397a5c0de">

- WebSocketConfig class: sets up a WebSocket endpoint and message broker.
  - Configure a websocket connection through the /ws endpoint.
  - Clients subscribe to messages with the /sub path and publish messages with the /pub path

WebSocketConfig 클래스에서 웹소켓 엔드포인트와 메시지 브로커를 설정했습니다. /ws 엔드포인트를 통해 웹소켓 연결을 설정하고, 클라이언트는 /sub 경로로 메시지를 구독하며, /pub 경로로 메시지를 발행합니다.

클라이언트 사용자 구독경로는 /sub/channel/채팅방아이디 의 형태로 가도록 구성하였습니다. 
메시지를 발송할 때에는 /pub/message로 json형태로 메시지를 보내며, 메시지를 보낼 때 채널아이디를 포함하도록 설정하였고, 이 부분은 뒤에 코드에서 설명드리겠습니다.

<img width="867" alt="스크린샷 2024-07-01 오후 8 11 29" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/28536522-12e7-4d6f-8ae1-60b52995f8ee">

- The MessageController class receives messages from clients, processes them, and sends them to subscribed clients.
- Publishing a message from a client to /pub/message
  - Clients who are subscribed to /sub/channel/chatroomId will receive the message. 

MessageController 클래스에서 클라이언트로부터 메시지를 수신하고, 이를 처리하여 구독된 클라이언트에게 전송합니다.

클라이언트에서 /pub/message로 메시지를 발행하면 메시지에 정의된 채팅방 id에 메시지를 보내게 됩니다. /sub/channel/채팅방id 에 구독중인 클라이언트는 메시지를 받게 됩니다. 
이때 저희는 받을뿐만 아니라 데이터베이스에 저장되는 기능도 추가하였습니다.

<img width="1735" alt="스크린샷 2024-07-01 오후 8 11 38" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/4383dfb6-1bc2-42dd-b76e-25371029d52c">
추가로 같은 채팅방에 있는 다른 유저가 메시지를 읽었는지에 대한 로직도 구현해두었습니다.

<img width="1710" alt="스크린샷 2024-06-09 오전 12 46 56" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/b5f0c185-36ef-48a4-a728-95a93adf447e">
이렇게 서로 다른 사용자가 통신하는 것을 확인해보실 수 있습니다. 콘솔창을 확인해보시면 메시지도 정상적으로 전달되는 것을 확인할 수 있습니다.

<br><br>

## 3. Community Service
(PIC: [@YoonYn9915](https://github.com/YoonYn9915))
> Object save to S3

<img width="1046" alt="스크린샷 2024-07-01 오후 8 27 19" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/a85e951e-6e7c-4aa6-a165-2f02b4db4fc2">

저희는 post service의 요구사항에 따라 post에 파일을 첨부할 수 있게 구현하였으며  첨부파일을 바이너리 형태로 mysql에 직접 저장하는 것은 비효율적이라고 판단해서 object storage인 s3를 이용해 첨부파일을 저장하기로 결정하였고 post 조회, 수정, 삭제시 s3에 저장된 첨부파일을 관리하기 쉽도록 파일 크기, 경로 등 파일의 메타데이터만 mysql에 저장했습니다. 

<img width="945" alt="스크린샷 2024-07-01 오후 8 26 51" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/adec5136-4f13-4023-bb22-0063ca3765c3">

1. requested from client by MultipartFile
2. extract original filename and extension
3. get ByteArrayInpuyStream from MultipartFile
4. save attachment to s3 

s3로 이미지를 저장하는 과정은 다음과 같습니다.
클라이언트로부터 MultipartFile형태로 첨부파일을 받아서 원본 파일명과 확장자를 추출하고 MultipartFile을 바이트 배열로 변환한 후 파일의 메타데이터를 설정하고  putObject 메서드를 사용하여 s3에 파일을 업로드합니다.



<br>

## 4. Trip Planner Service
(PIC: [@Mingguriguri](https://github.com/Mingguriguri))
> CRUD APIs

Trip Planner서비스에서는 여행 일정에 관련한 CRUD, 각각의 여행 일정에 대한 세부 일정에 관한 CRUD와 다른 사용자를 초대하는 API를 구성하였습니다.

- Trip
  <img width="1405" alt="스크린샷 2024-07-01 오후 8 11 54" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/368910bd-7a7c-4978-9c62-fc98686060cd">

- TripDate
<img width="1536" alt="스크린샷 2024-07-01 오후 8 14 40" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/d3fb6f3f-4679-455a-8548-85a65f9944cb">

- Invite User
<img width="1311" alt="스크린샷 2024-07-01 오후 8 12 08" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/54b07239-942b-41c3-a841-f9e65f37c084">


## 5. Alert Service
(PIC: [@YoonYn9915](https://github.com/YoonYn9915))

> Real time asynchronous alarm using Kafka, sse emitter
<img width="845" alt="스크린샷 2024-07-01 오후 8 25 29" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/068a8d35-4116-47e6-9543-07710f565c77">
저희는 요구사항에 따라 1. 팔로우한 회원이 새 post를 작성할 때, 2. 내 post에 좋아요가 생성되었을 때, 3. 내 post에 댓글이 생성되었을때, 4. 새 메시지가 도착했을때 알림이 발생하도록 구현하였습니다.
로그인되어있는 사용자가 주기적으로 요청하지 않아도 실시간으로 알림을 받을 수 있도록 SSE emitter를 사용해 알림을 구현하였으며, 알림을 발생시키는 로직(새 post 작성, 내 post 좋아요 생성, 내 post 댓글 생성, 새 메시지 도착)과 알림을 받는 로직을 분리하기 위해 kafka를 사용하여 내부적으로 비동기적으로 알림을 수신하였습니다.


<img width="672" alt="스크린샷 2024-07-01 오후 8 25 16" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/a8fa3bb1-f163-4d55-a8bc-cd09cef92a7a">

1. use event source to request connection and addEventListener to get streaming event data
2. connect one-way communication by sse 
3. check unread Alarm

실시간 알림을 수신하는 과정은 다음과 같습니다.
클라이언트에서 /subscribe로 sse 연결을 요청하면 서버에서는  sse emitter를 사용해 단방향 연결을 생성하고 이전에 미수신한 알람이 있는지 확인합니다.


<img width="684" alt="스크린샷 2024-07-01 오후 8 24 31" src="https://github.com/24AWP-FAVICON/backend/assets/101111603/6ad5aa57-692d-4313-b2dd-1da089cf520d">

1. save event that creates alarm and send alarm to Kafka.
2. listen ‘alarm’ topic.
3. send real time asynchronous alarm using Kafka and sse 

sse 연결이 된 회원에게 새로운 알람이 생성되면 Kafka producer에서는 알람을 발생시킨 이벤트를 저장하고 kafkaTemplate을 통해 kafka에 ‘alarm’이라는 topic 이름으로 alarm을 발생시킵니다.
Kafka Consumer에서는 alarm이 발생하면 sseEmitter send 메서드를 통해 클라이언트로 실시간 alarm을 전송합니다. 


# 7. Role
|Frontend|Backend|
|------|---|
|201835457 박재민|201939734 김민정|
|202031527 양지훈|201835532 조윤상|
|202031512 김민재||


