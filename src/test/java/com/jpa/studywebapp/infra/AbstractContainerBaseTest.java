package com.jpa.studywebapp.infra;

import org.testcontainers.containers.PostgreSQLContainer;

//각각의 클래스별로 테스트 클래스를 만들면 너무 용량 낭비가 심해서 추상 클래스로 최초 1 회만 띄우도록 정의
public abstract class AbstractContainerBaseTest {

    static final PostgreSQLContainer POSTGRE_SQL_CONTAINER;

    static {
        POSTGRE_SQL_CONTAINER = new PostgreSQLContainer();
        POSTGRE_SQL_CONTAINER.start();
    }
}
