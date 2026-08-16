package com.example.petnow.controller;

import com.example.petnow.exception.MvcExceptionHandler;
import com.example.petnow.service.PlaceAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlaceSlotControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PlaceAvailabilityService service = mock(PlaceAvailabilityService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PlaceSlotController(service))
                .setControllerAdvice(new MvcExceptionHandler())
                .build();
    }

    @Test
    void returnsBadRequestForInvalidYearMonth() throws Exception {
        mockMvc.perform(get("/places/1/package-days")
                        .param("yearMonth", "invalid"))
                .andExpect(status().isBadRequest());
    }
}
