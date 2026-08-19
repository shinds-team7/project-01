package com.example.petnow.common.session;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.response.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 로그인 세션을 채우고 비우는 단 하나의 통로.
 *
 * <p>세션에는 두 값이 함께 들어간다.
 * <ul>
 *   <li>{@link SessionConst#LOGIN_USER_ID} — 서버가 "누구인지" 판단하는 기준</li>
 *   <li>{@link SessionConst#LOGIN_USER} — 화면이 "이름을 뭐라고 그릴지" 쓰는 표시용 값</li>
 * </ul>
 *
 * <p>표시용 값은 로그인 시점의 스냅샷이라 사용자 정보가 바뀌면 낡는다.
 * <b>닉네임을 바꾸는 기능(마이페이지 프로필 수정)을 만들 때 저장 성공 직후 {@link #set} 을
 * 다시 호출해야 한다.</b> 두 값을 따로 넣는 코드를 컨트롤러에 흩뿌리면 둘 중 하나만 갱신되는
 * 사고가 나므로, 세션에 손대는 곳은 이 클래스로 모은다.
 */
public final class LoginSession {

    private LoginSession() {}

    /** 로그인 성공 시점, 그리고 사용자 정보가 바뀐 직후에 호출한다. */
    public static void set(HttpSession session, LoginUser loginUser) {
        session.setAttribute(SessionConst.LOGIN_USER_ID, loginUser.getId());
        session.setAttribute(SessionConst.LOGIN_USER, loginUser);
    }

    /** 로그아웃. 세션 자체를 버려 다른 잔여 속성도 함께 정리한다. */
    public static void clear(HttpSession session) {
        session.invalidate();
    }

    /**
     * 지금 요청을 보낸 사람의 id. 로그인 상태가 아니면 {@code null}.
     *
     * <p>없는 세션을 새로 만들지 않는다. 비로그인 방문자에게까지 JSESSIONID 를 발급하면
     * 서버가 쓰지도 않을 세션을 떠안는다.
     */
    public static Long currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        return session == null ? null : (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
    }

    /** 로그인 상태인지. "로그인했다"의 판정을 이 클래스 밖에서 따로 쓰지 않기 위한 통로다. */
    public static boolean isLoggedIn(HttpServletRequest request) {
        return currentUserId(request) != null;
    }
}
