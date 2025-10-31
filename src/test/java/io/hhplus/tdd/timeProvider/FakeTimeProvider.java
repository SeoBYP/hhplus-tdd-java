package io.hhplus.tdd.timeProvider;


/// 현재 가짜 시간을 저장하고 관리하는 TimeProvider
/// 테스트 시 Thread.Sleep을 대신 하여 따로 updateMillis를 저장하기 위한 테스트 코드
public class FakeTimeProvider implements TimeProvider
{
    private long current = 0;

    @Override
    public long currentTimeMillis() {
        return current;
    }

    public void advance(long millis){
        current += millis;
    }
}
