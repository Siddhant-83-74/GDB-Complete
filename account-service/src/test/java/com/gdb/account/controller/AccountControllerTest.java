package com.gdb.account.controller;

import com.gdb.account.security.UserContext;
import com.gdb.account.security.UserContextHolder;
import com.gdb.account.service.AccountService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// MOD9-CR-01: MockMvc integration tests for AccountController.
public class AccountControllerTest {

    private final AccountService accountService = Mockito.mock(AccountService.class);
    private final MockMvc mockMvc =
            MockMvcBuilders.standaloneSetup(new AccountController(accountService)).build();

    @AfterEach
    void clearContext() {
        UserContextHolder.clearContext();
    }

    @Test
    void getAccountByNumber_authorizedStaff_returnsOk() throws Exception {
        UserContextHolder.setContext(
                UserContext.builder().userId(1L).loginId("admin").role("ADMIN").build());
        Mockito.when(accountService.getAccountByNumber(1001L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/accounts/1001"))
                .andExpect(status().isOk());
    }

    @Test
    void createSavings_invalidPayload_returnsClientError() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/savings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is4xxClientError());
    }
}
