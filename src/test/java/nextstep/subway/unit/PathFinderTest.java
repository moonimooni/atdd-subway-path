package nextstep.subway.unit;

import nextstep.subway.domain.Line;
import nextstep.subway.domain.Section;
import nextstep.subway.domain.Station;
import nextstep.subway.domain.path.Path;
import nextstep.subway.domain.path.PathFinder;
import nextstep.subway.domain.path.PathFinderDijkstraStrategy;
import nextstep.subway.exception.path.PathException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static nextstep.subway.helper.fixture.LineFixture.*;
import static nextstep.subway.helper.fixture.SectionFixture.추가구간_엔티티;
import static nextstep.subway.helper.fixture.StationFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PathFinderTest {

    ArrayList<Line> 모든_노선 = new ArrayList<>();
    Station 출발역 = 논현역_엔티티;
    Station 도착역 = 역삼역_엔티티;

    PathFinder pathFinder = new PathFinder(new PathFinderDijkstraStrategy());

    /**
     *                        (distance 10)
     *                신논현역 --- *신분당선* --- 논현역
     *                  |
     *                  |
     * (distance 10) *신분당선*
     *                  |
     *                  |
     *                강남역 ---- *9호선* ------ 역삼역
     *                        (distance 7)
     */
    @BeforeEach
    void setup() {
        Line 이호선 = 이호선_엔티티(강남역_엔티티, 역삼역_엔티티); // distance 7
        Line 신분당선 = 신분당선_엔티티(논현역_엔티티, 신논현역_엔티티); // distance 10
        Section 신분당선_추가구간 = 추가구간_엔티티(신논현역_엔티티, 강남역_엔티티); // distance 10
        신분당선.addSection(신분당선_추가구간);

        모든_노선.add(이호선);
        모든_노선.add(신분당선);
    }

    /**
     * Given 여러 노선으로 연결된 지하철 시스템을 생성 후 (BeforeEach 참고)
     * When 모든 노선들과, 노선에 있는 역들 중 두 곳을 출발역, 도착역으로 하여 경로를 조회하면
     * Then 경로에 있는 역들과 경로 거리를 구할 수 있다.
     */
    @Test
    @DisplayName("최단경로를 찾아 그 사이 stations와 총 가중치(거리)를 반환한다.")
    void succeed() {
        // when
        Path path = pathFinder.findShortestPathAndItsDistance(모든_노선, 출발역, 도착역);

        // then
        List<String> 실제_경로_역들 = path.getStations().stream().map(Station::getName).collect(Collectors.toList());
        assertThat(실제_경로_역들).containsExactly("논현역", "신논현역", "강남역", "역삼역");
        assertThat(path.getDistance()).isEqualTo(27);
    }

    /**
     * Given 여러 노선으로 연결된 지하철 시스템을 생성 후
     * When 연결점이 없는 노선끼리 경로 조회 시도 시
     * Then 에러가 발생한다.
     */
    @Test
    @DisplayName("경로를 찾지 못했을 때 에러가 발생한다.")
    void failForPathNotExist() {
        // given
        Line 삼호선 = 삼호선_엔티티(고속터미널역_엔티티, 신사역_엔티티);
        모든_노선.add(삼호선);
        // when, then
        assertThrows(
                PathException.class,
                () -> pathFinder.findShortestPathAndItsDistance(모든_노선, 고속터미널역_엔티티, 도착역),
                "should throw"
        );
    }

    /**
     * Given 노선이 없는 경우
     * When 경로 조회 시도 시
     * Then 에러가 발생한다.
     */
    @Test
    @DisplayName("노선이 존재하지 않을 시 에러가 발생한다.")
    void failForNotFoundStation() {
        // given
        List<Line> 비어있는_노선 = new ArrayList<Line>();

        // when, then
        assertThrows(
                PathException.class,
                () -> pathFinder.findShortestPathAndItsDistance(비어있는_노선, 출발역, 도착역),
                "should throw"
        );
    }

    /**
     * Given 여러 노선으로 연결된 지하철 시스템을 생성 후
     * When 출발역과 도착역을 동일하게 두면
     * Then 에러가 발생한다.
     */
    @Test
    @DisplayName("출발역과 도착역이 같은 값으로 주어지는 경우 에러가 발생한다.")
    void failForSameSourceAndTargetStation() {
        assertThrows(
                PathException.class,
                () -> pathFinder.findShortestPathAndItsDistance(모든_노선, 역삼역_엔티티, 역삼역_엔티티),
                "should throw"
        );
    }
}
