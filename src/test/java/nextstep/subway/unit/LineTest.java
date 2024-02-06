package nextstep.subway.unit;

import nextstep.subway.domain.Line;
import nextstep.subway.domain.Section;
import nextstep.subway.domain.Station;
import nextstep.subway.exception.LineSectionException;
import nextstep.subway.helper.fixture.StationFixture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static nextstep.subway.helper.fixture.LineFixture.신분당선_엔티티;
import static nextstep.subway.helper.fixture.SectionFixture.추가구간_엔티티;
import static nextstep.subway.helper.fixture.StationFixture.강남역_엔티티;
import static nextstep.subway.helper.fixture.StationFixture.역삼역_엔티티;
import static org.junit.jupiter.api.Assertions.*;

class LineTest {
    @Nested
    class SectionAddTest {
        /**
         * Given 노선을 생성한 뒤
         * When 위 노선에 추가하려는 구간의 상행역, 하행역이 일치할 때
         * Then 실패한다
         */
        @Test
        @DisplayName("추가하려는 구간의 상행역과 하행역이 일치하면 실패한다")
        void failForStationsValidation() throws LineSectionException {
            // Given
            Line 신분당선 = 신분당선_엔티티(강남역_엔티티, 역삼역_엔티티);
            // When
            Section 추가구간 = 추가구간_엔티티(역삼역_엔티티, 역삼역_엔티티);
            // Then
            Exception thrown = assertThrows(
                    LineSectionException.class,
                    () -> 신분당선.addSection(추가구간),
                    "should throw"
            );
            assertNotNull(thrown);
        }

        /**
         * Given 노선을 생성한 뒤
         * When 위 노선을 생성할 때 넣어둔 역 중 하나를 하행역으로 두는 구간을 추가하려는 경우
         * Then 실패한다
         */
        @Test
        @DisplayName("추가하려는 구간의 하행역이 이미 노선에 있는 역이면 실패한다")
        void failForDownStationValidation() throws LineSectionException {
            // Given
            Line 신분당선 = 신분당선_엔티티(강남역_엔티티, 역삼역_엔티티);
            // When
            Section 추가구간 = 추가구간_엔티티(역삼역_엔티티, 강남역_엔티티);
            // Then
            Exception thrown = assertThrows(
                    LineSectionException.class,
                    () -> 신분당선.addSection(추가구간),
                    "should throw"
            );
            assertNotNull(thrown);
        }

        /**
         * Given 노선을 생성한 뒤
         * When 위 노선을 생성할 때 넣어둔 하행역을 상행역으로 두지 않는 구간을 추가하는 경우
         * Then 실패한다
         */
        @Test
        @DisplayName("추가하려는 구간의 상행역이 기존 노선의 하행역이 아니면 실패한다")
        void failForUpStationValidation() throws LineSectionException {
            // Given
            Line 신분당선 = 신분당선_엔티티(강남역_엔티티, 역삼역_엔티티);
            // When
            Section 추가구간 = 추가구간_엔티티(강남역_엔티티, StationFixture.선릉역_엔티티);
            // Then
            Exception thrown = assertThrows(
                    LineSectionException.class,
                    () -> 신분당선.addSection(추가구간),
                    "should throw"
            );
            assertNotNull(thrown);
        }

        /**
         * Given 노선을 생성한 뒤
         * When 상행역이 기존 노선의 하행역이고, 하행역이 새로운 역인 새 구간을 위 노선에 추가하면
         * Then 노선의 구간들을 조회 시 구간이 두 개 조회된다.
         */
        @Test
        @DisplayName("추가하려는 구간의 상행역, 하행역이 검증을 통과하면 성공한다")
        void succeed() {
            // Given
            Line 신분당선 = 신분당선_엔티티(강남역_엔티티, 역삼역_엔티티);
            // When
            Section 추가구간 = 추가구간_엔티티(역삼역_엔티티, StationFixture.선릉역_엔티티);
            신분당선.addSection(추가구간);
            // Then
            assertEquals(신분당선.getSections().size(), 2);
        }
    }

    @Nested
    class StationsGetTest {
        /**
         * When 노선을 생성한 뒤
         * Then 노선에 포함된 모든 역을 조회하면 두 개의 역이 조회된다.
         */
        @Test
        @DisplayName("노선을 생성한 직후엔 두 개의 역을 조회할 수 있다")
        void succeedToGetDefaultStations() {
            // When
            Line 신분당선 = 신분당선_엔티티(강남역_엔티티, 역삼역_엔티티);
            // Then
            List<String> stationNames = 신분당선.getAllStations()
                    .stream().map(Station::getName)
                    .collect(Collectors.toList());
            assertEquals(stationNames, List.of("강남역", "역삼역"));
        }

        /**
         * Given 노선을 생성한 뒤
         * When 노선에 구간을 하나 더 올바르게 추가하면
         * Then 세 개의 역이 조회된다.
         */
        @Test
        @DisplayName("구간을 두 개 가지고 있으면 역은 세 개가 조회된다")
        void succeedToGetAllDistinctStations() {
            // Given
            Line 신분당선 = 신분당선_엔티티(강남역_엔티티, 역삼역_엔티티);
            // When
            신분당선.addSection(추가구간_엔티티(역삼역_엔티티, StationFixture.선릉역_엔티티));
            // Then
            List<String> stationNames = 신분당선.getAllStations()
                    .stream().map(Station::getName)
                    .collect(Collectors.toList());
            assertEquals(stationNames, List.of("강남역", "역삼역", "선릉역"));
        }

        /**
         * Given 노선을 생성한 뒤
         * When 노선에 구간을 하나 더 올바르게 추가했을 때
         * Then 하행역은 마지막에 추가한 구간의 하행역이 조회된다.
         */
        @Test
        @DisplayName("하행역을 조회한다")
        void succeedToGetTheMostDownStation() {
            // Given
            Line 신분당선 = 신분당선_엔티티(강남역_엔티티, 역삼역_엔티티);
            // When
            신분당선.addSection(추가구간_엔티티(역삼역_엔티티, StationFixture.선릉역_엔티티));
            // Then
            Station station = 신분당선.getTheMostDownStation();
            assertEquals(station.getName(), "선릉역");
        }
    }

    @Nested
    class SectionRemoveTest {
        /**
         * Given
         * When 노선을 생성한 직후에
         * Then 구간을 삭제하려 하면 실패한다
         */
        @Test
        @DisplayName("노선에 구간이 하나만 존재하면 실패한다")
        void failForUniqueSection() throws LineSectionException {
            // When
            Line 신분당선 = 신분당선_엔티티(강남역_엔티티, 역삼역_엔티티);
            // Then
            Exception thrown = assertThrows(
                    LineSectionException.class,
                    () -> 신분당선.deleteStation(역삼역_엔티티.getId()),
                    "should throw"
            );
            assertNotNull(thrown);
        }

        /**
         * Given 노선을 생성하고, 구간을 하나 더 추가한 뒤
         * When 하행역이 아닌 역을 삭제하려는 경우
         * Then 실패한다
         */
        @Test
        @DisplayName("하행역이 아닌 역을 제거하려 하면 실패한다")
        void failForAttemptingToDeleteUpStations() throws LineSectionException {
            // Given
            Line 신분당선 = 신분당선_엔티티(강남역_엔티티, 역삼역_엔티티);
            // When
            Section 추가구간 = 추가구간_엔티티(역삼역_엔티티, StationFixture.선릉역_엔티티);
            신분당선.addSection(추가구간);
            // Then
            Exception thrown = assertThrows(
                    LineSectionException.class,
                    () -> 신분당선.deleteStation(역삼역_엔티티.getId()),
                    "should throw"
            );
            assertNotNull(thrown);
        }

        /**
         * Given 노선을 생성하고, 구간을 하나 더 추가한 뒤
         * When 하행역을 삭제하면
         * Then 구간이 하나 제거되고, 삭제한 하행역은 역 목록에 조회되지 않는다.
         */
        @Test
        @DisplayName("하행역이 아닌 역을 제거하려 하면 실패한다")
        void succeed() throws LineSectionException {
            // Given
            Line 신분당선 = 신분당선_엔티티(강남역_엔티티, 역삼역_엔티티);
            신분당선.addSection(추가구간_엔티티(역삼역_엔티티, StationFixture.선릉역_엔티티));
            // When
            신분당선.deleteStation(StationFixture.선릉역_엔티티.getId());
            // Then
            assertEquals(신분당선.getSections().size(), 1);
            assertFalse(신분당선.getAllStations().contains(StationFixture.선릉역_엔티티));
        }
    }
}
