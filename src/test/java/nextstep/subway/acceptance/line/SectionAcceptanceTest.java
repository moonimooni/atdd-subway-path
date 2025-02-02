package nextstep.subway.acceptance.line;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.acceptance.AcceptanceTest;
import nextstep.subway.helper.api.LineApi;
import nextstep.subway.helper.api.StationApi;
import nextstep.subway.helper.fixture.LineFixture;
import nextstep.subway.helper.fixture.SectionFixture;
import nextstep.subway.helper.fixture.StationFixture;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 노선 구간 관련 기능")
public class SectionAcceptanceTest extends AcceptanceTest {
    private static Long 강남역Id;
    private static Long 신논현역Id;
    private static Long 논현역Id;
    private static Long 신분당선Id;

    @BeforeEach
    void createFixture() {
        // stations
        강남역Id = StationApi.create(StationFixture.강남역).getLong("id");
        신논현역Id = StationApi.create(StationFixture.신논현역).getLong("id");
        논현역Id = StationApi.create(StationFixture.논현역).getLong("id");
        // line
        신분당선Id = LineApi.노선생성요청(LineFixture.신분당선_생성_바디(강남역Id, 신논현역Id)).getLong("id");
    }

    @Nested
    class SectionCreateTest {
        /**
         * Given 지하철 역 2개와 이 역들을 포함하는 노선을 1개 생성하고
         * When 앞서 생성된 노선의 하행역을 상행역으로 지정하지 않고 새 노선을 추가하려는 경우
         * Then 노선은 추가되지 않고 에러가 발생한다.
         */
        @DisplayName("등록하려는 상행역이 기존 노선의 하행역이 아닌 경우 에러가 발생한다.")
        @Test
        void createLineSectionFailForUpStationValidation() {
            // when
            ExtractableResponse<Response> response = LineApi.구간생성요청(
                    신분당선Id, SectionFixture.추가구간_생성_바디(강남역Id, 논현역Id)
            );

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
            assertThat(LineApi.구간조회요청(신분당선Id).getList("stations.id", Long.class))
                    .containsExactly(강남역Id, 신논현역Id);
        }

        /**
         * Given 지하철 역 2개와 이 역들을 포함하는 노선을 1개 생성하고
         * When 앞서 생성된 지하철 역 중 하행역을 상행선으로 두고 새 노선을 추가하면
         * Then 노선 조회 시 세 개의 역이 조회된다.
         */
        @DisplayName("지하철 노선 구간을 추가한다.")
        @Test
        void createLineSection() {
            // when
            ExtractableResponse<Response> response = LineApi.구간생성요청(
                    신분당선Id, SectionFixture.추가구간_생성_바디(신논현역Id, 논현역Id)
            );

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
            assertThat(LineApi.구간조회요청(신분당선Id).getList("stations.id", Long.class))
                    .containsExactly(강남역Id, 신논현역Id, 논현역Id);
        }

        /**
         * Given 노선을 생성하고,
         * When 기존 상행역을 상행역으로, 새로운 역을 하행역으로 두는 구간을 추가하면
         * Then 노선 역들 조회시 기존 상행역, 추가된 역, 하행역 순으로 조회된다.
         */
        @DisplayName("노선에 역 추가시 노선 가운데 추가 할 수 있다.(하행역이 새로운 역인 경우)")
        @Test
        void addSectionInMiddleWithNewDownStation() {
            // given
            신분당선Id = LineApi.노선생성요청(LineFixture.신분당선_생성_바디(강남역Id, 논현역Id)).getLong("id");

            // when
            ExtractableResponse<Response> response = LineApi.구간생성요청(
                    신분당선Id, SectionFixture.추가구간_생성_바디(강남역Id, 신논현역Id, 5)
            );

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
            assertThat(LineApi.구간조회요청(신분당선Id).getList("stations.id", Long.class))
                    .containsExactly(강남역Id, 신논현역Id, 논현역Id);
        }

        /**
         * Given 노선을 생성하고,
         * When 새로운 역을 상행역으로, 기존 하행역을 하행역으로 두는 구간을 추가하면
         * Then 노선 역들 조회시 기존 상행역, 추가된 역, 하행역 순으로 조회된다.
         */
        @DisplayName("노선에 역 추가시 노선 가운데 추가 할 수 있다.(상행역이 새로운 역인 경우)")
        @Test
        void addSectionInMiddleWithNewUpStation() {
            // given
            신분당선Id = LineApi.노선생성요청(LineFixture.신분당선_생성_바디(강남역Id, 논현역Id)).getLong("id");

            // when
            ExtractableResponse<Response> response = LineApi.구간생성요청(
                    신분당선Id, SectionFixture.추가구간_생성_바디(신논현역Id, 논현역Id, 5)
            );

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
            assertThat(LineApi.구간조회요청(신분당선Id).getList("stations.id", Long.class))
                    .containsExactly(강남역Id, 신논현역Id, 논현역Id);
        }

        /**
         * Given 노선을 생성하고,
         * When 새로운 역을 상행역으로, 기존 상행역을 하행역으로 두는 구간을 추가하면
         * Then 노선 역들 조회시 추가된 역, 기존 상행역, 기존 하행역 순으로 조회된다.
         */
        @DisplayName("노선에 역 추가시 노선 처음에 추가 할 수 있다.")
        @Test
        void addSectionAtFirst() {
            // given
            신분당선Id = LineApi.노선생성요청(LineFixture.신분당선_생성_바디(신논현역Id, 논현역Id)).getLong("id");

            // when
            ExtractableResponse<Response> response = LineApi.구간생성요청(
                    신분당선Id, SectionFixture.추가구간_생성_바디(강남역Id, 신논현역Id, 5)
            );

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
            assertThat(LineApi.구간조회요청(신분당선Id).getList("stations.id", Long.class))
                    .containsExactly(강남역Id, 신논현역Id, 논현역Id);
        }

        /**
         * Given 노선을 생성하고
         * When 그 노선에 있는 구간과 동일한 상행역, 하행역을 두는 구간을 추가하면
         * Then 에러가 발생한다.
         */
        @DisplayName("이미 등록되어있는 역은 노선에 등록될 수 없다.")
        @Test
        void failForDuplicateStation() {
            // given
            신분당선Id = LineApi.노선생성요청(LineFixture.신분당선_생성_바디(신논현역Id, 논현역Id)).getLong("id");

            // when
            ExtractableResponse<Response> response = LineApi.구간생성요청(
                    신분당선Id, SectionFixture.추가구간_생성_바디(신논현역Id, 신논현역Id, 5)
            );

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
            assertThat(LineApi.구간조회요청(신분당선Id).getList("stations.id", Long.class))
                    .containsExactly(신논현역Id, 논현역Id);
        }
    }

    @Nested
    class SectionDeleteTest {
        /**
         * Given 지하철 역 3개로 2개의 구간을 가진 1개의 노선을 생성 후
         * When 하행역을 제거하면
         * Then 노선 역 리스트 조회 시 상행 종점역, 중간역만 조회된다.
         */
        @DisplayName("하행역을 제거함으로써 구간을 제거한다.")
        @Test
        void succeedForDeletingLastStation() {
            // given
            LineApi.구간생성요청(신분당선Id, SectionFixture.추가구간_생성_바디(신논현역Id, 논현역Id));

            // when
            ExtractableResponse<Response> response = LineApi.구간삭제요청(신분당선Id, 논현역Id);

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
            assertThat(LineApi.구간조회요청(신분당선Id).getList("stations.id", Long.class))
                    .containsExactly(강남역Id, 신논현역Id);
        }

        /**
         * Given 지하철 역 3개로 2개의 구간을 가진 1개의 노선을 생성 후
         * When 상행역을 제거하면,
         * Then 노선 역 리스트 조회 시 중간역, 하행 종점역만 조회된다.
         */
        @DisplayName("상행역을 제거함으로써 구간을 제거한다.")
        @Test
        void succeedForDeletingFirstStation() {
            // given
            LineApi.구간생성요청(신분당선Id, SectionFixture.추가구간_생성_바디(신논현역Id, 논현역Id));

            // when
            ExtractableResponse<Response> response = LineApi.구간삭제요청(신분당선Id, 강남역Id);

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
            assertThat(LineApi.구간조회요청(신분당선Id).getList("stations.id", Long.class))
                    .containsExactly(신논현역Id, 논현역Id);
        }

        /**
         * Given 지하철 역 3개로 2개의 구간을 가진 1개의 노선을 생성 후
         * When 중간역을 제거하면,
         * Then 상행 종점역, 하행 종점역만 조회된다.
         */
        @DisplayName("노선 중간의 역을 제거함으로써 구간을 제거한다.")
        @Test
        void succeedForDeletingMiddleStation() {
            // given
            LineApi.구간생성요청(신분당선Id, SectionFixture.추가구간_생성_바디(신논현역Id, 논현역Id));

            // when
            ExtractableResponse<Response> response = LineApi.구간삭제요청(신분당선Id, 신논현역Id);

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
            assertThat(LineApi.구간조회요청(신분당선Id).getList("stations.id", Long.class))
                    .containsExactly(강남역Id, 논현역Id);
        }

        /**
         * Given 지하철 역 2개만 포함하는 노선을 1개 생성하고
         * When 앞서 생성한 노선에서 역을 하나 제거하려는 경우
         * Then 노선을 제거되지 않고 에러가 발생한다.
         */
        @DisplayName("구간이 1개인 경우 역을 제거하려 시도하면 에러가 발생한다.")
        @Test
        void failForLineWithOneSection() {
            // when
            ExtractableResponse<Response> response = LineApi.구간삭제요청(신분당선Id, 신논현역Id);

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
            assertThat(LineApi.구간조회요청(신분당선Id).getList("stations.id", Long.class))
                    .containsExactly(강남역Id, 신논현역Id);
        }
    }
}
