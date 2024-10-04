package com.example.demo.service.planner;

/**
 * 이 예외는 지정된 ID를 가진 컴포넌트를 찾을 수 없을 때 발생합니다.
 * 다양한 컴포넌트(예: 여행 계획, 세부 일정 등)가 시스템에 존재하지 않는다는 것을 나타냅니다.
 */
public class ComponentNotFoundException extends RuntimeException {

    /**
     * 지정된 상세 메시지를 사용하여 새로운 ComponentNotFoundException을 생성합니다.
     *
     * @param message 예외 발생 이유를 설명하는 상세 메시지
     */
    public ComponentNotFoundException(String message) {
        super(message);
    }
}
