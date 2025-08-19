package com.hocs.server.pipline_orchestrator.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskClassifierTest {

    private final TaskClassifier taskClassifier = new TaskClassifier();

    @Test
    @DisplayName("태스크 수가 200개 초과면 HEAVY 타입을 반환한다")
    void shouldReturnHeavyTaskTypeWhenSizeIsGreaterThan200() {
        // given
        int taskSize = 201;

        // when
        TaskType result = taskClassifier.classify(taskSize);

        // then
        assertThat(result).isEqualTo(TaskType.HEAVY);
    }

    @Test
    @DisplayName("태스크 수가 200개 이하면 FAST 타입을 반환한다")
    void shouldReturnFastTaskTypeWhenSizeIsLessThanOrEqual200() {
        // given
        int taskSize = 200;

        // when
        TaskType result = taskClassifier.classify(taskSize);

        // then
        assertThat(result).isEqualTo(TaskType.FAST);
    }

    @Test
    @DisplayName("태스크 수가 0개면 FAST 타입을 반환한다")
    void shouldReturnFastTaskTypeWhenSizeIsZero() {
        // given
        int taskSize = 0;

        // when
        TaskType result = taskClassifier.classify(taskSize);

        // then
        assertThat(result).isEqualTo(TaskType.FAST);
    }

    @Test
    @DisplayName("경계값 201에서 HEAVY 타입을 반환한다")
    void shouldReturnHeavyTaskTypeAtBoundary201() {
        // given
        int taskSize = 201;

        // when
        TaskType result = taskClassifier.classify(taskSize);

        // then
        assertThat(result).isEqualTo(TaskType.HEAVY);
    }
}
