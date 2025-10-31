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
        return userPointTable.insertOrUpdate(userId, amount);
    }
}
