package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.timeProvider.FakeTimeProvider;
import io.hhplus.tdd.timeProvider.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserPointTableTest {

    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;
    private PointService pointService;
    private FakeTimeProvider fakeTimeProvider;

    @BeforeEach
    void setUp() {
        this.userPointTable = new UserPointTable();
        this.pointHistoryTable = new PointHistoryTable();
        this.fakeTimeProvider = new FakeTimeProvider();
        this.pointService = new PointService(this.userPointTable, this.pointHistoryTable, this.fakeTimeProvider);
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
    void 기존_유저의_충전_테이블에_포인트가_누적된다() {
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
    void 기존_유저의_충전_테이블에_포인트가_차감된다() {
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
    void 잔액부족_포인트_사용시_예외가_발생한다() {
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
    void 음수_충전은_거부되어야_한다() {
        long givenUser = 1L;
        assertThrows(RuntimeException.class, () -> pointService.charge(givenUser, -10L));
    }

    /**
     * 음수 사용은 발생해서는 안됩니다.
     */
    @Test
    void 음수_사용은_거부되어야_한다() {
        long givenUser = 1L;
        assertThrows(RuntimeException.class, () -> pointService.use(givenUser, -10L));
    }

    /**
     * 신규 유저는 충전 히스토리가 비어있어야 합니다.
     */
    @Test
    void 신규유저는_충전_히스토리가_비어있다() {
        long givenUser = 1L;

        List<PointHistory> history = pointService.getUserPointHistories(givenUser);

        assertTrue(history.isEmpty());
    }

    /**
     * 충전시 히스토리가 생성되고 UserId, Amount, TransactionType, udpateMillis가 정확하게 기록되어야 합니다.
     */
    @Test
    void 충전_시_히스토리가_1건_생성되고_필드값이_정확하다() { // throws 절 제거
        // given
        long givenUser = 1L;
        long givenPoint = 100L;

        // when
        pointService.charge(givenUser, givenPoint);
        List<PointHistory> history = pointService.getUserPointHistories(givenUser);

        //then
        assertEquals(1, history.size());

        PointHistory h = history.get(0);
        assertEquals(givenUser, h.userId());
        assertEquals(givenPoint, h.amount());
        assertEquals(TransactionType.CHARGE, h.type());
        assertTrue(h.updateMillis() <= System.currentTimeMillis());
    }

    /**
     * 충전시 히스토리가 생성되고 UserId, Amount, TransactionType, udpateMillis가 정확하게 기록되어야 합니다.
     */
    @Test
    void 차감_시_히스토리가_1건_생성되고_필드값이_정확하다() {
        // given
        long givenUser = 1L;
        long givenPoint = 100L;
        userPointTable.insertOrUpdate(givenUser, givenPoint);

        // when
        long useAmount = 50L;
        pointService.use(givenUser, useAmount);
        List<PointHistory> history = pointService.getUserPointHistories(givenUser);

        //then
        assertEquals(1, history.size());

        PointHistory h = history.get(0);
        assertEquals(givenUser, h.userId());
        assertEquals(useAmount, h.amount());
        assertEquals(TransactionType.USE, h.type());
        assertTrue(h.updateMillis() <= System.currentTimeMillis());
    }

    /**
     * 2회 이상 충전시에는 충전 히스토리가 항상 최신순으로 정렬되어야 합니다.
     */
    @Test
    void 다건_충전_시_히스토리는_최신순으로_정렬된다() { // throws 절 제거
        // given
        long givenUser = 1L;
        long firstCharge = 100L;
        long secondCharge = 120L;

        // when
        pointService.charge(givenUser, firstCharge); // 이때 시간 0L
        fakeTimeProvider.advance(10); // 시간을 10ms 진행
        pointService.charge(givenUser, secondCharge); // 이때 시간 10L

        List<PointHistory> history = pointService.getUserPointHistories(givenUser);

        // then
        assertEquals(2, history.size(), "충전 이력은 2건이어야 한다.");

        // 최신순 정렬 검증 (두 번째 충전(10L)이 첫 번째(0L)보다 먼저 나와야 함)
        assertTrue(history.get(0).updateMillis() > history.get(1).updateMillis(),
                "첫 번째 이력이 두 번째보다 최신이어야 한다.");

        assertEquals(secondCharge, history.get(0).amount(), "가장 최근 충전 금액은 120이어야 한다.");
        assertEquals(10L, history.get(0).updateMillis(), "가장 최근 시간은 10L여야 한다.");
        assertEquals(firstCharge, history.get(1).amount(), "그 다음 충전 금액은 100이어야 한다.");
        assertEquals(0L, history.get(1).updateMillis(), "그 다음 시간은 0L여야 한다.");
    }

    /**
     * 다수의 유저가 충전시에는 구분되어서 히스토리가 기록되어야 합니다.
     */
    @Test
    void 히스토리는_사용자별로_격리되어야_한다() { // throws 절 제거
        // given
        long givenUser_1 = 1L;
        long givenUserPoint_1 = 100L;

        long givenUser_2 = 2L;
        long givenUserPoint_2 = 120L;

        // when
        pointService.charge(givenUser_1, givenUserPoint_1);
        pointService.charge(givenUser_2, givenUserPoint_2);

        // then
        List<PointHistory> h1 = pointService.getUserPointHistories(givenUser_1);
        List<PointHistory> h2 = pointService.getUserPointHistories(givenUser_2);

        assertEquals(1, h1.size());
        assertEquals(givenUser_1, h1.get(0).userId());
        assertEquals(givenUserPoint_1, h1.get(0).amount());

        assertEquals(1, h2.size());
        assertEquals(givenUser_2, h2.get(0).userId());
        assertEquals(givenUserPoint_2, h2.get(0).amount());
    }

    /**
     * 충전 히스토리의 충전, 차감의 합계는 유저의 잔액이랑 일치해야 합니다.
     */
    @Test
    void 이력의_합계와_현재_잔액이_일관적이어야_한다() { // throws 절 제거
        // given
        long givenUser = 1L;
        long firstCharge = 100L;
        long secondCharge = 120L;
        long useAmount = 50L;
        // when
        pointService.charge(givenUser, firstCharge);
        pointService.charge(givenUser, secondCharge);
        pointService.use(givenUser, useAmount);

        // then
        UserPoint up = pointService.getUserPoint(givenUser);
        List<PointHistory> history = pointService.getUserPointHistories(givenUser);

        long chargeSum = history.stream()
                .filter(h -> h.type() == TransactionType.CHARGE)
                .mapToLong(PointHistory::amount)
                .sum();
        long useSum = history.stream()
                .filter(h -> h.type() == TransactionType.USE)
                .mapToLong(PointHistory::amount)
                .sum();

        assertEquals(chargeSum - useSum, up.point());
    }
}
