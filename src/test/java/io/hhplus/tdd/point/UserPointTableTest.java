package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserPointTableTest {

    private PointService pointService;

    /**
     * 신규 유저 조회에는 포인트가 0이고 0을 반환해야 합니다.
     */
    @Test
    void 신규_유저_조회시_포인트0_반환() {
        // given
        pointService = new PointService(new UserPointTable());
        long givenUser = 1L;

        // when
        UserPoint userPoint = pointService.getUserPoint(givenUser);

        // then
        assertEquals(givenUser, userPoint.id());
        assertEquals(0L, userPoint.point());
    }

    /**
     * 신규 유저의 포인트 충전에는 테이블에 데이터가 생성되고 포인트가 누적된다(0에서 충선양 만큼)
     */
    @Test
    void 신규_유저의_충전_테이블에_생성되고_포인트가_누적된다() {
        // given
        pointService = new PointService(new UserPointTable());
        long givenUser = 1L;
        long givenPoint = 100L;

        // when & then
        UserPoint up = pointService.charge(givenUser, givenPoint);

        assertEquals(givenUser, up.id());
        assertEquals(givenPoint, up.point());
    }
}
