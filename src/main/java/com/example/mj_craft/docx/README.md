###  1. 기술 스택
- Project: Gradle - Kotlin
- Language: java 21
- SpringBoot 4.0.1
- Dependencies
    + Spring Boot Dev Tools
    + Lombok
    + Spring Web
    + Thymeleaf
    + Spring Security
    + Spring Data JPA
    + Validation
    + MySQL Driver
    + Swagger
--------------
 
### 2. 클래스 구조
<img src="https://github.com/user-attachments/assets/1e86d936-9d33-43ce-880a-4339ec29230c">

-------------
### 3. SecurityConfig.java
- filterchain method 제공
- password encoder 제공

---------
### 4. User.java
* 필드
  - Long id: database가 식별하기 위해 사용되는 id / 자동으로 배정 됨.
  - String userId: 사용자 아이디 / 상황에 따라 student id(학번)으로 수정.
  - String userName: 사용자 이름
  - String email; 사용자 이메일
  - String password; 사용자 비밀번호 / 암호화해서 DB 저장
  - Role role; enum / ADMIN, USER

---------
### 5. UserRepository.java
* JpaRepository를 상속받아 사용함.


* existsByUserId()
  - 데이터 베이스에서 userId가 같은 entity가 있는지 검색.

### 6. UserService.java & UserServiceImpl
* 서비스가 수정될 수 있다고 생각하여 구현체와 interface를 나눴음.


* UserRepository에 의존함.
  - 의존관계: UserController -> UserService -> UserRepository


* 서비스 기능 = signup(회원가입)
  1. repository에서 id 중복 체크
  2. password 암호화
  3. repository에 save : 이때 role도  설정.
  4. signup에서 다시 UserDTO를 return

### 7. UserController.java
*  UserServiceImpl에 의존함.
* UserDTO를 service에 전달하여 회원가입 구현.