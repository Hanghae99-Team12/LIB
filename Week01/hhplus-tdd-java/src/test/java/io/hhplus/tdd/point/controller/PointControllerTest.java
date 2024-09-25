package io.hhplus.tdd.point.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.dto.PointRequest;
import io.hhplus.tdd.point.dto.UserPoint;
import io.hhplus.tdd.point.enums.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserPointTable userPointTable;

    @MockBean
    private PointHistoryTable pointHistoryTable;

    private final long USER_ID = 1L;
    private final long AMOUNT = 1000L;

    @Test
    @DisplayName("유저 포인트 조회")
    public void selectPointById() throws Exception {
        when(userPointTable.selectById(USER_ID)).thenReturn(new UserPoint(USER_ID, AMOUNT, System.currentTimeMillis()));

        mockMvc.perform(get("/point/" + USER_ID).contentType(APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.point").value(AMOUNT));
    }

    @Test
    @DisplayName("포인트 이용 내역 조회")
    public void selectPointHistoryById() throws Exception {
        when(pointHistoryTable.selectAllByUserId(USER_ID)).thenReturn(
                List.of(
                        new PointHistory(1, USER_ID, AMOUNT, TransactionType.USE, System.currentTimeMillis()),
                        new PointHistory(2, USER_ID, AMOUNT, TransactionType.CHARGE, System.currentTimeMillis()),
                        new PointHistory(3, USER_ID, AMOUNT, TransactionType.USE, System.currentTimeMillis())
                )
        );

        mockMvc.perform(get("/point/" + USER_ID + "/histories").contentType(APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("포인트 충전")
    public void chargePoint() throws Exception {
        long chargePoint = 500L;
        long afterAmount = AMOUNT + chargePoint;
        PointRequest req = new PointRequest(chargePoint);

        when(userPointTable.selectById(USER_ID)).thenReturn(new UserPoint(USER_ID, AMOUNT, System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(USER_ID, afterAmount)).thenReturn(new UserPoint(USER_ID, afterAmount, System.currentTimeMillis()));

        mockMvc.perform(patch("/point/" + USER_ID + "/charge").contentType(APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(req)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.point").value(afterAmount));

    }

    @Test
    @DisplayName("포인트 차감")
    public void usePoint() throws Exception {
        long usePoint = 500L;
        long afterAmount = AMOUNT - usePoint;
        PointRequest req = new PointRequest(usePoint);

        when(userPointTable.selectById(USER_ID)).thenReturn(new UserPoint(USER_ID, AMOUNT, System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(USER_ID, afterAmount)).thenReturn(new UserPoint(USER_ID, afterAmount, System.currentTimeMillis()));

        mockMvc.perform(patch("/point/" + USER_ID + "/use").contentType(APPLICATION_JSON)
                                                           .content(objectMapper.writeValueAsString(req)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.point").value(afterAmount));
    }
}