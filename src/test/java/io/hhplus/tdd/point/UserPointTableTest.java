package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserPointTableTest {

    private UserPointTable userPointTable;
    private PointService pointService;

    @BeforeEach
    void setUp(){
        this.userPointTable = new UserPointTable();
        this.pointService = new PointService(this.userPointTable);
    }

    /**
     * 신규 유저 조회에는 포인트가 0이고 0을 반환해야 합니다.
     */
    @Test
    void 신규_유저_조회시_포인트0_반환() {
        // given
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
        long givenUser = 1L;
        long givenPoint = 100L;

        // when & then
        UserPoint up = pointService.charge(givenUser, givenPoint);

        assertEquals(givenUser, up.id());
        assertEquals(givenPoint, up.point());
    }

    /**
     * 기존 유저는 기존 포인트에서 누적되어야 합니다.
     */
    @Test
    void 기존_유저의_충전_테이블에_포인트가_누적된다() { // throws 절 제거
        long givenUser = 1L;
        long givenPoint = 100L;
        // given
        pointService.charge(givenUser, givenPoint);

        // when & then
        long chargeAmount = 50L;
        UserPoint newUp = pointService.charge(givenUser, chargeAmount);

        // 갱신된 Point로 되어있는 지 확인
        assertEquals(givenUser, newUp.id());
        assertEquals(givenPoint + chargeAmount, newUp.point());
    }

    /**
     * 기존 유저가 포인트를 사용하면 기존 포인트에서 포인트가 차감되어야 합니다
     */
    @Test
    void 기존_유저의_충전_테이블에_포인트가_차감된다() { // throws 절 제거
        long givenUser = 1L;
        long givenPoint = 100L;
        // given
        pointService.charge(givenUser, givenPoint);

        // when & then
        long useAmount = 50L;
        UserPoint up = pointService.use(givenUser, useAmount);

        // 갱신된 Point로 되어있는 지 확인
        assertEquals(givenUser, up.id());
        assertEquals(givenPoint - useAmount, up.point());
    }

    /**
     * 잔액이 부족하면 예외가 발생합니다.
     */
    @Test
    void 잔액부족_포인트_사용시_예외가_발생한다() { // throws 절 제거
        // given
        long givenUser = 1L;
        long givenPoint = 100L;
        pointService.charge(givenUser, givenPoint);

        // when & then
        long useAmount = 200L;
        assertThrows(RuntimeException.class,
                () -> pointService.use(givenUser, useAmount)
        );

        // 그리고 포인트는 그대로 유지되어야 함
        assertEquals(givenPoint, pointService.getUserPoint(givenUser).point());
    }

    /**
     * 음수 충전은 발생해서는 안됩니다.
     */
    @Test
    void 음수_충전은_거부되어야_한다() { // throws 절 제거
        long givenUser = 1L;
        assertThrows(RuntimeException.class, () -> pointService.charge(givenUser, -10L));
    }

}
