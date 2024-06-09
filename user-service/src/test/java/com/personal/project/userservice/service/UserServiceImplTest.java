package com.personal.project.userservice.service;

import com.personal.project.userservice.exceptionhandler.exception.SqlModifyFailedException;
import com.personal.project.userservice.exceptionhandler.exception.UserNotFoundException;
import com.personal.project.userservice.exceptionhandler.exception.WrongArgsException;
import com.personal.project.userservice.model.dto.common.*;
import com.personal.project.userservice.model.entity.User;
import com.personal.project.userservice.model.entity.UserDailyCommentDO;
import com.personal.project.userservice.model.entity.UserRegularCommentDO;
import com.personal.project.userservice.repository.dao.UserDao;
import com.personal.project.userservice.service.impl.UserServiceImpl;
import com.personal.project.userservice.util.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserDao userDao;

//    @Mock
//    RoleService roleService;

    @Mock
    UserDailyCommentService userDailyCommentService;

    @Mock
    UserRegularCommentService userRegularCommentService;

    @Captor
    private ArgumentCaptor<UserRegularCommentDO> regularCommentCaptor;

    @InjectMocks
    UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        System.out.println("running test case");
    }

    @AfterEach
    void tearDown() {
        System.out.println("end test case");
    }

    @Test
    void findOne() {
        //given
        String username = "testName";
        User expected = new User();
        expected.setUsername(username);
        when(userDao.findOne(username)).thenReturn(Optional.of(expected));

        //when
        UserResponseDTO result = userService.findOne(username);

        //then
        assertThat(result.username(), is(username));
    }

    @Test
    void findOneWhenNotFound() {
        //given
        String username = "testName";
        Optional<User> expected = Optional.empty();
        when(userDao.findOne(username)).thenReturn(expected);

        //when and then
        assertThrows(UserNotFoundException.class, () -> userService.findOne(username));
    }

    @Test
    void upsertDailyCommentSave() {
        //given
        UserDailyCommentRequestDTO dto = new UserDailyCommentRequestDTO();
        dto.setId(1L);
        dto.setUserId(1L);
        dto.setComment("test comment");
        when(userDailyCommentService.findOne(anyLong(), notNull(java.time.LocalDate.class))).thenReturn(Optional.of(new UserDailyCommentDO()));
        when(userDailyCommentService.insert(ArgumentMatchers.any(UserDailyCommentDO.class))).thenReturn(new UserDailyCommentDO());
        UserDailyCommentDTO expect = new UserDailyCommentDTO(dto.getId(), dto.getUserId(), dto.getComment());

        //when
        UserDailyCommentDTO result = userService.upsertDailyComment(dto);

        //then
        assertThat(expect.id(), is(result.id()));
        assertThat(expect.userId(), is(result.userId()));
        assertThat(expect.comment(), is(result.comment()));
    }

    @Test
    void upsertDailyCommentInsert() {
        //given
        UserDailyCommentRequestDTO dto = new UserDailyCommentRequestDTO();
        dto.setId(1L);
        dto.setUserId(1L);
        dto.setComment("test comment");
        when(userDailyCommentService.findOne(anyLong(), notNull(java.time.LocalDate.class))).thenReturn(Optional.empty());
        when(userDailyCommentService.insert(anyLong(), anyString(), ArgumentMatchers.any(LocalDate.class))).thenReturn(true);
        UserDailyCommentDTO expect = new UserDailyCommentDTO(dto.getId(), dto.getUserId(), dto.getComment());

        //when
        UserDailyCommentDTO result = userService.upsertDailyComment(dto);

        //then
        assertThat(expect.id(), is(result.id()));
        assertThat(expect.userId(), is(result.userId()));
        assertThat(expect.comment(), is(result.comment()));
    }

    @Test
    void upsertDailyCommentFailed() {
        //given
        UserDailyCommentRequestDTO dto = new UserDailyCommentRequestDTO();
        dto.setId(1L);
        dto.setUserId(1L);
        dto.setComment("test comment");
        when(userDailyCommentService.findOne(anyLong(), notNull(java.time.LocalDate.class))).thenReturn(Optional.empty());
        when(userDailyCommentService.insert(anyLong(), anyString(), ArgumentMatchers.any(LocalDate.class))).thenReturn(false);

        //when and then
        assertThrows(SqlModifyFailedException.class, () -> userService.upsertDailyComment(dto));
    }

    @Test
    void updatePassword() {
        //given
        UserChangePasswordRequestDTO dto = new UserChangePasswordRequestDTO();
        dto.setUserId(1L);
        dto.setFormerPassword("former");
        dto.setNewPassword("newPass");

        User user = new User();
        user.setPassword(PasswordUtil.generateUserPassword("former", "test"));
        user.setSalt("test");
        when(userDao.findOne(anyLong())).thenReturn(Optional.of(user));


        //when
        userService.updatePassword(dto);

        //then
        assertThat(1, is(1));
    }

    @Test
    void updatePasswordThrowUserNotFound() {
        //given
        UserChangePasswordRequestDTO dto = new UserChangePasswordRequestDTO();
        dto.setUserId(1L);
        dto.setFormerPassword("former");
        dto.setNewPassword("newPass");

//        User user = new User();
//        user.setPassword(PasswordUtil.generateUserPassword("former", "test"));
//        user.setSalt("test");
        when(userDao.findOne(anyLong())).thenReturn(Optional.empty());


        //when
        assertThrows(UserNotFoundException.class, () -> userService.updatePassword(dto));
    }

    @Test
    void updatePasswordThrowWrongArgs() {
        //given
        UserChangePasswordRequestDTO dto = new UserChangePasswordRequestDTO();
        dto.setUserId(1L);
        dto.setFormerPassword("formerla");
        dto.setNewPassword("newPass");

        User user = new User();
        user.setPassword(PasswordUtil.generateUserPassword("former", "test"));
        user.setSalt("test");
        when(userDao.findOne(anyLong())).thenReturn(Optional.of(user));

        //when
        assertThrows(WrongArgsException.class, () -> userService.updatePassword(dto));
    }

    @Test
    void upsertRegularCommentBrandNewUser() {
        //given
        UserRegularCommentRequestDTO requestDto = new UserRegularCommentRequestDTO();
        requestDto.setComment("hohoho");
        requestDto.setUserId(1L);
        when(userRegularCommentService.findOneById(any())).thenReturn(Optional.empty());

        UserRegularCommentDO regularComment = new UserRegularCommentDO();
        regularComment.setId(2L);
        regularComment.setUserId(requestDto.getUserId());
        regularComment.setComment(requestDto.getComment());
        when(userRegularCommentService.upsert(any())).thenReturn(regularComment);

        //when
        UserRegularCommentDTO userRegularCommentDto = userService.upsertRegularComment(requestDto);

        //then
        verify(userRegularCommentService, times(1)).upsert(regularCommentCaptor.capture());
        UserRegularCommentDO captorValue = regularCommentCaptor.getValue();
        assertThat(captorValue.getUserId(), is(1L));
        assertThat(captorValue.getComment(), is(requestDto.getComment()));
    }
}