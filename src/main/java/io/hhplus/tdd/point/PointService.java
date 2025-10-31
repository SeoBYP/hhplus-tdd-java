package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.timeProvider.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final TimeProvider timeProvider;

    /// 들어오는 UserId에 대한 Table 조회
    /// 신규 유저는 Default 반환
    public UserPoint getUserPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    public UserPoint charge(long userId, long amount) {
        if (amount < 0)
            throw new RuntimeException("음수로 충전은 불가능합니다.");
        // 기존에 유저가 있는 지 확인
        UserPoint oldUserPoint = userPointTable.selectById(userId);
        // 기존 유저의 Point에 새로 충전할 양을 추가(신규 유저는 Default, 즉 0 Point 반환)
        long newAmount = oldUserPoint.point() + amount;
        UserPoint newUserPoint = userPointTable.insertOrUpdate(userId, newAmount);
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, timeProvider.currentTimeMillis());
        return newUserPoint;
    }

    public UserPoint use(long userId, long amount) {
        if (amount < 0)
            throw new RuntimeException("음수로 사용은 불가능합니다.");
        // 기존에 유저가 있는 지 확인
        UserPoint oldUserPoint = userPointTable.selectById(userId);
        if (oldUserPoint.point() < amount)
            throw new RuntimeException("잔고 부족");
        // 기존 유저의 Point에 새로 차감할 양을 빼기(신규 유저는 Default, 즉 0 Point 반환)
        long newAmount = oldUserPoint.point() - amount;

        UserPoint newUserPoint = userPointTable.insertOrUpdate(userId, newAmount);
        pointHistoryTable.insert(userId, amount, TransactionType.USE, timeProvider.currentTimeMillis());
        return newUserPoint;
    }

    public List<PointHistory> getUserPointHistories(long userId) {
        return pointHistoryTable.selectAllByUserId(userId)
                .stream()
                .sorted(Comparator.comparingLong(PointHistory::updateMillis).reversed())
                .toList();
    }
}
