package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;

    /// 들어오는 UserId에 대한 Table 조회
    /// 신규 유저는 Default 반환
    public UserPoint getUserPoint(long userId)
    {
        return userPointTable.selectById(userId);
    }

    public UserPoint charge(long userId, long amount)
    {
        // 기존에 유저가 있는 지 확인
        UserPoint oldUserPoint = userPointTable.selectById(userId);
        // 기존 유저의 Point에 새로 충전할 양을 추가(신규 유저는 Default, 즉 0 Point 반환)
        long newAmount = oldUserPoint.point() + amount;
        return userPointTable.insertOrUpdate(userId, newAmount);
    }

    public UserPoint use(long userId, long amount)
    {
        // 기존에 유저가 있는 지 확인
        UserPoint oldUserPoint = userPointTable.selectById(userId);
        if(oldUserPoint.point() < amount)
            throw  new RuntimeException("잔고 부족");
        // 기존 유저의 Point에 새로 차감할 양을 빼기(신규 유저는 Default, 즉 0 Point 반환)
        long newAmount = oldUserPoint.point() - amount;
        return userPointTable.insertOrUpdate(userId, newAmount);
    }
}
