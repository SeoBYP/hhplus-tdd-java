package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserPointTableTest {

    private UserPointTable userPointTable;

    /**
     * 신규 유저 조회에는 포인트가 0이고 0을 반환해야 합니다.
     */
    @Test
    void 신규_유저_조회시_포인트0_반환() {
        // given
        userPointTable = new UserPointTable();
        long givenUser = 1L;

        // when
        // @BeforeEach에서 초기화된 service 인스턴스를 사용합니다.
        UserPoint userPoint = userPointTable.selectById(givenUser);

        // then
        assertEquals(givenUser, userPoint.id());
        assertEquals(0L, userPoint.point());
    }
}
