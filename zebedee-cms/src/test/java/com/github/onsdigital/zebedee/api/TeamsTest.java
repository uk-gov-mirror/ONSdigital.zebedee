package com.github.onsdigital.zebedee.api;

import com.github.onsdigital.zebedee.exceptions.BadRequestException;
import com.github.onsdigital.zebedee.exceptions.NotFoundException;
import com.github.onsdigital.zebedee.exceptions.UnauthorizedException;
import com.github.onsdigital.zebedee.session.service.Sessions;
import com.github.onsdigital.zebedee.teams.model.Team;
import com.github.onsdigital.zebedee.teams.service.TeamsService;
import com.github.onsdigital.zebedee.user.service.UsersService;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TeamsTest extends ZebedeeAPIBaseTestCase {

    static final String TEAM_NAME = "TheATeam";

    @Mock
    private UsersService usersService;

    @Mock
    private Sessions sessionsService;

    @Mock
    private TeamsService teamsService;

    @Mock
    private Team team;

    private Teams endpoint;

    @Override
    protected void customSetUp() throws Exception {
        when(mockRequest.getPathInfo())
                .thenReturn("/teams/TheATeam");

        when(sessionsService.get(mockRequest))
                .thenReturn(session);

        when(teamsService.findTeam(TEAM_NAME))
                .thenReturn(team);

        when(mockRequest.getParameter("email"))
                .thenReturn(TEST_EMAIL);

        endpoint = new Teams(usersService, sessionsService, teamsService);
    }

    @Override
    protected Object getAPIName() {
        return "teams";
    }

    @Test
    public void removeTeamMember_teamNameEmpty_shouldReturnFalse() throws Exception {
        when(mockRequest.getPathInfo())
                .thenReturn("/teams/");

        boolean result = endpoint.removeTeamMember(mockRequest, mockResponse);

        assertFalse(result);
        verifyZeroInteractions(sessionsService, teamsService, usersService);
    }

    @Test
    public void removeTeamMember_sessionNotFound_shouldThrowException() throws Exception {
        when(sessionsService.get(mockRequest))
                .thenReturn(null);

        UnauthorizedException actual = assertThrows(UnauthorizedException.class,
                () -> endpoint.removeTeamMember(mockRequest, mockResponse));

        assertThat(actual.getMessage(), equalTo("unauthorised access session required but was null"));
        verify(sessionsService, times(1)).get(mockRequest);
        verifyZeroInteractions(teamsService, usersService);
    }

    @Test
    public void removeTeamMember_teamsServiceError_shouldThrowException() throws Exception {
        when(teamsService.findTeam(TEAM_NAME))
                .thenThrow(IOException.class);

        assertThrows(IOException.class, () -> endpoint.removeTeamMember(mockRequest, mockResponse));

        verify(sessionsService, times(1)).get(mockRequest);
        verify(teamsService, times(1)).findTeam(TEAM_NAME);
        verifyZeroInteractions(usersService);
    }

    @Test
    public void removeTeamMember_teamsServiceReturnsNull_shouldThrowException() throws Exception {
        when(teamsService.findTeam(TEAM_NAME))
                .thenReturn(null);

        NotFoundException actual = assertThrows(NotFoundException.class,
                () -> endpoint.removeTeamMember(mockRequest, mockResponse));

        assertThat(actual.getMessage(), equalTo("requested team could not be found"));
        verify(sessionsService, times(1)).get(mockRequest);
        verify(teamsService, times(1)).findTeam(TEAM_NAME);
        verifyZeroInteractions(usersService);
    }

    @Test
    public void removeTeamMember_userEmailNull_shouldThrowException() throws Exception {
        when(mockRequest.getParameter("email"))
                .thenReturn(null);

        BadRequestException actual = assertThrows(BadRequestException.class,
                () -> endpoint.removeTeamMember(mockRequest, mockResponse));

        assertThat(actual.getMessage(), equalTo("email parameter required but none provided"));
        verify(sessionsService, times(1)).get(mockRequest);
        verify(teamsService, times(1)).findTeam(TEAM_NAME);
        verifyZeroInteractions(usersService);
    }

    @Test
    public void removeTeamMember_userEmailEmpty_shouldThrowException() throws Exception {
        when(mockRequest.getParameter("email"))
                .thenReturn("");

        BadRequestException actual = assertThrows(BadRequestException.class,
                () -> endpoint.removeTeamMember(mockRequest, mockResponse));

        assertThat(actual.getMessage(), equalTo("email parameter required but none provided"));
        verify(sessionsService, times(1)).get(mockRequest);
        verify(teamsService, times(1)).findTeam(TEAM_NAME);
        verifyZeroInteractions(usersService);
    }

    @Test
    public void removeTeamMember_removeMemberError_shouldThrowException() throws Exception {
        doThrow(IOException.class)
                .when(teamsService)
                .removeTeamMember(TEST_EMAIL, team, session);

        assertThrows(IOException.class, () -> endpoint.removeTeamMember(mockRequest, mockResponse));

        verify(sessionsService, times(1)).get(mockRequest);
        verify(teamsService, times(1)).findTeam(TEAM_NAME);
        verify(teamsService, times(1)).removeTeamMember(TEST_EMAIL, team, session);
        verifyZeroInteractions(usersService);
    }
}
